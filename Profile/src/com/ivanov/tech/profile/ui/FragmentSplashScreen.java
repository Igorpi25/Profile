package com.ivanov.tech.profile.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.ivanov.tech.profile.R;

public class FragmentSplashScreen extends SherlockDialogFragment{
	private static final String TAG = FragmentSplashScreen.class
            .getSimpleName();   

	public static FragmentSplashScreen newInstance(){
		FragmentSplashScreen f=new FragmentSplashScreen();
    	
		return f;
    }
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		
        View view = null;
        
        view=inflater.inflate(R.layout.splash_screen, container, false);
         
        return view;
    }
	
	@Override
    public void onStart() {
        super.onStart();  
        getSherlockActivity().getSupportActionBar().hide();
    }

    @Override
    public void onStop() {
        super.onStop();   
        getSherlockActivity().getSupportActionBar().show();
    }

}
