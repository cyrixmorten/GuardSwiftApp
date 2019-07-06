package com.guardswift.persistence.parse;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.common.collect.Lists;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.core.exceptions.LogError;
import com.guardswift.eventbus.EventBusController;
import com.guardswift.eventbus.events.UpdateUIEvent;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;

public abstract class ExtendedParseObject extends ParseObject implements Comparable<ExtendedParseObject> {

    // TODO get rid of this and use bolts Tasks instead
    public interface DataStoreCallback<T extends ParseObject> {
        void success(List<T> objects);

        void failed(ParseException e);
    }


    public static final String createdAt = "createdAt";
    public static final String updatedAt = "updatedAt";
    public static final String objectId = "objectId";

    protected static final String owner = "owner";


    protected final String TAG = this.getClass()
            .getSimpleName();

    protected void setDefaultOwner() {
        put(owner, ParseUser.getCurrentUser());
    }

    public abstract <T extends ParseObject> ParseQuery<T> getAllNetworkQuery();


    public String getPin() {
        return ParseObject.DEFAULT_PIN;
    }


    @SuppressWarnings("unchecked")
    public <T extends ParseObject> void updateAll(DataStoreCallback<T> callback) {
        updateAll((ParseQuery<T>) getAllNetworkQuery().setLimit(1000), callback);
    }


    public <T extends ParseObject> Task<List<T>> updateAll(ParseQuery<T> query, int limit) {

        if (query == null) {
            query = getAllNetworkQuery();
        }

        final bolts.TaskCompletionSource<List<T>> promise = new TaskCompletionSource<>();


        updateAll(query.setLimit(limit), new DataStoreCallback<T>() {
            @Override
            public void success(List<T> objects) {
                promise.setResult(objects);
            }

            @Override
            public void failed(ParseException e) {
                promise.setError(e);
            }
        });

        return promise.getTask();
    }


    private <T extends ParseObject> void updateAll(ParseQuery<T> query, final DataStoreCallback<T> callback) {
        query.findInBackground((objects, e) -> pinAllUpdates(objects, getPin(), callback));
    }


    public void saveEventuallyAndNotify() {
        saveEventuallyAndNotify(null, UpdateUIEvent.ACTION.UPDATE);
    }

    public void saveEventuallyAndNotify(final SaveCallback saved) {
        saveEventuallyAndNotify(saved, UpdateUIEvent.ACTION.UPDATE);
    }

    public void saveEventuallyAndNotify(final SaveCallback saved, final UpdateUIEvent.ACTION action) {
        ExtendedParseObject.this.saveEventually(e -> {
            if (e != null) {
                LogError.log(TAG, "Failed to save object", e);
            }

            doneSaving(e, saved, action);

        });
    }

    private void doneSaving(ParseException e, SaveCallback savedCallback, UpdateUIEvent.ACTION action) {
        if (savedCallback != null) {
            savedCallback.done(null);
        }

        if (e == null) {
            EventBusController.postUIUpdate(this, action);
        }
    }


    private <T extends ParseObject> void pinAllUpdates(final List<T> objects,
                                                                final String pin, final DataStoreCallback<T> callback) {
        if (objects == null) {
            callback.success(Lists.newArrayList());
            return;
        }
        ParseObject.pinAllInBackground(pin, objects, e -> {
            if (e == null) {
                if (callback != null)
                    callback.success(objects);
            } else {
                if (callback != null)
                    callback.failed(e);
            }
        });
    }


    protected ParseObject getLDSFallbackParseObject(String key) {
        ParseObject object = getParseObject(key);
        if (object == null)
                return null;

        if (object.isDataAvailable()) {
            return object;
        } else {
            try {
                object.fetchFromLocalDatastore();
            } catch (ParseException e) {
                new HandleException(TAG, "getLDSFallbackParseObject LDS failed: " + key + " objectId " + object.getObjectId(), e);
            } finally {
                if (!object.isDataAvailable()) {
                    try {
                        object.fetchIfNeeded(); // as a last resort make a network call
                    } catch (ParseException e) {
                        new HandleException(TAG, "getLDSFallbackParseObject NETWORK failed: " + " objectId " + object.getObjectId(), e);
                    }
                }
            }
            return object;
        }
    }

    protected int getIntSafe(String key, int defaultValue) {
        return has(key) ? getInt(key) : defaultValue;
    }

    protected String getStringSafe(String key) {
        return has(key) ? getString(key) : "";
    }

    protected String getStringSafe(String key, String defaultValue) {
        return has(key) ? getString(key) : defaultValue;
    }

    protected boolean getBooleanSafe(String key, boolean defaultValue) {
        if (has(key)) {
            return getBoolean(key);
        }

        return defaultValue;
    }

    @Override
    public int compareTo(@NonNull ExtendedParseObject another) {
        return 0;
    }


}
