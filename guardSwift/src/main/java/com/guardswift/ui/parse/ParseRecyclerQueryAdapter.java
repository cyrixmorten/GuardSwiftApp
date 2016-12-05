package com.guardswift.ui.parse;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;

import com.google.common.collect.Lists;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter.QueryFactory;

import java.util.ArrayList;
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
 *  NEARLY IDENTICAL REPLACEMENT FOR ParseQueryAdapter ON ListView.
 *  REQUIRES THAT YOU SUBCLASS TO CREATE ViewHolder, onBindViewHolder(), and onCreateViewHolder
 *  AS ENFORCED BY THE RECYCLERVIEW PATTERN.
 *
 *  TESTED SUCCESSFULLY with RecyclerView v7:21.0.3
 *  AND with SuperRecyclerView by Malinskiy
 *  @ https://github.com/Malinskiy/SuperRecyclerView
 *  SHOULD WORK WITH UltimateRecyclerView
 */
public abstract class ParseRecyclerQueryAdapter<T extends ParseObject, U extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<U>
{
    /**
     * START My own tweaks
     */

    // may be null if not attached to activity or fragment
    protected Context context;
    protected FragmentManager fragmentManager;
    protected boolean fromLocalDataStore;

    public void onAttatch(Context context) {
        this.context = context;
    }
    public void onDetatch() {
        this.context = null;
    }

    public void setFromLocalDataStore(boolean fromLocalDataStore) {
        this.fromLocalDataStore = fromLocalDataStore;
    }

    public boolean isFromLocalDataStore() {
        return fromLocalDataStore;
    }

    private PostProcessAdapterResults<T> postProcessor;

    public void setPostProcessor(PostProcessAdapterResults<T> postProcessor) {
        this.postProcessor = postProcessor;
    }

    /**
     * Enables an implementation to change/sort/order incoming results before being passed to the recycler adapter
     *
     * @param queriedItems
     * @return result after postprocessing
     */
//    protected List<T>  postProcessResults(List<T> queriedItems) {
//        return queriedItems;
//    };

    /**
     * END
     */

    private final QueryFactory<T> mFactory;
    private final boolean hasStableIds;
    private final List<T> mItems;

    public ParseRecyclerQueryAdapter(final QueryFactory<T> factory) {
        mFactory = factory;
        mItems = new ArrayList<T>();
        mDataSetListeners = Lists.newCopyOnWriteArrayList();
        mQueryListeners = Lists.newCopyOnWriteArrayList();
        this.hasStableIds = false;

        setHasStableIds(false);
    }

    // PRIMARY CONSTRUCTOR
    public ParseRecyclerQueryAdapter(final QueryFactory<T> factory, final boolean hasStableIds) {
        mFactory = factory;
        mItems = new ArrayList<T>();
        mDataSetListeners = Lists.newCopyOnWriteArrayList();
        mQueryListeners = Lists.newCopyOnWriteArrayList();
        this.hasStableIds = hasStableIds;

        setHasStableIds(hasStableIds);
    }

    // ALTERNATE CONSTRUCTOR
    public ParseRecyclerQueryAdapter(final String className, final boolean hasStableIds) {
        this(new QueryFactory<T>() {

            @Override public ParseQuery<T> create() {
                return ParseQuery.getQuery(className);
            }
        }, hasStableIds);
    }

    // ALTERNATE CONSTRUCTOR
    public ParseRecyclerQueryAdapter(final Class<T> clazz, final boolean hasStableIds) {
        this(new QueryFactory<T>() {

            @Override public ParseQuery<T> create() {
                return ParseQuery.getQuery(clazz);
            }
        }, hasStableIds);
    }



  /*
   *  REQUIRED RECYCLERVIEW METHOD OVERRIDES
   */

    @Override
    public long getItemId(int position) {
        if (hasStableIds) {
            return position;
        }
        return super.getItemId(position);
    }

    @Override public int getItemCount() {
        return mItems.size();
    }

    public T getItem(int position) { return mItems.get(position); }

    public List<T> getItems() { return mItems; }




    /**
     * Apply alterations to query prior to running findInBackground.
     */
    protected void onFilterQuery(ParseQuery<T> query) {
        // provide override for filtering query

        if (isFromLocalDataStore())
            query.fromLocalDatastore();
    }

    public synchronized void loadObjects() {
        dispatchOnLoading();
        final ParseQuery<T> query = mFactory.create();
        onFilterQuery(query);
        query.findInBackground(new FindCallback<T>() {;

            @Override public void done(
                    List<T> queriedItems,
                    @Nullable ParseException e) {

                if (postProcessor != null) {
                    queriedItems = postProcessor.postProcess(queriedItems);
                }

                if (e == null) {
                    mItems.clear();
                    mItems.addAll(queriedItems);
                    notifyDataSetChanged();
                }
                dispatchOnLoaded(queriedItems, e);
                fireOnDataSetChanged();
            }
        });
    }



    public interface OnDataSetChangedListener {
        public void onDataSetChanged();
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

    protected synchronized void  fireOnDataSetChanged() {
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

    public synchronized void removeOnQueryLoadListener(
            OnQueryLoadListener<T> listener) {
        if (mQueryListeners.contains(listener)) {
            mQueryListeners.remove(listener);
        }
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