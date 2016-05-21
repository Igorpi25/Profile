package com.ivanov.tech.profile.ui;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.ivanov.tech.CursorWrapper;
import com.ivanov.tech.multipletypesadapter.cursoradapter.CursorItemHolderButton;
import com.ivanov.tech.multipletypesadapter.cursoradapter.CursorItemHolderGridView;
import com.ivanov.tech.multipletypesadapter.cursoradapter.CursorItemHolderHeader;
import com.ivanov.tech.multipletypesadapter.cursoradapter.CursorItemHolderImageView;
import com.ivanov.tech.multipletypesadapter.cursoradapter.CursorItemHolderText;
import com.ivanov.tech.multipletypesadapter.cursoradapter.CursorMultipleTypesAdapter;
import com.ivanov.tech.profile.Profile;
import com.ivanov.tech.profile.R;
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
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class FragmentGroup extends SherlockDialogFragment implements LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener, OnClickListener {
	
	private static final String TAG = FragmentGroup.class
            .getSimpleName();    

	protected static final int TYPE_TEXT =0;
    protected static final int TYPE_TEXT_CLICKABLE =1;
    protected static final int TYPE_BUTTON =2; 
    protected static final int TYPE_GRIDVIEW =3;
    protected static final int TYPE_HEADER =4;
    protected static final int TYPE_PANORAMA =5;
    
    
    protected static final int GRIDVIEW_ADD_KEY =-1;
    protected static final int TEXT_KEY_NAME=0;
    protected static final int TEXT_KEY_UPLOAD_PANORAMA=1;
    protected static final int IMAGEVIEW_KEY_PANORAMA =2;
    
    protected ListView listview;
    
    protected CursorMultipleTypesAdapter adapter=null;
    
    protected CursorItemHolderGridView gridviewholder=null;

    protected static final int LOADER_GROUP_SERVER_ID = 1;
    protected static final int LOADER_GROUPUSERS_GROUPID = 2;

    //Source cursors from db
    protected Cursor cursor_group_server_id;
    protected Cursor cursor_groupusers;
    
    //Structured matrix cursor for gridview
    protected CursorWrapper gridviewcursor=new CursorWrapper(null);
    
    protected int group_server_id;
    
    protected int status_in_group_id=7;//Doesnt consist in group

    public static FragmentGroup newInstance(int group_server_id) {
    	FragmentGroup f = new FragmentGroup();  
        f.group_server_id=group_server_id;
        
        return f;
    }
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);  
        
        getLoaderManager().initLoader(LOADER_GROUP_SERVER_ID, null, this);
        getLoaderManager().initLoader(LOADER_GROUPUSERS_GROUPID, null, this);
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
        adapter.addItemHolder(TYPE_HEADER, new CursorItemHolderHeader(getActivity(),this));
        adapter.addItemHolder(TYPE_PANORAMA, new CursorItemHolderImageView(getActivity(),R.layout.details_item_panorama,R.id.details_item_panorama_imageview,this));
        
        gridviewcursor.setCursor(getGridViewCursor());
        
        gridviewholder = new CursorItemHolderGridView(getActivity(),gridviewcursor,new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				gridviewcursor.moveToPosition((int)position);
				
				int user_id=CursorMultipleTypesAdapter.getKey(gridviewcursor);					
				Log.d(TAG,"onItemClick TYPE_LINK_USER user_id="+user_id);
				
				if(user_id==GRIDVIEW_ADD_KEY){
					addUsers();
				}else{
					//Profile.showDetailsUser(user_id, getActivity(), getFragmentManager(), R.id.main_container);
					
					registerForContextMenu(listview);
					listview.showContextMenu();
					unregisterForContextMenu(listview);

				}
				
			}
        	
			
        });
        
        adapter.addItemHolder(TYPE_GRIDVIEW, gridviewholder);
        
        listview.setAdapter(adapter);
        
        listview.setOnItemClickListener(adapter);
        
        //adapter.changeCursor(createMergeCursor());
        
        return view;
    }
	
	@Override
    public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
		menu.clear();
		
		getSherlockActivity().getSupportActionBar().show();
		getSherlockActivity().getSupportActionBar().setTitle(" ");
    }
	
	@Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        
