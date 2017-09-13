package com.guardswift.ui.dialog.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.guardswift.R;
import com.guardswift.dagger.InjectingAppCompatActivity;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.ui.GuardSwiftApplication;

public class CheckpointsDialogActivity extends InjectingAppCompatActivity {

    public static void start(Context context, ParseTask circuitUnit) {
        GuardSwiftApplication.getInstance().getCacheFactory().getTasksCache().setSelected(circuitUnit);
        context.startActivity(new Intent(context, CheckpointsDialogActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkpoints);

//        com.guardswift.ui.parse.execution.circuit.CircuitUnitCheckpointsFragment
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_checkpoints, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

//        switch (item.getItemId()) {
//            case android.R.id.home:
//                NavUtils.navigateUpFromSameTask(this);
//                return true;
//        }

        return super.onOptionsItemSelected(item);
    }
}
