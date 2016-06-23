package com.ivanov.tech.profile.ui;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ivanov.tech.multipletypesadapter.cursoradapter.CursorItemHolderButton;
import com.ivanov.tech.multipletypesadapter.cursoradapter.CursorItemHolderImageView;
import com.ivanov.tech.multipletypesadapter.cursoradapter.CursorItemHolderText;
import com.ivanov.tech.multipletypesadapter.cursoradapter.CursorMultipleTypesAdapter;
import com.ivanov.tech.profile.Profile;
import com.ivanov.tech.profile.R;
import com.ivanov.tech.profile.Profile.CreateGroupResultListener;
import com.ivanov.tech.profile.provider.DBContentProvider;
import com.ivanov.tech.profile.provider.DBContract;
import com.ivanov.tech.session.Session;
import com.ivanov.tech.uploader.Uploader;
import com.ivanov.tech.uploader.PhotoMultipartRequest.Params;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class FragmentMe extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener, OnClickListener {
	
	private static final String TAG = FragmentMe.class
            .getSimpleName();    

	protected static final int TYPE_TEXT =0;
    protected static final int TYPE_TEXT_CLICKABLE =1;
    protected static final int TYPE_BUTTON =2; 
    protected static final int TYPE_AVATAR =3;
    
    protected static final int TEXT_KEY_UPLOAD_AVATAR =1;
    protected static final int TEXT_KEY_NAME =2;    
    protected static final int IMAGEVIEW_KEY_AVATAR =3;
    
    protected ListView listview;    
    protected CursorMultipleTypesAdapter adapter=null;
    
    protected static final int LOADER_USER_SERVER_ID = 1; 
    
    protected Cursor cursor_user_server_id;
    
    public static FragmentMe newInstance() {
    	FragmentMe f = new FragmentMe(); 
        
        return f;
    }
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);  
        
        getLoaderManager().initLoader(LOADER_USER_SERVER_ID, null, this);
    }
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;
        view = inflater.inflate(R.layout.fragment_details, container, false);
                
        Log.d(TAG,"onCreateView");
        
        listview=(ListView)view.findViewById(R.id.fragment_details_listview);
        
        adapter=new CursorMultipleTypesAdapter(getActivity(),null,adapter.FLAG_AUTO_REQUERY);
        
        adapter.addItemHolder(TYPE_TEXT, new CursorItemHolderText(getActivity(),this));
        adapter.addItemHolder(TYPE_TEXT_CLICKABLE, new CursorItemHolderText(getActivity(),this){
        	@Override
        	public boolean isEnabled() {
        		return true;
        	}
        });
        adapter.addItemHolder(TYPE_BUTTON, new CursorItemHolderButton(getActivity(),this));
        adapter.addItemHolder(TYPE_AVATAR, new CursorItemHolderImageView(getActivity(),R.layout.details_item_avatar,R.id.details_item_avatar_imageview,this));
        
        listview.setAdapter(adapter);
        
        listview.setOnItemClickListener(adapter);
        
        //adapter.changeCursor(createMergeCursor());
        
        return view;
    }
	
	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();

		((AppCompatActivity)getActivity()).getSupportActionBar().show();
		((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(" ");
    }
		
//------------Preparing cursor----------------------------
	
	protected Cursor createMergeCursor(){
		Log.d(TAG, "createMergeCursor");
		
    	List<Cursor> cursors_list=new ArrayList<Cursor>();  
    	
    	int _id=1;
    	
    	try{
    		cursors_list.add(getMatrixCursor(_id));
    	}catch(JSONException e){
    		Log.e(TAG, "createMergeCursor JSONException e="+e);
    	}
    	
    	if(cursors_list.size()==0)return null;
    	
    	Cursor[] cursors_array=new Cursor[cursors_list.size()];
    	MergeCursor mergecursor=new MergeCursor(cursors_list.toArray(cursors_array));
    	
    	return mergecursor;    	
    }
    
    protected MatrixCursor getMatrixCursor(int _id) throws JSONException{
   	
    	Log.d(TAG, "getMatrixCursor");
    	
    	MatrixCursor matrixcursor=new MatrixCursor(new String[]{adapter.COLUMN_ID, adapter.COLUMN_TYPE, adapter.COLUMN_KEY, adapter.COLUMN_VALUE});    	
    	    	
    	if(cursor_user_server_id.getCount()<1)return matrixcursor;
    	cursor_user_server_id.moveToFirst();
    	
    	JSONObject json;    
    	
    	json=new JSONObject("{ imageview:{image_url:'"+cursor_user_server_id.getString(cursor_user_server_id.getColumnIndex(DBContract.User.COLUMN_NAME_URL_AVATAR))+"' } }");    	
    	matrixcursor.addRow(new Object[]{++_id,TYPE_AVATAR,IMAGEVIEW_KEY_AVATAR,json.toString()});
    	
    	json=new JSONObject("{value:{ text:'"+this.getString(R.string.fragment_me_upload_text)+"' }, key:{visible : false}, icon:{image_res:'"+android.R.drawable.ic_menu_upload+"'} }");    	
    	matrixcursor.addRow(new Object[]{++_id,TYPE_TEXT_CLICKABLE,TEXT_KEY_UPLOAD_AVATAR,json.toString()});
    	    	
    	json=new JSONObject("{key:{ text:'"+this.getString(R.string.fragment_details_user_name)+"' }, value:{text:'"+cursor_user_server_id.getString(cursor_user_server_id.getColumnIndex(DBContract.User.COLUMN_NAME_NAME))+"'}, icon:{ } }");    	
    	matrixcursor.addRow(new Object[]{++_id,TYPE_TEXT_CLICKABLE,TEXT_KEY_NAME,json.toString()});
    	
    	json=new JSONObject("{button:{tag:'close', text:'"+getString(R.string.fragment_details_button_close_text)+"'} }");    	
    	matrixcursor.addRow(new Object[]{++_id,TYPE_BUTTON,0,json.toString()});
    	
    	return matrixcursor;
    }
        
    //---------------Utilities-----------------------
    
    private void updateMenuTitle(){
    	cursor_user_server_id.moveToFirst();
    	String title=cursor_user_server_id.getString(cursor_user_server_id.getColumnIndex(DBContract.User.COLUMN_NAME_NAME));;

		((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(title);
    }
    
    protected void close(){
    	getFragmentManager().popBackStack();
    }
    
	private void toast(String msg){
		Log.d(TAG, msg);
		Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
	}
	
    protected void addContact(){
    	Profile.showSearchContact(getActivity(),getActivity().getSupportFragmentManager(),R.id.main_container);
    }
    
    protected void addGroup(){
    	/*
    	 * First here we get users-list, then we send it the server to create the group of these users.
    	 * When server response us groupid, we take it and showGroup. At this point, 
    	 * server will have to send to us the group-info and group-users through the Communicator.
    	 * So when we get gropid by from response we already have the group-info and group-users list, and users-info in group
    	*/
    	
    	Profile.showSelectFriends(getString(R.string.selectusers_textview_empty), new FragmentSelectUsers.ResultListener(){

			@Override
			public void onSuccess(ArrayList<Integer> usersid) {
				//Add selected users to group. For that we create message to server
				
				Log.e(TAG, "showSelectFriends onSuccess usersid.size = "+usersid.size());
				
				JSONArray users=new JSONArray();
				try {					
					
					//List of users that have to be added to group
					for(Integer userid : usersid){
						users.put(new JSONObject().put("id", userid));
					}				
					
				} catch (JSONException e) {
					Log.e(TAG, "showSelectFriends onSuccess JSONException e = "+e);
				}
				
				//Call TransportProfile of Communicator protocol
				Profile.createGroupRequest(users, getActivity(),new CreateGroupResultListener(){

					@Override
					public void onCreated(int groupid) {
						Profile.showGroup(groupid, getActivity(), getFragmentManager(), R.id.main_container);
					}

					@Override
					public void onFailed(String message) {
						toast("Create group failed");
						Log.e(TAG, "addGroup createGroupRequest onFailed message="+message);						
					}
					
				});
			}
			
		}, getActivity(), getFragmentManager(), R.id.main_container);
    	    	
    }
    	
	//--------------Adapter Callbacks----------------------
    
  	@Override
  	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
  		switch(adapter.getType(adapter.getCursor())){
  		
  		case TYPE_TEXT_CLICKABLE: {
  			int key=adapter.getKey(adapter.getCursor());
  			toast("Item clicked key="+key);
  			
  			switch(key){
	  			case TEXT_KEY_NAME:{
		  			try {
		  				JSONObject json=new JSONObject(adapter.getValue(adapter.getCursor()));
		  				final String text_key=json.getJSONObject("key").getString("text");
		  				final String text_value=json.getJSONObject("value").getString("text");
		  				
						Profile.showText(text_key,text_value,new FragmentText.ResultListener() {
							
							@Override
							public void onSaved(String key, String newValue) {
								if(!newValue.equals(text_value)){
									JSONObject me_json=new JSONObject();
									
									try {
										me_json.put("name", newValue);
									} catch (JSONException e) {}
									
									Profile.meOperation(getActivity(), me_json);
								}
							}
							
						},getActivity(),getFragmentManager(),R.id.main_container);
						
					} catch (JSONException e) {
						Log.e(TAG, "onItemClick TYPE_TEXT_CLICKABLE.TEXT_KEY_NAME JSONException e="+e);
					}
		  			
		  			
	  			}break;
	  			
	  			case TEXT_KEY_UPLOAD_AVATAR:{
	  				Uploader.protocolChooseAndUpload( getActivity(), getFragmentManager(),
	  						new Params()	{
	  		    				
	  							@Override
	  							public String getPartName() {
	  								return Profile.AVATAR_UPLOAD_FILE_PART_NAME;					
	  							}
	  							
	  							@Override
	  							public int[] getSize() {
	  								return Profile.AVATAR_UPLOAD_SIZE;
	  							}
	  							
	  									
	  							@Override
	  							public String getUrl() {
	  								return Profile.getUrlAvatarUpload();
	  							}	    		
	  							
	  						},	
	  						new Uploader.UploadListener(){
	  							@Override
	  							public void onUploaded() {
	  								toast("Uploaded");
	  							}
	  							
	  							@Override
	  							public void onCancelled() {
	  								
	  							}
	  				
	  						});
		  			
	  			}break;
  			
  			}
  		}break;
  		
  		
  		}
  		
  	}

  	@Override
  	public void onClick(View v) {
  		
  		Log.d(TAG, "onClick v.tag="+v.getTag());	
  		
  		//if clicked View from TYPE_AVATAR
  		if(v.getTag(R.id.details_item_avatar_imageview)!=null){
  			//The key of View is IMAGEVIEW_KEY_AVATAR
  			if((Integer)v.getTag(R.id.details_item_avatar_imageview)==IMAGEVIEW_KEY_AVATAR){  			
	  			
  				String url_full=cursor_user_server_id.getString(cursor_user_server_id.getColumnIndex(DBContract.User.COLUMN_NAME_URL_FULL));	  			  			
	  			Profile.showPhoto(url_full, getActivity(), getFragmentManager(), R.id.main_container);
  			}
  			return;
  		}
  		
  		if((v.getTag()!=null)&&(v.getTag().toString().equals("close"))){
			Log.d(TAG, "onClick close");			
			close();			
			return;
		}
  	}
  	    
//-------------Loader<Cursor>------------------
	
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    	
    	//Log.d(TAG, "onCreateLoader");

        String[] projection=null;
        Uri uri=null;
        
        switch(id) {
            case LOADER_USER_SERVER_ID:
                projection = new String[]{
                        DBContract.User._ID,
                        DBContract.User.COLUMN_NAME_SERVER_ID,
                        DBContract.User.COLUMN_NAME_NAME,
                        DBContract.User.COLUMN_NAME_STATUS,
                        DBContract.User.COLUMN_NAME_URL_ICON,
                        DBContract.User.COLUMN_NAME_URL_AVATAR,
                        DBContract.User.COLUMN_NAME_URL_FULL
                        
                        
                };
                uri = Uri.parse(DBContentProvider.URI_USER+"/"+Session.getUserId());
                                
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
            case LOADER_USER_SERVER_ID:
            	
            	if(data.getCount()==0){
            		
            		Log.e(TAG, "onLoadFinished data.getCount() = 0");
            		return;
            	}
            		
            	cursor_user_server_id=data;
            	updateMenuTitle();
            	adapter.swapCursor(createMergeCursor());
            	
                break;          
                
        }
    }
	
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    	
    	//Log.d(TAG, "onLoaderReset");    
    }


}
