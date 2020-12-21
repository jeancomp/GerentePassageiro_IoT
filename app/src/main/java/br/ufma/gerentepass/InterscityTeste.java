package br.ufma.gerentepass;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import br.pucrio.inf.lac.mhub.models.locals.StartLocationSensorMessage;

public class InterscityTeste {
    public static final String TAG = InterscityTeste.class.getSimpleName();

    private MutableLiveData<LocalizacaoPass> coordenadaAtual;
    //LocalizacaoPass loc = new LocalizacaoPass();

    public static InterscityTeste instance;
    public RequestQueue requestQueue;
    public static Context ctx;
    final String[] text = new String[100];
    public final String baseUri = "http://cidadesinteligentes.lsdi.ufma.br/";

    public <T> void addToRequestQueue(Request<T> request){ getRequestQueue().add(request);}

    public static synchronized InterscityTeste getInstance(Context context) {
        if (instance == null) {
            instance = new InterscityTeste(context);
        }
        return instance;
    }

    public InterscityTeste(Context context) {
        ctx = context;
        requestQueue = getRequestQueue();

        Log.i(TAG,"###### Conectando Interscity");

        coordenadaAtual  = new MutableLiveData<LocalizacaoPass>();

        // Access the RequestQueue through your singleton class.
        //Interscity.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    public void getRequest(){
        String url = "http://cidadesinteligentes.lsdi.ufma.br/adaptor/subscriptions/3d0a4d48-2f34-4561-8975-29c8521bb828";

        // uuid: 3d0a4d48-2f34-4561-8975-29c8521bb828

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null,new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        text[0] = response.toString();
                        Log.i(TAG,"####### Interscity: " + response.toString());
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.i(TAG,"####### ERRO - Interscity: " + error.getMessage());
                    }
                });

        addToRequestQueue(jsonObjectRequest);
    }

    public void post(LocalizacaoPass dadosPassageiro){
        String url = "http://cidadesinteligentes.lsdi.ufma.br/adaptor/resources/7d604d0a-c54e-470d-8cc3-c2245d524110/data/localizacao";

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,

                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG,"##### Resultado: " + response);
