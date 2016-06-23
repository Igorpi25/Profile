package com.ivanov.tech.profile.ui;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.ivanov.tech.profile.R;
import com.ivanov.tech.profile.provider.DBContentProvider;
import com.ivanov.tech.profile.provider.DBContract;
import com.ivanov.tech.session.Session;
import com.meetme.android.horizontallistview.HorizontalListView;

/**
 * Created by Igor on 09.05.15.
 */
public class FragmentSelectUsers extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener{

    private static final String TAG = FragmentSelectUsers.class
            .getSimpleName();    
        
    protected static final int LOADER_USER = 1;
    
    MenuItem menuOK=null;
    
    ArrayList<UserData> users=new ArrayList<UserData>();
    ArrayList<UserData> users_selected=new ArrayList<UserData>();
    
    UsersAdapter adapter_users;
    HorizontalAdapter adapter_horizontal;
    
    ListView listview;
    HorizontalListView horizontal;
    TextView textview_empty;
    
    String tittle;
    ResultListener resultlistener;
    
    public static FragmentSelectUsers newInstance(String tittle,ResultListener resultlistener) {
    	FragmentSelectUsers f = new FragmentSelectUsers();
    	f.tittle=tittle;
    	f.resultlistener=resultlistener;
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Log.d(TAG, "onCreate");
        
        adapter_users=new UsersAdapter(getActivity(),users); 
        adapter_horizontal=new HorizontalAdapter(getActivity(),users_selected);
        
        getLoaderManager().initLoader(LOADER_USER, null, this);

        setHasOptionsMenu(true);
    }
    
    @Override
    public void onStart() {
        super.onStart();
        //Log.d(TAG, "onStart");
    }
    
    @Override
    public void onStop() {
        super.onStop();      
        //Log.d(TAG, "onStop"); 
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;
        
        view=inflater.inflate(R.layout.selectusers, container, false);                
       
        listview = (ListView)view.findViewById(R.id.selectusers_listview);
        listview.setEmptyView(view.findViewById(R.id.selectusers_textview_listview_is_empty));
        listview.setAdapter(adapter_users);
        listview.setOnItemClickListener(this);
        listview.setClickable(true);        
        
        horizontal = (HorizontalListView)view.findViewById(R.id.selectusers_horizontal);
        horizontal.setAdapter(adapter_horizontal);
        horizontal.setOnItemClickListener(this);
        horizontal.setClickable(true);
        
        textview_empty=(TextView)view.findViewById(R.id.selectusers_textview_empty);
        
        return view;
    }
    
    @Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
    	
    	if(parent==listview){
	    	//Log.d(TAG, "onItemClick listview position="+position);
	    	
	    	users.get(position).checked=!users.get(position).checked;
	    	
	    	if(users.get(position).checked)	users_selected.add(users.get(position));
	    	else users_selected.remove(users.get(position));
	    	
    	}else if(parent==horizontal){
    		//Log.d(TAG, "onItemClick horizontal position="+position);
    		
    		users_selected.get(position).checked=!users_selected.get(position).checked;
    		users_selected.remove(position);
    	}
    	
    	adapter_users.notifyDataSetChanged();
    	adapter_horizontal.notifyDataSetChanged();
    	updateViewsState();
    	
