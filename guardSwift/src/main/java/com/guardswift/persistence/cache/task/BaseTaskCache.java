package com.guardswift.persistence.cache.task;

import android.content.Context;

import com.guardswift.persistence.cache.ParseCache;
import com.guardswift.persistence.parse.execution.task.ParseTask;

public abstract class BaseTaskCache<T extends ParseTask> extends ParseCache<T> {

    protected BaseTaskCache(Class<T> subClass, Context context) {
        super(subClass, context);
    }


    @SuppressWarnings("unchecked")
    public void setSelected(ParseTask task) {
        put("selected", (T) task);
    }

    public T getSelected() {
        return get("selected");
    }

}
