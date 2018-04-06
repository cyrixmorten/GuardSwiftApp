package com.guardswift.eventbus;

import com.guardswift.eventbus.events.UpdateUIEvent;
import com.guardswift.util.Device;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by cyrix on 10/13/15.
 */
public class EventBusController {

    public static class ForceUIUpdate {}

    public static void post(Object object) {
        EventBus.getDefault().post(object);
    }

    public static void postUIUpdate(Object object) {
        postUIUpdate(object, UpdateUIEvent.ACTION.UPDATE);
    }

    public static void postUIUpdate(Object object, UpdateUIEvent.ACTION action) {
        if (!Device.isScreenOn()) {
//            Log.w("EventBusController", "Blocked UI Event - screen is off " + object.getClass().getSimpleName());
        }
        if (object instanceof List) {
            List objList = (List)object;
            if (!objList.isEmpty()) {
                post(new UpdateUIEvent(objList.iterator().next(), action));
            }
        } else {
            post(new UpdateUIEvent(object, action));
        }
    }

    public static void postUIUpdate() {
        postUIUpdate(new ForceUIUpdate());
    }



}