    	String menuTittle=getActivity().getResources().getString(R.string.menu_ok);
    	if(users_selected.size()>0)
    		menuTittle=menuTittle+"("+users_selected.size()+")";
    	menuOK.setTitle(menuTittle);
	}
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        
    	menu.clear();
    	
    	menuOK=menu.add(R.string.menu_ok);
        menuOK.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        

		((AppCompatActivity)getActivity()).getSupportActionBar().show();
		((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(tittle);
		((AppCompatActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
		((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	   
		int id = item.getItemId();
	     
		if(id==menuOK.getItemId()){
			//Log.d(TAG, "onOptionsItemSelected menuOK");
			
			SuccessAndClose();			
			return true;
		}
		
		if(id==android.R.id.home){
			//Log.d(TAG, "onOptionsItemSelected menuHome");

			Close();
			return true;
		}
	   
		return false;
	}

//-----------UI ViewsState Utils----------------------------
	
	void updateViewsState(){
		if(users_selected.size()==0){
			menuOK.setEnabled(false);
			textview_empty.setVisibility(View.VISIBLE);
		}else{
			menuOK.setEnabled(true);
			textview_empty.setVisibility(View.INVISIBLE);
		}
	}
	
//---------------Input&Output----------------------
	
    void createUsersData(Cursor cursor){
    	
    	//Log.d(TAG, "createUsersData");
    	
    	users.clear();
    	
    	if(cursor.getCount()==0)return;
    	cursor.moveToFirst();   	
    	
    	
    	    	
    	do{
    		UserData user=new UserData();
    		user.user_id=cursor.getInt(cursor.getColumnIndex(DBContract.User.COLUMN_NAME_SERVER_ID));
    		user.name=cursor.getString(cursor.getColumnIndex(DBContract.User.COLUMN_NAME_NAME));
    		user.icon_url=cursor.getString(cursor.getColumnIndex(DBContract.User.COLUMN_NAME_URL_ICON));
    		user.checked=false; 
    		
    		users.add(user);   		
    		
    	}while((cursor.moveToNext()));
    	
    	
    	adapter_users.notifyDataSetChanged();
    	adapter_horizontal.notifyDataSetChanged();  
    	updateViewsState();
    }
    
    void SuccessAndClose(){
    	final ArrayList<Integer> result=new ArrayList<Integer>();
		
		for(int i=0;i<users_selected.size();i++){
			result.add(users_selected.get(i).user_id);
		}
		

		getFragmentManager().popBackStack();	
		
		resultlistener.onSuccess(result);
	
    }
    
    void Close(){
    	
		getFragmentManager().popBackStack();	
    }
    
//-------------Adapters----------------------
    
    public class UsersAdapter extends ArrayAdapter<UserData> {
        private LayoutInflater mInflater;

        public UsersAdapter(Context context, ArrayList<UserData> values) {
            super(context, R.layout.selectusers_listview_item, values);
            mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            
            if (convertView == null) {
                // Inflate the view since it does not exist
                convertView = mInflater.inflate(R.layout.selectusers_listview_item, parent, false);

                // Create and save off the holder in the tag so we get quick access to inner fields
                // This must be done for performance reasons
                holder = new Holder();
                holder.textview = (TextView) convertView.findViewById(R.id.selectusers_listview_item_name);
                holder.imageview = (ImageView) convertView.findViewById(R.id.selectusers_listview_item_icon);
                holder.checkbox = (CheckBox) convertView.findViewById(R.id.selectusers_listview_item_check);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            
            holder.textview.setText(getItem(position).name);            
            holder.checkbox.setChecked(getItem(position).checked);
            Glide.with(getActivity()).load(getItem(position).icon_url).placeholder(R.drawable.ic_no_icon).error(R.drawable.ic_no_icon).into(holder.imageview);

            return convertView;
        }

        /** View holder for the views we need access to */
        private class Holder {
            public TextView textview;
            public CheckBox checkbox;
            public ImageView imageview;
        }
    }
    
    public class HorizontalAdapter extends ArrayAdapter<UserData> {
        private LayoutInflater mInflater;

        public HorizontalAdapter(Context context, ArrayList<UserData> values) {
            super(context, R.layout.selectusers_horizontal_item, values);
            mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            
            if (convertView == null) {
                // Inflate the view since it does not exist
                convertView = mInflater.inflate(R.layout.selectusers_horizontal_item, parent, false);

                // Create and save off the holder in the tag so we get quick access to inner fields
                // This must be done for performance reasons
                holder = new Holder();
                holder.imageview = (ImageView) convertView.findViewById(R.id.selectusers_horizontal_item_imageview);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            
            Glide.with(getActivity()).load(getItem(position).icon_url).placeholder(R.drawable.ic_no_icon).error(R.drawable.ic_no_icon).into(holder.imageview);
                        
            return convertView;
        }

        /** View holder for the views we need access to */
        protected class Holder {
            public ImageView imageview;
        }
    }
    
//------------UserData-----------------------------
    
    public class UserData{
    	public int user_id;
    	public String icon_url;
    	public String name;
    	public boolean checked;
    	
    }

//-------------Loader<Cursor>------------------
		
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    	
        String[] projection=null;
        Uri uri=null;

        switch(id) {
            case LOADER_USER:
                projection = new String[]{
                        DBContract.User._ID,
                        DBContract.User.COLUMN_NAME_SERVER_ID,
                        DBContract.User.COLUMN_NAME_NAME,
                        DBContract.User.COLUMN_NAME_URL_ICON
                        
                };
                uri = DBContentProvider.URI_USER;
                                
                break;
            
        }

        //Log.d(TAG, "onCreateLoader uri="+uri.toString());
        
        CursorLoader cursorLoader = new CursorLoader(getActivity(),
                uri, projection, null, null, null);

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    	
    	//Log.d(TAG, "onLoadFinished uri="+data.toString());

        switch(loader.getId()){
            case LOADER_USER:
            	createUsersData(data);
            	
                break;            
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    	
    	//Log.d(TAG, "onLoaderReset");    
    }
    
//---------Callback listener to get list of users------------------------------
    
    public interface ResultListener{
    	
    	public void onSuccess(ArrayList<Integer> usersid);
    }
	
}
