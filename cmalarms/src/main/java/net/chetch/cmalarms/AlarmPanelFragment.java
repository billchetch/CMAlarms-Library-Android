package net.chetch.cmalarms;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.helper.widget.Flow;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import net.chetch.cmalarms.data.Alarm;
import net.chetch.cmalarms.models.AlarmsMessageSchema;
import net.chetch.cmalarms.models.AlarmsMessagingModel;
import net.chetch.cmalarms.models.AlarmsWebserviceModel;
import net.chetch.messaging.MessagingViewModel;
import net.chetch.utilities.Animation;
import net.chetch.utilities.SLog;
import net.chetch.utilities.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlarmPanelFragment extends Fragment implements MenuItem.OnMenuItemClickListener{
    static final int MENU_ITEM_SILENCE_1_MIN = 1;
    static final int MENU_ITEM_SILENCE_10_MIN = 10;
    static final int MENU_ITEM_SILENCE_30_MIN = 30;
    static final int MENU_ITEM_VIEW_LOG = 100;
    static final int MENU_ITEM_TEST_BUZZER = 200;
    static final int MENU_ITEM_TEST_PILOT = 201;
    static final int MENU_ITEM_UNSILENCE = 300;

    public boolean horizontal = true;
    public IAlarmPanelListener listener;

    View contentView;
    ImageView buzzerButton;
    ContextMenu contextMenu; //we keep a reference so as to close if the buzzer goes off
    ValueAnimator animator;
    Map<String, Alarm> alarmsMap = new HashMap<>();
    AlarmsMessagingModel model;
    AlarmsWebserviceModel wsModel;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(horizontal) {
            contentView = inflater.inflate(R.layout.alarm_panel_horizontal_scroll, container, false);
        } else {
            contentView = inflater.inflate(R.layout.alarm_panel_vertical_scroll, container, false);
        }

        buzzerButton = contentView.findViewById(R.id.alarmsBuzzerButton);
        registerForContextMenu(buzzerButton);
        registerForContextMenu((View)buzzerButton.getParent());

        View mainLayout = contentView.findViewById(R.id.alarmsMainLayout);
        //progressCtn.setVisibility(View.INVISIBLE);
        mainLayout.setVisibility(View.INVISIBLE);

        SLog.i("AlarmPanelFragment", "Created " + (horizontal ? "horizontal" : "vertical") + " view");
        return contentView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(model == null) {
            model = new ViewModelProvider(getActivity()).get(AlarmsMessagingModel.class);

            model.observeMessagingServices(getViewLifecycleOwner(), ms -> {
                //we assume this is always the alarms messaging service
                View mainLayout = contentView.findViewById(R.id.alarmsMainLayout);
                View progressCtn = contentView.findViewById(R.id.alarmsProgressCtn);
                switch(ms.state){
                    case RESPONDING:
                        if(progressCtn != null)progressCtn.setVisibility(View.INVISIBLE);
                        mainLayout.setVisibility(View.VISIBLE);
                        Log.i("AlarmPanelFragment", "Messaging service is RESPONDING");
                        break;

                    case NOT_CONNECTED:
                    case NOT_RESPONDING:
                    case NOT_FOUND:
                        mainLayout.setVisibility(View.INVISIBLE);
                        if(progressCtn != null)progressCtn.setVisibility(View.VISIBLE);

                        TextView tv = contentView.findViewById(R.id.alarmsServiceState);
                        if(tv != null) {
                            tv.setVisibility(View.VISIBLE);
                            String msg = "";
                            if (ms.state == MessagingViewModel.MessagingServiceState.NOT_FOUND) {
                                msg = "Cannot configure service as configuration details not found (possible webserver issue)";
                            } else if (ms.state == MessagingViewModel.MessagingServiceState.NOT_RESPONDING) {
                                msg = "Alarms service is not responding";
                            } else {
                                msg = "Alarms service is not connected.  Check service has started.";
                            }
                            tv.setText(msg);
                        }
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
                Log.i("AlarmPanelFragment", "Alarm alert " + alarm.getAlarmID() + " state " + alarm.getAlarmState());
                updateAlarmState(alarm);
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

            model.getIsTesting().observe(getViewLifecycleOwner(), testing -> {
                Log.i("AlarmPanelFragment", testing ? "Testing" : "Not Testing");

            });
        }

        if(wsModel == null){
            wsModel = new ViewModelProvider(getActivity()).get(AlarmsWebserviceModel.class);
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
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if(v == buzzerButton || v == buzzerButton.getParent()){
            menu.clear();
            if(model.isBuzzerOn()) {
                menu.add(0, MENU_ITEM_SILENCE_1_MIN, 0, "Silence for 1 minute").setOnMenuItemClickListener(this);
                menu.add(0, MENU_ITEM_SILENCE_10_MIN, 0, "Silence for 10 minutes").setOnMenuItemClickListener(this);
                menu.add(0, MENU_ITEM_SILENCE_30_MIN, 0, "Silence for 30 minutes alarm").setOnMenuItemClickListener(this);
            } else if(model.isPilotOn()){
                if(model.isBuzzerSilenced()){
                    menu.add(0, MENU_ITEM_UNSILENCE, 0, "Unsilence buzzer").setOnMenuItemClickListener(this);
                }
            } else {
                //buzzer and pilot is off
                menu.add(0, MENU_ITEM_TEST_BUZZER, 0, "Test Buzzer").setOnMenuItemClickListener(this);
                menu.add(0, MENU_ITEM_TEST_PILOT, 0, "Test Pilot").setOnMenuItemClickListener(this);
            }


            menu.add(0, MENU_ITEM_VIEW_LOG, 0, "View Log").setOnMenuItemClickListener(this);
            contextMenu = menu;
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        model.resume(); //incase this is restarting
    }

    @Override
    public void onStop() {
        super.onStop();

        model.pause();
    }

    public void populateAlarms(List<Alarm> alarms) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragTransaction = fragmentManager.beginTransaction();
        List<AlarmFragment> afl = new ArrayList<>();

        alarmsMap.clear();
        for(Alarm a : alarms) {
            AlarmFragment af = (AlarmFragment) fragmentManager.findFragmentByTag(a.getAlarmID());
            if (af != null)fragTransaction.remove(af);
            af = new AlarmFragment();
            af.listener = listener;
            af.alarm = a;
            af.horizontal = horizontal; //TODO: make this a setting if needed;
            fragTransaction.add(R.id.alarmsCtn, af, a.getAlarmID());

            //we keep a record
            alarmsMap.put(a.getAlarmID(), a);
            afl.add(af);
        }
        fragTransaction.commit();

    }

    public void updateAlarmStates(Map<String, AlarmsMessageSchema.AlarmState> alarmStates){
        for(Map.Entry<String, AlarmsMessageSchema.AlarmState> entry : alarmStates.entrySet()){
            Alarm alarm = alarmsMap.get(entry.getKey());
            updateAlarmState(alarm);
        }
    }

    public void updateAlarmState(Alarm alarm){
        FragmentManager fragmentManager = getFragmentManager();
        AlarmFragment af = (AlarmFragment)fragmentManager.findFragmentByTag(alarm.getAlarmID());
        AlarmsMessageSchema.AlarmState newState = alarm.getAlarmState();
        if(af != null) {
            AlarmsMessageSchema.AlarmState oldState = alarm.oldAlarmState;
            af.updateAlarmState();
            if(listener != null && oldState != newState){
                listener.onAlarmStateChange(alarm, newState, oldState);
            }
        } else {
            Log.e("AlarmPanelFragment", "Cannot find fragment for alarm " + alarm.getAlarmID());
        }

        if(alarm.isRaised()){
            if(horizontal) {
                HorizontalScrollView sv = contentView.findViewById(R.id.alarmsCtnScrollView);
                sv.smoothScrollTo(af.getView().getLeft(), 0);
            } else {
                ScrollView sv = contentView.findViewById(R.id.alarmsCtnScrollView);
                sv.smoothScrollTo(0, af.getView().getTop());
            }
        }

        //update panel info
        updateAlarmPanelInfo();
    }

    private void updateAlarmPanelInfo(){
        if(alarmsMap == null || alarmsMap.size() == 0)return;

        int disabled = 0;
        int on  = 0;
        int off = 0;
        String alarmMessage = null;
        Alarm alarmLastRaised = null;

        for(Alarm a : alarmsMap.values()){
            try {
                switch (a.getAlarmState()) {
                    case DISABLED:
                        disabled++;
                        break;
                    case OFF:
                        off++;
                        break;
                    default:
                        if(alarmMessage == null) {
                            alarmMessage = a.getName() + ": " + a.getAlarmMessage();
                        }
                        on++;
                        break;
                }
            } catch (Exception e){
                Log.e("AlarmPanelFragment", e.getMessage());
            }

            if(a.getLastRaised() != null && (alarmLastRaised == null || a.getLastRaised().getTimeInMillis() > alarmLastRaised.getLastRaised().getTimeInMillis())){
                alarmLastRaised = a;
            }
        }

        TextView tv = contentView.findViewById(R.id.alarmInfo);
        if(on ==  0){
            tv.setTextColor(ContextCompat.getColor(getContext(), R.color.lightGrey));
            tv.setTypeface(tv.getTypeface(), Typeface.ITALIC);
            String s = disabled == 0 ? "All alarms operational" : disabled + " alarms disabled, " + off + " alarms operational";
            tv.setText(s);

            tv = contentView.findViewById(R.id.alarmPanelInfoSub);
            if(tv != null) {
                tv.setTextColor(ContextCompat.getColor(getContext(), R.color.lightGrey));
                tv.setTypeface(tv.getTypeface(), Typeface.ITALIC);
                if (alarmLastRaised != null) {
                    s = "Last alarm raised was " + alarmLastRaised.getName();
                    s+= " on " + Utils.formatDate(alarmLastRaised.getLastRaised(), AlarmFragment.ALARM_STATE_CHANGE_DATE_FORMAT) + " for " + Utils.formatDuration(alarmLastRaised.getLastRaisedFor() * 1000, Utils.DurationFormat.DAYS_HOURS_MINS_SECS);
                } else {
                    s = "No alarm has ever been raised";
                }
                tv.setText(s);
            }
        } else {
            tv.setTextColor(ContextCompat.getColor(getContext(), R.color.errorRed));
            tv.setTypeface(tv.getTypeface(), Typeface.BOLD_ITALIC);
            tv.setText(alarmMessage);
            tv = contentView.findViewById(R.id.alarmPanelInfoSub);
            if(tv != null) {
                tv.setTextColor(ContextCompat.getColor(getContext(), R.color.errorRed));
                tv.setTypeface(tv.getTypeface(), Typeface.BOLD_ITALIC);
                String s = on + (on == 1 ?  " alarm " : " alarms ") + "raised";
                tv.setText(s);
            }
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
            if(contextMenu != null){
                getActivity().closeContextMenu();
            }
        }
    }

    public void updateBuzzerSilenced(boolean silenced){
        if(silenced){
            buzzerButton.setImageResource(R.drawable.ic_soundoff_white_18dp);
        } else {
            buzzerButton.setImageResource(R.drawable.ic_soundon_white_18dp);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch(menuItem.getItemId()){
            case MENU_ITEM_SILENCE_1_MIN:
            case MENU_ITEM_SILENCE_10_MIN:
            case MENU_ITEM_SILENCE_30_MIN:
                if(model.isPilotOn() && !model.isBuzzerSilenced()) {
                    int duration = 60 * menuItem.getItemId();
                    model.silenceBuzzer(duration);
                }
                break;

            case MENU_ITEM_UNSILENCE:
                model.unsilenceBuzzer();
                break;

            case MENU_ITEM_TEST_BUZZER:
                model.testBuzzer();
                break;

            case MENU_ITEM_TEST_PILOT:
                model.testPilot();
                break;

            case MENU_ITEM_VIEW_LOG:
                if(listener != null)listener.onViewAlarmsLog(null);
                break;
        }
        return false;
    }
}
