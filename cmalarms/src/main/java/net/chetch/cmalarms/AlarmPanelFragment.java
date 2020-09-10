package net.chetch.cmalarms;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.constraintlayout.helper.widget.Flow;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import net.chetch.cmalarms.data.Alarm;
import net.chetch.cmalarms.models.AlarmsMessagingModel;
import net.chetch.utilities.Animation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlarmPanelFragment extends Fragment {
    public boolean horizontal = true;

    View contentView;
    ImageButton buzzerButton;
    ValueAnimator animator;
    Map<String, AlarmsMessageSchema.AlarmState> alarmStates = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(horizontal) {
            contentView = inflater.inflate(R.layout.alarm_panel_horizontal, container, false);
        } else {
            contentView = inflater.inflate(R.layout.alarm_panel_vertical, container, false);
        }

        buzzerButton = contentView.findViewById(R.id.buzzerButton);
        buzzerButton.setOnClickListener((view)->{
            //this will silence/unsilence
            try {
                IAlarmPanelActivity activity = (IAlarmPanelActivity) getActivity();
                AlarmsMessagingModel model = activity.getAlarmsMessagingModel();
                if(model.isPilotOn()) {
                    if(model.isBuzzerSilenced()){
                        model.unsilenceBuzzer();
                    } else {
                        model.silenceBuzzer(10); //TODO: this should be a setting
                    }
                }
            } catch(Exception e){
                Log.e("AlarmPanelFragment", e.getMessage());
            }
        });


        Log.i("AlarmPanelFragment", "Created view");
        return contentView;
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);

        try {
            TypedArray a = getActivity().obtainStyledAttributes(attrs, R.styleable.AlarmPanelFragment);
            horizontal = a.getBoolean(R.styleable.AlarmPanelFragment_horizontal, true);
            a.recycle();
        } catch (Exception e){
            Log.e("AlarmPanelFragment", e.getMessage());
        }

    }

    public void populateAlarms(List<Alarm> alarms){
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragTransaction = fragmentManager.beginTransaction();

        ConstraintLayout layout = (ConstraintLayout)contentView;
        int[] vids = new int[alarms.size()];
        int i = 0;
        int vid = 0;
        for(Alarm a : alarms){
            AlarmFragment af = (AlarmFragment)fragmentManager.findFragmentByTag(a.getDeviceID());
            if(af != null)fragTransaction.remove(af);
            af = new AlarmFragment();
            af.alarm = a;
            af.horizontal = horizontal;

            vid = View.generateViewId();
            ViewGroup vg = new FrameLayout(getContext());
            vg.setId(vid);

            fragTransaction.replace(vg.getId(), af, a.getDeviceID());

            layout.addView(vg);
            vids[i++] = vid;
        }

        fragTransaction.commit();
        Flow flow = contentView.findViewById(R.id.alarmsFlow);
        if(flow != null)flow.setReferencedIds(vids);
    }


    public void updateAlarmStates(Map<String, AlarmsMessageSchema.AlarmState> alarmStates){
        for(Map.Entry<String, AlarmsMessageSchema.AlarmState> entry : alarmStates.entrySet()){
            updateAlarmState(entry.getKey(), entry.getValue());

            //keep a record
            this.alarmStates.put(entry.getKey(), entry.getValue());
        }
    }

    private boolean hasAlarmWithState(AlarmsMessageSchema.AlarmState alarmState){
        for(AlarmsMessageSchema.AlarmState astate : alarmStates.values()){
            if(astate == alarmState)return true;
        }
        return false;
    }

    public void updateAlarmState(String deviceID, AlarmsMessageSchema.AlarmState alarmState){
        FragmentManager fragmentManager = getFragmentManager();
        AlarmFragment af = (AlarmFragment)fragmentManager.findFragmentByTag(deviceID);
        if(af != null) {
            af.updateAlarmState(alarmState);
        } else {
            Log.e("AlarmPanelFragment", "Cannot find fragment for alarm " + deviceID);
        }

        //keep a record
        alarmStates.put(deviceID, alarmState);
    }

    public void updatePilotOn(boolean isAlarmOn){
        //set the buzzer bg
        ImageView bg = contentView.findViewById(R.id.buzzerButtonBg);
        GradientDrawable d = (GradientDrawable)bg.getDrawable();
        int onColour = ContextCompat.getColor(getContext(), R.color.errorRed);
        int offColour = ContextCompat.getColor(getContext(), R.color.mediumnDarkGrey);
        if(isAlarmOn){
            if(animator == null) {
                animator = Animation.flash(d, offColour, onColour, 1000, ValueAnimator.INFINITE);
            }
        } else {
            if(animator != null)animator.cancel();
            animator = null;
            d.setColor(offColour);
        }
    }

    public void updateBuzzerSilenced(boolean silenced){
        if(silenced){
            buzzerButton.setImageResource(R.drawable.ic_soundoff_white_18dp);
        } else {
            buzzerButton.setImageResource(R.drawable.ic_soundon_white_18dp);
        }
    }
}
