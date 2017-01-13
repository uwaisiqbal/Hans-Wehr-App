package com.etjaal.arabicalmanac;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

/**
 * Created by Uwais on 07/01/2017.
 */
public class ShareTask extends AsyncTask<String,Void,File>{

    private final Context context;
    private ShareActionProvider shareActionProvider;
    private Intent intent;
    public ShareTask(Context context, ShareActionProvider shareActionProvider, Intent intent) {
        this.context = context;
        this.shareActionProvider = shareActionProvider;
        this.intent = intent;
        Log.v("ShareTask", "new Share Task");
    }

    @Override protected File doInBackground(String... params) {
        Log.v("ShareTask", "doInBackground");
        int index = Integer.parseInt(params[0]); // should be easy to extend to share multiple images at once
        try {
            String fileName = ImageAdapter.getFileNameFromIndex(index + 1);
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://arabic-almanac-hans-wehr.appspot.com");
            StorageReference pageRef = storageReference.child("hw4").child(fileName + ".png");
            Log.v("ShareTask", pageRef.getPath());
            return Glide.with(context).using(new FirebaseImageLoader()).load(pageRef).downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
        } catch (Exception ex) {
            Log.w("SHARE", "Sharing failed", ex);
            return null;
        }
    }
    @Override protected void onPostExecute(File result) {
        if (result == null) { return; }
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName(), result);
        intent.putExtra(Intent.EXTRA_STREAM, result);
       // share(uri); // startActivity probably needs UI thread
    }

    private void share(Uri result) {
        Log.v("ShareTask", "Sharing with Intent");
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/png");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Shared image");
        intent.putExtra(Intent.EXTRA_TEXT, "Look what I found!");
        intent.putExtra(Intent.EXTRA_STREAM, result);
        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(intent);
        }
        //context.startActivity(Intent.createChooser(intent, "Share image"));
    }
}
