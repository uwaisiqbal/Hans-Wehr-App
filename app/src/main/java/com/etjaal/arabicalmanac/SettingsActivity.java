package com.etjaal.arabicalmanac;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.provider.SearchRecentSuggestions;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
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
    ProgressDialog dialog;
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
    }

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
                //overridePendingTransition(R.anim.anim_slide_in_right,R.anim.anim_slide_out_right);
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

