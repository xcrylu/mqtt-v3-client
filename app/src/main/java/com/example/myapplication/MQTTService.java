package com.example.myapplication;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttConnect;
import org.greenrobot.eventbus.EventBus;

public class MQTTService extends Service {
    public static final String TAG = MQTTService.class.getSimpleName();

    private static MqttAndroidClient client;
    private MqttConnectOptions conOpt;

    private String host = "tcp://10.0.2.2:1883";
    private String userName = "xxxx";
    private String password  = "123";
    private  static  String myTopic = "xxx";

    private String clientId = "sdff";



    public MQTTService() {
    }

    @Override
    public  int onStartCommand(Intent intent,int flags, int startId){
        init();
        return super.onStartCommand(intent,flags,startId);
    }

    public static void publish(String msg){
        String topic = myTopic;
        Integer qos = 0;
        Boolean retained = false;
        try{
            client.publish(topic,msg.getBytes(),qos.intValue(),retained.booleanValue());
        }catch (MqttException e){
            e.printStackTrace();
        }
    }
    private  void init(){
        String uri = host;
        client = new MqttAndroidClient(this,uri,clientId);
        client.setCallback(mqttCallback);
        conOpt = new MqttConnectOptions();
        conOpt.setCleanSession(true);
        conOpt.setKeepAliveInterval(20);
        conOpt.setUserName(userName);
        conOpt.setPassword(password.toCharArray());

        boolean doConnect = true;
        String message = "{\"terminal_uid\":\"" + clientId + "\"}";
        String topic = myTopic;
        Integer qos = 0;
        Boolean retained = false;

        if((!message.equals(""))|| (!topic.equals(""))){
            try{
                conOpt.setWill(topic,message.getBytes(),qos.intValue(),retained.booleanValue());
            }catch (Exception e){
                Log.i(TAG,"Exception Occured",e);
                doConnect = false;
                iMQttActionListener.onFailure(null,e);
            }
        }
        if(doConnect){
            doClientConnection();
        }

    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
        //throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public  void onDestroy(){
        try{
           client.disconnect();
        }catch (MqttException e){
            e.printStackTrace();
        }


        super.onDestroy();
    }

    private  void doClientConnection(){
        if(!client.isConnected() && isConnectIsNormal()){
            try{
                client.connect(conOpt,null,iMQttActionListener);

            }catch (MqttException e){
                e.printStackTrace();
            }
        }
    }

    private IMqttActionListener iMQttActionListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            Log.i(TAG,"连接成功");
            try {
                client.subscribe(myTopic,1);

            }catch (MqttException e){
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, @NonNull Throwable exception) {
            exception.printStackTrace();
        }
    };

    private MqttCallback mqttCallback = new MqttCallback(){

        @Override
        public  void messageArrived(String topic, @NonNull MqttMessage message) throws Exception{
            String str1 = new String(message.getPayload());
            MQTTMessage msg = new MQTTMessage();
            msg.setMessage(str1);
            EventBus.getDefault().post(msg);
            String  str2 = topic + ";qos:" + message.getQos() + ";retained:" + message.isRetained();
            Log.i(TAG,"messageArrived:" + str1);
            Log.i(TAG,str2);
        }
        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0){

        }
        @Override
        public  void connectionLost(Throwable arg0){

        }
    };

    private  boolean isConnectIsNormal(){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) this.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if(info != null && info.isAvailable()){
            String name = info.getTypeName();
            Log.i(TAG,"MQTT 当前网络名称:" + name);
            return true;
        }else{
            Log.i(TAG,"MQTT 没有可用网络");
            return false;
        }
    }
}