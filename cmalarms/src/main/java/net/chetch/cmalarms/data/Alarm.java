package net.chetch.cmalarms.data;

import net.chetch.cmalarms.AlarmsMessageSchema;
import net.chetch.webservices.DataObject;

public class Alarm extends DataObject {

    public transient AlarmsMessageSchema.AlarmState alarmState;

    public String getDeviceID(){
        return getCasted("device_id");
    }

    public String getName(){
        return getCasted("alarm_name");
    }
}
