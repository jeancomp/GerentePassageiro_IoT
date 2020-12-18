package br.ufma.gerentepass;

import android.app.AlertDialog;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import br.ufma.lsdi.cddl.message.Message;
import br.ufma.lsdi.cddl.pubsub.Publisher;
import br.ufma.lsdi.cddl.pubsub.Subscriber;
import br.ufma.lsdi.cddl.listeners.IMonitorListener;

public class MonitorPrincipal {
    public static final String TAG = MonitorPrincipal.class.getSimpleName();

    PubSubActivity pubActivity;
    Publisher publisher;
    Subscriber subscriber;
    AlertDialog alerta;
    String monitorCode;

    private IMonitorListener monitorListener = new IMonitorListener() {
        @Override
        public void onEvent(final Message message) {
            Object[] valor = message.getServiceValue();
            String mensagemRecebida = StringUtils.join(valor, ", ");
            String[] separated = mensagemRecebida.split(",");
            String atividade = String.valueOf(separated[0]);
            //Double latitude = Double.valueOf(separated[1]);
            Log.i(TAG,"################# Mensagem Capturada Monitor: " + mensagemRecebida);
            System.out.printf("mensagemRecebida: %s \n", mensagemRecebida);
            System.out.printf("separated: %s \n", separated[0]);
            System.out.printf("atividade: %s \n", atividade);
            //System.out.printf("latitude: %.2f \n", latitude);


            String ms = "";
            ms = mensagemRecebida;
            String[] sin = ms.split(";|;\\s");
            String ss = sin[0];
            double vv = Double.parseDouble(sin[1]);
            if( ss.equals("RESP")) {
                System.out.printf("Sinais vitais string: %s \n",ss);
                System.out.printf("Sinais vitais numero: %f \n",vv);
                new Thread() {
                    public void run() {
                        pubActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //pubActivity.geraAlerta(1, msgAlerta(message));
                            }
                        });
                    }
                }.start();
            }


            System.out.println("A mensagem vem anexa: " + message);
            String line = message.toString();
            String[] sinal = line.split(";|;\\s");
            String latitude = sinal[0];
            String longitude = sinal[1];
            String altitude = sinal[2];
            String velocidade = sinal[3];

            System.out.printf(">>>>>>>>>>>>>>>>>>>>>>>>>>>> latitude: %s \n", sinal[0]);
            System.out.printf(">>>>>>>>>>>>>>>>>>>>>>>>>>>> longitude: %s \n", sinal[1]);
            System.out.printf(">>>>>>>>>>>>>>>>>>>>>>>>>>>> altitude: %s \n", sinal[2]);
            System.out.printf(">>>>>>>>>>>>>>>>>>>>>>>>>>>> velocidade: %s \n", sinal[3]);

            if (line.equals("RESP")) {
                new Thread() {
                    public void run() {
                        pubActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>Gerando alertas");
                                //pubActivity.geraAlerta(1, msgAlerta(message));
                            }
                        });
                    }
                }.start();
            }
        }
    };

    public String msgAlerta(Message msg){
        Object[] valor = msg.getServiceValue();
        String mensagemRecebida = StringUtils.join(valor, ", ");
        String ms = "";
        ms = mensagemRecebida;
        String[] sin = ms.split(";|;\\s");
        //String ss = sin[0];
        //double vv = Double.parseDouble(sin[1]);
        return sin[0];
    }

    public void metodo(Subscriber sub, Publisher pub, PubSubActivity p, Message msg) {
        publisher = pub;
        subscriber = sub;
        pubActivity = p;

        monitorCode = pub.getMonitor().addRule("select serviceValue[0], serviceValue[1], serviceValue[2], serviceValue[3] from Message", message -> {
            System.out.println("1 - ##########################");
            new Thread() {
                public void run() {
                    pubActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //pubActivity.geraAlerta(2, msgAlerta(msg));
                        }
                    });
                }
            }.start();});
    }

    public void removeMonitor(String monitorCode){
        // remove the monitor added above
        subscriber.getMonitor().removeRule(monitorCode);
    }
}