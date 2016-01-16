package com.guardswift.persistence.parse;

import android.content.Context;
import android.util.Log;

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

import bolts.Task;

public abstract class ExtendedParseObject extends ParseObject {


    // TODO get rid of this and use bolts Tasks instead
    public interface DataStoreCallback<T extends ParseObject> {
        public void success(List<T> objects);

        public void failed(ParseException e);
    }


    public static final String createdAt = "createdAt";

    protected static final String objectId = "objectId";
    protected static final String owner = "owner";


    protected final String TAG = this.getClass()
            .getSimpleName();

    public void setDefaultOwner() {
        put(owner, ParseUser.getCurrentUser());
    }

    public abstract <T extends ParseObject> ParseQuery<T> getAllNetworkQuery();

    public abstract void updateFromJSON(Context context, JSONObject jsonObject)
            throws JSONException;

    public abstract String getPin();



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

    public <T extends ParseObject> Task<List<ParseObject>> updateAllAsync() {
        final Task<List<ParseObject>>.TaskCompletionSource successful = Task.create();

        updateAll(getAllNetworkQuery().setLimit(1000), new DataStoreCallback<ParseObject>() {

            @Override
            public void success(List<ParseObject> objects) {
                successful.setResult(objects);
            }

            @Override
            public void failed(ParseException e) {
                successful.setError(e);
            }
        });

        return successful.getTask();
    }

    @SuppressWarnings("unchecked")
    public <T extends ParseObject> void updateAll(DataStoreCallback<T> callback) {
        updateAll((ParseQuery<T>) getAllNetworkQuery().setLimit(1000), callback);
    }

    public <T extends ParseObject> Task<List<T>> updateAll(ParseQuery<T> query) {

        if (query == null) {
            query = getAllNetworkQuery();
        }

        final Task<List<T>>.TaskCompletionSource successful = Task.create();

        updateAll(query.setLimit(1000), new DataStoreCallback<T>() {
            @Override
            public void success(List<T> objects) {
                successful.setResult(objects);
            }

            @Override
            public void failed(ParseException e) {

            }
        });

        return successful.getTask();
    }


    public <T extends ParseObject> void updateAll(ParseQuery<T> query, final DataStoreCallback<T> callback) {
        query.findInBackground(new FindCallback<T>() {

            @Override
            public void done(List<T> objects, ParseException e) {
//                Log.d(TAG, "Updating " + getPin() + " " + objects.size());
                unpinThenPinAllUpdates(objects, e,
                        callback);
            }

        });
    }

//    public <T extends ParseObject> void updateAll(List<T> objects) {
//        unpinThenPinAllUpdates(objects, null, null);
//    }


    public <T extends ParseObject> void pinThenSaveEventually() {
        pinThenSaveEventually(null, null, false);
    };

    public <T extends ParseObject> void pinThenSaveEventually(boolean postUIUpdate) {
        pinThenSaveEventually(null, null, postUIUpdate);
    };

    public <T extends ParseObject> void pinThenSaveEventually(final SaveCallback pinned) {
        pinThenSaveEventually(pinned, null, false);
    };

    public <T extends ParseObject> void pinThenSaveEventually(final SaveCallback pinned, final SaveCallback saved) {
        pinThenSaveEventually(pinned, saved, false);
    }

    public <T extends ParseObject> void pinThenSaveEventually(final SaveCallback pinned, final SaveCallback saved, final boolean postUIUpdate) {

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
                            if (saved != null) {
                                saved.done(e);
                            }
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

    public <T extends ParseObject> void unpinThenPinAllUpdates(
            final List<T> objects) {

        unpinThenPinAllUpdates(objects, null, null);
    }
    private <T extends ParseObject> void unpinThenPinAllUpdates(
            final List<T> objects,
            ParseException fetchFromNetworkException,
            final DataStoreCallback<T> callback) {

        if (fetchFromNetworkException != null || objects == null) {
            if (callback != null)
                callback.failed(fetchFromNetworkException);
            return;
        }

        final String pin = getPin();


//        Log.i(TAG, "updateServerDataPin " + pin + " " + objects.size());
//        Log.i(TAG, "remove existing - unpinning" + pin);
//
        ParseObject.unpinAllInBackground(objects, new DeleteCallback() {

            @Override
            public void done(ParseException e) {

                if (e != null) {
                    Log.e(TAG, "Pinning failed - unpinAllInBackground");
                    if (callback != null)
                        callback.failed(e);
                    return;
                }

                Log.i(TAG, "unpin " + pin + " complete");
                pinAllUpdates(objects, pin, callback);

            }

        });

    }

    private <T extends ParseObject> void pinAllUpdates(final List<T> objects,
                                                                final String pin, final DataStoreCallback<T> callback) {
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
