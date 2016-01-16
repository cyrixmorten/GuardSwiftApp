package com.guardswift.core.ca;

import android.util.Log;

import com.google.common.collect.Sets;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import dk.alexandra.positioning.wifi.AccessPoint;
import dk.alexandra.positioning.wifi.SampleBuilder;
import dk.alexandra.positioning.wifi.tools.Helper;

/**
 * Created by cyrix on 3/12/15.
 */
@Singleton
public class FingerprintingModule {

    private static final String TAG = FingerprintingModule.class.getSimpleName();

    private String lastKnownSample;

    @Inject
    public FingerprintingModule() {

    }

    public void setLastKnownSample(String lastKnownSample) {
        this.lastKnownSample = lastKnownSample;
//        Log.w(TAG, "Storing sample: " + lastKnownSample);
    }

    public Set<AccessPoint> getLastKnownSample() {
        Set<AccessPoint> samples = Sets.newHashSet();

        if (lastKnownSample != null) {
            SampleBuilder sampleBuilder = Helper.startSample();
            sampleBuilder.addSample(lastKnownSample);
            samples = Helper.stopSample(sampleBuilder);
        }
//        else {
//            Log.e(TAG, "lastKnownSample was null!!");
//        }

//        Log.w(TAG, "getLastKnownSample " + lastKnownSample + " " + samples.size());
        return samples;
    }

    public static JSONObject convertToJsonObject(AccessPoint sample) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("MAC", sample.getMAC());

            JSONArray jsonArray = new JSONArray();
            for (Integer ss : sample.getSignalStrengths()) {
                jsonArray.put(ss);
            }
            jsonObject.put("SignalStrengths", jsonArray);
        } catch (JSONException e) {
            Log.e(TAG, "convertToJsonObject", e);
            e.printStackTrace();
        }

        return jsonObject;
    }

    public static JSONArray convertToJsonArray(Set<AccessPoint> samples) {
        JSONArray outerJsonArray = new JSONArray();

        for (AccessPoint ap : samples) {

//                Log.e(TAG, "storing MAC: " + ap.getMAC() + " samples: " + ap.getSignalStrengths().size());

            outerJsonArray.put(convertToJsonObject(ap));
        }

        return outerJsonArray;
    }

    public static class Recent {

        private static boolean isTraining;

        public static void setTraining(boolean training) {
            isTraining = training;
        }

        public static boolean isTraining() {
            return isTraining;
        }
    }

}
