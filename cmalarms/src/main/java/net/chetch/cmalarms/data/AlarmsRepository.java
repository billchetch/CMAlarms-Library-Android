package net.chetch.cmalarms.data;

import net.chetch.utilities.Utils;
import net.chetch.webservices.DataCache;
import net.chetch.webservices.DataStore;
import net.chetch.webservices.WebserviceRepository;

import java.util.Calendar;
import java.util.TimeZone;

public class AlarmsRepository extends WebserviceRepository<IAlarmsWebservice> {
    static final String DATE_FORMAT_FOR_REQUESTS = "yyyy-MM-dd HH:mm:ss";

    static private AlarmsRepository instance = null;
    static public AlarmsRepository getInstance(){
        if(instance == null)instance = new AlarmsRepository();
        return instance;
    }

    public AlarmsRepository(){
        this(DataCache.MEDIUM_CACHE);
    }
    public AlarmsRepository(int defaultCacheTime){
        super(IAlarmsWebservice.class, defaultCacheTime);
    }

    private String date4request(Calendar cal){
        return Utils.formatDate(cal, DATE_FORMAT_FOR_REQUESTS, TimeZone.getTimeZone("UTC"));
    }

    public DataStore<AlarmsLog> getLog(Calendar fromDate, Calendar toDate, String alarmID){
        final DataStore<AlarmsLog> entries = new DataStore<>();

        service.getLog(fromDate == null ? null : date4request(fromDate), toDate == null ? null : date4request(toDate), alarmID).enqueue(createCallback(entries));

        return entries;
    }

    @Override
    public void setAPIBaseURL(String apiBaseURL) throws Exception {
        super.setAPIBaseURL(apiBaseURL);
    }
}
