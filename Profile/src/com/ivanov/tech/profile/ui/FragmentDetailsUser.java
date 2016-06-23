package com.ivanov.tech.profile.ui;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.ivanov.tech.multipletypesadapter.cursoradapter.CursorItemHolderButton;
import com.ivanov.tech.multipletypesadapter.cursoradapter.CursorItemHolderImageView;
import com.ivanov.tech.multipletypesadapter.cursoradapter.CursorItemHolderText;
import com.ivanov.tech.multipletypesadapter.cursoradapter.CursorMultipleTypesAdapter;
import com.ivanov.tech.profile.Profile;
import com.ivanov.tech.profile.R;
import com.ivanov.tech.profile.provider.DBContentProvider;
import com.ivanov.tech.profile.provider.DBContract;

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

public class FragmentDetailsUser extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener, OnClickListener {
	
	private static final String TAG = FragmentDetailsUser.class
            .getSimpleName();    

	protected static final int TYPE_TEXT =0;
    protected static final int TYPE_BUTTON =1; 
    protected static final int TYPE_AVATAR =2;
    
    protected static final int IMAGEVIEW_KEY_AVATAR = 3;

    protected ListView listview;
    
    protected CursorMultipleTypesAdapter adapter=null;

    protected static final int LOADER_USER_SERVER_ID = 1; 
    
    protected Cursor cursor_user_server_id;
    
    protected int user_server_id;

