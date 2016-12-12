package com.guardswift.persistence.cache.data;

import android.content.Context;

import com.guardswift.dagger.InjectingApplication;
import com.guardswift.persistence.cache.ParseCache;
import com.guardswift.persistence.parse.data.Guard;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by cyrix on 10/21/15.
 */
@Singleton
public class GuardCache extends ParseCache<Guard> {

    public static final String LOGGED_IN = "logged_in";
    public static final String LAST_ACTIVE = "last_active";

    @Inject
    GuardCache(@InjectingApplication.InjectingApplicationModule.ForApplication Context context) {
        super(Guard.class, context);
    }

    public void setLoggedIn(Guard guard) {
        put(LOGGED_IN, guard);
        put(LAST_ACTIVE, guard);
    }

    public Guard getLoggedIn() {
        return get(LOGGED_IN);
    }

    public boolean isLoggedIn() {
        return has(LOGGED_IN);
    }

    public Guard getLastActive() {
        return get(LAST_ACTIVE);
    }


    public void removeLoggedIn() {
        remove(LOGGED_IN);
    }


}