//                        JSONArray jsonArray = null;
//                        try {
//                            jsonArray = new JSONArray(response);
//                        }
//                        catch (JSONException e){
//                            e.printStackTrace();
//                        }
//
//                        JSONObject r = new JSONObject();
//                        JSONArray j;
//                        try{
//                            for(int i=0; i<jsonArray.length(); i++){
//                                JSONObject passageiro = jsonArray.getJSONObject(i);
//                                String latitude = passageiro.getString("latitude");
//                                String longitude = passageiro.getString("longitude");
//                                String altitude = passageiro.getString("altitude");
//                                String velocidade = passageiro.getString("velocidade");
//
//                                Log.i(TAG,"###### Resposta do Servidor: " + latitude + longitude + altitude + velocidade);
//                            }
//                        }
//                        catch (JSONException e){
//                            e.printStackTrace();
//                        }
//
//                        Log.i(TAG,"##### Resposta inteira do servidor: " + response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.getMessage());
                    }
                }
        ) {

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
            @Override
            public byte[] getBody() throws AuthFailureError {

                try {
                    TimeZone tz = TimeZone.getTimeZone("UTC");
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
                    df.setTimeZone(tz);
                    String nowAsISO = df.format(new Date());

                    JSONObject params = new JSONObject();
                    params.put("identificador",dadosPassageiro.getIdentificador());
                    params.put("latitude",dadosPassageiro.getLatitude());
                    params.put("longitude", dadosPassageiro.getLongitude());
                    params.put("altitude", dadosPassageiro.getAltitude());
                    params.put("velocidade", dadosPassageiro.getVelocidade());
                    params.put("timestamp", nowAsISO);

                    JSONArray array = new JSONArray();
                    array.put(params);

                    JSONObject json = new JSONObject();

                    json.put("data", array);

                    Log.i(TAG,"##### DADOS DO SMARTPHONE DO PASSAGEIRO: " + json);

                    return json.toString().getBytes(StandardCharsets.UTF_8);


                } catch (JSONException e) {
                    e.printStackTrace();
                }


                return super.getBody();
            }

//            @Override
//            public Map<String, String> getParams(){
//                TimeZone tz = TimeZone.getTimeZone("UTC");
//                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
//                df.setTimeZone(tz);
//                String nowAsISO = df.format(new Date());
//
//                Map<String, String>  data = new HashMap<>();
//
//                Map<String, String>  params = new HashMap<>();
//                params.put("identificador",dadosPassageiro.getIdentificador());
//                params.put("localizacao",dadosPassageiro.getLatitude());
//                params.put("longitude", dadosPassageiro.getLongitude());
//                params.put("altitude", dadosPassageiro.getAltitude());
//                params.put("velocidade", dadosPassageiro.getVelocidade());
//                params.put("timestamp", nowAsISO);
//
//                //Log.i(TAG,"##### DATA: " + nowAsISO);
//
//                Log.i(TAG,"##### JSON ANTIGO: " + params.toString());
//
//
//
//                data.put("data", (new String[] {params.toString()}).toString() );
//
//                Log.i(TAG,"##### JSON NOVO: " + data.toString());
//
//                return params;
//            }
        };
        //addToRequestQueue(postRequest);
        requestQueue.add(postRequest);
    }

    public MutableLiveData<LocalizacaoPass> getCoordenadaAtual() {
        return coordenadaAtual;
    }

    public void get(String resourceUUID, LocalizacaoPass dadosPassageiro){
        String final_uri = baseUri.concat("collector/resources/").concat(resourceUUID).concat("/").concat("data/").concat("last");

        StringRequest postRequest = new StringRequest(Request.Method.POST, final_uri, response -> {
            try {
                JSONObject jsonResponse = new JSONObject(response); // transforma a response em objeto json


                JSONObject dados = jsonResponse.getJSONArray("resources") // acessa cada um dos elementos ate chegar ao dado que ta la no centro do json
                        .getJSONObject(0)
                        .getJSONObject("capabilities")
                        .getJSONArray("localizacao").getJSONObject(0);

                String identificador = dados.getString("identificador");
                Double latitude = dados.getDouble("latitude");
                Double longitude = dados.getDouble("longitude");
                String altitude = dados.getString("altitude");
                String date = dados.getString("date");
                String velocidade = dados.getString("velocidade");
                Double latitudeDest = dados.getDouble("latitudeDestino");
                Double longitudeDest = dados.getDouble("longitudeDestino");

                Log.i(TAG,"####################");
                Log.i(TAG,"#### INTERSCITY - ID" + identificador);
                Log.i(TAG,"#### INTERSCITY - LAT" + String.valueOf(latitude));
                Log.i(TAG,"#### INTERSCITY - LON" + String.valueOf(longitude));
                Log.i(TAG,"#### INTERSCITY - ALT"+ altitude);
                Log.i(TAG,"#### INTERSCITY - DATE"+ date);
                Log.i(TAG,"#### INTERSCITY - VEL"+ velocidade);
                Log.i(TAG,"## INTERSCITY LatDEST"+ String.valueOf(latitudeDest));
                Log.i(TAG,"## INTERSCITY LogDEST"+ String.valueOf(longitudeDest));

                dadosPassageiro.setIdentificador(identificador);
                dadosPassageiro.setLatitude(latitude);
                dadosPassageiro.setLongitude(longitude);
                dadosPassageiro.setAltitude(altitude);
                dadosPassageiro.setVelocidade(velocidade);
                dadosPassageiro.setLatitudeDestino(latitudeDest);
                dadosPassageiro.setLongitudeDestino(longitudeDest);
                dadosPassageiro.setDate(date);

                String jsonStr = dados.toString();
                Gson gson = new Gson();
                coordenadaAtual.setValue(gson.fromJson(jsonStr, LocalizacaoPass.class)); // esse valor setado notifica automaticamente a interface e
                                                                                        //   lá podemos atualizar o mapa

                //loc = dadosPassageiro;

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> {
            System.out.println("Erro: "+ error.toString());

        }){
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
            @Override
            public byte[] getBody() throws AuthFailureError {

                try {
                    JSONArray capabilities_array = new JSONArray();
                    capabilities_array.put("localizacao"); // Aqui vc adiciona as capacidades que deseja consultar o ultimo dado

                    JSONObject data = new JSONObject();
                    data.put("capabilities", capabilities_array);
                    data.put("matchers", new JSONObject());
                    data.put("start_range", "2016-06-25T12:21:29");
                    data.put("end_range", "2022-06-25T16:21:29");

                    return data.toString().getBytes(StandardCharsets.UTF_8);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return super.getBody();
            }
        };
        requestQueue.add(postRequest);
        //queue.add(postRequest);
        //return loc;
    }
}