//	    MenuInflater inflater = getSherlockActivity().getMenuInflater();
//	    inflater.inflate(R.menu.group_gridview_context_menu, menu);
	    
        JSONObject json;
		try {
			json = new JSONObject(CursorMultipleTypesAdapter.getValue(gridviewcursor));
			int user_id=json.getInt("userid");
			int user_status=json.getInt("status");
			String user_name=json.getJSONObject("name").getString("text");
			
			Log.d(TAG,"onItemClick onCreateContextMenu user_id="+user_id);
			
			if(user_id==Session.getUserId()){
				menu.add(menu.NONE, R.id.group_context_menu_view_me, 1, R.string.group_context_menu_view_me);
				menu.add(menu.NONE, R.id.group_context_menu_leave, 2, R.string.group_context_menu_leave);
			}else{
				menu.add(menu.NONE, R.id.group_context_menu_view, 1, getString(R.string.group_context_menu_view)+" "+user_name);
				
				if( (user_status==0) && ((this.status_in_group_id==1)||(this.status_in_group_id==2)) )//if common user
					menu.add(menu.NONE, R.id.group_context_menu_make_admin, 2, getString(R.string.group_context_menu_make_admin_1)+getString(R.string.group_context_menu_make_admin_2));
				if( ((this.status_in_group_id==1)||(this.status_in_group_id==2)) && (user_status!=1) ){//if I'm super-admin or admin and user isn't super-admin
					menu.add(menu.NONE, R.id.group_context_menu_remove, 3, getString(R.string.group_context_menu_remove_1)+getString(R.string.group_context_menu_remove_2));
					
				}
				
			}
			
		} catch (JSONException e) { }
	    
    }
    
    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        
    	
    	int userid=CursorMultipleTypesAdapter.getKey(gridviewcursor);
    	
        if(item.getItemId() ==R.id.group_context_menu_view){            
        	Profile.showDetailsUser(userid, getActivity(), getFragmentManager(), R.id.main_container);
            return true;
            
        }else if(item.getItemId() ==R.id.group_context_menu_view_me){
        	Profile.showMe(getActivity(), getFragmentManager(), R.id.main_container);        	
            return true;
            	
        }else if(item.getItemId() ==R.id.group_context_menu_make_admin){
        	groupOperationUserStatus(userid,Profile.GROUPSTATUS_ADMIN);        	
            return true;
            	
        }else if(item.getItemId() ==R.id.group_context_menu_remove){
        	groupOperationUserStatus(userid,Profile.GROUPSTATUS_REMOVED);       	
        	return true;
        	
        }else if(item.getItemId() ==R.id.group_context_menu_leave){
        	groupOperationUserStatus(Session.getUserId(),Profile.GROUPSTATUS_LEAVE);        	
        	return true;
        	
        }else return super.onContextItemSelected(item);
        
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
   	
    	//Log.d(TAG, "getMatrixCursor");
    	
    	MatrixCursor matrixcursor=new MatrixCursor(new String[]{adapter.COLUMN_ID, adapter.COLUMN_TYPE, adapter.COLUMN_KEY, adapter.COLUMN_VALUE});    	
    	    	
    	if((cursor_group_server_id==null)||(cursor_group_server_id.getCount()<1))return matrixcursor;
    	cursor_group_server_id.moveToFirst();
    	    	
    	
    	JSONObject json;    
    	
    	String status_in_group_value=getResources().getStringArray(R.array.status_in_group_array)[this.status_in_group_id];
    	
    	if(cursor_group_server_id.getString(cursor_group_server_id.getColumnIndex(DBContract.Group.COLUMN_NAME_URL_FULL))!=null){
    	
	    	json=new JSONObject("{ imageview:{image_url:'"+cursor_group_server_id.getString(cursor_group_server_id.getColumnIndex(DBContract.Group.COLUMN_NAME_URL_FULL))+"' } }");    	
	    	matrixcursor.addRow(new Object[]{++_id,TYPE_PANORAMA,IMAGEVIEW_KEY_PANORAMA,json.toString()});
	    	
	    	//Header
			json= new JSONObject("{key:{text:' '}, value:{visible:false}, label:{visible:false} } ");	
	    	matrixcursor.addRow(new Object[]{++_id,TYPE_HEADER,0,json.toString()});
    	}
    	
    	json=new JSONObject("{visible:true}");    	
    	matrixcursor.addRow(new Object[]{++_id,TYPE_GRIDVIEW,0,json.toString()});
    	
    	json=new JSONObject("{key:{ text:'"+this.getString(R.string.fragment_details_group_name_key)+"' }, value:{text:'"+cursor_group_server_id.getString(cursor_group_server_id.getColumnIndex(DBContract.Group.COLUMN_NAME_NAME))+"'}, icon:{ } }");    	
    	matrixcursor.addRow(new Object[]{++_id,TYPE_TEXT_CLICKABLE,TEXT_KEY_NAME,json.toString()});
    	
    	json=new JSONObject("{key:{ text:'"+this.getString(R.string.fragment_details_group_status_key)+"' }, value:{text:'"+status_in_group_value+"'}, icon:{visible:false} }");    	
    	matrixcursor.addRow(new Object[]{++_id,TYPE_TEXT,0,json.toString()});
        
    	//Admin funcions
    	if( (this.status_in_group_id==1)||(this.status_in_group_id==2) ){
    	
    		//Header
    		json= new JSONObject("{key:{text:'"+this.getString(R.string.fragment_details_group_header_admin_key)+"'}, value:{visible:false}, label:{visible:false} } ");	
	    	matrixcursor.addRow(new Object[]{++_id,TYPE_HEADER,0,json.toString()});
	    	
	    	json=new JSONObject("{value:{ text:'"+this.getString(R.string.fragment_details_group_upload_profile_photo)+"' }, key:{visible : false}, icon:{image_res:'"+android.R.drawable.ic_menu_upload+"'} }");    	
	    	matrixcursor.addRow(new Object[]{++_id,TYPE_TEXT_CLICKABLE,TEXT_KEY_UPLOAD_PANORAMA,json.toString()});
    	
    	}
    	
    	if(getGroupVisibility(this.status_in_group_id))
    		addButtonNegative(matrixcursor,++_id,"leave",getString(R.string.fragment_details_group_button_leave_text));
    	    	
    	return matrixcursor;
    }
    
    //Используется как cursor для вложенного адаптера TYPE_GRIDVIEW
    protected MatrixCursor getGridViewCursor(){

    	MatrixCursor matrixcursor=new MatrixCursor(new String[]{adapter.COLUMN_ID, adapter.COLUMN_TYPE, adapter.COLUMN_KEY, adapter.COLUMN_VALUE});
    	
    	int _id=0;
    	
    	try{
    	
    		//Group users
    		if( (cursor_groupusers!=null) && (cursor_groupusers.getCount()>0)){
	    		cursor_groupusers.moveToFirst();
			    do{
			    	
			    	JSONObject json=new JSONObject();
			    	int userid=cursor_groupusers.getInt(cursor_groupusers.getColumnIndex("userid"));
			    	int user_status_in_group=cursor_groupusers.getInt(cursor_groupusers.getColumnIndex("status"));
			    	json=new JSONObject("{ name:{text:'"+cursor_groupusers.getString(cursor_groupusers.getColumnIndex("name"))+"'}, icon:{image_url:'"+cursor_groupusers.getString(cursor_groupusers.getColumnIndex("url_icon"))+"'}, button:{visible:false}, userid: "+userid+", status: "+user_status_in_group+" }");
				    
			    	if(user_status_in_group==1){
			    		json.put("label", new JSONObject("{visible:true, text_size:8, text_size_unit:"+TypedValue.COMPLEX_UNIT_DIP+", text:'"+getString(R.string.fragment_details_group_gridview_label_creater)+"'}"));
			    	}else			    	
			    	if(user_status_in_group==2){
			    		json.put("label", new JSONObject("{visible:true, text_size:10, text_size_unit:"+TypedValue.COMPLEX_UNIT_DIP+", text:'"+getString(R.string.fragment_details_group_gridview_label_admin)+"'}"));
			    	}else{
			    		json.put("label", new JSONObject("{visible:false}"));
			    	}
				    
				    if( getGroupVisibility(user_status_in_group) )
				    	matrixcursor.addRow(new Object[]{++_id,CursorItemHolderGridView.TYPE_GRIDVIEW_ITEM,userid,json.toString()});
			    	
			    	if(userid==Session.getUserId()){
				    	this.status_in_group_id=user_status_in_group;	
			    	}
			    }while(cursor_groupusers.moveToNext());
    		}
	    	
	    	//Add button
    		if( getGroupVisibility(this.status_in_group_id) ){
		    	JSONObject add_json=new JSONObject("{label:{visible:false}, name:{visible:false}, icon:{image_res:"+R.drawable.ic_add_user+"}, button:{visible:false} }");   	
		    	matrixcursor.addRow(new Object[]{++_id,CursorItemHolderGridView.TYPE_GRIDVIEW_ITEM,GRIDVIEW_ADD_KEY,add_json.toString()});
    		}
    	
    	}catch(JSONException e){
    		Log.e(TAG, "getGridViewCursor JSONException e="+e);
    	}
    	
    	return matrixcursor;
    }
    
    //--------------------Matrix cursor methods-------------------
        
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
    
    private void addButtonNegative(MatrixCursor matrixcursor, int index, String tag, String text) throws JSONException{
		JSONObject json=new JSONObject();
		json=new JSONObject("{button:{tag:'"+tag+"', text:'"+text+"',   background: "+R.drawable.drawable_button_dialog_negative+", text_color: "+R.color.color_white+"} }");    	
    	matrixcursor.addRow(new Object[]{++index,TYPE_BUTTON,0,json.toString()});
		
	}
    
    //---------------Utilities------------------------
        
    protected void addUsers(){
    	Profile.selectGroupAddUsers(getString(R.string.selectusers_textview_empty), group_server_id, new FragmentSelectUsers.ResultListener(){

			@Override
			public void onSuccess(ArrayList<Integer> usersid) {
				//Add selected users to group. For that we create message to server
				
				Log.e(TAG, "showSelectUsers onSuccess usersid.size = "+usersid.size());
				
				JSONObject json=new JSONObject();
				try {
					
					//Used in server to identify what operation it must to do
					json.put("operationid", Profile.GROUPOPERATION_ADD_USERS);
					json.put("groupid", group_server_id);
					
					//List of users that have to be added to group
					JSONArray users=new JSONArray();
					for(Integer userid : usersid){
						users.put(new JSONObject().put("id", userid));
					}								
					json.put("users", users);
					
				} catch (JSONException e) {
					Log.e(TAG, "showSelectUsers onSuccess JSONException e = "+e);
				}
				
				//Call TransportProfile of Communicator protocol
				Profile.groupOperation(getActivity(), json);
			}
			
		}, getActivity(), getFragmentManager(), R.id.main_container);
	
    }
    
    protected void groupOperationUserStatus(int userid,int status){
    	JSONObject json=new JSONObject();
    	
		try {
			json.put("operationid", Profile.GROUPOPERATION_USER_STATUS);
			json.put("groupid", group_server_id);
			json.put("userid", userid);
			json.put("status", status);			
		} catch (JSONException e) {}
		
		Profile.groupOperation(getActivity(), json);
    }
    
    private void updateMenuTitle(){
    	if((cursor_group_server_id==null)||(cursor_group_server_id.getCount()==0))return;
    	
    	cursor_group_server_id.moveToFirst();
    	String title=cursor_group_server_id.getString(cursor_group_server_id.getColumnIndex(DBContract.Group.COLUMN_NAME_NAME));
		getSherlockActivity().getSupportActionBar().setTitle(title);
    }
    
    protected void close(){
    	getFragmentManager().popBackStack();
    }
    
	private void toast(String msg){
		Log.d(TAG, msg);
		Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
	}

	//--------------Adapter Callbacks----------------------
    
  	
	@Override
  	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
  		switch(adapter.getType(adapter.getCursor())){
  		
  		case TYPE_TEXT_CLICKABLE:{
  			int key=adapter.getKey(adapter.getCursor());
  			
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
									JSONObject json=new JSONObject();
									
									try {
										json.put("operationid", Profile.GROUPOPERATION_SAVE);
										json.put("groupid", group_server_id);
										json.put("name", newValue);
									} catch (JSONException e) {}
									
									Profile.groupOperation(getActivity(), json);
								}
							}
							
						},getActivity(),getFragmentManager(),R.id.main_container);
						
					} catch (JSONException e) {
						Log.e(TAG, "onItemClick TYPE_TEXT_CLICKABLE.TEXT_KEY_NAME JSONException e="+e);
					}
		  			
		  			
	  			}break;
	  			
	  			case TEXT_KEY_UPLOAD_PANORAMA:{
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
	  								return Profile.getUrlGroupPanoramaUpload()+"/"+group_server_id;
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
  			
  			//toast("TYPE_TEXT_CLICKABLE item clicked key="+key);
  			
  		}break;
  		
  		
  		}
  		
  	}
  	
  	@Override
  	public void onClick(View v) {
  		
  		Log.d(TAG, "onClick v.tag="+v.getTag());	
  		
  		if((v.getTag()!=null)&&(v.getTag().toString().equals("close"))){
			//Log.d(TAG, "onClick close");			
			close();			
			return;
		}
  		
  		//if clicked View from TYPE_AVATAR
  		if(v.getTag(R.id.details_item_panorama_imageview)!=null){
  			//The key of View is IMAGEVIEW_KEY_AVATAR
  			if((Integer)v.getTag(R.id.details_item_panorama_imageview)==IMAGEVIEW_KEY_PANORAMA){  			
	  			
  				String url_full=cursor_group_server_id.getString(cursor_group_server_id.getColumnIndex(DBContract.Group.COLUMN_NAME_URL_FULL));	  			  			
	  			Profile.showPhoto(url_full, getActivity(), getFragmentManager(), R.id.main_container);
  			}
  			return;
  		}
  		
  		if((v.getTag()!=null)&&(v.getTag().toString().equals("leave"))){
			//Log.d(TAG, "onClick leave");	
  			groupOperationUserStatus(Session.getUserId(),Profile.GROUPSTATUS_LEAVE);  			
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
            case LOADER_GROUP_SERVER_ID:
                projection = new String[]{
                        DBContract.Group._ID,
                        DBContract.Group.COLUMN_NAME_SERVER_ID,
                        DBContract.Group.COLUMN_NAME_NAME,
                        DBContract.Group.COLUMN_NAME_STATUS,
                        DBContract.Group.COLUMN_NAME_URL_ICON,
                        DBContract.Group.COLUMN_NAME_URL_AVATAR,
                        DBContract.Group.COLUMN_NAME_URL_FULL
                        
                        
                };
                uri = Uri.parse(DBContentProvider.URI_GROUP+"/"+group_server_id);
                                
                break;
                
            case LOADER_GROUPUSERS_GROUPID:
                projection = new String[]{
                        DBContract.GroupUsers._ID,                        
                        DBContract.GroupUsers.COLUMN_NAME_USERID,
                        DBContract.GroupUsers.COLUMN_NAME_STATUS,
                        DBContract.User.COLUMN_NAME_NAME,     
                        DBContract.User.COLUMN_NAME_URL_ICON                        
                };
                uri = Uri.parse(DBContentProvider.URI_GROUPUSERS+"/"+group_server_id);
                                
                break;
                
            
        }

        //Log.d(TAG, "onCreateLoader uri="+uri.toString());
        
        CursorLoader cursorLoader = new CursorLoader(getActivity(),
                uri, projection, null, null, null);

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    	
        switch(loader.getId()){
            case LOADER_GROUP_SERVER_ID:
            	//Log.d(TAG, "onLoadFinished LOADER_GROUP_SERVER_ID");
            	cursor_group_server_id=data;            	
            	updateMenuTitle();
            	adapter.swapCursor(createMergeCursor());
            	
            	
                break;  
                
            case LOADER_GROUPUSERS_GROUPID:
            	//Log.d(TAG, "onLoadFinished LOADER_GROUPUSERS_GROUPID");
            	
            	cursor_groupusers=data;

            	gridviewcursor.setCursor(getGridViewCursor());
            	adapter.swapCursor(createMergeCursor());
            	
            	
            	
                break;
                
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    	
    	//Log.d(TAG, "onLoaderReset");    
    }

}
