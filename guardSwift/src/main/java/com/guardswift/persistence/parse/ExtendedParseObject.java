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


    public abstract String getParseClassName();
    public String getPin() {
        return ParseObject.DEFAULT_PIN;
    };



    public  Task<Object> unpinAllPinnedToClass() {
        Log.w(TAG, "Unpinning" + getPin());
        return unpinAllInBackground(getPin())
        .continueWithTask(new Continuation<Void, Task<Integer>>() {
            @Override
            public Task<Integer> then(Task<Void> task) throws Exception {
                Log.w(TAG, "Successfully unpinned " + getPin());

                return new ParseQuery<ParseObject>(getPin()).fromLocalDatastore().countInBackground();
            }
        }).continueWith(new Continuation<Integer, Object>() {
                    @Override
                    public Object then(Task<Integer> task) throws Exception {
                        if (task.getError() != null) {
                            new HandleException(TAG, "unpin class" + getPin(), task.getError());
                        }

                        int count = task.getResult();
                        if (count == 0) {
                            Log.w(TAG, "Unpin for " + getPin() + " completed successfully");
                        } else {
                            Log.e(TAG, "Still has objects in LDS after unpinning " + getPin() + ": " + count);
                        }
                        return null;
                    }
                });


    }

    public <T extends ParseObject> Task<List<T>> updateAllAsync() {
        ParseQuery<T> query = getAllNetworkQuery();
        return updateAll(query, 1000);
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
        query.findInBackground(new FindCallback<T>() {

            @Override
            public void done(List<T> objects, ParseException e) {
                pinAllUpdates(objects, getPin(), callback);
            }

        });
    }


    public void saveEventuallyAndNotify() {
        saveEventuallyAndNotify(null, UpdateUIEvent.ACTION.UPDATE);
    }

    public void saveEventuallyAndNotify(final SaveCallback saved) {
        saveEventuallyAndNotify(saved, UpdateUIEvent.ACTION.UPDATE);
    }

    public void saveEventuallyAndNotify(final SaveCallback saved, final UpdateUIEvent.ACTION action) {
        ExtendedParseObject.this.saveEventually(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    LogError.log(TAG, "Failed to save object", e);
                }

                doneSaving(e, saved, action);

            }
        });
    }

//    public void pinThenSaveEventually() {
//        pinThenSaveEventually(getPin(), null, null, UpdateUIEvent.ACTION.UPDATE);
//    }
//
//    public void pinThenSaveEventually(final SaveCallback pinned) {
//        pinThenSaveEventually(getPin(), pinned, null, UpdateUIEvent.ACTION.UPDATE);
//    }
//
//    public void pinThenSaveEventually(final SaveCallback pinned, final SaveCallback saved) {
//        pinThenSaveEventually(getPin(), pinned, saved, UpdateUIEvent.ACTION.UPDATE);
//    }
//
//
//    public void pinThenSaveEventually(final String pin, final SaveCallback pinned, final SaveCallback savedCallback, final UpdateUIEvent.ACTION action) {
//
//        Log.d(TAG, "pinThenSaveEventually: " + pin);
//
//
//        this.pinInBackground(pin, new SaveCallback() {
//            @Override
//            public void done(ParseException e) {
//                if (e != null) {
//                    new HandleException(TAG, "pinThenSaveEventually failed to pin", e);
//                }
//                if (pinned != null) {
//                    Log.d(TAG, "pin callback");
//                    pinned.done(e);
//                }
//
//
//                if (pin.equals(NEW_OBJECT_PIN)) {
//                    ExtendedParseObject.this.unpinInBackground(NEW_OBJECT_PIN);
//                }
//
//                ExtendedParseObject.this.saveInBackground(new SaveCallback() {
//                    @Override
//                    public void done(ParseException e) {
//                        if (e != null) {
//                            LogError.log(TAG, "Failed to save in background", e);
//
//                            new HandleException(TAG, "pinThenSaveEventually fallback to saveEventually", e);
//
//                            ExtendedParseObject.this.saveEventually(new SaveCallback() {
//                                @Override
//                                public void done(ParseException e) {
//                                    if (e != null) {
//                                        LogError.log(TAG, "Failed to save object", e);
//                                    }
//
//                                    doneSaving(e, savedCallback, action);
//
//                                }
//                            });
//
//                            return;
//                        }
//
//                        doneSaving(null, savedCallback, action);
//                    }
//                });
//            }
//        });
//
//    }

    private void doneSaving(ParseException e, SaveCallback savedCallback, UpdateUIEvent.ACTION action) {
        if (savedCallback != null) {
            savedCallback.done(null);
        }

        if (e == null) {
            EventBusController.postUIUpdate(this, action);
        }
    }

    public <T extends ParseObject> void pinUpdate(final T object, final DataStoreCallback<T> callback) {
        object.unpinInBackground(new DeleteCallback() {

            @Override
            public void done(ParseException e) {
                if (e != null) {
                    if (callback != null)
                        callback.failed(e);
                    return;
                }
                object.pinInBackground(getPin(), new SaveCallback() {

                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            if (callback != null) {
                                ArrayList<T> callbackObject = new ArrayList<T>();
                                callbackObject.add(object);
                                callback.success(callbackObject);
                            }
                        } else {
                            Log.e(TAG, "pinUpdate", e);
                            if (callback != null)
                                callback.failed(e);
                        }
                    }

                });
            }
        });
    }


    private <T extends ParseObject> void pinAllUpdates(final List<T> objects,
                                                                final String pin, final DataStoreCallback<T> callback) {

        if (objects == null) {
            callback.success(Lists.<T>newArrayList());
            return;
        }
        ParseObject.pinAllInBackground(pin, objects, new SaveCallback() {

            @Override
            public void done(ParseException e) {
                if (e == null) {
                    if (callback != null)
                        callback.success(objects);
                } else {
                    if (callback != null)
                        callback.failed(e);
                }
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

    protected String getStringSafe(String key) {
        return has(key) ? getString(key) : "";
    }

    protected String getStringSafe(String key, String defaultValue) {
        return has(key) ? getString(key) : defaultValue;
    }

    @Override
    public int compareTo(@NonNull ExtendedParseObject another) {
        return 0;
    }


}
