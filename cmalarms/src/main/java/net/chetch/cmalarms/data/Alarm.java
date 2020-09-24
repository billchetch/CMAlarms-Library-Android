package net.chetch.cmalarms.data;

import net.chetch.cmalarms.models.AlarmsMessageSchema;
import net.chetch.webservices.DataObject;

public class Alarm extends DataObject {

    public transient AlarmsMessageSchema.AlarmState alarmState;
    public transient String alarmMessage;

    public String getDeviceID(){
        return getCasted("device_id");
    }

    public String getName(){
        return getCasted("alarm_name");
    }
}
