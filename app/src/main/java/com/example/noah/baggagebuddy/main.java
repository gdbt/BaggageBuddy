package com.example.noah.baggagebuddy;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Button;
import java.util.Set;
import java.util.ArrayList;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
//import android.widget.AdapterView.OnClickListener;
import android.widget.TextView;
import android.content.Intent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

public class main extends AppCompatActivity {
    private BluetoothAdapter myBluetooth = null;
    private Set <BluetoothDevice> pairedDevices;
    public static String EXTRA_ADDRESS = "device_address";
    Button btnpaired;
    ListView devicelist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnpaired = (Button)findViewById(R.id.button);
        devicelist = (ListView)findViewById(R.id.listview);
        //variable creation


        //bluetooh variables


        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        if(myBluetooth == null){
            //no bluetooth message
            Toast.makeText(getApplicationContext(),"No bluetooth device", Toast.LENGTH_LONG).show();
            finish();
        }
        else if(!myBluetooth.isEnabled()){
                Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnBTon,1);
            }

        btnpaired.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                pairedDeviceList();
            }
        });
    }
    private void pairedDeviceList(){
        pairedDevices = myBluetooth.getBondedDevices();
        ArrayList list = new ArrayList();

        if(pairedDevices.size()>0){
            for(BluetoothDevice bt : pairedDevices){
                list.add(bt.getName() + "\n" + bt.getAddress()); //grabs device name and address
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        devicelist.setAdapter(adapter);
        devicelist.setOnItemClickListener(myListClickListener); //Method called when the device from the list is clicked
    }
    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener(){
        public void onItemClick (AdapterView<?> av, View v, int arg2, long arg3){
            //get mac address
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            //make intent
            Intent i = new Intent(main.this,control.class);
            i.putExtra(EXTRA_ADDRESS, address); //received in infograb
            startActivity(i);
        }
    };

}
