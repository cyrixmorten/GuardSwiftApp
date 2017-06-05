package com.guardswift.ui.helpers;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;


/**
 * Activities/fragments implementing this interface should be prepared to update the FAB of a parent once this method is invoked
 */
public interface UpdateFloatingActionButton {
	void updateFloatingActionButton(Context context, FloatingActionButton floatingActionButton);
}
