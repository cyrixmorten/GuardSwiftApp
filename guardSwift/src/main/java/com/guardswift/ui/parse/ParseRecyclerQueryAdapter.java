package com.guardswift.ui.parse;

import android.content.Context;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;

import com.google.common.collect.Lists;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ui.widget.ParseQueryAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * The MIT License (MIT)
 *
 * Copyright (c) <2015> <ameron32>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


/**
 * NEARLY IDENTICAL REPLACEMENT FOR ParseQueryAdapter ON ListView.
 * REQUIRES THAT YOU SUBCLASS TO CREATE ViewHolder, onBindViewHolder(), and onCreateViewHolder
 * AS ENFORCED BY THE RECYCLERVIEW PATTERN.
 * <p>
 * TESTED SUCCESSFULLY with RecyclerView v7:21.0.3
 * AND with SuperRecyclerView by Malinskiy
 *
 * @ https://github.com/Malinskiy/SuperRecyclerView
 * SHOULD WORK WITH UltimateRecyclerView
 */
public abstract class ParseRecyclerQueryAdapter<T extends ExtendedParseObject, U extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<U> {

    private static final String TAG = ParseRecyclerQueryAdapter.class.getSimpleName();
    /**
     * START My own tweaks
     */

    // may be null if not attached to activity or fragment
    protected Context context;
    protected FragmentManager fragmentManager;

    //private ParseQuery<T> liveQuery;

    public void onAttach(Context context) {
        this.context = context;
    }

    public void onDetach() {
        this.context = null;

        //unsubscribeLiveQuery();
    }



    private final ParseQueryAdapter.QueryFactory<T> mFactory;
    private final List<T> mItems;

    public ParseRecyclerQueryAdapter(final ParseQueryAdapter.QueryFactory<T> factory) {
        mFactory = factory;
        mItems = new ArrayList<>();
        mDataSetListeners = Lists.newCopyOnWriteArrayList();
        mQueryListeners = Lists.newCopyOnWriteArrayList();

        setHasStableIds(true);
    }




  /*
   *  REQUIRED RECYCLERVIEW METHOD OVERRIDES
   */

    @Override
    public long getItemId(int position) {
        return getItem(position).getObjectId().hashCode();
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public T getItem(int position) {
        return mItems.get(position);
    }

    public List<T> getItems() {
        return mItems;
    }



    synchronized void loadObjects() {
        dispatchOnLoading();
        final ParseQuery<T> query = mFactory.create();
        query.findInBackground((queriedItems, e) -> {

            if (queriedItems != null && e == null) {
                Log.d(TAG, "queriedItems: " + queriedItems.size());

                showResults(queriedItems);
            }

            if (e != null) {
                Log.e(TAG, "Adapter load", e);
            }

            dispatchOnLoaded(queriedItems, e);
        });

//        subscribeLiveQuery(query);
    }

    /*
    private void unsubscribeLiveQuery() {
        if (this.liveQuery != null) {

            GuardSwiftApplication.getInstance().getLiveQueryClient().unsubscribe(this.liveQuery);

            this.liveQuery = null;
        }
    }


    private void subscribeLiveQuery(ParseQuery<T> query) {
        ParseLiveQueryClient liveQueryClient = GuardSwiftApplication.getInstance().getLiveQueryClient();

        SubscriptionHandling<T> subscriptionHandling = liveQueryClient.subscribe(query);

        subscriptionHandling.handleEvents((query1, event, object) -> {

            Log.d(TAG, "onEvent: " + event.name());

            new Handler(Looper.getMainLooper()).post(() -> {
            switch (event) {
                case CREATE: {
                    addItem(object);
                    break;
                }
                case ENTER: {
                    addItem(object);
                    break;
                }
                case UPDATE: {
                    updateItem(object);
                    break;
                }
                case LEAVE: {
                    removeItem(object);
                    break;
                }
                case DELETE: {
                    removeItem(object);
                    break;
                }
            }
            });
        });
    }
    */

    private int indexOf(T object) {
        int index = -1;
        for (int i = 0; i < mItems.size(); i++) {
            T existing = mItems.get(i);

            if (existing.getObjectId().equals(object.getObjectId())) {
                index = i;
                break;
            }
        }
        return index;
    }

    public void addItem(T object) {
        Log.e(TAG, "addItem");
        int existingIndex = indexOf(object);

        if (existingIndex == -1) {
            mItems.add(object);
            Collections.sort(mItems);
            notifyDataSetChanged();
        }
    }

    public void removeItem(T object) {
        Log.e(TAG, "removeItem");
        int existingIndex = indexOf(object);

        if (existingIndex != -1) {
            mItems.remove(existingIndex);
            notifyDataSetChanged();
        }
    }

    public void updateItem(T object) {
        Log.e(TAG, "updateItem");
        int existingIndex = indexOf(object);

        if (existingIndex != -1) {
            mItems.remove(existingIndex);
            mItems.add(existingIndex, object);
            notifyItemChanged(existingIndex);
        }


    }

    private synchronized void showResults(List<T> results) {
        mItems.clear();
        mItems.addAll(results);
        Collections.sort(mItems);
        notifyUpdate();
    }


    private void notifyUpdate() {
        notifyDataSetChanged();
        fireOnDataSetChanged();
    }


    public interface OnDataSetChangedListener {
        void onDataSetChanged();
    }

    private final List<OnDataSetChangedListener> mDataSetListeners;

    public synchronized void addOnDataSetChangedListener(OnDataSetChangedListener listener) {
        mDataSetListeners.add(listener);
    }

    public synchronized void removeOnDataSetChangedListener(OnDataSetChangedListener listener) {
        if (mDataSetListeners.contains(listener)) {
            mDataSetListeners.remove(listener);
        }
    }

    private synchronized void fireOnDataSetChanged() {
        for (int i = 0; i < mDataSetListeners.size(); i++) {
            mDataSetListeners.get(i).onDataSetChanged();
        }
    }

    public interface OnQueryLoadListener<T> {

        void onLoaded(
                List<T> objects, Exception e);

        void onLoading();
    }

    private final List<OnQueryLoadListener<T>> mQueryListeners;

    public synchronized void addOnQueryLoadListener(
            OnQueryLoadListener<T> listener) {
        if (!(mQueryListeners.contains(listener))) {
            mQueryListeners.add(listener);
        }
    }

    synchronized void removeOnQueryLoadListener(
            OnQueryLoadListener<T> listener) {
        mQueryListeners.remove(listener);
    }

    private synchronized void dispatchOnLoading() {
        for (OnQueryLoadListener<T> l : mQueryListeners) {
            l.onLoading();
        }
    }

    private synchronized void dispatchOnLoaded(List<T> objects, ParseException e) {
        for (OnQueryLoadListener<T> l : mQueryListeners) {
            l.onLoaded(objects, e);
        }
    }


}