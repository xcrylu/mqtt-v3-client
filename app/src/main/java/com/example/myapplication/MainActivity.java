package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends AppCompatActivity {
    static int count;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EventBus.getDefault().register(this);

        startService(new Intent(this, MQTTService.class));

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener(){

        @Override
        public void onClick(View view){
            //int count = 0;
            count = count +1;
            // String message = "com from andriod " + count;
            String message = ((EditText)findViewById(R.id.txtPubMessage)).getText().toString();
            MQTTService.publish(message);
        }
    });
}
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getMqttMessage(@NonNull MQTTMessage mqttMessage){
        Log.i(MQTTService.TAG,"get message:"+ mqttMessage.getMessage());
        //Toast.makeText(this,mqttMessage.getMessage(),Toast.LENGTH_SHORT).show();
        ((TextView)findViewById(R.id.txtRcvMessage)).setText(mqttMessage.getMessage());
    }
}