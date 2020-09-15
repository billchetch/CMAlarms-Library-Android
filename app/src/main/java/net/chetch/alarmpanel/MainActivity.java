package net.chetch.alarmpanel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;
import android.util.Log;

import net.chetch.cmalarms.AlarmPanelFragment;
import net.chetch.cmalarms.models.AlarmsMessagingModel;
import net.chetch.webservices.WebserviceViewModel;
import net.chetch.webservices.network.NetworkRepository;

public class MainActivity extends AppCompatActivity {

    static boolean loaded = false;

    AlarmsMessagingModel model;

    Observer dataLoadProgress  = obj -> {
        WebserviceViewModel.LoadProgress progress = (WebserviceViewModel.LoadProgress) obj;
        try {
            String state = progress.startedLoading ? "Loading" : "Loaded";
            String progressInfo = state + (progress.info == null ? "" : " " + progress.info.toLowerCase());
            Log.i("Main", "in load data progress");
        } catch (Exception e){
            Log.e("Main", "load prigress: " + e.getMessage());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            //String apiBaseURL = "http://192.168.43.123:8001/api/";
            //String apiBaseURL = "http://192.168.1.100:8001/api/";
            String apiBaseURL = "http://192.168.0.106:8001/api/";
            //String apiBaseURL = "http://192.168.0.52:8001/api/";
            NetworkRepository.getInstance().setAPIBaseURL(apiBaseURL);
        } catch (Exception e) {
            Log.e("MVM", e.getMessage());
            return;
        }

        //now load up
        Log.i("Main", "Calling load data");
        model = ViewModelProviders.of(this).get(AlarmsMessagingModel.class);
        model.getError().observe(this, throwable -> {
            Log.e("Main", throwable.getMessage());
        });
        model.loadData(dataLoadProgress);

    }

}
