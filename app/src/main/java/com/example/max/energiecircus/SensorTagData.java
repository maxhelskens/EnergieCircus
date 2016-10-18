package com.example.max.energiecircus;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;

/**
 * Created by Max on 30/08/16.
 */
public class SensorTagData {

    public static double extractMagnetoData(BluetoothGattCharacteristic c) {

        /*MAGNETO*/
        byte[] value = new byte[0];

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            value = c.getValue();
        }

        int x = 0,y = 0,z = 0;

        final float SCALE = (float) (32768 / 4912);
        if (value.length >= 18) {
            x = (value[13] << 8) + value[12];
            y = (value[15] << 8) + value[14];
            z = (value[17] << 8) + value[16];
        }

        return 1.0 * y;
    }


    /*HUMIDITY*/

  /*  @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private static Integer shortUnsignedAtOffset(BluetoothGattCharacteristic c, int offset) {
        Integer lowerByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
        Integer upperByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 1); // Note: interpret MSB as unsigned.

        return (upperByte << 8) + lowerByte;
    }*/


}
