package br.ufma.gerentepass;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import br.ufma.lsdi.cddl.CDDL;
import br.ufma.lsdi.cddl.Connection;
import br.ufma.lsdi.cddl.ConnectionFactory;
import br.ufma.lsdi.cddl.message.Message;
import br.ufma.lsdi.cddl.pubsub.Publisher;
import br.ufma.lsdi.cddl.pubsub.PublisherFactory;
import br.ufma.lsdi.cddl.pubsub.Subscriber;
import br.ufma.lsdi.cddl.pubsub.SubscriberFactory;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    // Para debug
    public static final String TAG = MapsActivity.class.getSimpleName();
    public GoogleMap mMap;
    public Marker marcador_origem;
    public Marker marcador_destino;

    LatLng localizacaoOrigem = new LatLng(-2.532165,-44.296644);
    LatLng localizacaoDestino = new LatLng(-2.53456,-44.29345);

    InterscityTeste interscityTeste;
    LocalizacaoPass localizacaoPass = new LocalizacaoPass();
    String uuid = "7d604d0a-c54e-470d-8cc3-c2245d524110";

    private Handler handler2 = new Handler();

    public CDDL cddl;
    public String email = "jean.marques@lsdi.ufma.br";
    public List<String> sensorNames = new ArrayList<String>();
    public String currentSensor;
    public Subscriber subscriber;
    public Publisher pub;

    public ListView listView;
    public List<String> listViewMessages;
    public ListViewAdapter listViewAdapter;

    public Handler handler = new Handler();
    String ms;
    EventBus eb;
    String monitorCode;

    Message msgPassageiro = new Message();

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            interscityTeste.get(uuid, localizacaoPass);
            handler2.postDelayed(this,5 * 1000); // loop a cada 5 segundos
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        eb = EventBus.builder().build();
        //eb.register(this);
        if (savedInstanceState == null) {
            configCDDL();
        }

        setPermissions();
        configListView();

        interscityTeste = new InterscityTeste(getApplicationContext());

        //localizacaoPass = interscityTeste.get(uuid, localizacaoPass);

        //Log.i(TAG,"###### VALOR " + localizacaoPass.getLatitude());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // aqui o marcador atualiza cada vez que o valor da variavel coordenadasAtual dentro da view model atualiza
        interscityTeste.getCoordenadaAtual().observe(mapFragment.getViewLifecycleOwner(), new Observer<LocalizacaoPass>() {
            @Override
            public void onChanged(LocalizacaoPass coordenadas) {
                marcador_origem.setPosition(new LatLng(coordenadas.getLatitude(), coordenadas.getLongitude()));
            }
        });

    }

    @Override
    public void onResume() {
        startLoop();
        super.onResume();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        atualizaDestino();

        localizacaoOrigem = new LatLng(localizacaoPass.getLatitude(), localizacaoPass.getLongitude());
        // Definindo posição inicial do mapa
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localizacaoOrigem,15));
        marcador_origem = mMap.addMarker(new MarkerOptions().position(localizacaoOrigem).title("OrigemPassageiro"));

        //marcador_origem = mMap.addMarker(new MarkerOptions().title("OrigemPassageiro"));
    }

    private void startLoop() {
        handler2.postDelayed(runnable, 5*1000);
    }

    private void endLoop(){
        handler2.removeCallbacks(runnable); //stop handler when activity not visible
    }

    public void atualizaDestino() {
        localizacaoDestino = new LatLng(localizacaoPass.getLatitudeDestino(), localizacaoPass.getLongitudeDestino());
        marcador_destino = mMap.addMarker(new MarkerOptions().position(localizacaoDestino).title("DestinoPassageiro"));
    }

    public void configCDDL() {
        //Host leva o nome do microBroker
        String host = CDDL.startMicroBroker();

        //Abre conecção
        Connection connection = ConnectionFactory.createConnection();
        connection.setHost(host);
        connection.setClientId(email);
        connection.connect();

        cddl = CDDL.getInstance();
        cddl.setConnection(connection);
        cddl.setContext(this);

        cddl.startService();
        cddl.startCommunicationTechnology(CDDL.INTERNAL_TECHNOLOGY_ID);

        pub = PublisherFactory.createPublisher();
        pub.addConnection(cddl.getConnection());

        subscriber = SubscriberFactory.createSubscriber();
        subscriber.addConnection(cddl.getConnection());

        subscriber.subscribeServiceByName("Location");
        subscriber.setSubscriberListener(this::onMessage);
        //subscriber.setSubscriberListener(this::onMessageTopic);

        monitorCode = subscriber.getMonitor().addRule("select * from Message", message -> {
            new Thread() {
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //atualizaTotalAlerta(t1,1);
                            if(true) {
                                Log.d(TAG, "##########  Satisfeita a regra subscriber " + monitorCode);
                                //geraAlerta(1, msgAlerta(message));
                            }
                        }
                    });
                }
            }.start();});
        Log.i(TAG,"####### Disparo monitorCode: " + monitorCode);
    }

    public void onMessage(Message message) {
        handler.post(() -> {
            Object[] valor = message.getServiceValue();
            Log.i(TAG,"#### LLLLLLL " + valor.toString());
            listViewMessages.add(StringUtils.join(valor));
            listViewAdapter.notifyDataSetChanged();
        });
    }

    public void configListView() {
        listView = findViewById(R.id.listview);
        listViewMessages = new ArrayList<>();
        listViewAdapter = new ListViewAdapter(this, listViewMessages);
        listView.setAdapter(listViewAdapter);
    }

    @Override
    public void onDestroy() {
        endLoop();
        eb.unregister(this);
        super.onDestroy();
    }

    public void setPermissions() {
        Log.i(TAG,"###### Solicitando permissão");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

    }
}