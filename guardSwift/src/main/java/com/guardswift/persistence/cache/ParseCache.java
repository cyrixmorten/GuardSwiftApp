package com.guardswift.persistence.cache;

import android.content.Context;
import android.util.Log;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.persistence.Preferences;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by cyrix on 10/21/15.
 */
public abstract class ParseCache<T extends ParseObject> extends Preferences{


    private static final String TAG = ParseCache.class.getSimpleName();

    private Map<String, Set<T>> cacheCollection = Maps.newConcurrentMap();
    private Map<String, T> cacheSingle = Maps.newConcurrentMap();

    private final Class<T> subClass;

    protected ParseCache(Class<T> subClass, Context context) {
        super(context, subClass.getSimpleName());
        this.subClass = subClass;
    }


    /**
     *
     * Generates an unique key for this particular store to avoid name collision
     *
     * @param name
     * @return key
     */
    private String getPreferenceKey(String name) {
        return subClass.getSimpleName()+"-"+name;
    }


    protected void put(String name, T object) {
        if (object == null)
            return;

        String key = getPreferenceKey(name);
//        Log.w(TAG, subClass.getSimpleName() + " adding name: " + name + " object: " + object + " to key: " + key);
//        singleMapCache.put(key, object); // Store in memory
        super.put(key, object.getObjectId()); // Store objectId in SharedPreferences
//        super.addUnique(subClass.getSimpleName(), name); // Store a list of names associated to this class
        cacheSingle.put(key, object);
    }

    public void remove(String name) {
        String key = getPreferenceKey(name);
        super.remove(key);
        cacheSingle.remove(key);
    }

    public boolean has(String name) {
        return get(name) != null;
    }

    @SuppressWarnings("unchecked")
    protected T get(String name) {

        if (name.isEmpty()) {
            Log.w(TAG, "ParseCache LDS lookup empty name");
            return null;
        }
        String key = getPreferenceKey(name);
        String objectId = super.getString(key);

//        Log.w(TAG, subClass.getSimpleName() + " getting name: " + name + " with key: " + key + " and objectId: " + objectId);

        // look in cache
        if (cacheSingle.containsKey(key)) {
//            Log.w(TAG, subClass.getSimpleName() + " " + objectId + " found in cache");
            return cacheSingle.get(key);
        }
//        Log.w(TAG, subClass.getSimpleName() + objectId + " not found in cache - looking up in LDS");
        // look up in local datastore
        ParseQuery<T> query = ParseQuery.getQuery(subClass);
        query.fromLocalDatastore();
        try {
            Log.w(TAG, "ParseCache LDS lookup - " + name + " : " + objectId);
            T parseObject =  query.get(objectId);
            cacheSingle.put(key, parseObject);

            return parseObject;
        } catch (ParseException e) {
            new HandleException(TAG, subClass.getSimpleName() + " ParseCache get " + objectId, e);
        }

        return null;
    }

    protected boolean addUnique(String name, T object) {
        String key = getPreferenceKey(name);

        super.addUnique(key, object.getObjectId());

        // update collection
        Set<T> cache = (cacheCollection.containsKey(key)) ? cacheCollection.get(key) : Sets.<T>newConcurrentHashSet();
        boolean add = cache.add(object);
        cacheCollection.put(key, cache);

        return add;
    }

    protected boolean removeUnique(String name, T object) {
        String key = getPreferenceKey(name);

        super.removeUnique(key, object.getObjectId());

        // update collection
        Set<T> cache = (cacheCollection.containsKey(key)) ? cacheCollection.get(key) : Sets.<T>newConcurrentHashSet();
        boolean remove = cache.remove(object);
        cacheCollection.put(key, cache);

        return remove;
    }

    public void clearSet(String name) {
        String key = getPreferenceKey(name);

        super.clearSet(key);

        // clear collection
        cacheCollection.put(key, Sets.<T>newConcurrentHashSet());
    }

    @SuppressWarnings("unchecked")
    protected Set<T> getSet(String name) {
        String key = getPreferenceKey(name);
        Set<String> objectIds = super.getStringSet(key);

        // look in cache
        Set<T> cachedObjects = cacheCollection.get(key);
        if (cachedObjects != null && cachedObjects.size() == objectIds.size()) {
            return cachedObjects;
        }

        // look up in local datastore
        ParseQuery<T> query = ParseQuery.getQuery(subClass);
        query.whereContainedIn("objectId", objectIds);
        query.fromLocalDatastore();
        try {
            List<T> parseObjects =  query.find();

            // update cache
            cacheCollection.put(key, Sets.newConcurrentHashSet(parseObjects));

            return Sets.newConcurrentHashSet(parseObjects);
        } catch (ParseException e) {
            new HandleException(TAG, subClass.getSimpleName() + " ParseCache getSet " + objectIds.toString(), e);
        }

        return Sets.newConcurrentHashSet();
    }

    protected boolean hasUnique(String name, T object) {
        return getSet(name).contains(object);
    }

}
