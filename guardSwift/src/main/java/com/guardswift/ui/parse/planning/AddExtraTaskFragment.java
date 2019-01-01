package com.guardswift.ui.parse.planning;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.google.common.collect.Sets;
import com.guardswift.R;
import com.guardswift.dagger.InjectingFragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class AddExtraTaskFragment extends InjectingFragment {

    public static AddExtraTaskFragment newInstance() {

        AddExtraTaskFragment fragment = new AddExtraTaskFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.button_choose_client)
    Button buttonClient;

    @BindView(R.id.layout_task_form)
    LinearLayout layoutTaskForm;

    @BindView(R.id.radio_button_walking)
    RadioButton radioButtonWalking;

    @BindView(R.id.radio_button_driving)
    RadioButton radioButtonDriving;

    @BindView(R.id.layout_week_days)
    LinearLayout layoutWeekDays;

    private Set<Integer> weekDays = new HashSet<>();

    private Unbinder unbinder;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_add_extra_task, container,
                false);

        unbinder = ButterKnife.bind(this, rootView);

        addWeekDayCheckboxes();

        return rootView;
    }

    private String weekDayName(int dayOfWeek) {
        switch (dayOfWeek) {
            case 0: return "Søndag";
            case 1: return "Mandag";
            case 2: return "Tirsdag";
            case 3: return "Onsdag";
            case 4: return "Torsdag";
            case 5: return "Fredag";
            case 6: return "Lørdag";
        }

        return ""+ dayOfWeek;
    }

    private void addWeekDayCheckboxes() {
        for (int i = 1; i <= 7; i++) {
            final int day = i % 7; // turn 7 into 0

            CheckBox weekDayCheckbox = new CheckBox(getContext());

            weekDayCheckbox.setTag(day);
            weekDayCheckbox.setText(weekDayName(day));

            weekDayCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {

                if (isChecked) {
                    weekDays.add(day);
                } else {
                    weekDays.remove(day);
                }
            });

            layoutWeekDays.addView(weekDayCheckbox);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

}
