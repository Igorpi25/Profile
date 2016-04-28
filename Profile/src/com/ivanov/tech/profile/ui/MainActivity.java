package com.ivanov.tech.profile.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.ivanov.tech.connection.Connection;
import com.ivanov.tech.profile.Profile;
import com.ivanov.tech.profile.R;
import com.ivanov.tech.session.Session;

/**
 * Created by Igor Ivanov on 09.11.15.
 */
public class MainActivity extends SherlockFragmentActivity {

	private static final String TAG = MainActivity.class
            .getSimpleName();  
	
	private boolean ApiKeyActual=false;
	private boolean TimerFinished=false;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");
        Session.Initialize(getApplicationContext());
        
        setContentView(R.layout.activity_main);
        
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setTitle(R.string.app_name);
        getSupportActionBar().setIcon( new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        
        showSplashScreen();
        
        Log.d(TAG, "onCreate checkApiKey"); 
        
        //Api-Key checking protocol (through connection protocol) 
        Session.checkApiKey(this, getSupportFragmentManager(), R.id.main_container, new Connection.ProtocolListener(){
        	@Override
			public void isCompleted() {
        		Log.d(TAG, "onCreate checkApiKey isCompleted"); 
        		ApiKeyActual=true;
        		
        		Profile.startCommunicatorService(MainActivity.this);
        		
				tryToShowContacts();
			}
        	
        	@Override
			public void onCanceled() {
				finish();
				Log.d(TAG, "onCreate checkApiKey onCanceled");
			}

        });
        
        //Splash-screen timer
        new CountDownTimer(3000, 1000) {
            public void onTick(long millisUntilFinished) {}
            
            public void onFinish() {
            	TimerFinished=true;
            	Log.d(TAG, "onCreate CountDownTimer onFinish");
            	tryToShowContacts();
            }
            
         }.start();
        
        
    }
         
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
	   
		int id = item.getItemId();
	    
		if(id==android.R.id.home){
			getSupportFragmentManager().popBackStack();
			return true;
		}
		
		return false;
	}
    
    private void tryToShowContacts(){
    	if(ApiKeyActual&&TimerFinished){    
    		Log.d(TAG, "onCreate ApiKeyActual&&TimerFinished");
    		Profile.showContacts(MainActivity.this, getSupportFragmentManager(), R.id.main_container,false);

    	}
    }
    
    private void showSplashScreen(){
    	FragmentSplashScreen fragment=FragmentSplashScreen.newInstance();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container, fragment, "SplashScreen");
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.commit();
    }
}
