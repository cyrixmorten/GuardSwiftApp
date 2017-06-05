package com.guardswift.ui.parse.documentation.report.create.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.guardswift.R;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.dagger.InjectingFragment;
import com.guardswift.persistence.cache.data.ClientCache;
import com.guardswift.persistence.cache.data.EventTypeCache;
import com.guardswift.persistence.cache.task.GSTasksCache;
import com.guardswift.persistence.parse.data.EventType;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.documentation.event.EventRemark;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.parse.documentation.report.create.activity.AddEventHandler;
import com.guardswift.util.StringUtil;
import com.guardswift.util.Util;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class AddEventRemarkFragment extends InjectingFragment implements
        EventEntryFragment {

    protected static final String TAG = AddEventRemarkFragment.class
            .getSimpleName();

    private Unbinder unbinder;

    public static AddEventRemarkFragment newInstance(Client client, EventType eventType) {

        GuardSwiftApplication.getInstance().getCacheFactory().getClientCache().setSelected(client);
        GuardSwiftApplication.getInstance().getCacheFactory().getEventTypeCache().setSelected(eventType);

        AddEventRemarkFragment fragment = new AddEventRemarkFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public AddEventRemarkFragment() {
    }

    @Inject
    ClientCache clientCache;
    @Inject
    EventTypeCache eventTypeCache;
    @Inject
    GSTasksCache tasksCache;

    private ParseQueryAdapter<EventRemark> mAdapter;

    @BindView(R.id.edittext)
    MultiAutoCompleteTextView remarkEditText;
    //	@BindView(R.id.check_save_to_list) CheckedTextView check_save_to_list;
//
//	@BindView(R.id.layout_previous_remarks) LinearLayout layout_previous_remarks;
    @BindView(R.id.progressBar)
    ProgressBar progress;
    @BindView(R.id.list_previous_remarks)
    ListView prevous_remarks_list;
    @BindView(R.id.btn_clear)
    ImageView btnClear;
//    @BindView(R.id.header)
//    TextView header;
//	@BindView(R.id.list_empty_previous_remarks) TextView prevous_remarks_list_empty;
//	@BindView(R.id.tv_client_location) TextView client_and_location;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_event_remark, container,
                false);

        unbinder = ButterKnife.bind(this, rootView);

//        header.setText(getString(R.string.remark).toUpperCase());

        Log.e(TAG, "onCreateView");
        if (tasksCache.getLastSelected().getTaskType() == GSTask.TASK_TYPE.STATIC) {
            btnClear.setVisibility(View.GONE);
        }

        remarkEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                String remarks = s.toString().trim();
                remarks = StringUtil.removeIfLastLetter(remarks, ",");
                ((AddEventHandler) getActivity()).setRemarks(remarks);

//				boolean saveRemark = check_save_to_list.isChecked();
//				((AddEventFragment) getParentFragment()).saveRemarks(saveRemark);
//                ((AddEventFragment) getParentFragment()).saveRemarks(false);
            }
        });

        prepareRemarkSuggestions();

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.e(TAG, "onViewCreated");
        remarkEditText.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        String selectedRemarks = ((AddEventHandler) getActivity()).getRemarks();

        if (selectedRemarks != null) {
            remarkEditText.setText(selectedRemarks);
        }
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        if (getActivity() instanceof  AddEventHandler && remarkEditText != null) {
            ((AddEventHandler) getActivity()).setRemarks(remarkEditText.getText().toString());
        }
        super.onDestroy();
    }

    //    @OnClick(R.id.btn_accept)
