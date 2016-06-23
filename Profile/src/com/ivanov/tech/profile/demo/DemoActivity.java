package com.ivanov.tech.profile.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.ivanov.tech.communicator.Communicator;
import com.ivanov.tech.connection.Connection;
import com.ivanov.tech.profile.Profile;
import com.ivanov.tech.profile.R;
import com.ivanov.tech.profile.service.CommunicatorService;
import com.ivanov.tech.profile.ui.FragmentSplashScreen;
import com.ivanov.tech.session.Session;

/**
 * Created by Igor Ivanov on 09.11.15.
 */
public class DemoActivity extends AppCompatActivity {

	private static final String TAG = DemoActivity.class
            .getSimpleName();  
	
	private boolean ApiKeyActual=false;
	private boolean TimerFinished=false;

	//Profile URLs
	private static final String ur_server = "http://igorpi25.ru/v2/";	
	
	private static final String url_searchcontact = ur_server+"search_contact";
	public static final String url_avatarupload = ur_server+"avatars/upload";
	public static final String url_grouppanoramaupload = ur_server+"group_panorama/upload";
	private static final String url_creategroup = ur_server+"create_group";	
	
	//Session URLs
	static final String url_testapikey=ur_server+"testapikey";
	static final String url_login=ur_server+"login";
	static final String url_register=ur_server+"register";
		
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");
        
        getSupportActionBar().setDisplayShowCustomEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
		getSupportActionBar().setDisplayUseLogoEnabled(false);
		getSupportActionBar().setTitle(R.string.app_name);
		getSupportActionBar().setIcon( new ColorDrawable(getResources().getColor(android.R.color.transparent)));
		
		getSupportActionBar().hide();
        
        Session.Initialize(getApplicationContext(),url_testapikey,url_login,url_register);
        Profile.Initialize(getApplicationContext(),url_searchcontact,url_avatarupload,url_grouppanoramaupload,url_creategroup);
        Communicator.Initialize(getApplicationContext(), CommunicatorService.URL_SERVER,CommunicatorService.URL_START_SERVER, CommunicatorService.class.getCanonicalName());
        
        setContentView(R.layout.activity_main);
                        
        
        showSplashScreen();
        
        Log.d(TAG, "onCreate checkApiKey"); 
        
        //Api-Key checking protocol (through connection protocol) 
        Session.checkApiKey(this, getSupportFragmentManager(), R.id.main_container, new Connection.ProtocolListener(){
        	@Override
			public void isCompleted() {
        		Log.d(TAG, "onCreate checkApiKey isCompleted"); 
        		ApiKeyActual=true;
        		
        		Profile.startCommunicatorService(DemoActivity.this);
        		
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
    		getSupportActionBar().show();
    		Profile.showContacts(DemoActivity.this, getSupportFragmentManager(), R.id.main_container,false);

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
