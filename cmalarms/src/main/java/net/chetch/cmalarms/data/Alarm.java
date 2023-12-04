package net.chetch.cmalarms.data;

import net.chetch.cmalarms.models.AlarmsMessageSchema;
import net.chetch.utilities.Utils;
import net.chetch.webservices.DataObject;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class Alarm extends DataObject {

    public transient AlarmsMessageSchema.AlarmState oldAlarmState = AlarmsMessageSchema.AlarmState.OFF;

    public transient boolean testing = false;

    public String getAlarmID(){
        return getCasted("alarm_id");
    }

    public String getName(){
        return getCasted("alarm_name");
    }


    public boolean hasAlarmState(){
        String s =  getCasted("alarm_state");
        return s != null;
    }

    public AlarmsMessageSchema.AlarmState getAlarmState(){
        String s =  getCasted("alarm_state");
        return s == null ? AlarmsMessageSchema.AlarmState.OFF : AlarmsMessageSchema.AlarmState.valueOf(s);
    }

    public void setAlarmState(AlarmsMessageSchema.AlarmState alarmState, boolean testing){
        oldAlarmState = getAlarmState();
        this.testing = testing;
        boolean stateChange = oldAlarmState != alarmState;

        setValue("alarm_state", alarmState.toString());
        if(!testing && stateChange) {
            switch (alarmState) {
                case DISABLED:
                    setValue("last_disabled", Calendar.getInstance());
                    break;
                case OFF:
                    Calendar lastDisabled = getLastDisabled();
                    Calendar lastRaised = getLastRaised();
                    //last lowered depends on there being a last raised and either not ever disabled OR raised AFTER last disabled
                    if (lastRaised != null && (lastDisabled == null || lastRaised.getTimeInMillis() > lastDisabled.getTimeInMillis())) {
                        setValue("last_lowered", Calendar.getInstance());
                    }
                    break;
                default:
                    setValue("last_raised", Calendar.getInstance());
                    break;
            }
        }
    }

    public String getAlarmMessage(){
        return getCasted("alarm_message");
    }

    public void setAlarmMessage(String s){
        setValue("alarm_message", s);
    }

    public int getAlarmCode(){ return getCasted("alarm_code"); }

    public void setAlarmCode(int c){
        setValue("alarm_code", c);
    }

    public Calendar getLastRaised(){
        Calendar c = getCasted("last_raised");
        return c;
    }

    public Calendar getLastLowered(){
        Calendar c = getCasted("last_lowered");
        return c;
    }

    public Calendar getLastDisabled(){
        Calendar c = getCasted("last_disabled");
        return c;
    }

    public long getLastRaisedFor(){
        Calendar lr = getLastRaised();
        if(lr == null)return -1;

        Calendar ll = getLastLowered();
        if(ll == null)ll = Calendar.getInstance();

        return Utils.dateDiff(ll, lr, TimeUnit.SECONDS);
    }

    public boolean isRaised(){
        return hasAlarmState() ? AlarmsMessageSchema.isAlarmStateOn(getAlarmState()) : false;
    }

    public boolean isDisabled(){
        return hasAlarmState() && getAlarmState() == AlarmsMessageSchema.AlarmState.DISABLED;
    }

}
