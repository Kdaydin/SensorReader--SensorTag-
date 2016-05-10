package com.khome.kdaydin.sensorreader;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class ResultScreen extends Activity {

    private final static int STATE_CONNECT=0;
    private final static int STATE_SEARCH_SERVICES=1;
    private final static int STATE_READ_KEY=2;// Not used/supported -Skip this state
    private final static int STATE_NOTIFY_KEY=3;
    private final static int STATE_WRITE_ENABLE_HUMIDITY=4;
    private final static int STATE_READ_HUMIDITY=5;
    private final static int STATE_NOTIFY_HUMIDITY=6;
    private final static int STATE_WRITE_ENABLE_IR_TEMPERATURE=7;
    private final static int STATE_READ_IR_TEMPERATURE=8;
    private final static int STATE_NOTIFY_IR_TEMPERATURE=9;
    private final static int STATE_DUMMY=10;
    private final static int STATE_READ=11;
    private final static int STATE_DISCONNECT=12;


    private Context mContext;
    private SensorTagBLEBroadcastReceiver mBroadcastReceiver;
    private String mDeviceAddress;
    private int mState=0;

    private int i=0;
    private int nextstate_loopcount=-1;

    private TumakuBLE  mTumakuBLE=null;

    TextView mTemp1;
    TextView mTemp2;
    TextView mTemp3;
    TextView mTemp4;
    TextView mTemp5;
    TextView mTemp6;
    TextView mTemp7;
    TextView mTemp8;
    TextView mTemp9;
    TextView mTemp10;
    TextView mTempAVG;
    EditText mMin;
    EditText mMax;
    GraphView graph;
    Button mSet;

    double Temp1=0;
    double Temp2=0;
    double Temp3=0;
    double Temp4=0;
    double Temp5=0;
    double Temp6=0;
    double Temp7=0;
    double Temp8=0;
    double Temp9=0;
    double Temp10=0;
    int a=0;
    double TempAVG;
    double min=-10;
    double max=100;
    private LineGraphSeries<DataPoint> mSeries1;
    ArrayList<BluetoothDevice> mDeviceList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_screen);
        mContext=this;
        mBroadcastReceiver= new SensorTagBLEBroadcastReceiver();
        mTumakuBLE=((TumakuBLEApplication)getApplication()).getTumakuBLEInstance(this);

        Bundle bundle = getIntent().getExtras();
        mDeviceList = bundle.getParcelableArrayList("DEVICELIST");
        mDeviceAddress=mDeviceList.get(0).getAddress();
        mTumakuBLE.setDeviceAddress(mDeviceAddress);
        mTemp1=(TextView)findViewById(R.id.data1);
        mTemp2=(TextView)findViewById(R.id.data2);
        mTemp3=(TextView)findViewById(R.id.data3);
        mTemp4=(TextView)findViewById(R.id.data4);
        mTemp5=(TextView)findViewById(R.id.data5);
        mTemp6=(TextView)findViewById(R.id.data6);
        mTemp7=(TextView)findViewById(R.id.data7);
        mTemp8=(TextView)findViewById(R.id.data8);
        mTemp9=(TextView)findViewById(R.id.data9);
        mTemp10=(TextView)findViewById(R.id.data10);
        mTempAVG=(TextView)findViewById(R.id.dataavg);
        graph = (GraphView) findViewById(R.id.graph);
        mSet = (Button) findViewById(R.id.set);
        mMin = (EditText) findViewById(R.id.editText);
        mMax = (EditText) findViewById(R.id.editText2);
        mSeries1 = new LineGraphSeries<DataPoint>(new DataPoint[]{new DataPoint(0,0)});
        graph.addSeries(mSeries1);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(40);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(40);
        graph.getViewport().setScrollable(true);
        graph.getViewport().setScalable(true);
        mSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                min=Double.valueOf(mMin.getText().toString());
                max=Double.valueOf(mMax.getText().toString());
                Toast.makeText(getApplicationContext(),"min is "+min+" max is "+max+" ",Toast.LENGTH_SHORT).show();
            }
        });
