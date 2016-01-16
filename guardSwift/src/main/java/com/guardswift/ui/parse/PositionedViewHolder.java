package com.guardswift.ui.parse;

import android.location.Location;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.beardedhen.androidbootstrap.AwesomeTextView;
import com.beardedhen.androidbootstrap.BootstrapText;
import com.beardedhen.androidbootstrap.api.defaults.DefaultBootstrapBrand;
import com.beardedhen.androidbootstrap.font.FontAwesome;
import com.guardswift.R;
import com.guardswift.core.ca.LocationModule;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.persistence.parse.Positioned;
import com.guardswift.ui.GuardSwiftApplication;
import com.parse.ParseGeoPoint;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by cyrix on 11/22/15.
 */
public class PositionedViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.tv_distance_text)
        public AwesomeTextView vDistanceText;
        @Bind(R.id.tv_distance_icon)
        public AwesomeTextView vDistanceIcon;

        public PositionedViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

    public static class CalcDistanceAsync extends AsyncTask<Void, Void, ParseModule.DistanceStrings> {


        private final Positioned positionedObject;

        private final Location deviceLocation;

        private final AwesomeTextView vDistanceText;
        private final AwesomeTextView vDistanceIcon;

        public CalcDistanceAsync(Positioned positionedObject, PositionedViewHolder holder) {
            this.positionedObject = positionedObject;
            this.deviceLocation = LocationModule.Recent.getLastKnownLocation();
            this.vDistanceText = holder.vDistanceText;
            this.vDistanceIcon = holder.vDistanceIcon;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ParseModule.DistanceStrings doInBackground(Void... voids) {
            if (deviceLocation == null) {
                return null;
            }
            ParseGeoPoint targetGeoPoint = positionedObject.getPosition();
            return ParseModule.distanceBetweenString(deviceLocation, targetGeoPoint);
        }

        @Override
        protected void onPostExecute(ParseModule.DistanceStrings distanceStrings) {
            // default when missing location
            vDistanceText.setMarkdownText("");
            vDistanceText.setBootstrapBrand(DefaultBootstrapBrand.REGULAR);
            vDistanceIcon.setBootstrapBrand(DefaultBootstrapBrand.REGULAR);
            vDistanceIcon.setFontAwesomeIcon(FontAwesome.FA_MAP_MARKER);


            vDistanceText.setVisibility(View.GONE);
            vDistanceIcon.setVisibility(View.GONE);

            if (distanceStrings != null) {

                vDistanceText.setVisibility(View.VISIBLE);
                vDistanceIcon.setVisibility(View.VISIBLE);

                String type = distanceStrings.distanceType;
                String value = distanceStrings.distanceValue;


//            BootstrapBrand brand = DefaultBootstrapBrand.DANGER;
//                if (tasksCache.isGeofenced(positionedObject)) {
//                    brand = DefaultBootstrapBrand.INFO;
//                }
//                if (tasksCache.isMovedOutsideGeofence(positionedObject)) {
//                    brand = DefaultBootstrapBrand.WARNING;
//                }
//                if (tasksCache.isWithinGeofence(positionedObject)) {
//                    brand = DefaultBootstrapBrand.SUCCESS;
//                }


                BootstrapText text = new BootstrapText.Builder(GuardSwiftApplication.getInstance())
                        .addText(value)
                        .addText(" ")
                        .addText(type)
                        .addText(" ")
                        .build();

                vDistanceText.setBootstrapText(text);

            }
            super.onPostExecute(distanceStrings);
        }
    }
}
