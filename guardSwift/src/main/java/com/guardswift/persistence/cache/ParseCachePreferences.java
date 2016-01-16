package com.guardswift.persistence.cache;

import android.content.Context;

import com.guardswift.dagger.InjectingApplication;
import com.guardswift.persistence.Preferences;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Private SharedPreferences instance used by ParseCache objects
 *
 * Created by cyrix on 10/23/15.
 */
@Singleton
public class ParseCachePreferences extends Preferences{

    @Inject
    public ParseCachePreferences(@InjectingApplication.InjectingApplicationModule.ForApplication Context context) {
        super(context, "ParseCache");
    }
}