//    public void onActionAccept() {
//        Util.hideKeyboard(getActivity(), remarkEditText.getWindowToken());
//        remarkEditText.clearFocus();
//        ((AddEventFragment) getParentFragment()).nextPageDelayed();
//    }

    @OnClick(R.id.btn_clear)
    public void clear() {
        remarkEditText.setText("");
    }

    private void deleteRemark(final List<EventRemark> remarks, final String remark) {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.remove_suggestion)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .content(getString(R.string.confirm_suggestion_removal, remark))
                .callback(new MaterialDialog.ButtonCallback() {
                              @Override
                              public void onPositive(MaterialDialog dialog) {
                                  for (EventRemark eventRemark : remarks) {
                                      if (eventRemark.getRemark().equals(remark)) {
                                          Toast.makeText(getActivity(), getString(R.string.successfully_removed_suggestion), Toast.LENGTH_SHORT).show();

                                          prepareRemarkSuggestions();
                                      }
                                  }
                              }
                          }
                ).show();
    }


    boolean remarkSuggestionsFound;
    private void prepareRemarkSuggestions() {
        Log.e(TAG, "prepareRemarkSuggestions");
        if (tasksCache.getLastSelected().getTaskType() == GSTask.TASK_TYPE.STATIC) {
            // do not show suggestions for static tasks
            return;
        }

        final Client client = clientCache.getSelected();
        final EventType eventType = eventTypeCache.getSelected();


        if (client == null || eventType == null)
            return;

        mAdapter = new ParseQueryAdapter<EventRemark>(getActivity(), new ParseQueryAdapter.QueryFactory<EventRemark>() {
            @Override
            public ParseQuery<EventRemark> create() {
                return EventRemark.getQueryBuilder(remarkSuggestionsFound)
                        .matching(client).matching(eventType)
                        .build().addAscendingOrder(EventRemark.remark);
            }
        }, android.R.layout.simple_list_item_1);

        mAdapter.setTextKey(EventRemark.remark);

//        mAdapter = new SimpleParseAdapter<>(getActivity(), EventRemark.remark, new ParseQueryAdapter.QueryFactory<EventRemark>() {
//            @Override
//            public ParseQuery<EventRemark> createWithFontAwesomeIcon() {
//                return EventRemark.getQueryBuilder(false)
//                        .matching(client).matching(eventType)
//                        .build();
//            }
//        });

        mAdapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener<EventRemark>() {
            @Override
            public void onLoading() {
                if (progress != null)
                    progress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoaded(List<EventRemark> list, Exception e) {
                if (progress != null)
                    progress.setVisibility(View.INVISIBLE);

                if (e != null) {
                    new HandleException(getActivity(), TAG, "onLoaded", e);
                    return;
                }

                if (!remarkSuggestionsFound) {
                    EventRemark.pinAllInBackground(list);
                }

                if (getActivity() != null && remarkEditText != null) {

                    final List<String> remarks = new ArrayList<String>();
                    for (EventRemark eventRemark : list) {
                        remarks.add(eventRemark.getRemark());
                    }
                    Collections.sort(remarks);

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, remarks);

                    remarkEditText.setAdapter(adapter);
                    remarkEditText.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
                }
                remarkSuggestionsFound = true;
            }
        });

        prevous_remarks_list.setAdapter(mAdapter);

        prevous_remarks_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EventRemark remark = mAdapter.getItem(position);

                String currentRemark = remarkEditText.getText().toString();
                boolean hasPreviousToken = currentRemark.trim().endsWith(",");


                String tokenPrefix = (hasPreviousToken || currentRemark.isEmpty()) ? "" : ", ";
                String addRemark = remark.getRemark();
                String tokenPostfix = ", ";

                remarkEditText.getText().append(tokenPrefix).append(addRemark).append(tokenPostfix);
            }
        });

//        EventRemark.getQueryBuilder(false)
//                .matching(client).matching(eventType)
//                .build().findInBackground(new FindCallback<EventRemark>() {
//            @Override
//            public void done(final List<EventRemark> eventRemarks, ParseException e) {
//                if (e != null) {
//                    Crashlytics.logException(e);
//                    return;
//                }
//
//                final List<String> remarks = new ArrayList<String>();
//                for (EventRemark eventRemark : eventRemarks) {
//                    remarks.addUnique(eventRemark.getRemark());
//                }
//                Collections.sort(remarks);
//
//
//                if (getActivity() != null) {
////                    mAdapter = new SimpleMultichoiceArrayAdapter(getActivity(), all_options, new boolean[]{});
//                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, remarks);
//
//                    remarkEditText.setAdapter(adapter);
//                    remarkEditText.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
//                    remarkEditText.showDropDown();
//                }
//            }
//        });


    }

    @Override
    public void fragmentBecameVisible() {
        Log.e(TAG, "fragmentBecameVisible");
        if (getActivity() != null) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        prepareRemarkSuggestions();

    }

    @Override
    public void fragmentBecameInvisible() {
        Util.hideKeyboard(getActivity());
    }

    @Override
    public int getTitle() {
        return R.string.title_event_remarks;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();

        Log.e(TAG, "onDestroyView");
    }

    @Override
    public void onPause() {
        Log.e(TAG, "onPause");
        InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(remarkEditText.getWindowToken(), 0);
        super.onPause();
    }

}