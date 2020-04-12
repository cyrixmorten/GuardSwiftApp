package com.guardswift.ui.parse.data.tracker;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.guardswift.R;
import com.guardswift.persistence.parse.documentation.gps.TrackerData;

public class TrackerDataFragment extends Fragment {

    public static TrackerDataFragment newInstance(TrackerData trackerData) {
        TrackerDataFragment fragment = new TrackerDataFragment();
        fragment.trackerData = trackerData;
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private TrackerData trackerData;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_empty_table,
                container, false);

        TableLayout table = rootView.findViewById(R.id.layout_table);

//        map.put("Tidspunkt",  ;
//        map.put("Hastighed",  );
//        map.put("Latitude",  String.valueOf(getLatitude()));
//        map.put("Longitude",  String.valueOf(getLongitude()));
//        map.put("Højde",  ;

        table.addView(
                createRow(inflater, "Tidspunkt", trackerData.getHumanReadableLongDate(getContext()))
        );

        table.addView(
                createRow(inflater, "Hastighed", trackerData.getHumanReadableSpeed(getContext()))
        );

        table.addView(
                createRow(inflater, "Latitude", String.valueOf(trackerData.getLatitude()))
        );

        table.addView(
                createRow(inflater, "Longitude", String.valueOf(trackerData.getLongitude()))
        );

        table.addView(
                createRow(inflater, "Højde", trackerData.getHumanReadableAltitude(getContext()))
        );

        return rootView;
    }

    private TableRow createRow(LayoutInflater inflater, String key, String value) {
        TableRow row = new TableRow(getContext());
        View keyValueRow =  inflater.inflate(R.layout.gs_view_key_value, row, true);
        TextView keyView = keyValueRow.findViewById(R.id.tv_key);
        TextView valueView = keyValueRow.findViewById(R.id.tv_value);
        keyView.setText(key);
        valueView.setText(value);

        return row;
    }
}
