package net.chetch.cmalarms.data;

import net.chetch.webservices.DataObjectCollection;

public class AlarmsLog extends DataObjectCollection<AlarmsLogEntry> {

    public AlarmsLog() {
        super(AlarmsLog.class);
    }
}
