//package com.guardswift.ui.parse.data.checkpoint;
//
//import android.content.Context;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.CheckedTextView;
//import android.widget.LinearLayout;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//
//import com.beardedhen.androidbootstrap.AwesomeTextView;
//import com.beardedhen.androidbootstrap.font.FontAwesome;
//import com.google.common.collect.Sets;
//import com.guardswift.R;
//import com.guardswift.persistence.parse.data.client.ClientLocation;
//import com.guardswift.ui.view.CheckableFrameLayout;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import butterknife.BindView;
//import butterknife.ButterKnife;
//import dk.alexandra.positioning.wifi.PositionEstimate;
//
//public class CheckpointAdapter extends
//        ArrayAdapter<ClientLocation> {
//
//    private static final String TAG = CheckpointAdapter.class
//            .getSimpleName();
//
//    private final Context context;
//    private final int item_layout;
//    private final List<ClientLocation> checkpoints;
//    private Map<String, Integer> checkpointProbabilities;
//    private Map<String, PositionEstimate> checkpointEstimates;
//
//    public CheckpointAdapter(Context context, int item_layout, List<ClientLocation> checkpoints, List<String> checkpointNames) {
//        super(context, item_layout, checkpoints);
//
//        this.context = context;
//        this.item_layout = item_layout;
//        this.checkpoints = checkpoints;
//    }
//
//    public void setCheckpointProbabilities(Map<String, Integer> checkpointProbabilities, Map<String, PositionEstimate> checkpointEstimates) {
//        this.checkpointProbabilities = checkpointProbabilities;
//        this.checkpointEstimates = checkpointEstimates;
//        notifyDataSetInvalidated();
//    }
//
//    @BindView(R.id.qualityIcon)
//    AwesomeTextView qualityIcon;
//    @BindView(R.id.item)
//    CheckedTextView item;
//    @BindView(R.id.progress1)
//    ProgressBar progress1;
//    @BindView(R.id.progress2)
//    ProgressBar progress2;
//    @BindView(R.id.layout_estimated)
//    LinearLayout estLayout;
//    @BindView(R.id.tv_est_distance)
//    TextView estDistance;
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        LayoutInflater inflater = (LayoutInflater) context
//                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
//        CheckableFrameLayout rowView = (CheckableFrameLayout) convertView;
//        if (convertView == null) {
//            rowView = (CheckableFrameLayout) inflater.inflate(item_layout, parent, false);
//        }
//
//        ButterKnife.bind(this, rowView);
//
//        ClientLocation checkpoint = checkpoints.get(position);
//        String checkpointName = checkpoint.getLocation();//checkpointNames.get(clientPosition);
//
//        item.setText(checkpointName);
//
//
//        item.setChecked(rowView.isChecked());
//
//
//        updateProgress(progress1, checkpointName);
//        updateProgress(progress2, checkpointName);
//        determineQuality(checkpoint);
//
//        return rowView;
//    }
//
//
//    private void updateProgress(ProgressBar progress, String checkpoint) {
//        if (checkpointProbabilities == null)
//            return;
//
//        progress.setProgress(0);
//
//        Integer progressValue = checkpointProbabilities.get(checkpoint);
//        if (progressValue != null) {
//            progress.setProgress(progressValue);
//        }
//    }
//
//    private void determineQuality(ClientLocation checkpoint) {
//
//        String icon_unknown = FontAwesome.FA_QUESTION;
//        String icon_warning = FontAwesome.FA_WARNING;
//        String icon_wifi = FontAwesome.FA_SIGNAL;
//
//        int color_default = R.color.bootstrap_brand_primary;
//        int color_red = R.color.bootstrap_brand_danger;
//        int color_yellow = R.color.bootstrap_brand_warning;
//        int color_green = R.color.bootstrap_brand_success;
//
//        String icon = icon_unknown;
//        int color = color_default;
//
//
//        String message = "";
//
//        if (checkpoint.getFingerprint() == null) {
//            // not trained yet
//            message = getContext().getString(R.string.checkpoint_not_learned);
//        } else {
//            JSONObject fingerprint = checkpoint.getFingerprint();
//            try {
//                JSONObject averageSignalStrengths = fingerprint.getJSONObject("averageSignalStrengths");
//                int apCount = countAccessPoints(averageSignalStrengths.keys());
//                message = getContext().getString(R.string.visible_access_points) + ": " + apCount;
//
//
//                icon = icon_warning;
//                if (apCount <= 1) {
//                    // unusable
//                    // red warning
//                    color = color_red;
//                }
//                if (apCount >= 1) {
//                    // bad
//                    // yellow warning
//                    color = color_yellow;
//                }
//                if (apCount >= 3) {
//                    // poor
//                    // red wifi
//                    icon = icon_wifi;
//                    color = color_red;
//                }
//                if (apCount >= 4) {
//                    // ok
//                    // yellow wifi
//                    color = color_yellow;
//                }
//                if (apCount >= 5) {
//                    // great
//                    // green wifi
//                    color = color_green;
//                }
//                if (apCount >= 6) {
//                    // excellent
//                    // green wifi
//                }
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//                Log.e(TAG, "determineHealth", e);
//                message = "Error " + e.getMessage();
//
//            }
//        }
//        qualityIcon.setFontAwesomeIcon(icon);
//        qualityIcon.setTextColor(getContext().getResources().getColor(color));
//        estDistance.setText(message);
//    }
//
//    private int countAccessPoints(Iterator<String> aps) {
//        Set<String> accesspoints = Sets.newHashSet();
//        while (aps.hasNext()) {
//            String ap = aps.next();
//            String ap_sub = ap.substring(0, ap.length()-1); // ommit last entry of MAC for comparison
//            accesspoints.add(ap_sub);
//        }
//        return accesspoints.size();
//    }
//
//
//}
