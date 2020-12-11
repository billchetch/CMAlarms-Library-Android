package net.chetch.cmalarms;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;

import net.chetch.cmalarms.data.Alarm;
import net.chetch.cmalarms.models.AlarmsMessageSchema;
import net.chetch.cmalarms.models.AlarmsMessagingModel;
import net.chetch.messaging.MessagingViewModel;
import net.chetch.utilities.Animation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlarmPanelFragment extends Fragment {
    public boolean horizontal = true;

    View contentView;
    ImageView buzzerButton;
    ValueAnimator animator;
    Map<String, Alarm> alarmsMap = new HashMap<>();
    AlarmsMessagingModel model;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(horizontal) {
            contentView = inflater.inflate(R.layout.alarm_panel_horizontal_scroll, container, false);
        } else {
            contentView = inflater.inflate(R.layout.alarm_panel_vertical, container, false);
        }

        buzzerButton = contentView.findViewById(R.id.alarmsBuzzerButton);
        buzzerButton.setOnClickListener((view)->{
            //this will silence/unsilence
            try {
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


        View mainLayout = contentView.findViewById(R.id.alarmsMainLayout);
        //progressCtn.setVisibility(View.INVISIBLE);
        mainLayout.setVisibility(View.INVISIBLE);

        Log.i("AlarmPanelFragment", "Created view");
        return contentView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(model == null) {
            model = ViewModelProviders.of(getActivity()).get(AlarmsMessagingModel.class);

            model.getMessagingService().observe(getViewLifecycleOwner(), ms -> {
                //we assume this is always the alarms messaging service
                View mainLayout = contentView.findViewById(R.id.alarmsMainLayout);
                View progressCtn = contentView.findViewById(R.id.alarmsProgressCtn);
                switch(ms.state){
                    case RESPONDING:
                        progressCtn.setVisibility(View.INVISIBLE);
                        mainLayout.setVisibility(View.VISIBLE);
                        Log.i("AlarmPanelFragment", "Messaging service is RESPONDING");
                        break;

                    case NOT_CONNECTED:
                    case NOT_RESPONDING:
                    case NOT_FOUND:
                        mainLayout.setVisibility(View.INVISIBLE);
                        progressCtn.setVisibility(View.VISIBLE);
                        contentView.findViewById(R.id.alarmsProgressBar).setVisibility(View.INVISIBLE);

                        TextView tv = contentView.findViewById(R.id.alarmsServiceState);
                        tv.setVisibility(View.VISIBLE);
                        String msg = "";
                        if(ms.state == MessagingViewModel.MessagingServiceState.NOT_FOUND) {
                            msg = "Cannot configure service as configuration details not found (possible webserver issue)";
                        } else if(ms.state == MessagingViewModel.MessagingServiceState.NOT_RESPONDING){
                            msg = "Alarms service is not responding";
                        } else {
                            msg = "Alarms service is not connected.  Check service has started.";
                        }
                        tv.setText(msg);

                        Log.i("AlarmPanelFragment", "Messaging service is " + ms.state);
                        break;

                    default:
                        Log.i("AlarmPanelFragment", "Messaging service is " + ms.state);
                        break;
                }
            });

            model.getAlarms().observe(getViewLifecycleOwner(), alarms -> {
                Log.i("AlarmPanelFragment", "Alarms list " + alarms.size() + " alarms arrived!");
                populateAlarms(alarms);
            });

            model.getAlarmStates().observe(getViewLifecycleOwner(), alarmStates -> {
                Log.i("AlarmPanelFragment", "Alarm states " + alarmStates.size() + " states arrived!");
                updateAlarmStates(alarmStates);
            });

            model.getAlertedAlarm().observe(getViewLifecycleOwner(), alarm -> {
                Log.i("AlarmPanelFragment", "Alarm alert " + alarm.getAlarmID() + " state " + alarm.alarmState);
                updateAlarmState(alarm.getAlarmID(), alarm.alarmState);
            });

            model.getPilotOn().observe(getViewLifecycleOwner(), on -> {
                Log.i("AlarmPanelFragment", "Pilot light on " + on);
                updatePilotOn(on);
            });

            model.getBuzzerOn().observe(getViewLifecycleOwner(), on -> {
                Log.i("AlarmPanelFragment", "Buzzer on " + on);
                updateBuzzerOn(on);
            });

            model.getBuzzerSilenced().observe(getViewLifecycleOwner(), silenced -> {
                Log.i("AlarmPanelFragment", "Buzzer silenced " + silenced);
                updateBuzzerSilenced(silenced);
            });
        }
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

    @Override
    public void onStart() {
        super.onStart();

        model.resumePingServices(); //incase this is restarting
    }

    @Override
    public void onStop() {
        super.onStop();

        model.pausePingServices();
    }

    public void populateAlarms(List<Alarm> alarms) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragTransaction = fragmentManager.beginTransaction();

        alarmsMap.clear();
        for(Alarm a : alarms) {
            AlarmFragment af = (AlarmFragment) fragmentManager.findFragmentByTag(a.getAlarmID());
            if (af != null)fragTransaction.remove(af);
            af = new AlarmFragment();
            af.alarm = a;
            af.horizontal = horizontal;
            fragTransaction.add(R.id.alarmsCtn, af, a.getAlarmID());

            //we keep a record
            alarmsMap.put(a.getAlarmID(), a);
        }
        fragTransaction.commit();
    }

    public void updateAlarmStates(Map<String, AlarmsMessageSchema.AlarmState> alarmStates){
        for(Map.Entry<String, AlarmsMessageSchema.AlarmState> entry : alarmStates.entrySet()){
            updateAlarmState(entry.getKey(), entry.getValue());
        }
    }

    public void updateAlarmState(String alarmID, AlarmsMessageSchema.AlarmState alarmState){
        FragmentManager fragmentManager = getFragmentManager();
        AlarmFragment af = (AlarmFragment)fragmentManager.findFragmentByTag(alarmID);
        if(af != null) {
            af.updateAlarmState(alarmState);
        } else {
            Log.e("AlarmPanelFragment", "Cannot find fragment for alarm " + alarmID);
        }

        if(AlarmsMessageSchema.isAlarmStateOn(alarmState)){
            HorizontalScrollView sv = contentView.findViewById(R.id.alarmsCtnScrollView);
            sv.smoothScrollTo(af.getView().getLeft(), 0);
        }
        //update panel info
        updateAlarmPanelInfo();
    }

    private void updateAlarmPanelInfo(){
        int disabled = 0;
        int on  = 0;
        int off = 0;
        String alarmMessages = null;
        for(Alarm a : alarmsMap.values()){
            switch(a.alarmState){
                case DISABLED:
                    disabled++; break;
                case OFF:
                    off++; break;
                default:
                    alarmMessages = (alarmMessages == null ? "" : alarmMessages + ", ") + a.getName() + ": " + a.alarmMessage;
                    on++; break;
            }
        }

        TextView tv = contentView.findViewById(R.id.alarmInfo);
        if(on ==  0){
            tv.setTextColor(ContextCompat.getColor(getContext(), R.color.lightGrey));
            tv.setTypeface(tv.getTypeface(), Typeface.ITALIC);
            tv.setText(disabled == 0 ? "All alarms operational" : disabled + " alarms disabled, " + off + " alarms operational");
        } else {
            tv.setTextColor(ContextCompat.getColor(getContext(), R.color.errorRed));
            tv.setTypeface(tv.getTypeface(), Typeface.BOLD_ITALIC);
            tv.setText(alarmMessages);
        }
    }

    public void updatePilotOn(boolean isPilotOn){
        //set the buzzer bg
        ImageView pilot = contentView.findViewById(R.id.alarmsPilot);
        GradientDrawable d = (GradientDrawable)pilot.getDrawable();
        int onColour = ContextCompat.getColor(getContext(), R.color.errorRed);
        int offColour = ContextCompat.getColor(getContext(), R.color.mediumnDarkGrey);
        if(isPilotOn){
            if(animator == null) {
                animator = Animation.flash(d, offColour, onColour, 1000, ValueAnimator.INFINITE);
            }
        } else {
            if(animator != null)animator.cancel();
            animator = null;
            d.setColor(offColour);
        }
    }

    public void updateBuzzerOn(boolean isBuzzerOn){
        if(isBuzzerOn){
            buzzerButton.setVisibility(View.VISIBLE);
        } else {
            buzzerButton.setVisibility(View.INVISIBLE);
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
