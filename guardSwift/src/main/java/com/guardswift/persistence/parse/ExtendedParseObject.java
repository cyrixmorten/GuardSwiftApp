package com.guardswift.persistence.parse;

import android.content.Context;
import android.util.Log;

import com.google.common.collect.Lists;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.eventbus.EventBusController;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;

public abstract class ExtendedParseObject extends ParseObject {


    // TODO get rid of this and use bolts Tasks instead
    public interface DataStoreCallback<T extends ParseObject> {
        void success(List<T> objects);

        void failed(ParseException e);
    }


    public static final String createdAt = "createdAt";

    protected static final String objectId = "objectId";
    protected static final String owner = "owner";


    protected final String TAG = this.getClass()
            .getSimpleName();

    protected void setDefaultOwner() {
        put(owner, ParseUser.getCurrentUser());
    }

    public abstract <T extends ParseObject> ParseQuery<T> getAllNetworkQuery();

    public abstract void updateFromJSON(Context context, JSONObject jsonObject)
            throws JSONException;

    public abstract String getParseClassName();
    public String getPin() {
        return ParseObject.DEFAULT_PIN;
    };



    public <T extends ParseObject> void getFromObjectId(final Class<T> clazz,
                                                        final String objectId, final boolean tryNetwork,
                                                        final GetCallback<T> getCallback) {

        Log.d(TAG, "getFromObjectId " + clazz.getSimpleName() + " : "
                + objectId);
        final ParseQuery<T> networkQuery = new ParseQuery<T>(clazz)
                .whereEqualTo(ExtendedParseObject.objectId, objectId);
        ParseQuery<T> datastoreQuery = new ParseQuery<T>(clazz).whereEqualTo(
                ExtendedParseObject.objectId, objectId).fromLocalDatastore();

        datastoreQuery.getFirstInBackground(new GetCallback<T>() {

            @Override
            public void done(T object, ParseException e) {
                if (e == null) {
                    // found in datastore
                    Log.d(TAG, "getFromObjectId found in datastore");
                    getCallback.done(object, e);

                } else {
                    if (!tryNetwork) {
                        getCallback.done(object, e);
                        return;
                    }

                    // try network
                    Log.d(TAG, "getFromObjectId network lookup for " + clazz.getSimpleName() + " with id " + objectId);
                    networkQuery.getFirstInBackground(getCallback);
                }
            }
        });
    }

    public  Task<Object> unpinAllPinnedToClass() {
        Log.w(TAG, "Unpinning" + getPin());
        return unpinAllInBackground(getPin())
//        return getAllNetworkQuery().fromLocalDatastore().findInBackground().continueWithTask(new Continuation<List<ParseObject>, ParseTask<Void>>() {
//            @Override
//            public ParseTask<Void> then(ParseTask<List<ParseObject>> task) throws Exception {
//                Log.w(TAG, "Unpinning from " + getPin() + " objects: " + task.getResult().size());
//                return ParseObject.unpinAllInBackground(task.getResult());
//            }
//        }).
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

    public <T extends ParseObject> Task<List<ParseObject>> updateAllAsync() {
        return updateAll(getAllNetworkQuery(), 1000);
    }

    @SuppressWarnings("unchecked")
    public <T extends ParseObject> void updateAll(DataStoreCallback<T> callback) {
        updateAll((ParseQuery<T>) getAllNetworkQuery().setLimit(1000), callback);
    }


    public <T extends ParseObject> Task<List<T>> updateAll(ParseQuery<T> query, int limit) {

        if (query == null) {
            query = getAllNetworkQuery();
        }

        final bolts.TaskCompletionSource promise = new TaskCompletionSource();


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


    public <T extends ParseObject> void updateAll(ParseQuery<T> query, final DataStoreCallback<T> callback) {
        query.findInBackground(new FindCallback<T>() {

            @Override
            public void done(List<T> objects, ParseException e) {
//                Log.d(TAG, "Updating " + getPin() + " " + objects.size());
//                unpinThenPinAllUpdates(objects, e,
//                        callback);

                pinAllUpdates(objects, getPin(), callback);
            }

        });
    }

//    public <T extends ParseObject> void updateAll(List<T> objects) {
//        unpinThenPinAllUpdates(objects, null, null);
//    }


    public void pinThenSaveEventually() {
        pinThenSaveEventually(null, null, false);
    }

    public void pinThenSaveEventually(boolean postUIUpdate) {
        pinThenSaveEventually(null, null, postUIUpdate);
    }

    public void pinThenSaveEventually(final SaveCallback pinned) {
        pinThenSaveEventually(pinned, null, false);
    }

    public void pinThenSaveEventually(final SaveCallback pinned, final SaveCallback saved) {
        pinThenSaveEventually(pinned, saved, false);
    }

    public void pinThenSaveEventually(final SaveCallback pinned, final SaveCallback saved, final boolean postUIUpdate) {

        this.pinInBackground(getPin(), new SaveCallback() {
            @Override
            public void done(ParseException e) {

                if (pinned != null) {
                    pinned.done(e);
                }

                if (postUIUpdate) {
                    EventBusController.postUIUpdate(ExtendedParseObject.this);
                }

                ExtendedParseObject.this.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            new HandleException(TAG, "pinThenSaveEventually " + getPin() + " fallback to saveEventually", e);
                            ExtendedParseObject.this.saveEventually(saved);
                            return;
                        }

                        if (saved != null) {
                            saved.done(null);
                        }
                    }
                });
            }
        });
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

