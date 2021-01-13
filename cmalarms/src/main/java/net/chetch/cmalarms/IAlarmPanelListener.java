package net.chetch.cmalarms;

import net.chetch.cmalarms.models.AlarmsWebserviceModel;

public interface IAlarmPanelListener {

    public void onViewAlarmsLog(AlarmsWebserviceModel model);
    public void onSilenceAlarmBuzzer(int duration);
}
