package com.guardswift.ui.dialog.activity;

import android.os.Bundle;
import android.view.WindowManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.guardswift.dagger.InjectingAppCompatActivity;

//import com.guardswift.modules.LocationsModule;

public abstract class AbstractDialogActivity extends InjectingAppCompatActivity {

	protected static final String TAG = AbstractDialogActivity.class
			.getSimpleName();


    private MaterialDialog dialog;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);

		getWindow().addFlags(
//				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
//						|
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
						| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
//						| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

	}

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // do nothing
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog != null) {
            dialog.cancel();
            dialog = null;
        }
    }

    protected void showDialog(MaterialDialog dialog) {
        this.dialog = dialog;
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

}
