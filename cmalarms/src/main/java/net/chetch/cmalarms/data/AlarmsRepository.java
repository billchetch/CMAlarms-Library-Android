package net.chetch.cmalarms.data;

import net.chetch.webservices.DataCache;
import net.chetch.webservices.WebserviceRepository;

public class AlarmsRepository extends WebserviceRepository<IAlarmsWebservice> {

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
}
