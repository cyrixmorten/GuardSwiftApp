package com.guardswift.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.guardswift.BuildConfig;
import com.guardswift.core.exceptions.HandleException;

import java.io.File;
import java.lang.ref.WeakReference;

public class UpdateApp extends AsyncTask<File,Void,File> {

    private static String TAG = UpdateApp.class.getSimpleName();


    public static void fromFile(Context context, File file) {
        Log.i(TAG, "Start update");

        new UpdateApp(context.getApplicationContext()).execute(file);
    }

    private WeakReference<Context> activityWeakReference;

    UpdateApp(Context context) {
        activityWeakReference = new WeakReference<>(context);
    }

    @Override
    protected File doInBackground(File... arg0) {
        try {
            File fromFile = arg0[0];

            String PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
            String FILENAME = "update.apk";
            File file = new File(PATH);

            boolean dirCreated = file.mkdirs();
            Log.i(TAG, "dirCreated: " + dirCreated);

            if (file.exists()) {
                boolean fileDeleted = file.delete();
                Log.i(TAG, "fileDeleted: " + fileDeleted);
            }

            File outputFile = new File(file, FILENAME);

            FileIO.copyFile(fromFile, outputFile);

            return outputFile;
        } catch (Exception e) {
            new HandleException(TAG, "Update app apk", e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(File file) {
        super.onPostExecute(file);

        Log.i(TAG, "onPostExecute: " + file.exists());

        Context context = activityWeakReference.get();
        if (context != null) {
            try {

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    Uri uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);

                    Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                    intent.setData(uri);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    context.startActivity(intent);
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // without this flag android returned a intent error!
                    context.startActivity(intent);
                }



            } catch (Exception e) {
                Log.i(TAG, "error");
                new HandleException(TAG, "Start update apk activity", e);
            }
        }
    }
}
