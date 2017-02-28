package com.guardswift.core.tasks.automation;

/**
 * Created by cyrix on 6/7/15.
 */
public class NoAutomationStrategy implements TaskAutomationStrategy {

    private static final String TAG = NoAutomationStrategy.class.getSimpleName();


    public static NoAutomationStrategy getInstance() { return new NoAutomationStrategy(); }

    private NoAutomationStrategy() {}

    @Override
    public void automaticArrival() {
    }

    @Override
    public void automaticDeparture() {
    }


}
