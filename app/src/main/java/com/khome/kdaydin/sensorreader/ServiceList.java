package com.khome.kdaydin.sensorreader;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ServiceList extends ListActivity {


    ListView mServiceList;
    List Services=new ArrayList();
    ArrayList<BluetoothDevice> mDeviceList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_list);
        mServiceList=(ListView)findViewById(android.R.id.list);
        Services.add(0,"Ambient Temperature");
        Services.add(1, "Humidity");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                 Services);

        mServiceList.setAdapter(arrayAdapter);
        Bundle bundle = getIntent().getExtras();
        mDeviceList = bundle.getParcelableArrayList("DEVICELIST");

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        switch (position) {
            case 0:
            //    Toast.makeText(this, "HEYOOO TEMP"+mDeviceList.size(), Toast.LENGTH_LONG).show();
                Intent intentActivity= new Intent(this, ResultScreen.class);
                Bundle b = new Bundle();
                b.putParcelableArrayList("DEVICELIST", mDeviceList);
                intentActivity.putExtras(b);
                this.startActivity(intentActivity);

                break;
            case 1:
            //    Toast.makeText(this, "HEYOOO HUM"+mDeviceList.size(), Toast.LENGTH_LONG).show();
                Intent intentActivity2= new Intent(this, ResultScreen2.class);
                Bundle b2 = new Bundle();
                b2.putParcelableArrayList("DEVICELIST", mDeviceList);
                intentActivity2.putExtras(b2);
                this.startActivity(intentActivity2);

                break;
        }
        super.onListItemClick(l, v, position, id);
    }
}
