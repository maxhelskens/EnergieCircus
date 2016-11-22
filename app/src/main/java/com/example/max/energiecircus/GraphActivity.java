package com.example.max.energiecircus;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.lang.Math.pow;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.AsyncTask;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.lang.Math.pow;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class GraphActivity extends AppCompatActivity{

    /***********************
     * BLE parameters   *
     ***********************/

    private static final String TAG = "MainActivity";


    /* Light Service */
    private static final UUID LIGHT_SERVICE = UUID.fromString("F000AA70-0451-4000-B000-000000000000");
    private static final UUID LIGHT_DATA_CHAR = UUID.fromString("f000aa71-0451-4000-b000-000000000000");
    private static final UUID LIGHT_CONFIG_CHAR = UUID.fromString("f000aa72-0451-4000-b000-000000000000");
    /* Magneto Service */
    private static final UUID MAGNETO_SERVICE = UUID.fromString("F000AA80-0451-4000-B000-000000000000");
    private static final UUID MAGNETO_DATA_CHAR = UUID.fromString("f000aa81-0451-4000-b000-000000000000");
    private static final UUID MAGNETO_CONFIG_CHAR = UUID.fromString("f000aa82-0451-4000-b000-000000000000");
    /* Client Configuration Descriptor */
    private static final UUID CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");


    private BluetoothAdapter mBluetoothAdapter;
    private SparseArray<BluetoothDevice> mDevices;

    private BluetoothGatt mGatt;

    private ProgressDialog mProgress;

    private int REQUEST_ENABLE_BT = 1;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private Toolbar toolbar;

    /**************************
     * Layout parameters   *
     **************************/

    /*Use of graph: https://github.com/PhilJay/MPAndroidChart/wiki*/
    private LineChart chart;
    private List<Entry> entries;
    private LineDataSet dataSet;

    private List<Entry> startEntries;
    private LineDataSet startDataSet;

    private LineData lineData;
    private TextView textMagneto;

    /*Lux*/
    private int i = 0;


    private Stopwatch timerLux = new Stopwatch();

    private double outputLux;
    private double amountEnergy;
    private double powerTotal = 0;
    private double energyLeft;
    private double averagePowerUsage;
    private double minLuxValue;
    private double powerUsageLamp;
    private EditText inputValue;

    private String naamRegistratie;
    private String klasRegistratie;
    private int aantalLampenRegistratie;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        /************
         *    BLE   *
         ************/

        setProgressBarIndeterminate(true);

        /*Check if BLE is supported*/
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE Not Supported",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        mDevices = new SparseArray<BluetoothDevice>();

        /*
         * A progress dialog will be needed while the connection process is
         * taking place
         */
        mProgress = new ProgressDialog(this);
        mProgress.setIndeterminate(true);
        mProgress.setCancelable(false);


        /***************
         *    Layout   *
         ***************/
        Button stopButton = (Button) findViewById(R.id.stopGame);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopGame();
            }
        });

        chart = (LineChart) findViewById(R.id.chart);
        styleChart(chart);

        /**
         * Creating Arraylist to store the different entries. Entries can include "Lux" values and the total amount of energy.
         */
        entries = new ArrayList<Entry>();

        /**
         * Creating the entries for the Lux values. Every entry will be added to the dataset and operations will be performed.
         */
        dataSet = new LineDataSet(entries, "Lux"); // add entries to dataset
        dataSet.setDrawValues(false);
        dataSet.setDrawCircles(false);
        dataSet.setFillColor(R.color.colorAccent);
        dataSet.setFillAlpha(0);

        startEntries = new ArrayList<Entry>();

        /**
         * startDataSet is the starting amount of Energy, used to measure against.
         */
        startDataSet = new LineDataSet(startEntries, "Lux"); // add entries to dataset
        startDataSet.setDrawValues(false);
        startDataSet.setDrawCircles(false);
        startDataSet.setFillColor(R.color.colorPrimaryLight);
        startDataSet.setFillAlpha(0);

        lineData = new LineData(dataSet, startDataSet);
        chart.setData(lineData);
        chart.invalidate(); // refresh
        textMagneto = (TextView) findViewById(R.id.magnetoValue);

        /**
         * Loading shared preferences into new class
         */
        SharedPreferences prefs = getSharedPreferences("MainActivity", 0);
        naamRegistratie = prefs.getString("Naam", null);
        klasRegistratie = prefs.getString("Klas", null);
        aantalLampenRegistratie = prefs.getInt("AantalLampen", 0);
    }

    /**********************
     * BLE Functions   *
     **********************/
    @Override
    protected void onResume() {
        DatabaseHelper dbh = new DatabaseHelper(this);
        super.onResume();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            if (Build.VERSION.SDK_INT >= 21) {
                mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();
                filters = new ArrayList<ScanFilter>();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        /*Make sure dialog is hidden*/
        mProgress.dismiss();
        /*Cancel any scans in progress*/
        mHandler.removeCallbacks(mStopRunnable);
        mHandler.removeCallbacks(mStartRunnable);

        if (Build.VERSION.SDK_INT < 21) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        } else {
            mLEScanner.stopScan(mScanCallback);

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        /*Disconnect from any active tag connection*/
        if (mGatt != null) {
            mGatt.close();
            mGatt = null;
        }
    }

    @Override
    protected void onDestroy() {
        if (mGatt == null) {
            return;
        }
        mGatt.close();
        mGatt = null;
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                /*Bluetooth not enabled.*/
                finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*Menu icons are inflated just as they were with actionbar*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /*Inflate the menu; this adds items to the action bar if it is present.*/
        getMenuInflater().inflate(R.menu.menu_main, menu);

        /*Add any device elements we've discovered to the overflow menu*/
        for (int i = 0; i < mDevices.size(); i++) {
            BluetoothDevice device = mDevices.valueAt(i);
            if (device.getName() != null) {
                menu.add(0, mDevices.keyAt(i), 0, device.getName());
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.bluetoothSearch:
                mDevices.clear();
                startScan();
                /*scanLeDevice(true);*/

                return true;
            default:
                /*Obtain the discovered device to connect with*/
                BluetoothDevice device = mDevices.get(item.getItemId());
                Log.i(TAG, "Connecting to " + device.getName());
                /*
                 * Make a connection with the device
                 */
                connectToDevice(device);
                /*Display progress UI*/
                mHandler.sendMessage(Message.obtain(null, MSG_PROGRESS, "Connecting to " + device.getName() + "..."));
                return super.onOptionsItemSelected(item);
        }
    }

    private Runnable mStopRunnable = new Runnable() {
        @Override
        public void run() {
            stopScan();
        }
    };
    private Runnable mStartRunnable = new Runnable() {
        @Override
        public void run() {
            startScan();
        }
    };

    private void startScan() {
        if (Build.VERSION.SDK_INT < 21) {
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mLEScanner.startScan(filters, settings, mScanCallback);
        }
        setProgressBarIndeterminateVisibility(true);
        mHandler.postDelayed(mStopRunnable, 2500);
    }

    private void stopScan() {
        if (Build.VERSION.SDK_INT < 21) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        } else {
            mLEScanner.stopScan(mScanCallback);
        }
        setProgressBarIndeterminateVisibility(false);
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    Log.i("onLeScan", device.toString());

                    mDevices.put(device.hashCode(), device);
                    /*Update the overflow menu*/
                    invalidateOptionsMenu();
                }
            };

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i("callbackType", String.valueOf(callbackType));
            Log.i("result", result.toString());
            BluetoothDevice btDevice = result.getDevice();

            mDevices.put(btDevice.hashCode(), btDevice);
            /*Update the overflow menu*/
            invalidateOptionsMenu();
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i("ScanResult - Results", sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };


    public void connectToDevice(BluetoothDevice device) {
        if (mGatt == null) {
            mGatt = device.connectGatt(this, false, gattCallback);
            stopScan();
        }
    }

    private void clearDisplayValues() {
        textMagneto.setText("---");
    }

    /*
     * In this callback, we've created a bit of a state machine to enforce that only
     * one characteristic be read or written at a time until all of our sensors
     * are enabled and we are registered to get notifications.
     */
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        /* State Machine Tracking */
        private int mState = 0;

        private void reset() {
            mState = 0;
        }

        private void advance() {
            mState++;
        }

        /*
         * Send an enable command to each sensor by writing a configuration
         * characteristic.  This is specific to the SensorTag to keep power
         * low by disabling sensors you aren't using.
         */
        private void enableNextSensor(BluetoothGatt gatt) {
            BluetoothGattCharacteristic characteristic;
            switch (mState) {
                case 0:
                    Log.d(TAG, "Enabling light");
                    characteristic = gatt.getService(LIGHT_SERVICE)
                            .getCharacteristic(LIGHT_CONFIG_CHAR);
                    characteristic.setValue(new byte[]{0x01});
                    break;
                case 1:
                    Log.d(TAG, "Enabling magneto");
                    characteristic = gatt.getService(MAGNETO_SERVICE)
                            .getCharacteristic(MAGNETO_CONFIG_CHAR);
                    characteristic.setValue(new byte[]{(byte) 0x7F, (byte) 0x00});
                    break;
                default:
                    mHandler.sendEmptyMessage(MSG_DISMISS);
                    Log.i(TAG, "All Sensors Enabled");
                    return;
            }

            gatt.writeCharacteristic(characteristic);
        }

        /*
         * Read the data characteristic's value for each sensor explicitly
         */
        private void readNextSensor(BluetoothGatt gatt) {
            BluetoothGattCharacteristic characteristic;
            switch (mState) {
                case 0:
                    Log.d(TAG, "Reading light");
                    characteristic = gatt.getService(LIGHT_SERVICE)
                            .getCharacteristic(LIGHT_DATA_CHAR);
                    break;
                case 1:
                    Log.d(TAG, "Reading Magneto");
                    characteristic = gatt.getService(MAGNETO_SERVICE)
                            .getCharacteristic(MAGNETO_DATA_CHAR);
                    break;
                default:
                    mHandler.sendEmptyMessage(MSG_DISMISS);
                    Log.i(TAG, "All Sensors Enabled");
                    return;
            }

            gatt.readCharacteristic(characteristic);
        }


        /*
         * Enable notification of changes on the data characteristic for each sensor
         * by writing the ENABLE_NOTIFICATION_VALUE flag to that characteristic's
         * configuration descriptor.
         */
        private void setNotifyNextSensor(BluetoothGatt gatt) {
            BluetoothGattCharacteristic characteristic;
            switch (mState) {
                case 0:
                    Log.d(TAG, "Set notify light");
                    characteristic = gatt.getService(LIGHT_SERVICE)
                            .getCharacteristic(LIGHT_DATA_CHAR);
                    break;
                case 1:
                    Log.d(TAG, "Set notify magneto");
                    characteristic = gatt.getService(MAGNETO_SERVICE)
                            .getCharacteristic(MAGNETO_DATA_CHAR);
                    break;
                default:
                    mHandler.sendEmptyMessage(MSG_DISMISS);
                    Log.i(TAG, "All Sensors Enabled");
                    return;
            }

            /*Enable local notifications*/
            gatt.setCharacteristicNotification(characteristic, true);
            /*Enabled remote notifications*/
            BluetoothGattDescriptor desc = characteristic.getDescriptor(CONFIG_DESCRIPTOR);
            desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(desc);
        }


        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "Connection State Change: " + status + " -> " + connectionState(newState));
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                /*
                 * Once successfully connected, we must next discover all the services on the
                 * device before we can read and write their characteristics.
                 */
                gatt.discoverServices();
                mHandler.sendMessage(Message.obtain(null, MSG_PROGRESS, "Discovering Services..."));
            } else if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_DISCONNECTED) {
                /*
                 * If at any point we disconnect, send a message to clear the weather values
                 * out of the UI
                 */
                mHandler.sendEmptyMessage(MSG_CLEAR);
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                /*
                 * If there is a failure at any stage, simply disconnect
                 */
                gatt.close();
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
            Log.i("onServicesDiscovered", services.toString());

            mHandler.sendMessage(Message.obtain(null, MSG_PROGRESS, "Enabling Sensors..."));
            /*
             * With services discovered, we are going to reset our state machine and start
             * working through the sensors we need to enable
             */
            reset();
            enableNextSensor(gatt);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i("onCharacteristicRead", characteristic.toString());
            /*For each read, pass the data up to the UI thread to update the display*/
            if (LIGHT_DATA_CHAR.equals(characteristic.getUuid())) {
                mHandler.sendMessage(Message.obtain(null, MSG_LUX, characteristic));
            }
            /*After reading the initial value, next we enable notifications*/
            setNotifyNextSensor(gatt);

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            //After writing the enable flag, next we read the initial value
            readNextSensor(gatt);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            /*
             * After notifications are enabled, all updates from the device on characteristic
             * value changes will be posted here.  Similar to read, we hand these up to the
             * UI thread to update the display.
             */
            if (LIGHT_DATA_CHAR.equals(characteristic.getUuid())) {
                mHandler.sendMessage(Message.obtain(null, MSG_LUX, characteristic));
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            //Once notifications are enabled, we move to the next sensor and start over with enable
            advance();
            enableNextSensor(gatt);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.d(TAG, "Remote RSSI: " + rssi);
        }

        private String connectionState(int status) {
            switch (status) {
                case BluetoothProfile.STATE_CONNECTED:
                    return "Connected";
                case BluetoothProfile.STATE_DISCONNECTED:
                    return "Disconnected";
                case BluetoothProfile.STATE_CONNECTING:
                    return "Connecting";
                case BluetoothProfile.STATE_DISCONNECTING:
                    return "Disconnecting";
                default:
                    return String.valueOf(status);
            }
        }
    };


    /*
     * We have a Handler to process event results on the main thread
     * MSG have random int values
     *//*
    private static final int MSG_MAGNETO = 102;*/
    private static final int MSG_LUX = 104;
    private static final int MSG_PROGRESS = 201;
    private static final int MSG_DISMISS = 202;
    private static final int MSG_CLEAR = 301;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            BluetoothGattCharacteristic characteristic;
            switch (msg.what) {
                case MSG_LUX:
                    timerLux.start();
                    characteristic = (BluetoothGattCharacteristic) msg.obj;
                    if (characteristic.getValue() == null) {
                        Log.w(TAG, "Error obtaining magneto value");
                        return;
                    }
                    getLuxValue(characteristic);
                    timerLux.getElapsedTime();
                    Log.e("Timer Lux: ", String.valueOf(timerLux.getElapsedTime()));
                    break;
                case MSG_PROGRESS:
                    mProgress.setMessage((String) msg.obj);
                    if (!mProgress.isShowing()) {
                        mProgress.show();
                    }
                    break;
                case MSG_DISMISS:
                    mProgress.hide();
                    break;
                case MSG_CLEAR:
                    clearDisplayValues();
                    break;
            }
        }
    };


    /*************************
     * Layout Functions   *
     *************************/

    public void styleChart(LineChart c) {
        /*styling*/
        c.setNoDataText("Er is geen data beschikbaar");
        c.setDescription("");

        XAxis xAxis = c.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(10f);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawLabels(false);
        YAxis yAxis = c.getAxisLeft();
        yAxis.setDrawLabels(true); // axis labels
        yAxis.setDrawAxisLine(true); // axis line
        yAxis.setDrawGridLines(false); // no grid lines
        yAxis.setDrawZeroLine(true); // draw a zero line

        c.getAxisRight().setEnabled(false); // no right axis
        c.animateX(3000);
    }


    /*************************
     * Lux Calculation Functions *
     *************************/

    public void getLuxValue(BluetoothGattCharacteristic c) {
        amountEnergy = 5000f; //Initialize amount of starting energy in watts. (500000 = 500kW)
        Log.e("test", "GetLuxVALUEMETHOD");   //Get Light intensity
        byte[] value = c.getValue();
        int mantissa;
        int exponent;
        Integer sfloat = shortUnsignedAtOffset(value, 0);  //data from sensor
        mantissa = sfloat & 0x0FFF;
        exponent = (sfloat & 0xF000) >> 12;
        double magnitude = pow(2.0f, exponent);
        outputLux = mantissa * (0.01 * magnitude);   //New output lux value
        calculatePowerUsage(outputLux);
    }

    public void calculatePowerUsage(double outputLux) {
        Intent intention = getIntent();
        Classroom classroom = (Classroom)intention.getSerializableExtra("classroomObject");
        double power = calculateWattFromLux(outputLux);   //Current power consumption
        powerTotal += power;   //Getting total power, "+", this will be reduced from the "energyLeft" variable, power is CONSUMED by the lights.

        if ((double) i != 0.0) {
            averagePowerUsage = powerTotal / ((double) i);
        }
        //Log.e("Powser: ", String.valueOf(power));
        dataSet.setFillColor(R.color.colorAccent);
        startDataSet.setFillColor(R.color.colorPrimaryLight);
        energyLeft = amountEnergy - (powerTotal * 0.0002777);
        dataSet.addEntry(new Entry((float) i, (float) energyLeft));
        startDataSet.addEntry(new Entry((float) i, (float) amountEnergy)); //Adding entry: using total power consumption.
        lineData.notifyDataChanged(); // let the data know a dataSet changed
        chart.notifyDataSetChanged(); // let the chart know its data changed
        chart.invalidate(); // refresh
        i++; //Every second i gets increased

        /**
         * Every 10 minutes, the data should be updated.
         * energyLeft variable should be stored in dB
         */
        if (i % 10 == 0) {

            /**
             * Sync to SQLite dB
             */
            DatabaseHelper dbh = new DatabaseHelper(this);
            for(int i=0;i<dbh.getAllClassrooms().size();i++){
                Log.e("naamresgistratie: " , naamRegistratie);
                Log.e("classrooms: " , dbh.getAllClassrooms().get(i).getGroepsnaam());
                if(dbh.getAllClassrooms().get(i).getGroepsnaam().equals(naamRegistratie)){
                    Log.e("energy left : " , String.valueOf(energyLeft));
                    dbh.updateHighscore(classroom, naamRegistratie, String.valueOf(energyLeft));
                    Log.e("Highscore is: ", dbh.getAllClassrooms().get(i).getHighscore());
                    Log.e("Groepsnaam= ", classroom.getGroepsnaam());
                }
            }

            Log.e("Energy left: ", String.valueOf(classroom.getHighscore()));

            //To implement
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                //Fetch data
                JSONArray json = buildJSONArray();
                String url = "http://thinkcore.be/sync/TESTPHP3.php";
                new SendSQLiteData().execute(url, json.toString());
            } else {
                Log.d("CONNECTION", "There are connection issues");
            }
        }
       // textMagneto.setText("Verbruik op dit moment: " + String.valueOf(power) + " W" + "\n" + "Gemiddeld verbruik: " + String.valueOf(averagePowerUsage) + " W" + "\n" + "Lux op dit moment: " + String.valueOf(outputLux)/*+ "\n" + "Time passed: " + String.valueOf(timePassed)*/);
    }

    private JSONArray buildJSONArray() {
        DatabaseHelper dbh = new DatabaseHelper(this);
        ArrayList<Classroom> list = dbh.getAllClassrooms();
        JSONArray classArray = new JSONArray();
        for(int i = 0; i < list.size(); i++){
            JSONObject classroomjson = new JSONObject();
            try {
                classroomjson.put("Schoolname", list.get(i).getGroepsnaam());
                classroomjson.put("Classname", list.get(i).getClassname());
                classroomjson.put("Highscore", list.get(i).getHighscore());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            classArray.put(classroomjson);
        }
        Log.d("JSONArray", classArray.toString());
        return classArray;
    }

    private class SendSQLiteData extends AsyncTask<String, String, String>{

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection con = null;
            try {
                URL url = new URL(params[0]);
                con = (HttpURLConnection) url.openConnection();
                con.setReadTimeout(10000 /*milliseconds*/);
                con.setConnectTimeout(15000 /* milliseconds */);
                con.setDoInput(true);
                con.setDoOutput(true);
                con.setRequestMethod("POST");
                con.setFixedLengthStreamingMode(params[1].length());
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Accept", "application/json");

                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                Log.d("WRITER", params[1]);
                writer.write(params[1]);
                writer.flush();
                writer.close();

                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String result = "";
                String echo = "";
                while((echo = br.readLine()) != null){
                    Log.d("RESULT", echo);
                }
                br.close();

                if(con.getResponseCode() == HttpURLConnection.HTTP_OK){
                    Log.d("CONNECTION", con.getResponseMessage());
                }

                return "Found connection";
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (con != null) {
                    con.disconnect();
                }
            }
            return null;
        }
    }


    /*********************************************************************
     * Get Watt from lux.
     * Fluorescent lamp: 60lm/W
     * Formula P(W) = Ev(lx) × A(m2) / η(lm/W)
     * 60 because average TL-Lamp has a 60 efficiency.
     * Source: http://www.rapidtables.com/calc/light/lux-to-watt-calculator.htm
     *********************************************************************/

    public double calculateWattFromLux(double outputValueOfLux) {
        double power = 0.0;
        minLuxValue = 30.0; //Can be changed. Hardcoded to 200 lux. (0-200 Off <-> 200-inf On)
        powerUsageLamp = 28.0; //http://www.t5-adapter.nl/Webwinkel-Page-148527/Informatie.html#.WDMKQ9XhCUk. TL-5.
        if (outputValueOfLux > minLuxValue)
            power = (double) aantalLampenRegistratie * powerUsageLamp; //Counting power usage of lamps, using lowest energy wattage for tl-5 lamps; depending on the amount of lamps in the classroom.
        return power;
    }

    private static Integer shortUnsignedAtOffset(byte[] c, int offset) {
        Integer lowerByte = (int) c[offset] & 0xFF;
        Integer upperByte = (int) c[offset + 1] & 0xFF;
        return (upperByte << 8) + lowerByte;
    }

    public void goToPlayground(View v) {
        showDistanceInput();
    }

    public void showDistanceInput() {
        View v = getLayoutInflater().inflate(R.layout.input_popup, null);
        AlertDialog.Builder adb = new AlertDialog.Builder(GraphActivity.this);
        adb.setView(v);
        inputValue = (EditText) v.findViewById(R.id.editText);
        toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        toolbar.setTitle("Hoeveel energie in kWh hebben jullie opgewekt?");
        toolbar.setTitleTextColor(Color.WHITE);
        AlertDialog alert = adb.create();
        alert.show();
    }

    public void stopGame() {

        new AlertDialog.Builder(this)
                .setTitle("Stop spel?")
                .setMessage("Ben je zeker dat je het spel wilt stoppen?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Ja", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        onStop(); //disconnect tag sensor
                        getApplicationContext().getSharedPreferences("MainActivity", 0).edit().clear().commit(); //clear preferences
                        //Start new activity
                        Intent showActivity = new Intent(GraphActivity.this, EndResultActivity.class);
                        startActivity(showActivity);
                        //syncen met SQLite
                        //naar dB
                    }
                })
                .setNegativeButton("Neen", null).show();
    }

    public void powerInputToGraph(View v) {
        double inputPower = 0.0;
        try {
            String text = inputValue.getText().toString();
            if (!text.equals("")) {
                inputPower = Double.parseDouble(text);
                Toast.makeText(getApplicationContext(), "" + inputPower + "Dit Is De Toaster Text", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Null object", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        powerTotal -= inputPower / 0.0002777;

    }

}

