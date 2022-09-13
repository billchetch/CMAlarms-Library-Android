package net.chetch.alarmpanel;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import net.chetch.appframework.ChetchApplication;
import net.chetch.webservices.network.NetworkRepository;

public class APApplication extends ChetchApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        try{
            //String apiBaseURL = sharedPref.getString("api_base_url", null);
            String apiBaseURL = "http://192.168.1.101:8001/api";
            NetworkRepository.getInstance().setAPIBaseURL(apiBaseURL);

        } catch (Exception e){
            Log.e("APApplication", e.getMessage());
        }
    }
}