package br.ufma.gerentepass;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import br.pucrio.inf.lac.mhub.models.base.LocalMessage;
import br.pucrio.inf.lac.mhub.models.locals.StartLocationSensorMessage;
import br.ufma.lsdi.cddl.CDDL;
import br.ufma.lsdi.cddl.Connection;
import br.ufma.lsdi.cddl.ConnectionFactory;
import br.ufma.lsdi.cddl.listeners.ISubscriberListener;
import br.ufma.lsdi.cddl.message.Message;
import br.ufma.lsdi.cddl.pubsub.Publisher;
import br.ufma.lsdi.cddl.pubsub.PublisherFactory;
import br.ufma.lsdi.cddl.pubsub.Subscriber;
import br.ufma.lsdi.cddl.pubsub.SubscriberFactory;

public class MainActivity extends AppCompatActivity {
    // Para debug
    public static final String TAG = MainActivity.class.getSimpleName();

    private Spinner spinner;
    private List<String> spinnerSensors;
    private ArrayAdapter<String> spinnerAdapter;

    private ListView listView;
    private List<String> listViewMessages;
    private ListViewAdapter listViewAdapter;
    private EditText filterEditText;

    private CDDL cddl;
    private String email = "jean.marques@lsdi.ufma.br";
    private List<String> sensorNames = new ArrayList<String>();
    private String currentSensor;
    private Subscriber subscriber;
    private Publisher pub;

    private boolean filtering;

    public Handler handler = new Handler();

    private String caminho = "/storage/emulated/0/Download/pacientes/055/05500001.csv";
    Button startButton;
    Button stopButton;
    Message msg = new Message();
    String ms;
    EventBus eb;

    String monitorCode;

    LocalMessage localMessage;

    MonitorPrincipal monit = new MonitorPrincipal();

    InterscityTeste interscityTeste;

    Context context;

    Gson gson = new Gson();

    String url = "http://cidadesinteligentes.lsdi.ufma.br/adaptor/";
    String uuid = "7d604d0a-c54e-470d-8cc3-c2245d524110";

    String dadosPassageiro = "[-2.5520319,-44.2537906,8.800000190734863,6.908985621920687E-8]";

    LocalizacaoPass localizacaoPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        eb = EventBus.builder().build();
        eb.register(this);
        if (savedInstanceState == null) {
            configCDDL();
        }

        interscityTeste = new InterscityTeste(getApplicationContext());

        localizacaoPass = new LocalizacaoPass();

//        context = cddl.getContext();
//        interscity = new Interscity(context);
//        conectInterscity();
//        interscity.getDados();

        setPermissions();

        //configSpinner();
        //configListView();
        configStartButton();
        configStopButton();
        configClearButton();
        //configFilterButton();

        //cddl.startSensor("Location");
        //subscribeAccelerometer();
        //publishMessage();

