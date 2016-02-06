package com.guardswift.util;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by cyrix on 10/11/15.
 */
public class FileIO {

    private static final String TAG = FileIO.class.getSimpleName();

    /**
     * @param context
     * @param filename
     * @param mode
     * @param data Context.MODE_*
     */
    public static void writeToFile(Context context, String filename, int mode, String data) throws IOException {
        Log.d(TAG, "writeToFile: " + filename + " - " + data);
        if (filename == null || filename.isEmpty())
            return;

        OutputStreamWriter outputStreamWriter = null;
        try {
            outputStreamWriter = new OutputStreamWriter(context.openFileOutput(filename, mode));
            outputStreamWriter.write(data);
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
            throw e;
        } finally {
            if (outputStreamWriter != null) {
                outputStreamWriter.flush();
                outputStreamWriter.close();
            }
        }
    }


    public static String readFromFile(Context context, String filename) throws IOException {
        Log.d(TAG, "readFromFile: " + filename);
        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(filename);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
            throw e;
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
            throw e;
        }

        Log.d(TAG, "readFromFile: " + ret.length());

        return ret;
    }

}
