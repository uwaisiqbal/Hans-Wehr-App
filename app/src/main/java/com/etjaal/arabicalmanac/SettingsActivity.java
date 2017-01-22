package com.etjaal.arabicalmanac;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.etjaal.arabicalmanac.util.IabHelper;
import com.etjaal.arabicalmanac.util.IabResult;
import com.etjaal.arabicalmanac.util.Inventory;

/**
 * Created by Uwais on 09/01/2017.
 */
public class SettingsActivity extends PreferenceActivity {

    Preference restorePurchases;
    Preference clearCache;
    Preference clearSearchHistory;
    CheckBoxPreference offlineMode;
    ProgressDialog dialog;
    String downloadUrl = "https://firebasestorage.googleapis.com/v0/b/arabic-almanac-hans-wehr.appspot.com/o/hw4.zip?alt=media&token=67f33cdf-b015-4ae2-97a1-f6751113720d";
    long downloadId;
    DownloadManager downloadManager;
    ProgressDialog progressDialog;
    BroadcastReceiver receiver;

    @Override
    protected void onDestroy() {
        try {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //ActionBar actionBar = getSupportActionBar();
        //actionBar.setDisplayHomeAsUpEnabled(true);
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        dialog = new ProgressDialog(this);
        dialog.setTitle("Clearing Cache...");
        dialog.setMessage("Please Wait");
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);

        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        root.addView(bar, 0); // insert at top
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        addPreferencesFromResource(R.xml.settings);
        restorePurchases = (Preference) findPreference("restore_purchases_pref");
        restorePurchases.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                try {
                    HansWehrApplication.mHelper.queryInventoryAsync(new IabHelper.QueryInventoryFinishedListener() {
                        @Override
                        public void onQueryInventoryFinished(IabResult result, Inventory inv) {
                            if (inv.hasPurchase(HansWehrApplication.SKU_REMOVE_ADS)) {
                                if (result.isSuccess()) {
                                    HansWehrApplication.editor.putBoolean(HansWehrApplication.removeAdsKey, true);
                                    HansWehrApplication.editor.commit();
                                    Toast.makeText(getApplicationContext(),
                                            "Purchase of Remove Ads has been restored! Restart the application for the changes to take effect.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(),"There are no previous purchases to restore", Toast.LENGTH_SHORT).show();
                            }


                        }
                    });
                } catch (IabHelper.IabAsyncInProgressException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });

        clearCache = (Preference) findPreference("clear_cache_pref");
        clearCache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new ClearCacheTask(getApplicationContext(), dialog).execute();
                return true;
            }
        });

        clearSearchHistory = (Preference) findPreference("clear_search_history_pref");
        clearSearchHistory.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getApplicationContext(), MySuggestionProvider.AUTHORITY,
                        MySuggestionProvider.MODE);
                suggestions.clearHistory();
                Toast.makeText(getApplicationContext(),"Search history successfully cleared",Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        offlineMode = (CheckBoxPreference) findPreference("offline_mode_pref");


        if(offlineMode.isChecked()){
            offlineMode.setEnabled(false);
        }else{
            //Check for permissions on Android 6.0 devices and above

            offlineMode.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if(offlineMode.isChecked()) {
                        if(Build.VERSION.SDK_INT >= 23){
                            if (checkPermission()) {
                                // Code for above or equal 23 API Oriented Device
                                // Your Permission granted already .Do next code
                                showDownloadDialogPrompt();
                            } else {
                                requestPermission(); // Code for permission
                            }
                        }else {
                            showDownloadDialogPrompt();
                        }
                    }
                    return true;
                }
            });
        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(SettingsActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(SettingsActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(SettingsActivity.this, "Please allow permission for this app in Settings to download and install files for offline mode.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 500);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 500:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                    Toast.makeText(SettingsActivity.this, "Permissions granted", Toast.LENGTH_LONG).show();
                    showDownloadDialogPrompt();

                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                    Toast.makeText(SettingsActivity.this, "Please enable permission to download and install files", Toast.LENGTH_LONG).show();
                    offlineMode.setChecked(false);
                }
                break;
        }
    }

    private void showDownloadDialogPrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setTitle("Download Files");
        builder.setMessage("Please click proceed to begin the download of the necessary files");
        builder.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                setupDownloadBroadcastReceiver();
                startDownloadProcess();
            }
        });
        builder.setCancelable(false);
        builder.create().show();
    }

    private void setupDownloadBroadcastReceiver(){
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    long downloadId = intent.getLongExtra(
                            DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(downloadId);
                    Cursor c = downloadManager.query(query);
                    if (c.moveToFirst()) {
                        int columnIndex = c
                                .getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (DownloadManager.STATUS_SUCCESSFUL == c
                                .getInt(columnIndex)) {
                            offlineMode.setEnabled(false);
                            progressDialog.dismiss();
                            progressDialog = new ProgressDialog(SettingsActivity.this);
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                            progressDialog.setIndeterminate(true);
                            progressDialog.setTitle("Install in progress ...");
                            progressDialog.setMessage("Installing Hans Wehr Files");
                            progressDialog.setProgressNumberFormat(null);
                            progressDialog.setProgressPercentFormat(null);
                            progressDialog.setCancelable(false);
                            UnzipTask task = new UnzipTask(getApplicationContext(), progressDialog);
                            task.execute();
                        }
                    }
                }
            }
        };

        registerReceiver(receiver, new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private void startDownloadProcess() {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
        request.setDescription("Download in Progress");
        request.setTitle(getResources().getString(R.string.app_name));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(getResources().getString(R.string.app_name) + "/", "hw4.zip");

        request.setVisibleInDownloadsUi(false);
        downloadId = downloadManager.enqueue(request);
        progressDialog = new ProgressDialog(SettingsActivity.this);
        progressDialog.setTitle("Download in progress ...");
        progressDialog.setMessage("Downloading Hans Wehr Files");
        progressDialog.setProgressStyle(progressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.setProgress(0);
        progressDialog.setMax(100);
        progressDialog.show();


        new Thread(new Runnable() {

            @Override
            public void run() {

                boolean downloading = true;

                while (downloading) {

                    DownloadManager.Query q = new DownloadManager.Query();
                    q.setFilterById(downloadId);

                    Cursor cursor = downloadManager.query(q);
                    cursor.moveToFirst();
                    int bytes_downloaded = cursor.getInt(cursor
                            .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                        downloading = false;
                    }

                    final int dl_progress = (int) ((bytes_downloaded * 100l) / bytes_total);


                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            progressDialog.setProgress((int) dl_progress);

                        }
                    });

                    cursor.close();
                }

            }
        }).start();

    }

    private class ClearCacheTask extends AsyncTask<Void, Void, Void> {

        private Context context;
        ProgressDialog dialog;
        SettingsActivity activity;

        public ClearCacheTask(Context context, ProgressDialog dialog) {
            this.context = context;
            this.dialog = dialog;
        }

        @Override
        protected void onPreExecute(){
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void...strings){
            Glide.get(context).clearDiskCache();
            return null;
        }

        @Override
        protected void onPostExecute (Void result){
            if(dialog.isShowing()){
                dialog.dismiss();
            }
            Toast.makeText(context,"Cache successfully cleared",Toast.LENGTH_SHORT).show();
        }
    }
}

