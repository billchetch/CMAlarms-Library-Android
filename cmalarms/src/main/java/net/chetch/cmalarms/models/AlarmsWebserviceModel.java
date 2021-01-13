package net.chetch.cmalarms.models;

import net.chetch.cmalarms.data.AlarmsLog;
import net.chetch.cmalarms.data.AlarmsRepository;
import net.chetch.webservices.WebserviceViewModel;

import java.util.Calendar;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class AlarmsWebserviceModel extends WebserviceViewModel {

    AlarmsRepository alarmsRepository = AlarmsRepository.getInstance();

    public AlarmsWebserviceModel(){
        addRepo(alarmsRepository);
    }

    public LiveData<AlarmsLog> getLog(Calendar fromDate, Calendar toDate){
        MutableLiveData<AlarmsLog> liveDataAlarmsLog = new MutableLiveData<>();
        alarmsRepository.getLog(fromDate, toDate).add(liveDataAlarmsLog);
        return liveDataAlarmsLog;
    }
}