        // Teste de EventBus
//        GPS gps;
//        final EventBus eventBus;
//        gps = new GPS();
//        eventBus = new EventBus();
//        eventBus.getDefault().post(new StartLocationSensorMessage(1000));
//
//        eventBus.register(gps);
    }


    private void subscribeAccelerometer() {

        Subscriber sub = SubscriberFactory.createSubscriber();
        sub.addConnection(cddl.getConnection());
        sub.subscribeServiceByName("Location");
        sub.subscribeObjectConnectedTopic();
        sub.subscribeObjectDiscoveredTopic();

        //sub.setFilter("select serviceValue[0] as Latitude, serviceValue[1] as Longitude, serviceValue[2] as Altitude, serviceValue[3] as Speed from Message");

        sub.setFilter("select * from Message");

        sub.setSubscriberListener(new ISubscriberListener() {
            @Override
            public void onMessageArrived(Message message) {
                //Log.d("_MAIN", ">>>>>>>>>>>>>>>>>>>>>>>>>>>>" + message);
                Log.i(TAG,"############### Mensagem recebida por subscriber: " + message.toString());
            }
        });

    }


    private void configCDDL() {
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
                                Log.d(TAG, "##########  Satisfeita a regra subscriber " + monitorCode.toString());
                                //geraAlerta(1, msgAlerta(message));
                            }
                        }
                    });
                }
            }.start();});
        Log.i(TAG,"####### Disparo monitorCode: " + monitorCode);
    }

    private void onMessage(Message message) {
        handler.post(() -> {
            Object[] valor = message.getServiceValue();
            //listViewMessages.add(StringUtils.join(valor[0], ", ", valor[1], ", ", valor[2], ", ", valor[3]));
            //listViewAdapter.notifyDataSetChanged();
            //Log.i(TAG,"###### O que vai aparecer no listview: " + message.toString());

            //dadosPassageiro =  ("100" + message.getSourceLocationLatitude() + message.getSourceLocationLongitude() + valor[2] + valor[3] + "");
            //localizacaoPass.setIdentificador("100");
            //localizacaoPass.setLatitude(message.getSourceLocationLatitude().toString());
            //localizacaoPass.setLongitude(message.getSourceLocationLongitude().toString());
            //localizacaoPass.setAltitude(valor[2].toString());
            //localizacaoPass.setVelocidade(valor[3].toString());

            //interscityTeste.post(localizacaoPass);

            //Log.i(TAG, "####### Dados transformado em JSON: " + dadosPassageiro);
        });
    }

    private void conectInterscity(){
//        Log.i(TAG,"###### Conectando Interscity");
//        final String[] text = new String[100];
//
//        String url = "http://cidadesinteligentes.lsdi.ufma.br/adaptor/subscriptions/1";
//
//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
//                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
//
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        text[0] = response.toString();
//                        Log.i(TAG,"####### Interscity: " + response.toString());
//                    }
//                }, new Response.ErrorListener() {
//
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        // TODO: Handle error
//
//                    }
//                });

        // Access the RequestQueue through your singleton class.
        //Interscity.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    public void onMessageTopic(Message message) {

        handler.post(() -> {
            Object valor = ms;
            //listViewMessages.add(0, StringUtils.join(valor, ", "));
            //listViewAdapter.notifyDataSetChanged();
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void on(MessageEvent event) {
        Object[] valor = event.getMessage().getServiceValue();
        //listViewMessages.add(StringUtils.join((String)valor[0], ", "));
        Log.i(TAG,"################## onEvent: ");
        Log.i(TAG,valor.toString());
    }

    @Override
    protected void onDestroy() {
        eb.unregister(this);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        AppMenu appMenu = AppMenu.getInstance();
        appMenu.setMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AppMenu appMenu = AppMenu.getInstance();
        appMenu.setMenuItem(MainActivity.this, item);
        return super.onOptionsItemSelected(item);
    }

//    private void configSpinner() {
//        List<Sensor> sensors = cddl.getInternalSensorList();
//        System.out.println(sensors);
//
//        //sensorNames = sensors.stream().map(Sensor::getName).collect(Collectors.toList());
//
//        for (Sensor sensor: cddl.getInternalSensorList()) {
//            sensorNames.add(sensor.getName());
//            //List<String> str = Collections.singletonList(sensor.getName());
//            //System.out.println(sensorNames);
//        }
//
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sensorNames);
//        spinner = findViewById(R.id.spinner);
//        spinner.setAdapter(adapter);
//    }

    private void configListView() {
        //listView = findViewById(R.id.listview);
        //listViewMessages = new ArrayList<>();
       // listViewAdapter = new ListViewAdapter(this, listViewMessages);
        //listView.setAdapter(listViewAdapter);
    }

    private void configStartButton() {
        startButton = findViewById(R.id.start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG,"####### Botão ativado");

                //interscityTeste.get(uuid);

                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);

                //telaMapa();

                //interscity.getInstance(MainActivity.this).getRequest();

                //interscityTeste.getInstance(MainActivity.this).post(dadosPassageiro);
            }
        });
//        startButton.setOnClickListener(e -> {
//            // Identifica qual o paciente selecionado para monitorar os sinais vitais
//            Spinner spinner = findViewById(R.id.spinner);
//            String paciente_selecionado = spinner.getSelectedItem().toString();
//            startSelectedSensor();
//            //stopCurrentSensor();
//            //startButton.setEnabled(false);
//            //stopButton.setEnabled(true);
//        });
    }

    private void telaMapa() {
        Log.i(TAG,"##### Redirecionamento para tela do map");
        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
        startActivity(intent);
    }

    private void startSelectedSensor() {
        String selectedSensor = spinner.getSelectedItem().toString();
        cddl.startSensor(selectedSensor); // inicializa o sensor

        subscriber.subscribeServiceByName(selectedSensor);
        currentSensor = selectedSensor;
        cddl.startLocationSensor();
        //readDataByColumn(caminho);
    }

    private void stopCurrentSensor() {
        if (currentSensor != null) {
            cddl.stopSensor(currentSensor);
        }
    }

    private void configStopButton() {
        Button button = findViewById(R.id.stop_button);
        button.setOnClickListener(e -> stopCurrentSensor());
        //startButton.setEnabled(true);
        //stopButton.setEnabled(false);
    }

    private void configClearButton() {
        final Button clearButton = findViewById(R.id.clear_button);
        clearButton.setOnClickListener(e -> {
            //listViewMessages.clear();
            //listViewAdapter.notifyDataSetChanged();
        });
    }

//    private void configFilterButton() {
//
//        filterEditText = findViewById(R.id.filter_edittext);
//
//        Button button = findViewById(R.id.filter_button);
//        button.setOnClickListener(e -> {
//            if (filterEditText.getText().toString().equals(""))
//                return;
//            if (filtering) {
//                subscriber.clearFilter();
//                button.setText(R.string.filter_button_label);
//            }
//            else {
//                subscriber.setFilter(filterEditText.getText().toString());
//                button.setText(R.string.clear_filter_button_label);
//            }
//            filtering = !filtering;
//        });
//    }

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
