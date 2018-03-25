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
import com.guardswift.core.ca.location.LocationModule;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.persistence.parse.Positioned;
import com.guardswift.ui.GuardSwiftApplication;
import com.parse.ParseGeoPoint;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by cyrix on 11/22/15.
 */
public class PositionedViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_distance_text)
        public AwesomeTextView vDistanceText;
        @BindView(R.id.tv_distance_icon)
        public AwesomeTextView vDistanceIcon;

        public PositionedViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

    public static class CalcDistanceAsync extends AsyncTask<Void, Void, ParseModule.DistanceStrings> {


        private final Positioned positionedObject;

        private final Location deviceLocation;

        private final ThreadLocal<AwesomeTextView> vDistanceText;
        private final ThreadLocal<AwesomeTextView> vDistanceIcon;

        public CalcDistanceAsync(Positioned positionedObject, final PositionedViewHolder holder) {
            this.positionedObject = positionedObject;
            this.deviceLocation = LocationModule.Recent.getLastKnownLocation();

            this.vDistanceText = new ThreadLocal<AwesomeTextView>() {
                @Override
                protected AwesomeTextView initialValue() {
                    return holder.vDistanceText;
                }
            };

            this.vDistanceIcon = new ThreadLocal<AwesomeTextView>() {
                @Override
                protected AwesomeTextView initialValue() {
                    return holder.vDistanceIcon;
                }
            };
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
            AwesomeTextView distanceText = vDistanceText.get();
            AwesomeTextView distanceIcon = vDistanceIcon.get();

            if (distanceText == null || distanceIcon == null) {
                return;
            }

            distanceText.setMarkdownText("");
            distanceText.setBootstrapBrand(DefaultBootstrapBrand.REGULAR);
            distanceIcon.setBootstrapBrand(DefaultBootstrapBrand.REGULAR);
            distanceIcon.setFontAwesomeIcon(FontAwesome.FA_MAP_MARKER);


            distanceText.setVisibility(View.GONE);
            distanceIcon.setVisibility(View.GONE);

            if (distanceStrings != null) {

                distanceText.setVisibility(View.VISIBLE);
                distanceIcon.setVisibility(View.VISIBLE);

                String type = distanceStrings.distanceType;
                String value = distanceStrings.distanceValue;


                BootstrapText text = new BootstrapText.Builder(GuardSwiftApplication.getInstance())
                        .addText(value)
                        .addText(" ")
                        .addText(type)
                        .addText(" ")
                        .build();

                distanceText.setBootstrapText(text);

            }
            super.onPostExecute(distanceStrings);
        }
    }
}
