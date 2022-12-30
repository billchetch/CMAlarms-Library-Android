package net.chetch.cmalarms;

import net.chetch.cmalarms.data.Alarm;
import net.chetch.cmalarms.models.AlarmsMessageSchema;
import net.chetch.cmalarms.models.AlarmsWebserviceModel;

public interface IAlarmPanelListener {

    public void onAlarmStateChange(Alarm alarm, AlarmsMessageSchema.AlarmState newState, AlarmsMessageSchema.AlarmState oldState);
    public void onViewAlarmsLog(Alarm alarm);
    public void onSilenceAlarmBuzzer(int duration);
    public void onDisableAlarm(Alarm alarm);
}
