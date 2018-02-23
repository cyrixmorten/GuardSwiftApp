package com.guardswift.util;

import android.content.Context;
import android.util.Log;

import com.guardswift.core.exceptions.HandleException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by cyrix on 10/11/15.
 */
public class FileIO {

    private static final String TAG = FileIO.class.getSimpleName();

    /**
     * @param context
     * @param filename
     * @param mode
     * @param data     Context.MODE_*
     */
    public static void writeToFile(Context context, String filename, int mode, String data) throws IOException {
        Log.d(TAG, "writeToFile: " + filename + " - " + data);
        if (filename == null || filename.isEmpty())
            return;

        OutputStreamWriter outputStreamWriter = null;
        try {
            outputStreamWriter = new OutputStreamWriter(context.openFileOutput(filename, mode));
            outputStreamWriter.write(data);
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

        InputStream inputStream = null;

        try {
            inputStream = context.openFileInput(filename);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString;
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            new HandleException(TAG, "File not found", e);
        } catch (IOException e) {
            new HandleException(TAG, "Could not read file", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    new HandleException(TAG, "Close input stream", e);
                }
            }
        }


        return ret;
    }

    public static byte[] compress(String string) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream(string.length());
        GZIPOutputStream gos = new GZIPOutputStream(os);
        gos.write(string.getBytes());
        gos.close();
        byte[] compressed = os.toByteArray();
        os.close();
        return compressed;
    }

    public static String decompress(byte[] compressed) throws IOException {
        final int BUFFER_SIZE = 32;
        ByteArrayInputStream is = new ByteArrayInputStream(compressed);
        GZIPInputStream gis = new GZIPInputStream(is, BUFFER_SIZE);
        StringBuilder string = new StringBuilder();
        byte[] data = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = gis.read(data)) != -1) {
            string.append(new String(data, 0, bytesRead));
        }
        gis.close();
        is.close();
        return string.toString();
    }

    public static void copyFile(File src, File dst) throws IOException {
        FileInputStream var2 = new FileInputStream(src);
        FileOutputStream var3 = new FileOutputStream(dst);
        byte[] var4 = new byte[1024];

        int var5;
        while((var5 = var2.read(var4)) > 0) {
            var3.write(var4, 0, var5);
        }

        var2.close();
        var3.close();
    }
}
