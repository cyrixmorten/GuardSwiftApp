package com.guardswift.ui.parse.execution.circuit;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.PopupMenu;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.guardswift.R;
import com.guardswift.persistence.cache.planning.CircuitStartedCache;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.execution.task.regular.Circuit;
import com.guardswift.persistence.parse.execution.task.regular.CircuitStarted;
import com.guardswift.persistence.parse.misc.Message;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.activity.MainActivity;
import com.guardswift.ui.dialog.CommonDialogsBuilder;
import com.guardswift.ui.parse.AbstractTabsViewPagerFragment;
import com.guardswift.ui.view.drawer.OverflowMenuDrawerItem;
import com.guardswift.util.ToastHelper;
import com.guardswift.util.Util;
import com.mikepenz.actionitembadge.library.ActionItemBadge;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.parse.FindCallback;
import com.parse.ParseException;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

public class CircuitViewPagerFragment extends AbstractTabsViewPagerFragment {

    protected static final String TAG = CircuitViewPagerFragment.class
            .getSimpleName();


    public static CircuitViewPagerFragment newInstance(Context context, CircuitStarted circuitStarted) {

        Log.e(TAG, "SHOW: " + circuitStarted.getName());
        GuardSwiftApplication.getInstance()
                .getCacheFactory()
                .getCircuitStartedCache()
                .setSelected(circuitStarted);

        CircuitViewPagerFragment fragment = new CircuitViewPagerFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Inject
    CircuitStartedCache circuitStartedCache;

    Map<String, Fragment> fragmentMap = Maps.newLinkedHashMap();

    private Drawer messagesDrawer;
    private MenuItem messagesMenu;


    public CircuitViewPagerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        fragmentMap = Maps.newLinkedHashMap();
        fragmentMap.put(getString(R.string.title_tasks_new), ActiveCircuitUnitsFragment.newInstance(getContext(), circuitStartedCache.getSelected()));
        fragmentMap.put(getString(R.string.title_tasks_old), FinishedCircuitUnitsFragment.newInstance(getContext(), circuitStartedCache.getSelected()));
        super.onCreate(savedInstanceState);
    }

    private int newMessagesCount(List<Message> messages) {
        if (GuardSwiftApplication.hasReadGroups.contains(getGroupId())) {
            return 0;
        }

        int newCount = 0;

        Guard guard = GuardSwiftApplication.getLoggedIn();
        Date lastLogout = guard.getLastLogout();

        for (Message message : messages) {
            if (message.getGuard() == guard) {
                continue;
            }
            if (message.getCreatedAt().after(lastLogout)) {
                newCount++;
            }
        }
        return newCount;
    }

    private String getGroupId() {
        CircuitStarted circuitStarted = circuitStartedCache.getSelected();
        if (circuitStarted != null) {
            Circuit circuit = circuitStarted.getCircuit();
            if (circuit != null) {
                return circuit.getObjectId();
            }
        }
        return "";
    }

    private boolean enableMessagesDrawer() {
        return !getGroupId().isEmpty();
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {

        inflater.inflate(R.menu.taskgroup, menu);

        messagesMenu = menu.findItem(R.id.menu_messages);
        messagesMenu.setVisible(false);

        Message.getQueryBuilder(true, getGroupId()).build().findInBackground(new FindCallback<Message>() {
            @Override
            public void done(List<Message> messages, ParseException e) {
                if (getActivity() == null || !isAdded()) {
                    return;
                }


//                IconicsDrawable messagesIcon = new IconicsDrawable(getContext())
//                        .icon(GoogleMaterial.Icon.gmd_email)
//                        .color(Color.DKGRAY)
//                        .sizeDp(24);


                int messagesCount = newMessagesCount(messages);

                if (messagesCount == 0) {
                    // Hide the badge https://github.com/mikepenz/Android-ActionItemBadge/issues/9
                    messagesCount = Integer.MIN_VALUE;
                }

                ActionItemBadge.update(getActivity(), messagesMenu, ContextCompat.getDrawable(getActivity(), R.drawable.ic_forum_black_24dp), ActionItemBadge.BadgeStyles.RED, messagesCount);

                messagesMenu.setVisible(true);
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_messages) {
            messagesDrawer.openDrawer();
            GuardSwiftApplication.hasReadGroups.add(getGroupId());
            ActionItemBadge.update(messagesMenu, Integer.MIN_VALUE);
        }
        return super.onOptionsItemSelected(item);
    }


    private PrimaryDrawerItem addMessageItem() {
        PrimaryDrawerItem addMessageItem = new PrimaryDrawerItem().withName(R.string.add_message);

        addMessageItem.withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                addMessageDialog();

                return false;
            }
        });

        return addMessageItem;
    }