    public static FragmentDetailsUser newInstance(int user_server_id) {
    	FragmentDetailsUser f = new FragmentDetailsUser();  
        f.user_server_id=user_server_id;
        
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
    	
    	String status="Missing";
    	
    	int statusid=cursor_user_server_id.getInt(cursor_user_server_id.getColumnIndex(DBContract.User.COLUMN_NAME_STATUS));
    	try{
    		
    		status=getResources().getStringArray(R.array.user_status_array)[statusid];
    	}catch(ArrayIndexOutOfBoundsException e){
    		Log.e(TAG, "User's status index is undefined");
    		status=getResources().getStringArray(R.array.user_status_array)[0];
    	}
    	
    	json=new JSONObject("{ imageview:{image_url:'"+cursor_user_server_id.getString(cursor_user_server_id.getColumnIndex(DBContract.User.COLUMN_NAME_URL_AVATAR))+"'} }");    	
    	matrixcursor.addRow(new Object[]{++_id,TYPE_AVATAR,IMAGEVIEW_KEY_AVATAR,json.toString()});
    	
    	json=new JSONObject("{key:{ text:'"+this.getString(R.string.fragment_details_user_name)+"' }, value:{text:'"+cursor_user_server_id.getString(cursor_user_server_id.getColumnIndex(DBContract.User.COLUMN_NAME_NAME))+"'}, icon:{visible:false} }");    	
    	matrixcursor.addRow(new Object[]{++_id,TYPE_TEXT,0,json.toString()});
    	
    	json=new JSONObject("{key:{ text:'"+this.getString(R.string.fragment_details_user_status)+"' }, value:{text:'"+status+"'}, icon:{visible:false} }");    	
    	matrixcursor.addRow(new Object[]{++_id,TYPE_TEXT,0,json.toString()});
        	
    	switch(statusid){    	
	    	case Profile.USERSTATUS_DEFAULT:{
	    		addButtonPositive(matrixcursor,_id,"add",Profile.FRIENDOPERATION_ADD);
	    	}break;
	    	
	    	case Profile.USERSTATUS_INVITE_OUTGOING:{
	    		addButtonNormal(matrixcursor,_id,"cancel",Profile.FRIENDOPERATION_CANCEL);    		
	    	}break;
	    	
	    	case Profile.USERSTATUS_INVITE_INCOMING:{
	    		addButtonPositive(matrixcursor,_id,"confirm",Profile.FRIENDOPERATION_CONFIRM);
	    		addButtonNegative(matrixcursor,_id,"decline",Profile.FRIENDOPERATION_DECLINE);
	    		addButtonAlter(matrixcursor,_id,"block",Profile.FRIENDOPERATION_BLOCK);
	    	}break;
	    	
	    	case Profile.USERSTATUS_FRIEND:{
	    		addButtonNegative(matrixcursor,_id,"delete",Profile.FRIENDOPERATION_DELETE);
	    		addButtonAlter(matrixcursor,_id,"block",Profile.FRIENDOPERATION_BLOCK);
	    	}break;
	    	
	    	case Profile.USERSTATUS_BLOCK_OUTGOING:{
	    		addButtonNormal(matrixcursor,_id,"unlock",Profile.FRIENDOPERATION_UNLOCK);    		
	    	}break;
	    	
	    	case Profile.USERSTATUS_BLOCK_INCOMING:{
	    		json=new JSONObject("{button:{tag:'close', text:'"+getString(R.string.fragment_details_button_close_text)+"'} }");    	
	        	matrixcursor.addRow(new Object[]{++_id,TYPE_BUTTON,0,json.toString()});
	    	}break;
		
    	}	
    	
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

	private String getFriendOperationName(int id){
		return getActivity().getResources().getStringArray(R.array.friend_operation_array)[id];
	}
  
	private void addButtonNormal(MatrixCursor matrixcursor, int index, String tag, int operation) throws JSONException{
		JSONObject json=new JSONObject();
		json=new JSONObject("{button:{tag:'"+tag+"', text:'"+getFriendOperationName(operation)+"' } }");    	
    	matrixcursor.addRow(new Object[]{++index,TYPE_BUTTON,0,json.toString()});
		
	}
	
	private void addButtonPositive(MatrixCursor matrixcursor, int index, String tag, int operation) throws JSONException{
		JSONObject json=new JSONObject();
		json=new JSONObject("{button:{tag:'"+tag+"', text:'"+getFriendOperationName(operation)+"',   background: "+R.drawable.drawable_button_dialog_positive+", text_color: "+R.color.color_white+"} }");    	
    	matrixcursor.addRow(new Object[]{++index,TYPE_BUTTON,0,json.toString()});
		
	}
	
	private void addButtonNegative(MatrixCursor matrixcursor, int index, String tag, int operation) throws JSONException{
		JSONObject json=new JSONObject();
		json=new JSONObject("{button:{tag:'"+tag+"', text:'"+getFriendOperationName(operation)+"',   background: "+R.drawable.drawable_button_dialog_negative+", text_color: "+R.color.color_white+"} }");    	
    	matrixcursor.addRow(new Object[]{++index,TYPE_BUTTON,0,json.toString()});
		
	}
	
	private void addButtonAlter(MatrixCursor matrixcursor, int index, String tag, int operation) throws JSONException{
		JSONObject json=new JSONObject();
		json=new JSONObject("{button:{tag:'"+tag+"', text:'"+getFriendOperationName(operation)+"',   background: "+R.drawable.drawable_button_dialog_alter+", text_color: "+R.color.color_white+"} }");    	
    	matrixcursor.addRow(new Object[]{++index,TYPE_BUTTON,0,json.toString()});
		
	}
	
	//--------------Adapter Callbacks----------------------
    
  	@Override
  	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
  		switch(adapter.getType(adapter.getCursor())){
  		
  		case TYPE_TEXT:{
  			int key=adapter.getKey(adapter.getCursor());
  			
  			toast("TYPE_TEXT_CLICKABLE item clicked key="+key);
  			
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
  		
  		if((v.getTag()!=null)&&(v.getTag().toString().equals("add"))){
			Log.d(TAG, "onClick add");			
			Profile.friendOperation(getActivity(),user_server_id,Profile.FRIENDOPERATION_ADD);			
			return;
		}
		
		if((v.getTag()!=null)&&(v.getTag().toString().equals("cancel"))){
			Log.d(TAG, "onClick cancel");			
			Profile.friendOperation(getActivity(),user_server_id,Profile.FRIENDOPERATION_CANCEL);			
			return;
		}
		
		if((v.getTag()!=null)&&(v.getTag().toString().equals("confirm"))){
			Log.d(TAG, "onClick confirm");			
			Profile.friendOperation(getActivity(),user_server_id,Profile.FRIENDOPERATION_CONFIRM);			
			return;
		}
		
		if((v.getTag()!=null)&&(v.getTag().toString().equals("decline"))){
			Log.d(TAG, "onClick decline");			
			Profile.friendOperation(getActivity(),user_server_id,Profile.FRIENDOPERATION_DECLINE);			
			return;
		}
		
		if((v.getTag()!=null)&&(v.getTag().toString().equals("block"))){
			Log.d(TAG, "onClick block");			
			Profile.friendOperation(getActivity(),user_server_id,Profile.FRIENDOPERATION_BLOCK);			
			return;
		}
		
		if((v.getTag()!=null)&&(v.getTag().toString().equals("unlock"))){
			Log.d(TAG, "onClick unlock");			
			Profile.friendOperation(getActivity(),user_server_id,Profile.FRIENDOPERATION_UNLOCK);			
			return;
		}
		
		if((v.getTag()!=null)&&(v.getTag().toString().equals("delete"))){
			Log.d(TAG, "onClick delete");			
			Profile.friendOperation(getActivity(),user_server_id,Profile.FRIENDOPERATION_DELETE);			
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
                uri = Uri.parse(DBContentProvider.URI_USER+"/"+user_server_id);
                                
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
