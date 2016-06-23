package com.ivanov.tech.profile.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ivanov.tech.profile.R;
import com.ivanov.tech.session.Session;

public class FragmentText extends DialogFragment implements OnClickListener{
	
    private static final String TAG="FragmentText";
     
    String key=null,value=null;
    ResultListener resultstatus;
    
    EditText edittext_value;
    TextView textview_key;
    Button button_save;

	public static FragmentText newInstance(String key, String value, ResultListener listener) {
		
		FragmentText f = new FragmentText();  
		
        f.key=key;
        f.value=value;        
        f.resultstatus=listener;
        
        return f;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(edittext_value, InputMethodManager.SHOW_FORCED);
        
        setHasOptionsMenu(true);  
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(key);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edittext_value.getWindowToken(), 0);
        
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;
        view = inflater.inflate(R.layout.fragment_text, container, false);
                
        Log.d(TAG,"onCreateView key="+key+" value="+value);
        
        
        textview_key=(TextView)view.findViewById(R.id.fragment_text_textview_key);
        edittext_value=(EditText)view.findViewById(R.id.fragment_text_edittext_value);
        
        if(key!=null)textview_key.setText(key);
        
        if(value!=null)edittext_value.setHint(value);        
                
        button_save=(Button)view.findViewById(R.id.fragment_text_button_save);
        button_save.setOnClickListener(this);
        
              
        return view;
    }
    
    @Override
	public void onClick(View v) {
		
    	if(v.getId()==button_save.getId()){
    		
    		String newValue=edittext_value.getText().toString();
    		
    		close();
    		
    		if((newValue!=null)&&(newValue.length()>0)){    		
    			resultstatus.onSaved(key,newValue);
    		}    		
    		
    	}
	}

    void close(){
    	
    	InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edittext_value.getWindowToken(), 0);
    	
    	getFragmentManager().popBackStack();        
    }
    
    public interface ResultListener{
    	void onSaved(String key,String newValue);
    }

	
}
