package com.guardswift.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.guardswift.ui.GuardSwiftApplication;

import java.io.File;

public class UpdateApp extends AsyncTask<File,Void,Void> {

    private Context context;

    public static void fromFile(File file) {
        new UpdateApp().execute(file);
    }

    UpdateApp() {
        this.context = GuardSwiftApplication.getInstance();
    }

    @Override
    protected Void doInBackground(File... arg0) {
        try {
            File fromFile = arg0[0];

            String PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
            String FILENAME = "update.apk";
            File file = new File(PATH);
            file.mkdirs();
            File outputFile = new File(file, FILENAME);

            FileIO.copyFile(fromFile, outputFile);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(outputFile), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // without this flag android returned a intent error!
            context.startActivity(intent);


        } catch (Exception e) {
            Log.e("UpdateAPP", "Update error! " + e.getMessage());
        }
        return null;
    }
}
