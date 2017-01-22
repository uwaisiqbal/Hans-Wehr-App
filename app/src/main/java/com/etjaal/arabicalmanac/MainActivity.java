package com.etjaal.arabicalmanac;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.etjaal.arabicalmanac.util.IabHelper;
import com.etjaal.arabicalmanac.util.IabResult;
import com.etjaal.arabicalmanac.util.Inventory;
import com.etjaal.arabicalmanac.util.Purchase;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    ImageAdapter imageAdapter;
    ViewPagerFixed viewPager;
    ListView drawerList;
    SearchView searchView;
    ActionBarDrawerToggle drawerToggle;
    DrawerLayout drawerLayout;
    DrawerRowAdapter drawerRowAdapter;
    AdView adView;
    boolean removeAdsPurchased;
    boolean offlineModePurchased;
    Context context;
    final String[] navArray = {"Verb Forms","Roman Arabic Letters","Tutorial", "Remove Ads", "About Us", "Settings", "Help and Feedback", "Rate Us", "Donate"};
    NetworkChangeReceiver networkChangeReceiver;
    DatabaseReference database;
    ProgressDialog progressDialog;
    String downloadUrl = "https://firebasestorage.googleapis.com/v0/b/arabic-almanac-hans-wehr.appspot.com/o/hw4.zip?alt=media&token=67f33cdf-b015-4ae2-97a1-f6751113720d";
    long downloadId;
    DownloadManager downloadManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        database = FirebaseDatabase.getInstance().getReference();
        removeAdsPurchased = HansWehrApplication.prefs.getBoolean(HansWehrApplication.removeAdsKey, false);
        offlineModePurchased = HansWehrApplication.prefs.getBoolean(HansWehrApplication.offlineModeKey, false);
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        if(removeAdsPurchased) {
            setContentView(R.layout.main_activity_no_ads);
        }else{
            setContentView(R.layout.main_activity);
            adView = (AdView) findViewById(R.id.adView);
            setupAds();
        }

        setupViews();
        setupDrawer();
        handleIntent(getIntent());

    }

    private void setupDrawer() {
        int[] icons = {R.drawable.ic_verb_form_icon, R.drawable.ic_roman_letters_icon, R.drawable.ic_help_white_24dp, R.drawable.ic_lock_open_white_24dp,
                R.drawable.ic_info_white_24dp,R.drawable.ic_settings_applications_white_24dp, R.drawable.ic_announcement_white_24dp, R.drawable.ic_thumb_up_white_24dp, R.drawable.ic_payment_white_24dp};
        drawerRowAdapter = new DrawerRowAdapter(this, navArray, icons);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setLogo(R.mipmap.ic_launcher);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#F44336")));
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        drawerList = (ListView) findViewById(R.id.navList);
        drawerList.setAdapter(drawerRowAdapter);
        drawerList.setOnItemClickListener(mNavigationClickListener);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {
            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //getSupportActionBar().setTitle("Menu");
                InputMethodManager inputMethodManager = (InputMethodManager)  getApplicationContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(drawerView.getApplicationWindowToken(), 0);
                searchView.onActionViewCollapsed();
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //getSupportActionBar().setTitle("Hans Wehr");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.setDrawerListener(drawerToggle);
    }

    @Override
    public void onBackPressed() {
        if (this.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.drawerLayout.closeDrawer(GravityCompat.START);
            Toast.makeText(getApplicationContext(),"Press back again to exit the app",Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(!HansWehrApplication.prefs.getBoolean(HansWehrApplication.offlineModeKey,false)) {
            unregisterReceiver(networkChangeReceiver);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!HansWehrApplication.prefs.getBoolean(HansWehrApplication.offlineModeKey,false)) {
            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            networkChangeReceiver = new NetworkChangeReceiver();
            registerReceiver(networkChangeReceiver, filter);
        }
    }

    private void showAboutDialog() {
        //Show Verb Forms Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("About");
        builder.setMessage(Html.fromHtml(getResources().getString(R.string.about)));
        builder.setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        TextView txt = (TextView) alertDialog.findViewById(android.R.id.message);
        txt.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void setupViews() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        viewPager = (ViewPagerFixed) findViewById(R.id.viewPagerMain);
        viewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        imageAdapter = new ImageAdapter(getApplicationContext());
        viewPager.setAdapter(imageAdapter);
        viewPager.setCurrentItem(0);
    }

    private void setupAds() {
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-8841765102895133~2753513600");
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private void showVerbFormsDialog() {
        //Show Verb Forms Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Verb Forms");
        builder.setMessage(R.string.verb_forms);
        builder.setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    private void showRomanLettersDialog() {
        //Show Roman Letters Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Verb Forms");
        builder.setMessage(Html.fromHtml(getString(R.string.roman_letters)));
        builder.setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    private void showTutorialDialog() {
        //Show Tutorial
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Tutorial");
        builder.setMessage(Html.fromHtml(getString(R.string.tutorial)));
        builder.setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    private void showInternetNotConnectedDialog() {
        //Show Internet Connection Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("No Internet Connection");
        builder.setMessage(Html.fromHtml(getString(R.string.nointernet)));
        builder.setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            //TODO: Check for internet
            String query = intent.getStringExtra(SearchManager.QUERY);
            viewPager.setCurrentItem(getIndexFromQuery(query.trim())-1);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, MySuggestionProvider.AUTHORITY,
                    MySuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);
            database.child("searches").push().setValue(query);
            //remove Ads for anyone with the password
            if(query.equals("Haamid3369")){
                removeAdsPurchased = true;
                HansWehrApplication.editor.putBoolean(HansWehrApplication.removeAdsKey, removeAdsPurchased);
                HansWehrApplication.editor.commit();
                Toast.makeText(getApplicationContext(),
                        "Ads successfully removed! Restart the application for the changes to take effect.",
                        Toast.LENGTH_SHORT).show();
            }
            //setShareIntent();
        }
    }

    public int getIndexFromQuery(String query) {
        query = convertFromRomanToArabic(query);
        int index = Arrays.binarySearch(imageAdapter.getDictionaryIndexes().toArray(), query);
        if (index < 0) {
            index = (index + 1) * (-1);
        }
        return index;
    }

    private String convertFromRomanToArabic(String query) {
        query = query.replaceAll("[إآٱأءﺀﺀﺁﺃﺅﺇﺉ]", "ا");
        query = query.replaceAll("[إآٱأءﺀﺀﺁﺃﺅﺇﺉ]", "ا");
        query = query.replaceAll("[ﻯ]", "ي");
        query = query.replaceAll("th", "ث");
        query = query.replaceAll("gh", "غ");
        query = query.replaceAll("[gG]", "غ");
        query = query.replaceAll("kh", "خ");
        query = query.replaceAll("sh", "ش");
        query = query.replaceAll("dh", "ذ");
        query = query.replaceAll("d", "د");
        query = query.replaceAll("D", "ض");
        query = query.replaceAll("z", "ز");
        query = query.replaceAll("Z", "ظ");
        query = query.replaceAll("s", "س");
        query = query.replaceAll("S", "ص");
        query = query.replaceAll("t", "ت");
        query = query.replaceAll("T", "ط");
        query = query.replaceAll("h", "ه");
        query = query.replaceAll("H", "ح");
        query = query.replaceAll("[xX]", "خ");
        query = query.replaceAll("[vV]", "ث");
        query = query.replaceAll("[aA]", "ا");
        query = query.replaceAll("[bB]", "ب");
        query = query.replaceAll("[jJ]", "ج");
        query = query.replaceAll("[7]", "ح");
        query = query.replaceAll("[rR]", "ر");
        query = query.replaceAll("[3]", "ع");
        query = query.replaceAll("[eE]", "ع");
        query = query.replaceAll("[fF]", "ف");
        query = query.replaceAll("[qQ]", "ق");
        query = query.replaceAll("[kK]", "ك");
        query = query.replaceAll("[lL]", "ل");
        query = query.replaceAll("[mM]", "م");
        query = query.replaceAll("[nN]", "ن");
        query = query.replaceAll("[wW]", "و");
        query = query.replaceAll("[yY]", "ي");
        return query;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        //MenuItem shareItem = menu.findItem(R.id.menu_item_share);
        return true;
    }

    // Call to update the share intent
    //private void setShareIntent() {
    //ShareTask s = new ShareTask(this,shareActionProvider,shareIntent);
    //s.execute(Integer.toString(viewPager.getCurrentItem()));
        /*if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(shareIntent);
        }*/
    //}

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.search) {
            return true;
        }
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (HansWehrApplication.mHelper == null) return;

        // Pass on the activity result to the helper for handling
        if (!HansWehrApplication.mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Log.d("Billing", "onActivityResult handled by IABUtil.");
        }
    }

    AdapterView.OnItemClickListener mNavigationClickListener = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            switch (i) {
                //Verb Forms
                case 0:
                    showVerbFormsDialog();
                    break;
                case 1:
                    showRomanLettersDialog();
                    break;
                //Show Tutorial
                case 2:
                    showTutorialDialog();
                    break;
                //Remove Ads
                case 3:
                    if(!removeAdsPurchased){
                        try {
                            HansWehrApplication.mHelper.launchPurchaseFlow((Activity) context,
                                    HansWehrApplication.SKU_REMOVE_ADS, 10001, mPurchaseFinishedListener,
                                    null);
                        } catch (IabHelper.IabAsyncInProgressException e) {
                            e.printStackTrace();
                        }
                    }else{
                        Toast.makeText(getApplicationContext(),
                                "Ads have already been removed",
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
                //About us
                case 4:
                    showAboutDialog();
                    break;
                //Settings
                case 5:
                    Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                    startActivity(intent);
                    break;
                //Help
                case 6:
                    Intent email = new Intent(Intent.ACTION_SENDTO, Uri
                            .fromParts("mailto",
                                    "arabicalmanacandroid@gmail.com", null));
                    email.putExtra(Intent.EXTRA_SUBJECT,
                            "Help and Feedback");
                    startActivity(Intent.createChooser(email,
                            "Choose an Email client :"));
                    break;
                //Rate Us
                case 7:
                    Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
                    Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                    // To count with Play market backstack, After pressing back button,
                    // to taken back to our application, we need to add following flags to intent.
                    goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                            Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                            Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    try {
                        startActivity(goToMarket);
                    } catch (ActivityNotFoundException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName())));
                    }
                    break;
                //Donate
                case 8:
                    try {
                        HansWehrApplication.mHelper.launchPurchaseFlow((Activity) context,
                                HansWehrApplication.SKU_DONATION, 10001, mPurchaseFinishedListener,
                                null);
                    } catch (IabHelper.IabAsyncInProgressException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }

    };

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (result.isFailure()) {
                Log.d("Billing", "Error purchasing: " + result);
                return;
            }

            if (purchase.getSku().equals(HansWehrApplication.SKU_DONATION)) {
                // consume the donation and update the UI
                // Query Purchased Item
                try {
                    HansWehrApplication.mHelper.consumeAsync(purchase, mConsumeFinishedListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    e.printStackTrace();
                }
            } else if(purchase.getSku().equals(HansWehrApplication.SKU_REMOVE_ADS)){
                //Don't consume and remove ads
                removeAdsPurchased = true;
                HansWehrApplication.editor.putBoolean(HansWehrApplication.removeAdsKey, removeAdsPurchased);
                HansWehrApplication.editor.commit();
                Toast.makeText(getApplicationContext(),
                        "Ads successfully removed! Restart the application for the changes to take effect.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            if (purchase.getSku().equals(HansWehrApplication.SKU_DONATION)) {
                if (result.isSuccess()) {
                    //donate_pref.setEnabled(true);
                    Toast.makeText(getApplicationContext(),
                            "Donation Successful! Thank you for your support.",
                            Toast.LENGTH_SHORT).show();
                    Log.v("Billing", "Purchase Successful");
                } else {
                    Log.v("Billing", "Purchase Failed");
                    Toast.makeText(getApplicationContext(),
                            "Donation Failed! Please try again!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }

    };

    public class NetworkChangeReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = NetworkUtil.getConnectivityStatusString(context);
            Log.e("Network", "Network Connection Changed");
            if(status==NetworkUtil.NETWORK_STATUS_NOT_CONNECTED){
                showInternetNotConnectedDialog();
            }
        }
    }
}


