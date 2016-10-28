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
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.lang.Math.pow;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class GraphActivity extends AppCompatActivity {

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

    // Use of graph: https://github.com/PhilJay/MPAndroidChart/wiki
    private LineChart chart;
    private List<Entry> entries;
    private LineDataSet dataSet;

    private List<Entry> startEntries;
    private LineDataSet startDataSet;

    private LineData lineData;
    private TextView textMagneto;

    /*Lux*/
    private int i = 0;

    /*Needed for magneto*/
    private int totalTurns = 0;
    private int finished = 0;
    private int prevFinished = 0;
    private Stopwatch timerLux = new Stopwatch();
   // private Stopwatch turnTimer = new Stopwatch();
    private double outputLux;
    private double amountEnergy;
    private double powerTotal = 0;
    private double energyLeft;
    private double averagePowerUsage;

    private EditText inputValue;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        /************
         *    BLE   *
         ************/

        setProgressBarIndeterminate(true);

        //Check if BLE is supported
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


        chart = (LineChart) findViewById(R.id.chart);
        styleChart(chart);

        entries = new ArrayList<Entry>();

        dataSet = new LineDataSet(entries, "Lux"); // add entries to dataset
        dataSet.setDrawValues(false);
        dataSet.setDrawCircles(false);
        dataSet.setFillColor(R.color.colorAccent);
        dataSet.setFillAlpha(0);

        startEntries = new ArrayList<Entry>();

        startDataSet = new LineDataSet(startEntries, "Lux"); // add entries to dataset
        startDataSet.setDrawValues(false);
        startDataSet.setDrawCircles(false);
        startDataSet.setFillColor(R.color.colorPrimaryLight);
        startDataSet.setFillAlpha(0);

        lineData = new LineData(dataSet, startDataSet);
        chart.setData(lineData);
        chart.invalidate(); // refresh

        //textMagneto
        textMagneto = (TextView) findViewById(R.id.magnetoValue);



    }


    /**********************
     * BLE Functions   *
     **********************/

    @Override
    protected void onResume() {
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

        //Make sure dialog is hidden
        mProgress.dismiss();
        //Cancel any scans in progress
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
        //Disconnect from any active tag connection
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
                //Bluetooth not enabled.
                finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        //Add any device elements we've discovered to the overflow menu
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
                //scanLeDevice(true);

                return true;
            default:
                //Obtain the discovered device to connect with
                BluetoothDevice device = mDevices.get(item.getItemId());
                Log.i(TAG, "Connecting to " + device.getName());
                /*
                 * Make a connection with the device
                 */

                connectToDevice(device);
                //Display progress UI
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
                    //Update the overflow menu
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
            //Update the overflow menu
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

    private void clearDisplayValues(){
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
                    characteristic.setValue(new byte[] {(byte)0x7F, (byte)0x00});
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

            //Enable local notifications
            gatt.setCharacteristicNotification(characteristic, true);
            //Enabled remote notifications
            BluetoothGattDescriptor desc = characteristic.getDescriptor(CONFIG_DESCRIPTOR);
            desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(desc);
        }


        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "Connection State Change: "+status+" -> "+connectionState(newState));
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
            //For each read, pass the data up to the UI thread to update the display
            if (LIGHT_DATA_CHAR.equals(characteristic.getUuid())) {
                mHandler.sendMessage(Message.obtain(null, MSG_LUX, characteristic));
            }
            if(MAGNETO_DATA_CHAR.equals(characteristic.getUuid())) {
                mHandler.sendMessage(Message.obtain(null, MSG_MAGNETO, characteristic));
            }

            //After reading the initial value, next we enable notifications
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
            if(MAGNETO_DATA_CHAR.equals(characteristic.getUuid())){
                mHandler.sendMessage(Message.obtain(null, MSG_MAGNETO, characteristic));
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
     */
    private static final int MSG_MAGNETO = 102;
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
                case MSG_MAGNETO:
                    characteristic = (BluetoothGattCharacteristic) msg.obj;
                    if (characteristic.getValue() == null) {
                        Log.w(TAG, "Error obtaining magneto value");
                        return;
                    }
                    updateMagnetoValues(characteristic);
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

    private void updateMagnetoValues(BluetoothGattCharacteristic characteristic){
        double magneto = SensorTagData.extractMagnetoData(characteristic);
        magneto=Math.abs(magneto);
       // textMagneto.setText(String.format("Magneet waarde:", magneto));
        Log.e("test","getMagnetoValueMethod");
    }

    public void getLuxValue(BluetoothGattCharacteristic c) {
       // timerLux.start();
        //Initialize amount of starting energy in watts. (500000 = 500kW)
        amountEnergy = 500000f;
        //Previous power
        double previousPower = calculateWattFromLux(outputLux);
        //Get Light intensity
        Log.e("test","GetLuxVALUEMETHOD");
        byte[] value = c.getValue();
        int mantissa;
        int exponent;
        //data from sensor
        Integer sfloat = shortUnsignedAtOffset(value, 0);
        mantissa = sfloat & 0x0FFF;
        exponent = (sfloat & 0xF000) >> 12;
        double magnitude = pow(2.0f, exponent);
        //New output lux value
        outputLux = mantissa * (0.01*magnitude);
        //Current power consumption
        double power = calculateWattFromLux(outputLux);
        //Getting total power, summing previous and current power constantly.
        powerTotal += power;

        if((double) i !=0.0){
            averagePowerUsage = powerTotal/((double) i);
        }

        Log.e("Power: ", String.valueOf(power));
        /*Set entry and intialize graph*/
        //Instant power consumption
        dataSet.setFillColor(R.color.colorAccent);
        startDataSet.setFillColor(R.color.colorPrimaryLight);
        //Adding entry: using total power consumption.
        energyLeft = amountEnergy - (powerTotal * 0.000222);
        dataSet.addEntry(new Entry((float) i, (float) energyLeft));
        startDataSet.addEntry(new Entry((float) i, (float) amountEnergy));
        lineData.notifyDataChanged(); // let the data know a dataSet changed
        chart.notifyDataSetChanged(); // let the chart know its data changed
        chart.invalidate(); // refresh
        i++;
       // long timePassed = timerLux.getElapsedTime();
        textMagneto.setText("Verbruik op dit moment: " + String.valueOf(power) + " W"+"\n" + "Gemiddeld verbruik: " + String.valueOf(averagePowerUsage) + " W" + "\n" + "Lux op dit moment: " + String.valueOf(outputLux)/*+ "\n" + "Time passed: " + String.valueOf(timePassed)*/);


    }

    public double calculateWattFromLux(double outputValueOfLux){
         /*Get Watt from lux
        Fluorescent lamp: 60lm/W
        Formula P(W) = Ev(lx) × A(m2) / η(lm/W)*/
        SharedPreferences prefs = getSharedPreferences("RegistrationActivity",0);
        int klasOppervlakteRegistratie = prefs.getInt("KlasOpp", 0);
        /*60 omdat een gemiddelde TL-Lamp 60 efficientie heeft.
        Source: http://www.rapidtables.com/calc/light/lux-to-watt-calculator.htm*/
        double power = (outputValueOfLux*(double)klasOppervlakteRegistratie)/60.0;
        return power;
    }


    /**Magneto data**/

   /* ArrayList<Double> magnets = new ArrayList<>();
    double average = 0;
    int wait5 = 0;
    long prevTurnTimer = 0;*/
    private void updateValues(BluetoothGattCharacteristic characteristic) {

       /* if (totalTurns < 471) {
            //if (totalTurns < 5) { // to Test
            double magnet = SensorTagData.extractMagnetoX(characteristic);
            magnet = Math.abs(magnet);

            if (wait5 == 0) {

                if (magnet > average + 300 ||  magnet < average - 300) {
                    if(totalTurns == 1){
                        timer.start();
                        turnTimer.start();
                    }

                    Boolean skippedTurn = false;
                    turnTimer.stop();
                    if(turnTimer.getElapsedTime() < 3 * prevTurnTimer && turnTimer.getElapsedTime() > 1.5 * prevTurnTimer){
                        skippedTurn = true;
                    }
                    else {
                        prevTurnTimer = turnTimer.getElapsedTime();
                    }
                    turnTimer.start();

                    if(skippedTurn) {
                        totalTurns ++;
                    }

                    wait5 = 6;

                    totalTurns ++;

                    ViewGroup.MarginLayoutParams lpimg = (ViewGroup.MarginLayoutParams) img.getLayoutParams();
                    lpimg.leftMargin = (int) (Math.round(totalTurns * 1.7) -100);
                    img.setLayoutParams(lpimg);

                    //mTurns.setText("" + totalTurns);
                    mDist.setText((int)(totalTurns * 2.125) + " m");

                }
            }
            else {
                wait5 --;
            }

            if (magnets.size() >= 5) {
                magnets.remove(0);
            }
            magnets.add(magnet);
            double sum = 0;
            for (int i = 0; i < magnets.size(); i++) {
                sum += magnets.get(i);
            }
            average = sum/magnets.size();

            //TODO: reset value
            if(totalTurns % 94 == 0) {
                //if(totalTurns % 3 == 0) { //To test
                addStar(totalTurns/94);
                //addStar(totalTurns/3); //To test
            }
        }
        else {
            //finished
            finished = 1;
            if (finished == 1 && prevFinished == 0) {
                prevFinished = 1;

                timer.stop();

                showDialog();
            }
        }*/
    }

    private static Integer shortUnsignedAtOffset(byte[] c, int offset) {
        Integer lowerByte = (int) c[offset] & 0xFF;
        Integer upperByte = (int) c[offset + 1] & 0xFF;
        return (upperByte << 8) + lowerByte;
    }


    /*************************
     * Layout Functions   *
     *************************/

    public void styleChart(LineChart c) {
        //styling
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

    public void goToPlayground(View v) {
        //disconnect current tag connection
        onStop();
        //Shows toolbar with bluetooth icon
        //showDialogue();
        showDistanceInput();
    }

    public void showDistanceInput(){
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


    public void powerInputToGraph(View v){
        double inputPower = 0.0;
        try {
            String text = inputValue.getText().toString();
            if (!text.equals("")) {
                inputPower = Double.parseDouble(text);
                Toast.makeText(getApplicationContext(), "" + inputPower, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Null object", Toast.LENGTH_LONG).show();
            }
        }catch (Exception e){
            System.out.println(e);
        }


        powerTotal -= inputPower;

        dataSet.setFillColor(R.color.colorAccent);
        startDataSet.setFillColor(R.color.colorPrimaryLight);
        //Adding entry: using total power consumption.
        energyLeft = amountEnergy - powerTotal;
        dataSet.addEntry(new Entry((float) i, (float) energyLeft));
        startDataSet.addEntry(new Entry((float) i, (float) amountEnergy));
        lineData.notifyDataChanged(); // let the data know a dataSet changed
        chart.notifyDataSetChanged(); // let the chart know its data changed
        chart.invalidate(); // refresh
        i++;

    }

    public void showDialogue() {
        /*Nog bluetooth icoontje toevoegen*/
        //Set popupscherm as view
        View v = getLayoutInflater().inflate(R.layout.popupscherm,null);
        AlertDialog.Builder adb = new AlertDialog.Builder(GraphActivity.this);
        adb.setView(v);
        //Find the toolbar view inside the activity layout

        /*because view v has the popupscherm.xml view, you have to put "v.method();"
        otherwise with this findViewById(R.id.toolbar);
        you are trying to find the toolbar in the current set layout of activity
        which is different from popupscherm.xml*/
        toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        toolbar.setTitle("Ga naar de SMERGY fietsen");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.inflateMenu(R.menu.menu_main);
        AlertDialog alert = adb.create();
        alert.show();
    }
}

