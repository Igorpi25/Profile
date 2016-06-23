package com.ivanov.tech.profile.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ivanov.tech.profile.Profile;
import com.ivanov.tech.profile.Profile.CloseListener;
import com.ivanov.tech.profile.Profile.SearchResultListener;
import com.ivanov.tech.profile.R;
import com.ivanov.tech.session.Session;

public class FragmentSearchContact extends DialogFragment implements OnClickListener{
	
    private static final String TAG="FragmentSearchContact";
    
    MenuItem menuOK=null;
    
    EditText edittext;    
    Button button_search,button_clear;

	public static FragmentSearchContact newInstance() {
		
		FragmentSearchContact f = new FragmentSearchContact();  
		
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
		
		menuOK=menu.add(R.string.menu_search_contact_text);
        menuOK.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menuOK.setEnabled(false);
        
		((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.search_tittle);
		((AppCompatActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
		((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
	   
		int id = item.getItemId();
	     
		if(id==menuOK.getItemId()){
			//Log.d(TAG, "onOptionsItemSelected menuOK");
			
			searchContact();
			
			return true;
		}
		
		return false;
	}
        
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        showKeyboard();
        
    }

    @Override
    public void onDestroy() {
        super.onDestroy();        
        Log.d(TAG, "onPause");        
        hideKeyboard();
        
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;
        view = inflater.inflate(R.layout.fragment_search, container, false);
                
        edittext=(EditText)view.findViewById(R.id.search_edittext);
        
        button_search=(Button)view.findViewById(R.id.search_button_search);
        button_search.setOnClickListener(this);
        
        button_clear=(Button)view.findViewById(R.id.search_button_clear);
        button_clear.setOnClickListener(this);
        
        edittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()==0){
                	button_search.setVisibility(View.GONE);
                	button_clear.setVisibility(View.GONE);
                	
                	menuOK.setEnabled(false);
                }
                else {
                	button_search.setVisibility(View.VISIBLE);
                	button_clear.setVisibility(View.VISIBLE);
                	
                	menuOK.setEnabled(true);
                }
                
                button_search.setText(getActivity().getResources().getString(R.string.search_button_text)+" "+s);
                
                String menuTittle=getActivity().getResources().getString(R.string.menu_search_contact_text);
            	if((s.length()>0))
            		menuTittle=menuTittle+": ("+s+")";
            	menuOK.setTitle(menuTittle);
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            
            }
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
    
            } 
        });        
        
        edittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if(edittext.length()>0)
                    	searchContact();
                	
                    return true;
                }
                return false;
            }

			
        });
                      
        return view;
    }
    
    @Override
	public void onClick(View v) {
		
    	if(v.getId()==button_search.getId()){
    		searchContact();
    	}
    	
    	if(v.getId()==button_clear.getId()){
    		clear();
    	}
	}
    
    void searchContact(){
    	Log.d(TAG, "searchContact");
    	String value=edittext.getText().toString();
    	
    	Profile.searchContactRequest(value, getActivity(), new SearchResultListener(){

			@Override
			public void onSuccess(int userid) {
				goFoundUserDetails(userid);
			}

			@Override
			public void onFailed(String message) {
				Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
			}
    		
    	});
    	
    	
    }
    
    void goFoundUserDetails(int userid){
    	hideKeyboard();
    	
    	if(userid==Session.getUserId()){
    		toast("It's me");
    		return;
    	}
    	
    	showFoundUser(userid);
    	
    }
    
    //-------------Utilities---------------------
    
    private void showFoundUser(int userid){
    	//Show user by replacing FragmentSearch
    	
		if( (getFragmentManager().findFragmentByTag("DetailsUser")!=null) && (getFragmentManager().findFragmentByTag("DetailsUser").isVisible()) )return;
		
		FragmentDetailsUser fragment=FragmentDetailsUser.newInstance(userid);

		getFragmentManager().popBackStackImmediate();
		
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();        
        fragmentTransaction.add(R.id.main_container, fragment, "DetailsUser");
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.addToBackStack("DetailsUser");
        fragmentTransaction.commit();
	}
    
    private void toast(String msg){
		Log.d(TAG, msg);
		Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
	}
    
    void clear(){
    	Log.d(TAG, "clear");
    	
    	edittext.setText("");
    }

    void close(){
    	Log.d(TAG, "close");
    	hideKeyboard();
    	getFragmentManager().popBackStack();        
    }
    
    void showKeyboard(){
    	InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(edittext, InputMethodManager.SHOW_FORCED);
    }
    
    void hideKeyboard(){
    	InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edittext.getWindowToken(), 0);
    }
    
}
