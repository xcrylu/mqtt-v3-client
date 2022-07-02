package com.example.myapplication;

public class MQTTMessage {
    private  String message;

    public  void MQTTMessage(String msg){
        this.message = msg;
    }
    public  void MQTTMessage(){
        this.message = null;
    }
    public  String getMessage(){
        return this.message;
    }
    public void setMessage(String msg){
        this.message = msg;
    }
}
