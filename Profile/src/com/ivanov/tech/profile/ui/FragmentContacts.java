package com.ivanov.tech.profile.ui;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.ivanov.tech.multipletypesadapter.cursoradapter.CursorItemHolderButton;
import com.ivanov.tech.multipletypesadapter.cursoradapter.CursorItemHolderGridView;
import com.ivanov.tech.multipletypesadapter.cursoradapter.CursorItemHolderHeader;
import com.ivanov.tech.multipletypesadapter.cursoradapter.CursorItemHolderImageView;
import com.ivanov.tech.multipletypesadapter.cursoradapter.CursorItemHolderLink;
import com.ivanov.tech.multipletypesadapter.cursoradapter.CursorItemHolderText;
import com.ivanov.tech.multipletypesadapter.cursoradapter.CursorMultipleTypesAdapter;
import com.ivanov.tech.profile.Profile;
import com.ivanov.tech.profile.Profile.CreateGroupResultListener;
import com.ivanov.tech.profile.Profile.FriendOperationListener;
import com.ivanov.tech.profile.R;
import com.ivanov.tech.profile.provider.DBContentProvider;
import com.ivanov.tech.profile.provider.DBContract;
import com.ivanov.tech.session.Session;

public class FragmentContacts extends SherlockDialogFragment implements LoaderManager.LoaderCallbacks<Cursor>,OnItemClickListener,OnClickListener {
	
	private static final String TAG = FragmentContacts.class
            .getSimpleName();
	
	protected static final int TYPE_LINK_USER = 0;
    protected static final int TYPE_LINK_GROUP =1;
    protected static final int TYPE_HEADER =2;
            
    protected ListView listview;    
    protected CursorMultipleTypesAdapter adapter=null;

    protected static final int LOADER_USERS = 1;
    protected static final int LOADER_GROUPS = 2;

    protected Cursor cursor_users;
    protected Cursor cursor_groups;
    
    MenuItem menuAddContact=null;
    MenuItem menuAddGroup=null;
		
	public static FragmentContacts newInstance() {
		FragmentContacts f = new FragmentContacts(); 
        
        return f;
    }
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setHasOptionsMenu(true);
        