//    public <T extends ParseObject> void unpinThenPinAllUpdates(
//            final List<T> objects) {
//
//        unpinThenPinAllUpdates(objects, null, null);
//    }
//    private <T extends ParseObject> void unpinThenPinAllUpdates(
//            final List<T> objects,
//            ParseException fetchFromNetworkException,
//            final DataStoreCallback<T> callback) {
//
//        if (fetchFromNetworkException != null || objects == null) {
//            if (callback != null)
//                callback.failed(fetchFromNetworkException);
//            return;
//        }
//
//        final String pin = getPin();
//
//
//        Log.i(TAG, "updateServerDataPin " + pin + " " + objects.size());
//        Log.i(TAG, "remove existing - unpinning" + pin);
//
//        ParseObject.unpinAllInBackground(objects, new DeleteCallback() {
//
//            @Override
//            public void done(ParseException e) {
//
//                if (e != null) {
//                    Log.e(TAG, "Pinning failed - unpinAllInBackground");
//                    if (callback != null)
//                        callback.failed(e);
//                    return;
//                }
//
//                Log.i(TAG, "unpin " + pin + " complete");
//                pinAllUpdates(objects, pin, callback);
//
//            }
//
//        });
//
//    }

    private <T extends ParseObject> void pinAllUpdates(final List<T> objects,
                                                                final String pin, final DataStoreCallback<T> callback) {

        if (objects == null) {
            callback.success(Lists.<T>newArrayList());
            return;
        }
//        Log.i(TAG, "pinning " + pin + "...");
        // ParseObject.pinAllInBackground(objects, objects, new SaveCallback() {
        ParseObject.pinAllInBackground(pin, objects, new SaveCallback() {

            @Override
            public void done(ParseException e) {
                if (e == null) {
//                    Log.i(TAG, "pinning " + pin + " complete " + objects.size()
//                            + " pinned");
                    if (callback != null)
                        callback.success(objects);
                } else {
//                    Log.e(TAG, "unpinThenPinAllUpdates", e);
                    if (callback != null)
                        callback.failed(e);
                }
            }

        });
    }

    /**
     * A flat JSONObject version of all the information stored in this object
     *
     * @return JSONObject
     */
    public JSONObject asJSONObject() {
        JSONObject jsonObject = new JSONObject();
        for (String key : keySet()) {
            try {
                jsonObject.put(key, get(key));
            } catch (JSONException e) {
                new HandleException(getPin(), "asJSONObject", e);
            }
        }
        return jsonObject;
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
//    public void pinAndSaveEventually() {
//        pinAndSaveEventually(null);
//    }
//
//    public void pinAndSaveEventually(final SaveCallback saveCallback) {
//        pinInBackground(getPin(), new SaveCallback() {
//
//            @Override
//            public void done(ParseException e) {
//                if (saveCallback != null) {
//                    saveCallback.done(e);
//                }
//                saveEventually();
//            }
//
//        });
//    }


}
