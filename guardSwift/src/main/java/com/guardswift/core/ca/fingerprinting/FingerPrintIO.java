package com.guardswift.core.ca.fingerprinting;


import android.util.Log;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.data.client.ClientLocation;
import com.guardswift.persistence.parse.execution.GSTask;
import com.parse.SaveCallback;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import dk.alexandra.positioning.wifi.AccessPoint;
import dk.alexandra.positioning.wifi.Fingerprint;
import dk.alexandra.positioning.wifi.io.WiFiIO;


/**
* Created by cyrix on 2/13/15.
*/
@Singleton
public class FingerPrintIO {

    private static final String TAG = FingerPrintIO.class.getSimpleName();


    @Inject
    public FingerPrintIO() {

    }

    public Set<Fingerprint> loadFingerprints(GSTask task) {
        Set<Fingerprint> fingerprints = Sets.newHashSet();

        Client client = task.getClient();
        if (client == null) {
            Log.e(TAG, "loadFingerprints client is null");
            return fingerprints;
        }
        List<ClientLocation> checkpoints = Lists.newArrayList(client.getCheckpoints());
        for (ClientLocation checkpoint : checkpoints) {
            if (checkpoint.hasFingerprint()) {
                Log.d(TAG, "Loading fingerprint " + checkpoint.getLocation());
                if (checkpoint.getFingerprint() == null) {
                    Log.e(TAG, "Invalid fingerprint - removing");
                    checkpoint.removeFingerprint();
                    checkpoint.pinThenSaveEventually();
                    continue;
                }
                fingerprints.add(WiFiIO.loadFingerprint(checkpoint.getFingerprintString()));
            }
        }

        return fingerprints;
    }

    public Set<Fingerprint> loadFingerprints(Set<GSTask> tasks) {
        Set<Fingerprint> fingerprints = Sets.newHashSet();

        for (GSTask task: tasks) {
            fingerprints.addAll(loadFingerprints(task));
        }
        return fingerprints;
    }

//    public Set<Fingerprint> loadFingerprints(File fingerprintFile) {
//
//        if (fingerprintFile.exists() && fingerprintFile.canRead()) {
//            try {
//                return Sets.newHashSet(WiFiIO.loadFingerprints(new FileInputStream(fingerprintFile)));
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//                return new HashSet<>();
//            }
//        } else {
//            return new HashSet<>();
//        }
//    }

    public void storeFingerprint(Set<AccessPoint> samples, Fingerprint fingerprint, final ClientLocation clientLocation, final SaveCallback saveCallback) {
        clientLocation.storeSamples(samples);
        clientLocation.storeFingerprint(fingerprint);
        clientLocation.pinThenSaveEventually(saveCallback);
    }

    public void storeFingerprints(Set<Fingerprint> fingerprints, final Client client, final SaveCallback saveCallback) {
        if (client == null) {
            Log.e(TAG, "No client");
            return;
        }

        client.storeFingerprints(fingerprints);
        client.pinThenSaveEventually(saveCallback);

    }



//    public void storeFingerprints(Set<Fingerprint> fingerprints, File fingerprintFile) {
//        try {
//            WiFiIO.storeFingerprints(fingerprints, new FileOutputStream(fingerprintFile));
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }
}