        getLoaderManager().initLoader(LOADER_GROUPS, null, this);
        getLoaderManager().initLoader(LOADER_USERS, null, this);
        
    }
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;
        view = inflater.inflate(R.layout.fragment_details, container, false);
                
        Log.d(TAG,"onCreateView");
        
        listview=(ListView)view.findViewById(R.id.fragment_details_listview);
        
        adapter=new CursorMultipleTypesAdapter(getActivity(),null,adapter.FLAG_AUTO_REQUERY);
        
        //Prepare map of types and set listeners for them. There are different ways in which you can define ItemHolder      
        adapter.addItemHolder(TYPE_LINK_USER, new CursorItemHolderLink(getActivity(),this,this));                
       
        adapter.addItemHolder(TYPE_LINK_GROUP, new CursorItemHolderLink(getActivity(),this,null));
       
        adapter.addItemHolder(TYPE_HEADER, new CursorItemHolderHeader(getActivity(),this));
        
        listview.setAdapter(adapter);
        
        listview.setOnItemClickListener(adapter);
        
        adapter.changeCursor(createMergeCursor());
        
        return view;
    }
	
	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        
		menu.clear();
		       
        menuAddContact=menu.add(Menu.NONE, 4, Menu.NONE,R.string.menu_add_contact);
        menuAddContact.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
       
        menuAddGroup=menu.add(Menu.NONE, 3, Menu.NONE,R.string.menu_add_group);
        menuAddGroup.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        
        getSherlockActivity().getSupportActionBar().setTitle(R.string.app_name);
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	   
		int id = item.getItemId();
	     
		
		if(id==menuAddContact.getItemId()){
			Log.d(TAG, "onOptionsItemSelected menuAddContact");
			
			addContact();
			
			return true;
		}
		
		if(id==menuAddGroup.getItemId()){
			Log.d(TAG, "onOptionsItemSelected menuAddGroup");
			
			addGroup();
			
			return true;
		}
		
		if(id==android.R.id.home){
			Log.d(TAG, "onOptionsItemSelected menuHome");

			close();
			
			return true;
		}
	   
		return false;
	}
	
	//------------Preparing cursor----------------------------

	protected Cursor createMergeCursor(){
    	
    	List<Cursor> cursors_list=new ArrayList<Cursor>();	
    	
    	int _id=1;
    	
    	if(cursor_users!=null) {
    		cursors_list.add(getMeMatrixCursor(_id));
    	}
    	
    	if(cursor_groups!=null)
    		cursors_list.add(getGroupsMatrixCursor(_id));
    	    	
    	if(cursor_users!=null) {
    		cursors_list.add(getUsersMatrixCursor(_id));
    	}
    	
    	if(cursors_list.size()==0)return null;
    	
    	Cursor[] cursors_array=new Cursor[cursors_list.size()];
    	MergeCursor mergecursor=new MergeCursor(cursors_list.toArray(cursors_array));
    	
    	return mergecursor;    	
    }

	protected MatrixCursor getUsersMatrixCursor(int _id){

    	MatrixCursor matrixcursor=new MatrixCursor(new String[]{adapter.COLUMN_ID,adapter.COLUMN_TYPE,adapter.COLUMN_KEY,adapter.COLUMN_VALUE});    	
    	
    	//Create users header_json
		JSONObject header_json=null;
		try {
			header_json=new JSONObject("{key:{text:'"+this.getString(R.string.fragment_contacts_users_header_key)+"'}, value:{visible:false}, label:{visible:false} } ");
		} catch (JSONException e) {
			Log.e(TAG, "getMeMatrixCursor JSONException header_json e="+e.toString());
		}    	
    	//Add TYPE_HEADER
		matrixcursor.addRow(new Object[]{++_id,TYPE_HEADER,0,header_json.toString()});
    	
    	if(cursor_users.getCount()<1)return matrixcursor;
    	cursor_users.moveToFirst();
    	
    	do{    		
    		int user_id=0;
    		String name="";
    		String url_icon=null;
    		String statusvalue="Missing";    		
    		
    		user_id=cursor_users.getInt(cursor_users.getColumnIndex(DBContract.User.COLUMN_NAME_SERVER_ID));
    		name=cursor_users.getString(cursor_users.getColumnIndex(DBContract.User.COLUMN_NAME_NAME));
    		url_icon=cursor_users.getString(cursor_users.getColumnIndex(DBContract.User.COLUMN_NAME_URL_ICON));
    		
    		int statusid=cursor_users.getInt(cursor_users.getColumnIndex(DBContract.User.COLUMN_NAME_STATUS));
    		
    		JSONObject json=new JSONObject();
    		
    		try{
    			if(statusid==Profile.USERSTATUS_INVITE_INCOMING){
    				statusvalue="";
    				try {
						json.put("button", new JSONObject("{visible:true, tag:'accept', text:'"+this.getString(R.string.contact_item_button_accept_text)+"'}"));
						
					} catch (JSONException e) {						
					}
    			}else{
    				statusvalue=getResources().getStringArray(R.array.user_status_array)[statusid];
    				try {
    					json.put("button", new JSONObject("{visible : false}"));
    				} catch (JSONException e) {						
					}
    			}
        	}catch(ArrayIndexOutOfBoundsException e){
        		Log.e(TAG, "getUsersMatrixCursor ArrayIndexOutOfBoundsException e="+e.toString());
        		statusvalue=getResources().getStringArray(R.array.user_status_array)[0];
        	}    		
    		
    		try {
				json.put("user_id", user_id);	
				json.put("statusid", statusid);
				json.put("name", new JSONObject("{text: '"+name+"'}"));
				json.put("icon", new JSONObject("{image_url: '"+url_icon+"'}"));
				if(statusid==Profile.USERSTATUS_FRIEND){
					json.put("status", new JSONObject("{visible:false}"));
				}else{
					json.put("status", new JSONObject("{visible:true, text: '"+statusvalue+"'}"));
				}
				json.put("label", new JSONObject("{visible: false}"));
				
    		} catch (JSONException e) {
    			Log.e(TAG, "getUsersMatrixCursor JSONException e="+e.toString());
			}    		
    		
    		Log.d(TAG, "getUsersMatrixCursor json = "+json.toString());
    		    		
    		//Чтобы среди контактов не было заблокированных и незнакомцев
    		if( (user_id!=0)&&(user_id!=Session.getUserId()) && getFriendVisibility(statusid) ){
    			matrixcursor.addRow(new Object[]{++_id,TYPE_LINK_USER,String.valueOf(user_id),json.toString()});
    		}
    		    		
        	
    	}while(cursor_users.moveToNext());
    	    	
    	return matrixcursor;
    }
	
    protected MatrixCursor getGroupsMatrixCursor(int _id){

    	MatrixCursor matrixcursor=new MatrixCursor(new String[]{adapter.COLUMN_ID,adapter.COLUMN_TYPE,adapter.COLUMN_KEY,adapter.COLUMN_VALUE});    	
    	
    	//Create groups header
		JSONObject header_json=null;
		try {
			header_json=new JSONObject("{key:{text:'"+this.getString(R.string.fragment_contacts_groups_header_key)+"'}, value:{visible:false}, label:{visible:false}} ");
		} catch (JSONException e) {
			Log.e(TAG, "getGroupsMatrixCursor JSONException header_json e="+e.toString());
		}    			
		matrixcursor.addRow(new Object[]{++_id,TYPE_HEADER,0,header_json.toString()});
    	
				
    	if(cursor_groups.getCount()<1)return matrixcursor;
    	cursor_groups.moveToFirst();
    	
    	do{    		
    		int groupid=0;
    		String name="";
    		String url_icon=null;
    		
    		int count=0;
    		
    		groupid=cursor_groups.getInt(cursor_groups.getColumnIndex(DBContract.Group.COLUMN_NAME_SERVER_ID));
    		name=cursor_groups.getString(cursor_groups.getColumnIndex(DBContract.Group.COLUMN_NAME_NAME));
    		url_icon=cursor_groups.getString(cursor_groups.getColumnIndex(DBContract.Group.COLUMN_NAME_URL_ICON));
    		count=cursor_groups.getInt(cursor_groups.getColumnIndex("count"));
    		
    		int statusid=cursor_groups.getInt(cursor_groups.getColumnIndex(DBContract.Group.COLUMN_NAME_STATUS));
    		String status=getResources().getStringArray(R.array.status_in_group_array)[statusid];
    		
    		JSONObject json=new JSONObject();
    		
    		try {
				json.put("group_id", groupid);	
				json.put("statusid", statusid);
				json.put("name", new JSONObject("{text: '"+name+"'}"));
				json.put("icon", new JSONObject("{image_url: '"+url_icon+"'}"));
				json.put("button", new JSONObject("{visible:false}"));
				json.put("status", new JSONObject("{visible:false}"));
				
				json.put("label", new JSONObject("{tag: 'group_row_button', text_size:12, text_size_unit:"+TypedValue.COMPLEX_UNIT_DIP+", text: '"+"["+count+" "+getString(R.string.contact_item_group_label)+"]', text_color:"+R.color.color_green+", visible: true}"));
				
    		} catch (JSONException e) {
    			Log.e(TAG, "getGroupsMatrixCursor JSONException e="+e.toString());
			}    		
    		
    		Log.d(TAG, "getGroupsMatrixCursor json = "+json.toString());
    		    		
    		//Чтобы среди групп не было групп где я забанен, сам вышел или меня удалили
    		if( getGroupVisibility(statusid) ){
    			matrixcursor.addRow(new Object[]{++_id,TYPE_LINK_GROUP,String.valueOf(groupid),json.toString()});
    		}
    		    		
        	
    	}while(cursor_groups.moveToNext());
    	    	
    	return matrixcursor;
    }
	
    protected MatrixCursor getMeMatrixCursor(int _id){

    	MatrixCursor matrixcursor=new MatrixCursor(new String[]{adapter.COLUMN_ID,adapter.COLUMN_TYPE,adapter.COLUMN_KEY,adapter.COLUMN_VALUE});    	
    	
    	if(cursor_users.getCount()<1)return matrixcursor;
    	cursor_users.moveToFirst();
    	
    	Log.d(TAG, "getMeMatrixCursor");
    	
    	do{    		
    		int user_id=cursor_users.getInt(cursor_users.getColumnIndex(DBContract.User.COLUMN_NAME_SERVER_ID));
    		Log.d(TAG, "getMeMatrixCursor user_id = "+user_id);
    		if( (user_id==Session.getUserId()) ) {
    			
    			String name="";
        		String url_icon=null;
        		String statusvalue="Missing";  
    		
	    		name=cursor_users.getString(cursor_users.getColumnIndex(DBContract.User.COLUMN_NAME_NAME));
	    		url_icon=cursor_users.getString(cursor_users.getColumnIndex(DBContract.User.COLUMN_NAME_URL_ICON));
	    		
	    		int statusid=cursor_users.getInt(cursor_users.getColumnIndex(DBContract.User.COLUMN_NAME_STATUS));
	    		
	    		JSONObject json=new JSONObject();
	    		    		
	    		try {
					json.put("user_id", user_id);	
					json.put("statusid", statusid);
					json.put("name", new JSONObject("{text: '"+name+"'}"));
					json.put("icon", new JSONObject("{image_url: '"+url_icon+"'}"));
					json.put("label", new JSONObject("{visible: false}"));
					json.put("status", new JSONObject("{visible: false}"));
					json.put("button", new JSONObject("{visible: false}"));
					
	    		} catch (JSONException e) {
	    			Log.e(TAG, "getMeMatrixCursor JSONException e="+e.toString());
				}    		
	    		
	    		Log.d(TAG, "getMeMatrixCursor json = "+json.toString());
	    		
	    		//Create header_json
	    		JSONObject header_json=null;
	    		try {
					header_json=new JSONObject("{key:{text:'"+this.getString(R.string.fragment_contacts_me_header_key)+"'}, value:{visible:false}, label:{visible:false} } ");
				} catch (JSONException e) {
					Log.e(TAG, "getMeMatrixCursor JSONException header_json e="+e.toString());
				}    	
	        	
	    		
	        	//Add TYPE_HEADER
	    		matrixcursor.addRow(new Object[]{++_id,TYPE_HEADER,0,header_json.toString()});
	    		
	    		//Add TYPE_LINK_USER of me
	    		matrixcursor.addRow(new Object[]{++_id,TYPE_LINK_USER,String.valueOf(user_id),json.toString()});
	    		
    		    return 	matrixcursor;	
    		}
    		
    	}while(cursor_users.moveToNext());
    	    	
    	return matrixcursor;
    }
    
	//--------------Requests----------------------
	   
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
    	
    //--------------Utilities----------------------
    
	protected boolean getFriendVisibility(int statusid){
		
		switch(statusid){
			case (Profile.USERSTATUS_DEFAULT ):
			case (Profile.USERSTATUS_BLOCK_OUTGOING ):
			case (Profile.USERSTATUS_BLOCK_INCOMING ):
				return false;
		}
		
		return true;
		
	}
	
	protected boolean getGroupVisibility(int statusid){
		
		//Hide group if my status in group is
		switch(statusid){
			case (Profile.GROUPSTATUS_BANNED ):
			case (Profile.GROUPSTATUS_LEAVE ):
			case (Profile.GROUPSTATUS_REMOVED ):
			case (Profile.GROUPSTATUS_NOT_IN_GROUP ):
				return false;
		}
		
		return true;
		
	}
	
    protected void close(){
    	getFragmentManager().popBackStack();
    }

	private void toast(String msg){
		Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
	}
    
	//------------------Adapter Callbacks-----------------
    
    @Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    	
    	switch(adapter.getType(adapter.getCursor())){
		
    	case TYPE_LINK_USER:{
			int user_id=adapter.getKey(adapter.getCursor());
			
			Log.d(TAG,"onItemClick TYPE_LINK_USER user_id="+user_id);
			
			if(user_id==Session.getUserId()){
				Profile.showMe(getActivity(), getFragmentManager(), R.id.main_container);
			}else{
				Profile.showDetailsUser(user_id, getActivity(), getFragmentManager(), R.id.main_container);
			}
		}break;
		
		case TYPE_LINK_GROUP:{
			int group_id=adapter.getKey(adapter.getCursor());
			
			Profile.showGroup(group_id, getActivity(), getFragmentManager(), R.id.main_container);
			
		}break;
		
		
		}
	}

    @Override
	public void onClick(View v) {
		Log.d(TAG, "onClick v.getTag()="+v.getTag());
		
		if((v.getTag()!=null)&&(v.getTag().toString().equals("accept"))){
			Log.d(TAG, "onClick link_user_button");
			int userid=(Integer)v.getTag(R.id.details_item_link_button);
			
			//toast("Button clicked tag="+v.getTag()+" userid="+userid);
			
			Profile.friendOperation(getActivity(), userid, Profile.FRIENDOPERATION_CONFIRM);
				
			return;
		}
		
	}
	
	//-------------Loader<Cursor>------------------
		
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    	
    	Log.d(TAG, "onCreateLoader");

        String[] projection=null;
        Uri uri=null;

        switch(id) {
            case LOADER_USERS:
                projection = new String[]{
                        DBContract.User._ID,
                        DBContract.User.COLUMN_NAME_SERVER_ID,
                        DBContract.User.COLUMN_NAME_NAME,
                        DBContract.User.COLUMN_NAME_STATUS,
                        DBContract.User.COLUMN_NAME_URL_ICON
                        
                        
                };
                uri = DBContentProvider.URI_USER;
                                
                break;
            case LOADER_GROUPS:
                projection = new String[]{
                        DBContract.Group._ID,
                        DBContract.Group.COLUMN_NAME_SERVER_ID,
                        DBContract.Group.COLUMN_NAME_NAME,
                        DBContract.Group.COLUMN_NAME_STATUS,
                        DBContract.Group.COLUMN_NAME_URL_ICON,
                        "count"
                        
                        
                };
                uri = DBContentProvider.URI_CONTACTS_GROUPS;
                                
                break;
        }

        Log.d(TAG, "onCreateLoader uri="+uri.toString());
        
        CursorLoader cursorLoader = new CursorLoader(getActivity(),
                uri, projection, null, null, null);

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    	
    	Log.d(TAG, "onLoadFinished uri="+data.toString());

        switch(loader.getId()){
            case LOADER_USERS:
            	cursor_users=data;
            	adapter.swapCursor(createMergeCursor());
            	
                break;
                
            case LOADER_GROUPS:
            	cursor_groups=data;
            	adapter.swapCursor(createMergeCursor());
            	
                break;
                
        }
    }
	
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    	
    	Log.d(TAG, "onLoaderReset");    
    }

	

}
