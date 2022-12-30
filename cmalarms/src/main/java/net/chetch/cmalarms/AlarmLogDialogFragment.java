package net.chetch.cmalarms;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;

import net.chetch.appframework.GenericDialogFragment;


public class AlarmLogDialogFragment extends GenericDialogFragment{

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        inflateContentView(R.layout.alarms_log_dialog);

        Dialog dialog = createDialog();

        Log.i("ALDF", "Dialog created");
        getChildFragmentManager().getFragments().clear();


        //setFullScreen(0.9);

        return dialog;
    }
}
