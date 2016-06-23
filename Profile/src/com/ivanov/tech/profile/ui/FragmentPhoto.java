package com.ivanov.tech.profile.ui;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import android.support.v7.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.ivanov.tech.profile.R;
import com.ivanov.tech.session.Session;

public class FragmentPhoto extends DialogFragment {
	
    private static final String TAG="FragmentPhoto";
     
    String url;
    
    ImageView imageview;
    View layout_dimming;

	public static FragmentPhoto newInstance(String url) {
		
		FragmentPhoto f = new FragmentPhoto();  
        f.url=url;
        
        return f;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setHasOptionsMenu(true);  
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.app_name);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;
        view = inflater.inflate(R.layout.fragment_photo, container, false);
                
        Log.d(TAG,"onCreateView url="+url);
          
        layout_dimming=view.findViewById(R.id.fragment_photo_layout_dimming);        
        
        layout_dimming.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                	//close();
                }
                return true;
            }
        });
        
        imageview=(ImageView)view.findViewById(R.id.fragment_photo_imageview);
        Glide.with(getActivity()).load(url).error(R.drawable.image_missing).placeholder(R.drawable.image_missing).into(imageview);
                       
        return view;
    }
    

    void close(){
    	getFragmentManager().popBackStack();        
    }
}