    private OverflowMenuDrawerItem createMessageItem(final Message message) {
        final OverflowMenuDrawerItem drawerItem = new OverflowMenuDrawerItem();

        drawerItem.withName(message.getGuard().getName());

        Date createdAt = (message.getCreatedAt() != null) ? message.getCreatedAt() : new Date();
        drawerItem.withBottomEndCaption(Util.relativeTimeString(createdAt));

        drawerItem.withDescription(message.getMessage());
        drawerItem.withSelectable(false);
        drawerItem.withDisabledTextColor(ContextCompat.getColor(getActivity(), R.color.md_black_1000));


        if (GuardSwiftApplication.getLoggedIn() == message.getGuard()) {
            drawerItem.withMenu(R.menu.message_options);
        }

        drawerItem.withOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.edit: {
                        editMessageDialog(drawerItem, message);
                        break;
                    }
                    case R.id.delete: {
                        deleteMessageDialog(drawerItem, message);
                        break;
                    }
                }
                return false;
            }
        });

        return drawerItem;
    }

    private void addMessageDialog() {
        openMessageDialog(null, null);
    }
    private void editMessageDialog(OverflowMenuDrawerItem drawerItem, Message message) {
        openMessageDialog(drawerItem, message);
    }

    private void openMessageDialog(final OverflowMenuDrawerItem drawerItem, final Message editMessage) {

        final boolean editMode = drawerItem != null && editMessage != null;

        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.add_message)
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
                .input(getString(R.string.message), (editMode) ? editMessage.getMessage() : getString(R.string.input_empty), new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        String editTextString = input.toString();
                        if (!editTextString.isEmpty()) {
                            if (editMode) {
                                editMessage.setMessage(editTextString);
                                editMessage.pinThenSaveEventually();

                                messagesDrawer.updateItem(drawerItem.withDescription(editTextString));
                            } else {
                                Message message = Message.newInstance(getGroupId(), editTextString);
                                message.pinThenSaveEventually();

                                messagesDrawer.addItemAtPosition(createMessageItem(message), 0);
                                messagesDrawer.closeDrawer();

                                ToastHelper.toast(getContext(), getString(R.string.message_saved));
                            }
                        }
                    }
                }).negativeText(android.R.string.cancel).build();

        EditText editText = dialog.getInputEditText();

        editText.setSingleLine(true);
//        editText.setLines(4); // desired number of lines
        editText.setHorizontallyScrolling(false);
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);

        dialog.show();
    }

    private void deleteMessageDialog(final OverflowMenuDrawerItem drawerItem, final Message message) {
        new CommonDialogsBuilder.MaterialDialogs(getActivity()).yesNo(R.string.confirm_delete_message, message.getMessage(), new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                int position = messagesDrawer.getPosition(drawerItem);
                messagesDrawer.removeItemByPosition(position);

                message.unpinInBackground();
                message.deleteEventually();
            }
        }).show();
    }


    private void loadMessages() {

            messagesDrawer.removeAllItems();

            Message.getQueryBuilder(true, getGroupId()).build().addDescendingOrder(Message.createdAt).findInBackground(new FindCallback<Message>() {
                @Override
                public void done(List<Message> messages, ParseException e) {
                    if (messagesDrawer == null || getActivity() == null) {
                        return;
                    }

                    for (Message message : messages) {
                        messagesDrawer.addItem(createMessageItem(message));
                    }

                }
            });
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Activity activity = getActivity();
        if (activity instanceof MainActivity) {
            messagesDrawer = ((MainActivity) activity).getMessagesDrawer();

            messagesDrawer.removeAllStickyFooterItems();
            messagesDrawer.addStickyFooterItem(addMessageItem());

            messagesDrawer.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

            if (enableMessagesDrawer()) {
                loadMessages();
            }
        }
    }



    @Override
    protected Map<String, Fragment> getTabbedFragments() {
        return fragmentMap;
    }
}

