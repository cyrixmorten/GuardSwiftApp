package com.guardswift.eventbus;

import android.os.Handler;

import com.guardswift.eventbus.events.UpdateUIEvent;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.util.Device;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by cyrix on 10/13/15.
 */
public class EventBusController {

    public static class ForceUIUpdate {};

    public static void post(Object object) {
        EventBus.getDefault().post(object);
    }

    public static void postParseObjectUpdated(ExtendedParseObject object) {
        // ignore
//        post(new ParseObjectUpdatedEvent(object));
    }

    public static void postUIUpdate(final Object object, int delayMilliseconds) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                postUIUpdate(object);
            }
        }, delayMilliseconds);
    }

    public static void postUIUpdate(Object object) {
        if (!Device.isScreenOn()) {
//            Log.w("EventBusController", "Blocked UI Event - screen is off " + object.getClass().getSimpleName());
        }
        if (object instanceof List) {
            List objList = (List)object;
            if (!objList.isEmpty()) {
                post(new UpdateUIEvent(objList.iterator().next()));
            }
        } else {
            post(new UpdateUIEvent(object));
        }
    }

//    public static void postUIUpdate() {
//        postUIUpdate(new ForceUIUpdate());
//    }



}
