package com.etjaal.arabicalmanac;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.etjaal.arabicalmanac.util.IabHelper;
import com.etjaal.arabicalmanac.util.IabResult;
import com.etjaal.arabicalmanac.util.Inventory;
import com.etjaal.arabicalmanac.util.Purchase;

/**
 * Created by Uwais on 10/01/2017.
 */
public class HansWehrApplication extends Application {

    static SharedPreferences prefs;
    static SharedPreferences.Editor editor;
//    static IabHelper mHelper;
    final static String SKU_DONATION = "0";
    final static String SKU_REMOVE_ADS = "hanswehr.removeads";
    static String removeAdsKey = "com.etjaal.arabicalmanac.removeads";
    static String offlineModeKey = "offline_mode_pref";
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        HansWehrApplication.context = getApplicationContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();
//        setupInAppBilling();
    }

//    @Override
//    public void onTerminate() {
//        super.onTerminate();
//        Log.d("Billing", "Destroying helper.");
//        if (HansWehrApplication.mHelper != null) {
//            HansWehrApplication.mHelper.disposeWhenFinished();
//            HansWehrApplication.mHelper = null;
//        }
//    }

    public static Context getAppContext() {
        return HansWehrApplication.context;
    }

//    private void setupInAppBilling() {
//        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA6ECYlk9FDoWQL8XSDuEwzLWa+"
//                + "5EU+4CDNIhoq7WMZFY3Nj+ewHN/ylwbBtS7/N2MmOuqX/LzA9iqpRTlbLlQZxoO5dPotRh+zIkcxLyssh1YCWMbNnyv5Iv"
//                + "chIFBEun7AxZhkaTE0FyocVzd+jClfEJUbFa2op5jWcl8tkCceYi6PGJNC0ogcn2p2+kddjo8VJWX4qdMxykccXm5l"
//                + "+ShDNFbcsX/1gFYzIQQ6QFGkAXRrxNEBqsiuVdo2wPNbBOHrFE8q4B3PCXs1413Ol8ykvFRdTJENMA2l2VFlEfms"
//                + "2IhAU7Zhl/osaX/M/672zjBgWsp8Ob70GfMZC6VCGpXkQIDAQAB";
//
//        // compute your public key and store it in base64EncodedPublicKey
//        mHelper = new IabHelper(this, base64EncodedPublicKey);
//        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
//            public void onIabSetupFinished(IabResult result) {
//                if (!result.isSuccess()) {
//                    // Oh noes, there was a problem.
//                    Log.d("Billing", "Problem setting up In-app Billing: " + result);
//                    return;
//                }                     // Hooray, IAB is fully set up!
//
//                if (mHelper == null) return;
//
//                try {
//                    mHelper.queryInventoryAsync(mGotInventoryListener);
//                    Log.d("Billing", "In-app Billing is set up OK");
//                } catch (IabHelper.IabAsyncInProgressException e) {
//                    e.printStackTrace();
//                }
//            }
//
//        });
//    }
//
//    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
//        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
//            Log.d("Hans Wehr Application", "Query inventory finished.");
//
//            // Have we been disposed of in the meantime? If so, quit.
//            if (mHelper == null) return;
//
//            // Is it a failure?
//            if (result.isFailure()) {
//                Log.v("Hans Wehr Application","Failed to query inventory: " + result);
//                return;
//            }
//
//            if(inventory.hasPurchase(SKU_REMOVE_ADS)){
//                HansWehrApplication.editor.putBoolean(HansWehrApplication.removeAdsKey, true);
//                HansWehrApplication.editor.commit();
//            }
//        }
//    };
}
