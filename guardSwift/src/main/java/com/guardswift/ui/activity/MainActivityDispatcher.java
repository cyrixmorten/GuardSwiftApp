package com.guardswift.ui.activity;

import com.parse.ui.login.ParseLoginDispatchActivity;

/**
 * Created by cyrix on 1/31/16.
 */
public class MainActivityDispatcher extends ParseLoginDispatchActivity {
    @Override
    protected Class<?> getTargetClass() {
        return MainActivity.class;
    }
}