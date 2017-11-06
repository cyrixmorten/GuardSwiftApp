package com.guardswift.util;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.util.Log;


public class RFIDUtils {

    private static final String TAG = RFIDUtils.class.getSimpleName();

    private static String byteArrayToHexString(byte[] inarray)
    {
        int i, j, in;
        String [] hex = {"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};
        String out= "";

        for(j = 0 ; j < inarray.length ; ++j)
        {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }

    public static String readIdFromIntent(Intent intent) {
        String stringId = "";
        if (intent != null) {
            boolean NFC_NDEF_DISCOVERED = NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction());
            boolean NFC_TAG_DISCOVERED = NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction());
            boolean NFC_TECH_DISCOVERED = NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction());

            if (NFC_NDEF_DISCOVERED || NFC_TAG_DISCOVERED || NFC_TECH_DISCOVERED) {

                byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);

                stringId = RFIDUtils.byteArrayToHexString(id);
            }
        }

        Log.d(TAG, "RFID: " + stringId);

        return stringId;
    }
}
