package net.chetch.cmalarms.models;

import net.chetch.cmalarms.data.Alarm;
import net.chetch.cmalarms.data.AlarmsLog;
import net.chetch.cmalarms.data.AlarmsRepository;
import net.chetch.webservices.WebserviceViewModel;

import java.util.Calendar;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class AlarmsWebserviceModel extends WebserviceViewModel {

    AlarmsRepository alarmsRepository = AlarmsRepository.getInstance();

    private MutableLiveData<AlarmsLog> liveDataAlarmsLog = new MutableLiveData<>();

    public AlarmsWebserviceModel(){
        addRepo(alarmsRepository);
    }

    public LiveData<AlarmsLog> getLog(){
        return liveDataAlarmsLog;
    }

    public LiveData<AlarmsLog> getLog(Calendar fromDate, Calendar toDate, Alarm alarm){
        alarmsRepository.getLog(fromDate, toDate, alarm == null ? null : alarm.getAlarmID()).add(liveDataAlarmsLog);
        return liveDataAlarmsLog;
    }

    public LiveData<AlarmsLog> getLog(Calendar fromDate, Calendar toDate){
        return getLog(fromDate, toDate, null);
    }

    public LiveData<AlarmsLog> getLog(Alarm alarm){
        return getLog(null, null, alarm);
    }
}
