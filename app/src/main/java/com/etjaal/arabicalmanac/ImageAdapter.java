package com.etjaal.arabicalmanac;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import uk.co.senab.photoview.PhotoView;

/**
 * Created by Uwais on 06/01/2017.
 */
public class ImageAdapter extends PagerAdapter {

    private Context context;
    private ArrayList<String> dictionaryIndexes;
    private FirebaseStorage storage;

    public ImageAdapter(Context context) {
        this.context = context;
        dictionaryIndexes = new ArrayList<String>();
        parseFileToArrayList();
        storage = FirebaseStorage.getInstance();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = layoutInflater.inflate(R.layout.image, container, false);
        final ProgressBar progressBar = (ProgressBar) v.findViewById(R.id.loadingProgressBar);
        progressBar.setVisibility(View.VISIBLE);
        PhotoView imageView = (PhotoView) v.findViewById(R.id.imageView);
        String fileName = getFileNameFromIndex(position+1);
        Log.v("ImageAdapter", fileName);
        StorageReference storageReference = storage.getReferenceFromUrl("gs://arabic-almanac-hans-wehr.appspot.com");
        StorageReference pageRef = storageReference.child("hw4").child(fileName + ".png");
        Log.v("ImageApater", pageRef.getPath());
        Glide.with(context).using(new FirebaseImageLoader()).load(pageRef).listener(new RequestListener<StorageReference, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, StorageReference model, Target<GlideDrawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, StorageReference model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                progressBar.setVisibility(View.GONE);
                return false;
            }
        }).dontTransform().fitCenter().diskCacheStrategy(DiskCacheStrategy.RESULT).into(imageView);
        container.addView(v);
        return v;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((View) object);
    }

    @Override
    public int getCount() {
        return dictionaryIndexes.size();
    }

    private void parseFileToArrayList() {
        try {
            //Load json file and convert it to an Array List
            String json = loadJSONFromAsset();
            Log.v("MainActivity", json);
            JSONObject jb = new JSONObject(json);
            JSONArray dictIndexes = jb.getJSONArray("hw4");
            Log.v("Main Activity", dictIndexes.toString());
            for (int i = 0; i < dictIndexes.length(); i++) {
                dictionaryIndexes.add(dictIndexes.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getDictionaryIndexes(){
        return  dictionaryIndexes;
    }

    public FirebaseStorage getFirebaseStorage(){
        return storage;
    }
    /**
     * Loads the json file located in assests into a json object
     */
    private String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = context.getAssets().open("hw4_indexes.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            Log.e("MainActivity", "JSON Parsing Failed");
            return null;
        }
        return json;
    }

    public static String getFileNameFromIndex(int index) {
        String indexString = Integer.toString(index);
        int folder = (int) Math.round(index / 100 - 0.5f);
        int length = String.valueOf(index).length();
        //Add on the preceeding 000... to the filename
        if (length < 4) {
            for (int i = length; i < 4; i++) {
                indexString = "0" + indexString;
            }
        }
        return "hw4" + "-" + indexString;
    }
}


