package com.etjaal.arabicalmanac;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Dictionary;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by Uwais on 13/01/2017.
 */
public class UnzipTask extends AsyncTask<Void,Void,String> {

    private Context context;
    private ProgressDialog unzipProgressDialog;
    private String path;

    public UnzipTask(Context context,ProgressDialog progressDialog) {
        this.context = context;
        this.unzipProgressDialog = progressDialog;
        path = Environment.getExternalStorageDirectory().toString() + "/"
                + context.getResources().getString(R.string.app_name) + "/";
    }

    @Override
    protected void onPreExecute() {
        unzipProgressDialog.show();
    }

    @Override
    protected String doInBackground(Void... params) {
        Unzip(path + "hw4.zip", path);
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        unzipProgressDialog.dismiss();
        Toast.makeText(HansWehrApplication.getAppContext(), "Offline mode is now enabled", Toast.LENGTH_SHORT).show();
        File file = new File(path,"hw4.zip");
        file.delete();
        File f = new File(path, ".nomedia");
        if (!f.exists()) {
            boolean created = f.mkdir();
            Log.d("Unzip", ".nomedia " + String.valueOf(created));
        }
    }

    public static void Unzip(String zipFile, String location) {
        Log.d("Unzip", "Unzipping");
        int BUFFER_SIZE = 1024;
        int count = 0;
        byte[] buffer = new byte[BUFFER_SIZE];

        try {
            if (!location.endsWith("/")) {
                location += "/";
            }
            File f = new File(location);
            if (!f.isDirectory()) {
                f.mkdirs();
            }

            ZipInputStream zin = new ZipInputStream(new BufferedInputStream(
                    new FileInputStream(zipFile), BUFFER_SIZE));
            try {
                ZipEntry ze = null;
                while ((ze = zin.getNextEntry()) != null) {
                    String path = location + ze.getName();
                    File unzipFile = new File(path);

                    if (ze.isDirectory()) {
                        if (!unzipFile.isDirectory()) {
                            unzipFile.mkdirs();
                        }
                    } else {
                        // check for and create parent directories if
                        // they don't
                        // exist
                        File parentDir = unzipFile.getParentFile();
                        if (null != parentDir) {
                            if (!parentDir.isDirectory()) {
                                parentDir.mkdirs();
                            }
                        }

                        // unzip the file
                        FileOutputStream out = new FileOutputStream(unzipFile,
                                false);
                        BufferedOutputStream fout = new BufferedOutputStream(
                                out, BUFFER_SIZE);
                        try {
                            while ((count = zin.read(buffer, 0, BUFFER_SIZE)) != -1) {
                                fout.write(buffer, 0, count);
                            }

                            zin.closeEntry();
                        } finally {
                            fout.flush();
                            fout.close();

                        }
                    }
                }
            } finally {
                zin.close();
            }
        } catch (Exception e) {
            Log.e("Unzip", "Unzip exception", e);
        }

    }
}