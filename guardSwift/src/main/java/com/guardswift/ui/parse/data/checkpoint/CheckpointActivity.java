package com.guardswift.ui.parse.data.checkpoint;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.guardswift.R;
import com.guardswift.persistence.cache.task.CircuitUnitCache;
import com.guardswift.persistence.parse.execution.task.regular.CircuitUnit;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.ui.activity.AbstractToolbarActivity;
import com.guardswift.ui.parse.execution.circuit.CircuitUnitCheckpointsFragment;

import javax.inject.Inject;

public class CheckpointActivity extends AbstractToolbarActivity {

    public static void start(Context context, CircuitUnit circuitUnit) {
        GuardSwiftApplication.getInstance().getCacheFactory().getCircuitUnitCache().setSelected(circuitUnit);
        context.startActivity(new Intent(context, CheckpointActivity.class));
    }

    @Inject
    CircuitUnitCache circuitUnitCache;

    @Override
    protected Fragment getFragment() {
        return CircuitUnitCheckpointsFragment.newInstance(circuitUnitCache.getSelected());
    }

    @Override
    protected String getToolbarTitle() {
        return getString(R.string.checkpoints);
    }

    @Override
    protected String getToolbarSubTitle() {
        return circuitUnitCache.getSelected().getClient().getFullAddress();
    }

}
