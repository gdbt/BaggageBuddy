package com.example.noah.baggagebuddy;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class control extends Activity {
    Button btnOn, btnOff, btnDis, btngrab;
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    TextView finalprint;
    public ReadInput mReadThread = null;

    //test variables




    private class ConnectBT extends AsyncTask<Void, Void, Void>{
        private boolean ConnectSuccess = true; //almost connected
        @Override
        protected void onPreExecute(){
            progress = ProgressDialog.show(control.this, "Connecting...","Please wait!"); //shows progress
        }
        @Override
        protected Void doInBackground(Void... devices) {//doing progress in background{
            if(control.this.mReadThread != null){
                control.this.mReadThread.stop();
                do{

                }while(control.this.mReadThread.isRunning());
                control.this.mReadThread = null;
            }
            try {
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects and checks if available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();


                }
            } catch (IOException e) {
                ConnectSuccess = false;
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result){
            super.onPostExecute(result);
            if(!ConnectSuccess){
                msg("Connection Failed");
                finish();
            }
            else{
                msg("Connected");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }
    private class ReadInput implements Runnable{
        private boolean bStop = false;
        private Thread f2t = new Thread(this,"Input Thread");

        public ReadInput(){
            this.f2t.start();
        }

        public boolean isRunning(){
            return this.f2t.isAlive();
        }
        public void run(){
            try{
                InputStream input = control.this.btSocket.getInputStream();
                while(!this.bStop){
                    byte[] buffer = new byte[256];
                    if(input.available() > 0){
                        input.read(buffer);
                        int i =0;
                        while(i < buffer.length && buffer[i] != 0){
                            i++;
                        }
                        final String strinput = new String(buffer,0,i);
                        control.this.finalprint.post(new Runnable(){
                            public void run(){
                                control.this.finalprint.setText(strinput); //appending the string input
                                int txtlength = control.this.finalprint.getEditableText().length(); //grabbing length of textbox
                                if(txtlength > 50000){
                                    control.this.finalprint.getEditableText().delete(0,txtlength - 50000);
                                }
                            }
                        });
                    }
                }
                Thread.sleep(500);
        }
        catch (IOException e){
                e.printStackTrace();
            }catch (InterruptedException e2){
                e2.printStackTrace();
            }
        }
        public void stop(){
            this.bStop = true;
        }
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        //receive the address of bluetooth device
        Intent newint = getIntent();
        address = newint.getStringExtra(main.EXTRA_ADDRESS);
        btnOn = (Button)findViewById(R.id.btnOn);
        btnOff = (Button)findViewById(R.id.btnOff);
        btnDis = (Button)findViewById(R.id.btnDis);
        finalprint = (TextView) findViewById(R.id.result);
        btngrab = (Button) findViewById(R.id.button2);

        new ConnectBT().execute(); //Call the class to connect

        btnOn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                turnOnLed();

            }
        });
        btnOff.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                turnOffLed();
            }
        });
        btnDis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Disconnect();
            }
        });
        btngrab.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                grabdata();
            }
        });

    }

    private void Disconnect()
    {
        if (btSocket!=null) //If the btSocket is busy
        {
            try
            {
                btSocket.close(); //close connection
            }
            catch (IOException e)
            { msg("Error");}
        }
        finish(); //return to the first layout

    }

    private void turnOffLed()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("0".toString().getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    private void turnOnLed()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("1".toString().getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }
    private void grabdata(){
        if (btSocket!=null){
            try {
                btSocket.getOutputStream().write("2".toString().getBytes());
            }
            catch (IOException e){
                msg("Error");
            }
        }
    }
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }
}