//        mHum1=(TextView)findViewById(R.id.data1);
//        mHum2=(TextView)findViewById(R.id.data2);
//        Toast.makeText(this,"HEYAHEYAHEY"+mDeviceList.size(),Toast.LENGTH_SHORT).show();
    }

    protected void updateHumidityValues(byte [] value) {
        try{
            double temperatureValue=TumakuBLE.calcHumRel(TumakuBLE.shortUnsignedAtOffset(value, 0));
            double humidityValue=TumakuBLE.calcHumRel(TumakuBLE.shortUnsignedAtOffset(value, 2));
            //mTextTemperature.setText(String.format("%.1f", temperatureValue));
//            mHum1.setText(String.format("%.1f", humidityValue));
//            mHum2.setText(String.format("%.1f", humidityValue));

        } catch (Exception exc) {
            if (Constant.DEBUG) {
                Log.i("JMG","Exception while updating Humidity values");
                Log.i("JMG",exc.getMessage());
            }

        }

    }

    protected void updateIRTemperatureValues(byte [] value) {
        try{
            final double ambientTemperatureValue=TumakuBLE.extractAmbientTemperature(TumakuBLE.shortUnsignedAtOffset(value, 2));
            double targetTemperatureValue=TumakuBLE.extractTargetTemperature(TumakuBLE.shortUnsignedAtOffset(value, 0), ambientTemperatureValue);
            if (nextstate_loopcount>=mDeviceList.size()&&TempAVG>max){
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+2:00"));
                            Date currentLocalTime = cal.getTime();
                            DateFormat date = new SimpleDateFormat("HH:mm:ss");
// you can get seconds by adding  "...:ss" to it
                            date.setTimeZone(TimeZone.getTimeZone("GMT+2:00"));
                            String localTime = date.format(currentLocalTime);

                            GMailSender sender = new GMailSender("sensorreaderapp@gmail.com",
                                    "sensorreader");
                            sender.sendMail("ALARM HIGH TEMPERATURE [SENSORTAG]", "Your device measured a value that is above max limit "+max+" C at "+localTime+". Value : "+TempAVG+" C",
                                    "sensorreaderapp@gmail.com", "sensorreaderapp@gmail.com");
                        } catch (Exception e) {
                            Log.e("SendMail", e.getMessage(), e);
                        }
                    }

                }).start();
            }

            if (nextstate_loopcount>=mDeviceList.size()&&TempAVG<min){
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+2:00"));
                            Date currentLocalTime = cal.getTime();
                            DateFormat date = new SimpleDateFormat("HH:mm:ss");
// you can get seconds by adding  "...:ss" to it
                            date.setTimeZone(TimeZone.getTimeZone("GMT+2:00"));
                            String localTime = date.format(currentLocalTime);

                            GMailSender sender = new GMailSender("sensorreaderapp@gmail.com",
                                    "sensorreader");
                            sender.sendMail("ALARM LOW TEMPERATURE [SENSORTAG]", "Your device measured a value that is below min limit "+min+" C at "+localTime+". Value : "+TempAVG+" C",
                                    "sensorreaderapp@gmail.com", "sensorreaderapp@gmail.com");
                        } catch (Exception e) {
                            Log.e("SendMail", e.getMessage(), e);
                        }
                    }

                }).start();
            }

            if(nextstate_loopcount%(mDeviceList.size())==0) {
                mTemp1.setText(String.format("%.1f", ambientTemperatureValue));
                Temp1=ambientTemperatureValue;
            }
            else if (nextstate_loopcount%(mDeviceList.size())==1) {
                mTemp2.setText(String.format("%.1f", ambientTemperatureValue));
                Temp2=ambientTemperatureValue;
            }
            else if(nextstate_loopcount%(mDeviceList.size())==2) {
                mTemp3.setText(String.format("%.1f", ambientTemperatureValue));
                Temp3=ambientTemperatureValue;
            }
            else if (nextstate_loopcount%(mDeviceList.size())==3) {
                mTemp4.setText(String.format("%.1f", ambientTemperatureValue));
                Temp4=ambientTemperatureValue;
            }
            else if(nextstate_loopcount%(mDeviceList.size())==4) {
                mTemp5.setText(String.format("%.1f", ambientTemperatureValue));
                Temp5=ambientTemperatureValue;
                }
            else if (nextstate_loopcount%(mDeviceList.size())==5) {
                mTemp6.setText(String.format("%.1f", ambientTemperatureValue));
                Temp6=ambientTemperatureValue;
            }
            else if(nextstate_loopcount%(mDeviceList.size())==6) {
                mTemp7.setText(String.format("%.1f", ambientTemperatureValue));
                Temp7=ambientTemperatureValue;
            }
            else if (nextstate_loopcount%(mDeviceList.size())==7) {
                mTemp8.setText(String.format("%.1f", ambientTemperatureValue));
                Temp8=ambientTemperatureValue;
            }
            else if(nextstate_loopcount%(mDeviceList.size())==8) {
                mTemp9.setText(String.format("%.1f", ambientTemperatureValue));
                Temp9=ambientTemperatureValue;
            }
            else if (nextstate_loopcount%(mDeviceList.size())==9) {
                mTemp10.setText(String.format("%.1f", ambientTemperatureValue));
                Temp10=ambientTemperatureValue;
            }
            TempAVG=(Temp1+Temp2+Temp3+Temp4+Temp5+Temp6+Temp7+Temp8+Temp9+Temp10)/mDeviceList.size();
            mTempAVG.setText(String.format("%.1f",TempAVG));

            mSeries1.appendData(new DataPoint(a,TempAVG),true,10);
            a=a+5;

        } catch (Exception exc) {
            if (Constant.DEBUG) {
                Log.i("JMG","Exception while updating Temperature values");
                Log.i("JMG",exc.getMessage());
            }

        }

    }

    @Override
    public void onResume(){
        super.onResume();
        IntentFilter filter = new IntentFilter(TumakuBLE.WRITE_SUCCESS);
        filter.addAction(TumakuBLE.READ_SUCCESS);
        filter.addAction(TumakuBLE.WRITE_DESCRIPTOR_SUCCESS);
        filter.addAction(TumakuBLE.NOTIFICATION);
        filter.addAction(TumakuBLE.DEVICE_CONNECTED);
        filter.addAction(TumakuBLE.DEVICE_DISCONNECTED);
        filter.addAction(TumakuBLE.SERVICES_DISCOVERED);
        this.registerReceiver(mBroadcastReceiver, filter);
        if (mTumakuBLE.isConnected()){
            mState=STATE_NOTIFY_KEY;
            nextState();

        } else {
            mState=STATE_CONNECT;
            nextState();

        }

    }

    @Override
    public void onStop(){
        super.onStop();
        this.unregisterReceiver(this.mBroadcastReceiver);
        try {
            mTumakuBLE.enableNotifications(TumakuBLE.SENSORTAG_HUMIDITY_SERVICE,TumakuBLE.SENSORTAG_HUMIDITY_DATA,false);
            mTumakuBLE.enableNotifications(TumakuBLE.SENSORTAG_KEY_SERVICE,TumakuBLE.SENSORTAG_KEY_DATA,false);
            mTumakuBLE.enableNotifications(TumakuBLE.SENSORTAG_IR_TEMPERATURE_SERVICE,TumakuBLE.SENSORTAG_IR_TEMPERATURE_DATA,false);
            mTumakuBLE.write(TumakuBLE.SENSORTAG_HUMIDITY_SERVICE,TumakuBLE.SENSORTAG_HUMIDITY_CONF, new byte[]{0});
            mTumakuBLE.write(TumakuBLE.SENSORTAG_IR_TEMPERATURE_SERVICE,TumakuBLE.SENSORTAG_IR_TEMPERATURE_CONF, new byte[]{0});

        }catch (Exception exc) {
            if (Constant.DEBUG) {
                Log.i("JMG","Exception caught during SensorTag activity onStop()");
                Log.i("JMG","Exception: " + exc.getMessage());
            }
        }

    }

    protected void nextState(){
        switch(mState) {
            case (STATE_CONNECT):
                if (Constant.DEBUG) Log.i("JMG","State Connected");
                mTumakuBLE.connect();
                break;
            case(STATE_SEARCH_SERVICES):
                if (Constant.DEBUG) Log.i("JMG","State Search Services");
                mTumakuBLE.discoverServices();
                break;
            case(STATE_READ):
                if (Constant.DEBUG) Log.i("JMG","State Read ");
                mTumakuBLE.read(TumakuBLE.SENSORTAG_IR_TEMPERATURE_SERVICE,TumakuBLE.SENSORTAG_IR_TEMPERATURE_DATA);
                break;
            case(STATE_READ_KEY):
                if (Constant.DEBUG) Log.i("JMG","State Read Key");
                mTumakuBLE.read(TumakuBLE.SENSORTAG_KEY_SERVICE,TumakuBLE.SENSORTAG_KEY_DATA);
                break;
            case(STATE_NOTIFY_KEY):
                if (Constant.DEBUG) Log.i("JMG","State Notify Key");
                mTumakuBLE.enableNotifications(TumakuBLE.SENSORTAG_KEY_SERVICE,TumakuBLE.SENSORTAG_KEY_DATA,true);
                break;
            case(STATE_WRITE_ENABLE_HUMIDITY):
                if (Constant.DEBUG) Log.i("JMG","State Enable Humidity");
                mTumakuBLE.write(TumakuBLE.SENSORTAG_HUMIDITY_SERVICE,TumakuBLE.SENSORTAG_HUMIDITY_CONF, new byte[]{1});
                break;
            case(STATE_READ_HUMIDITY):
                if (Constant.DEBUG) Log.i("JMG","State Read Humidity");
                mTumakuBLE.read(TumakuBLE.SENSORTAG_HUMIDITY_SERVICE, TumakuBLE.SENSORTAG_HUMIDITY_DATA);
                break;
            case(STATE_NOTIFY_HUMIDITY):
                if (Constant.DEBUG) Log.i("JMG","State Notify Humidity");
                mTumakuBLE.enableNotifications(TumakuBLE.SENSORTAG_HUMIDITY_SERVICE,TumakuBLE.SENSORTAG_HUMIDITY_DATA,true);
                break;
            case(STATE_WRITE_ENABLE_IR_TEMPERATURE):
                if (Constant.DEBUG) Log.i("JMG","State Enable IR Temperature");
                mTumakuBLE.write(TumakuBLE.SENSORTAG_IR_TEMPERATURE_SERVICE,TumakuBLE.SENSORTAG_IR_TEMPERATURE_CONF, new byte[]{1});
                break;
            case(STATE_READ_IR_TEMPERATURE):
                if (Constant.DEBUG) Log.i("JMG","State Read IR Temperature");
                mTumakuBLE.read(TumakuBLE.SENSORTAG_IR_TEMPERATURE_SERVICE,TumakuBLE.SENSORTAG_IR_TEMPERATURE_DATA);
                break;
            case(STATE_NOTIFY_IR_TEMPERATURE):
                if (Constant.DEBUG) Log.i("JMG","State Notify IR Temperature");
                mTumakuBLE.enableNotifications(TumakuBLE.SENSORTAG_IR_TEMPERATURE_SERVICE,TumakuBLE.SENSORTAG_IR_TEMPERATURE_DATA,true);
                /////////////
                for(int x=0; x<200000; x++)    //4saniye periyot
                    Log.d("XXXXXXXXXX","sayac: " + x);
                /////////////
                nextstate_loopcount++;
                Log.d("XXXXXXXXXX","nextstate_loopcount: " + nextstate_loopcount);
                mState=STATE_DISCONNECT;
                nextState();
                break;
            case(STATE_DISCONNECT):
                if (Constant.DEBUG) Log.i("JMG","State Disconect");
                mDeviceAddress=mDeviceList.get(++i%mDeviceList.size()).getAddress();
                mTumakuBLE.setDeviceAddress(mDeviceAddress);
                mTumakuBLE.disconnect();
                mState=STATE_CONNECT;
                nextState();
                break;
            default:
        }

    }

    private class SensorTagBLEBroadcastReceiver extends BroadcastReceiver {
        //YeelightCallBack.WRITE_SUCCESS);
        //YeelightCallBack.READ_SUCCESS);
        //YeelightCallBack.DEVICE_CONNECTED);

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(TumakuBLE.DEVICE_CONNECTED)) {
                if (Constant.DEBUG) Log.i("JMG", "DEVICE_CONNECTED message received");


                mState=STATE_SEARCH_SERVICES;
                nextState();
                return;
            }
            if (intent.getAction().equals(TumakuBLE.DEVICE_DISCONNECTED)) {
                if (Constant.DEBUG) Log.i("JMG","DEVICE_DISCONNECTED message received");
                //This is an unexpected device disconnect situation generated by Android BLE stack
                //Usually happens on the service discovery step :-(
                //Try to reconnect
                String fullReset=intent.getStringExtra(TumakuBLE.EXTRA_FULL_RESET);
                if (fullReset!=null){
                    if (Constant.DEBUG) Log.i("JMG","DEVICE_DISCONNECTED message received with full reset flag");
                    Toast.makeText(mContext, "Unrecoverable BT error received. Launching full reset", Toast.LENGTH_SHORT).show();
                    mState=STATE_CONNECT;
                    mTumakuBLE.resetTumakuBLE();
                    mTumakuBLE.setDeviceAddress(mDeviceAddress);
                    mTumakuBLE.setup();
                    nextState();
                    return;
                } else {
                    if (mState!=STATE_CONNECT){
                        Toast.makeText(mContext, "Device disconnected unexpectedly. Reconnecting.", Toast.LENGTH_SHORT).show();
                        mState=STATE_CONNECT;
                        mTumakuBLE.resetTumakuBLE();
                        mTumakuBLE.setDeviceAddress(mDeviceAddress);
                        nextState();
                        return;
                    }
                }
            }
            if (intent.getAction().equals(TumakuBLE.SERVICES_DISCOVERED)) {
                if (Constant.DEBUG) Log.i("JMG","SERVICES_DISCOVERED message received");


                mState=STATE_NOTIFY_KEY;
                nextState();
                return;
            }

            if (intent.getAction().equals(TumakuBLE.READ_SUCCESS)) {
                if (Constant.DEBUG) Log.i("JMG","READ_SUCCESS message received");
                String readValue= intent.getStringExtra(TumakuBLE.EXTRA_VALUE);

                if (mState==STATE_READ_KEY) {
                    mState=STATE_NOTIFY_KEY;
                    nextState();
                }
                if (mState==STATE_READ_HUMIDITY) {
                    mState=STATE_NOTIFY_HUMIDITY;
                    nextState();
                }
                if (mState==STATE_READ_IR_TEMPERATURE) {
                    mState=STATE_NOTIFY_IR_TEMPERATURE;
                    nextState();
                }
                return;
            }

            if (intent.getAction().equals(TumakuBLE.WRITE_SUCCESS)) {
                if (Constant.DEBUG) Log.i("JMG","WRITE_SUCCESS message received");

                if (mState==STATE_WRITE_ENABLE_HUMIDITY) {
                    mState=STATE_READ_HUMIDITY;
                    nextState();
                }
                if (mState==STATE_WRITE_ENABLE_IR_TEMPERATURE) {
                    mState=STATE_READ_IR_TEMPERATURE;
                    nextState();
                }
                return;
            }

            if (intent.getAction().equals(TumakuBLE.NOTIFICATION)) {
                String notificationValue= intent.getStringExtra(TumakuBLE.EXTRA_VALUE);
                String characteristicUUID= intent.getStringExtra(TumakuBLE.EXTRA_CHARACTERISTIC);
                byte [] notificationValueByteArray =  intent.getByteArrayExtra(TumakuBLE.EXTRA_VALUE_BYTE_ARRAY);
                if (notificationValue==null) notificationValue="NULL";
                if (characteristicUUID==null) characteristicUUID="MISSING";
                if (Constant.DEBUG) {
                    Log.i("JMG","NOTIFICATION message received");
                    Log.i("JMG", "Characteristic: "+ characteristicUUID);
                    Log.i("JMG","Value: " + notificationValue);
                }

                if (!notificationValue.equalsIgnoreCase("null")) {
                    if (characteristicUUID.equalsIgnoreCase(TumakuBLE.SENSORTAG_KEY_DATA)) {
                        if (Constant.DEBUG) Log.i("JMG","NOTIFICATION of Key Service");
                        if (notificationValueByteArray==null) {
                            if (Constant.DEBUG) Log.i("JMG","No notificationValueByteArray received. Discard notification");
                            return;
                        }

                    }
                    if (characteristicUUID.equalsIgnoreCase(TumakuBLE.SENSORTAG_HUMIDITY_DATA)) {
                        if (Constant.DEBUG) Log.i("JMG","NOTIFICATION of Humidity Service");
                        if (notificationValueByteArray==null) {
                            if (Constant.DEBUG) Log.i("JMG","No notificationValueByteArray received. Discard notification");
                            return;
                        }
                        updateHumidityValues(notificationValueByteArray);
                    }
                    if (characteristicUUID.equalsIgnoreCase(TumakuBLE.SENSORTAG_IR_TEMPERATURE_DATA)) {
                        if (Constant.DEBUG) Log.i("JMG","NOTIFICATION of IR Temperature Service");
                        if (notificationValueByteArray==null) {
                            if (Constant.DEBUG) Log.i("JMG","No notificationValueByteArray received. Discard notification");
                            return;
                        }
                        updateIRTemperatureValues(notificationValueByteArray);
                    }
                }
                return;
            }


            if (intent.getAction().equals(TumakuBLE.WRITE_DESCRIPTOR_SUCCESS)) {
                if (Constant.DEBUG) Log.i("JMG","WRITE_DESCRIPTOR_SUCCESS message received");

                if (mState==STATE_NOTIFY_KEY) {
                    mState=STATE_WRITE_ENABLE_HUMIDITY;
                    nextState();
                }
                if (mState==STATE_NOTIFY_HUMIDITY) {
                    mState=STATE_WRITE_ENABLE_IR_TEMPERATURE;
                    nextState();
                }
                if (mState==STATE_NOTIFY_IR_TEMPERATURE) {
                    mState=STATE_DUMMY;
                    if (Constant.DEBUG) Log.i("JMG","Sensor initialisation completed");
                    nextState();
                }
                return;
            }


        }

    }
}
