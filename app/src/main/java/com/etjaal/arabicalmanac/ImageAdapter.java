package com.etjaal.arabicalmanac;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

    public ImageAdapter(Context context) {
        this.context = context;
        dictionaryIndexes = new ArrayList<String>();
        parseFileToArrayList();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        boolean isOfflineModeEnabled = HansWehrApplication.prefs.getBoolean(HansWehrApplication.offlineModeKey, false);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = layoutInflater.inflate(R.layout.image, container, false);
        final ProgressBar progressBar = (ProgressBar) v.findViewById(R.id.loadingProgressBar);
        PhotoView imageView = (PhotoView) v.findViewById(R.id.imageView);
        if(isOfflineModeEnabled){
            progressBar.setVisibility(View.GONE);
            Drawable bmp = new BitmapDrawable(
                    BitmapFactory.decodeFile(getImagePathForIndex(position+1)));
            imageView.setImageDrawable(bmp);
            //imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }else{
            progressBar.setVisibility(View.VISIBLE);
            String fileName = getFileNameFromIndex(position+1);
            Log.v("ImageAdapter", fileName);
            FirebaseStorage storage = FirebaseStorage.getInstance();
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
            }).dontTransform().diskCacheStrategy(DiskCacheStrategy.RESULT).into(imageView);
            //imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
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

    public String getImagePathForIndex(int index) {
        String fileName = getFileNameFromIndex(index);
        int folder = (int) Math.round(index / 100 - 0.5f);
        String location = Environment.getExternalStorageDirectory().toString() + "/"
                + context.getResources().getString(R.string.app_name) + "/" +"img/hw4/"
                + Integer.toString(folder) + "/" + fileName + ".png";
        Log.v("location", location);
        return location;
    }

}


