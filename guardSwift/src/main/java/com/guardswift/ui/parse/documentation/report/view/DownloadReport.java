package com.guardswift.ui.parse.documentation.report.view;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;
import com.guardswift.BuildConfig;
import com.guardswift.R;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.persistence.parse.documentation.report.Report;
import com.guardswift.rest.GuardSwiftServer;
import com.parse.GetCallback;
import com.parse.ParseException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by cyrixmorten on 13/11/2016.
 */

public class DownloadReport {

    public interface CompletedCallback {
        void done(File file, Error e);
    }

    private static String TAG = DownloadReport.class.getSimpleName();


    private Context context;
    private Dialog dialog;

    public DownloadReport(Context context) {
        this.context = context;
    }

    private void showDialog() {
        if (context == null) {
            return;
        }
        dialog = new MaterialDialog.Builder(context)
                .title(R.string.working)
                .content(R.string.fetching_report)
                .progress(true, 0)
                .show();
    }

    private void dismissDialog() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    public void execute(String reportId, final CompletedCallback callback) {
        Report.getQueryBuilder(false).matching(reportId).build().getFirstInBackground(new GetCallback<Report>() {
            @Override
            public void done(Report report, ParseException e) {
                execute(report, callback);
            }
        });
    }

    public void execute(Report report, final CompletedCallback callback) {

        if (report == null) {
            callback.done(null, new Error("Report is null"));
            return;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GuardSwiftServer.API_URL)
                .build();

        GuardSwiftServer.API guardSwift = retrofit.create(GuardSwiftServer.API.class);

        guardSwift.report(report.getObjectId()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    new DownloadFileAsyncTask(callback).execute(response.body().byteStream());
                } else {
                    callback.done(null, new Error("Error generating report"));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                new HandleException("Report PDF", "Error generating PDF", t);
                callback.done(null, new Error("Error connecting to server"));
            }
        });
    }


    private class DownloadFileAsyncTask extends AsyncTask<InputStream, Void, File> {

        final String appDirectoryName = BuildConfig.APPLICATION_ID;
        final File fileRoot = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), appDirectoryName);
        final String filename = "report.pdf";

        CompletedCallback callback;

        DownloadFileAsyncTask(CompletedCallback callback) {
            this.callback = callback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            showDialog();
        }

        @Override
        protected File doInBackground(InputStream... params) {


            InputStream inputStream = params[0];
            File file = new File(fileRoot, filename);

            OutputStream output = null;
            try {

                file.mkdirs();

                if (file.exists()) {
                    file.delete();
                }

                file.createNewFile();

                output = new FileOutputStream(file);

                byte[] buffer = new byte[1024]; // or other buffer size
                int read;

                Log.d(TAG, "Attempting to write to: " + fileRoot + "/" + filename);
                while ((read = inputStream.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                    Log.v(TAG, "Writing to buffer to output stream. " + Arrays.toString(buffer));
                }
                Log.d(TAG, "Flushing output stream.");
                output.flush();
                Log.d(TAG, "Output flushed.");
            } catch (IOException e) {
                Log.e(TAG, "IO Exception: " + e.getMessage());
                e.printStackTrace();
                return null;
            } finally {
                try {
                    if (output != null) {
                        output.close();
                        Log.d(TAG, "Output stream closed sucessfully.");
                    } else {
                        Log.d(TAG, "Output stream is null");
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Couldn't close output stream: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            return file;
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);

            callback.done(file, null);

            dismissDialog();
        }
    }
}


