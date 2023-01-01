package net.chetch.alarmpanel;

import android.content.SharedPreferences;
import android.util.Log;

import net.chetch.appframework.SettingsActivityBase;
import net.chetch.webservices.network.NetworkRepository;

public class SettingsActivity extends SettingsActivityBase {
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals("api_base_url")){
            restartMainActivityOnFinish = true;
            try{
                String apiBaseURL = sharedPreferences.getString(key, null);
                NetworkRepository.getInstance().setAPIBaseURL(apiBaseURL);
            } catch (Exception e){
                Log.e("Settings", e.getMessage());
            }
        }
    }
}
