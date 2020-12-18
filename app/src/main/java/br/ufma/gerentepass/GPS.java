package br.ufma.gerentepass;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import br.pucrio.inf.lac.mhub.models.locals.StartLocationSensorMessage;

public class GPS extends Service {
    public static final String TAG = GPS.class.getSimpleName();

    public GPS(){
        Log.i(TAG,"###### Start EventBus");
        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
    }

    public static class EventHandler {
        final StartLocationSensorMessage message;

        public EventHandler(StartLocationSensorMessage startLocationSensorMessage) {
            this.message = startLocationSensorMessage;
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.ASYNC)
    public void onEvent(StartLocationSensorMessage event) {

        Log.i(TAG,"####################### Qual o dado: " + event.getInterval());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
