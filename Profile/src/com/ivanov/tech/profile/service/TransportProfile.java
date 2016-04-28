package com.ivanov.tech.profile.service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.codebutler.android_websockets.WebSocketClient;
import com.ivanov.tech.communicator.service.TransportBase;
import com.ivanov.tech.profile.Profile;
import com.ivanov.tech.profile.provider.DBContentProvider;
import com.ivanov.tech.profile.provider.DBContract;
import com.ivanov.tech.session.Session;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class TransportProfile extends TransportBase{


	private static final String TAG = TransportProfile.class
            .getSimpleName();    
    
	
    public static final String JSON_TRANSPORT="transport";
    public static final String JSON_TYPE="type";   
    
    public static final int OUTGOING_TYPE_FRIENDOPERATION=1;
    public static final int OUTGOING_TYPE_GROUPOPERATION=2;
    public static final int OUTGOING_TYPE_MEOPERATION=3;
    
    public static final int INCOMING_TYPE_USER=1;
    public static final int INCOMING_TYPE_GROUP=2;
    public static final int INCOMING_TYPE_GROUP_USERS=3;    
       
    public TransportProfile(Context context) {    		
		super(context);
	}
        
//--------------------TransportProtocol----------------------
    
    @Override
    public boolean onOutgoingMessage(int transport, JSONObject json){
	    	    
	    if(transport==Profile.TRANSPORT_PROFILE) {
	    	Log.d(TAG, "onOutgoingMessage transport="+transport+" json="+json);
		    
	    	
	    	try {
	    		//Добавляем номер транспорта, на всякий случай если нету
				json.put(TransportProfile.JSON_TRANSPORT, Profile.TRANSPORT_PROFILE);
				
				//Также добавляем обязательно 
				json.put(TransportProfile.JSON_TRANSPORT, Profile.TRANSPORT_PROFILE);
				
			} catch (JSONException e) {
				Log.e(TAG, "onOutgoingMessage JSONException e="+e);
			    
			}
			sendMessage(0,0, json);	
			
			return true;
	    }
	    
	    return false;
	}
    
    @Override
    public boolean onIncomingMessage(int transport, JSONObject json){
		    	
	    if(transport==Profile.TRANSPORT_PROFILE) {
	    	Log.d(TAG, "onIncomingMessage transport="+transport+" json="+json);			
	    	try {
	    		
				switch(json.getInt(JSON_TYPE)){
				
					case INCOMING_TYPE_USER:{
						
				        if(!json.isNull("users")){	    	                        	
				            replaceUsers(this,json.getJSONArray("users"));				            	
				        }else{
				        	throw(new JSONException("INCOMING_TYPE_USER users isNull"));
				        }
				            
					}break;
					
					case INCOMING_TYPE_GROUP:{
						
				        if(!json.isNull("groups")){	    	                        	
				            replaceGroups(this,json.getJSONArray("groups"));				            	
				        }else{
				        	throw(new JSONException("INCOMING_TYPE_GROUP groups isNull"));
				        }
				            
					}break;
					
					case INCOMING_TYPE_GROUP_USERS:{
						
						if(!json.isNull("group_users")){	    	                        	
							replaceGroupUsers(this,json.getJSONArray("group_users"));				            	
				        }else{
				        	throw(new JSONException("INCOMING_TYPE_GROUP_USERS group_users isNull"));
				        }
						
				            
					}break;
					
				}
				
			} catch (JSONException e) {
				Log.e(TAG, "onIncomingMessage e = "+e);	
			}
	    	
			return true;
	    }
	    
	    return false;
	}    
    
    @Override
	public void onOutgoingFailed(int outgoing_failed_type, int message_id) {
    	Log.d(TAG, "onOutgoingFailed outgoing_failed_type="+outgoing_failed_type+" message_id="+message_id);
    	toast("TransportProfile onOutgoingFailed");
	}	
    
//------------WebsocketClientListener------------------------
    
    @Override
	public void onCreate(WebSocketClient websocketclient) {
		Log.d(TAG, "WebsocketClientListener.onCreate");
		
	}
    
    @Override
    public void onDisconnect(int code, String reason) {
        super.onDisconnect(code, reason);                
        Log.d(TAG, "WebsocketClientListener.onDisconnect ");
    	
    }

    @Override
    public void onError(Exception error) {
    	super.onError(error);        
        Log.d(TAG, "WebsocketClientListener.onError");
    	
    }

//---------------User Methods------------------
    
	public static void replaceUsers(Context context,JSONArray json_users){
		
		//deleteAllUsers(context);
		
		for (int i = 0; i < json_users.length(); i++) {
			 
			try {
				JSONObject json_user =  json_users.getJSONObject(i);
				replaceUser(context,json_user);				
				
			} catch (Exception e) {
				Log.e(TAG, "replaceUsers i="+i+" "+e.toString());				
			}
        }
	}
	
	public static void replaceUser(Context context,JSONObject json_user){
		
		ContentValues values=new ContentValues();
		int user_server_id=0;
		try {
			
			Log.d(TAG, "replaceUser replaceUser json_user:"+json_user);
			
			if(!json_user.isNull("name"))values.put(DBContract.User.COLUMN_NAME_NAME, json_user.getString("name"));
			if(!json_user.isNull("status"))values.put(DBContract.User.COLUMN_NAME_STATUS, json_user.getInt("status"));
			if(!json_user.isNull("changed_at"))values.put(DBContract.User.COLUMN_NAME_CHANGED_AT, json_user.getString("changed_at"));
			
			if(!json_user.isNull("id")){
				user_server_id=json_user.getInt("id");
				values.put(DBContract.User.COLUMN_NAME_SERVER_ID, user_server_id);
			}
			
			if(!json_user.isNull("avatars")){
				
				JSONObject json_avatars=json_user.getJSONObject("avatars");
								
				if(json_avatars.has("icon"))values.put(DBContract.User.COLUMN_NAME_URL_ICON, json_avatars.getString("icon"));					
				if(json_avatars.has("avatar"))values.put(DBContract.User.COLUMN_NAME_URL_AVATAR, json_avatars.getString("avatar"));
				if(json_avatars.has("full"))values.put(DBContract.User.COLUMN_NAME_URL_FULL, json_avatars.getString("full"));
				
			} 	
		 	
		} catch (JSONException e) {
			Log.e(TAG, "replaceUser replaceUser "+e.toString());				
		}
		
		replaceUser(context,user_server_id,values);			
		
	}
	
	public static void replaceUser(Context context,int userid,ContentValues values){
		 
		Log.d(TAG, "replaceUser userid="+userid+" values="+values);
		
		int affectedRowsCount=context.getContentResolver().update(Uri.parse(DBContentProvider.URI_USER+"/"+userid), values, userid+" = "+DBContract.User.COLUMN_NAME_SERVER_ID, null);	
		if(affectedRowsCount==0){
			context.getContentResolver().insert(DBContentProvider.URI_USER, values);
		}
	}
	
	public static int deleteAllUsers(Context context){
		 
		//Log.d(TAG, "deleteAllUsers");
		
		//Delete all users except owner-user
	 	int deletedCount=context.getContentResolver().delete(DBContentProvider.URI_USER, "( ? != 0 ) AND (? != ? )", new String[]{ DBContract.User.COLUMN_NAME_SERVER_ID, DBContract.User.COLUMN_NAME_SERVER_ID, String.valueOf(Session.getUserId()) });
	 	
        return deletedCount;
	}

//----------------Group Methods------------------
	
	public static void replaceGroups(Context context,JSONArray json_groups){
		
		for (int i = 0; i < json_groups.length(); i++) {
			 
			try {
				JSONObject json_group =  json_groups.getJSONObject(i);
				replaceGroup(context,json_group);				
				
			} catch (Exception e) {
				Log.e(TAG, "replaceGroups i="+i+" "+e.toString());				
			}
        }
	}
	
	public static void replaceGroup(Context context,JSONObject json_group){
		
		ContentValues values=new ContentValues();
		int group_server_id=0;
		try {
			
			Log.d(TAG, "replaceGroup json_group:"+json_group);
			
			if(!json_group.isNull("name"))values.put(DBContract.Group.COLUMN_NAME_NAME, json_group.getString("name"));
			if(!json_group.isNull("status"))values.put(DBContract.Group.COLUMN_NAME_STATUS, json_group.getInt("status"));
			if(!json_group.isNull("changed_at"))values.put(DBContract.Group.COLUMN_NAME_CHANGED_AT, json_group.getString("changed_at"));
			
			if(!json_group.isNull("id")){
				group_server_id=json_group.getInt("id");
				values.put(DBContract.Group.COLUMN_NAME_SERVER_ID, group_server_id);
			}
			
			if(!json_group.isNull("avatars")){
				
				JSONObject json_avatars=json_group.getJSONObject("avatars");
								
				if(json_avatars.has("icon"))values.put(DBContract.Group.COLUMN_NAME_URL_ICON, json_avatars.getString("icon"));					
				if(json_avatars.has("avatar"))values.put(DBContract.Group.COLUMN_NAME_URL_AVATAR, json_avatars.getString("avatar"));
				if(json_avatars.has("full"))values.put(DBContract.Group.COLUMN_NAME_URL_FULL, json_avatars.getString("full"));
				
			} 	
		 	
		} catch (JSONException e) {
			Log.e(TAG, "replaceGroup "+e.toString());				
		}
		
		replaceGroup(context,group_server_id,values);			
		
	}
	
	public static void replaceGroup(Context context,int groupid,ContentValues values){
		 
		Log.d(TAG, "replaceGroup groupid="+groupid+" values="+values);
		
		int affectedRowsCount=context.getContentResolver().update(Uri.parse(DBContentProvider.URI_GROUP+"/"+groupid), values, groupid+" = "+DBContract.Group.COLUMN_NAME_SERVER_ID, null);	
		if(affectedRowsCount==0){
			context.getContentResolver().insert(DBContentProvider.URI_GROUP, values);
		}
	}
	
	public static int deleteAllGroups(Context context){
		 
		//Log.d(TAG, "deleteAllGroups");
		
	 	int deletedCount=context.getContentResolver().delete(DBContentProvider.URI_GROUP, null, null);
	 	
        return deletedCount;
	}

//-----------------Group Users Methods---------------
	
	public static void replaceGroupUsers(Context context, JSONArray group_users){
		
		//Log.d(TAG, "replaceGroupUsers json_user="+group_users.toString());
				
		JSONObject json_user=null;
		for (int i = 0; i < group_users.length(); i++) {
			
			try {
				json_user =  group_users.getJSONObject(i);
				
				int status_in_group=4;
				int userid=-1;
				int groupid=-1;
				
				if(!json_user.isNull("userid"))
					userid=json_user.getInt("userid");
				
				if(!json_user.isNull("groupid"))
					groupid=json_user.getInt("groupid");
				
				if(!json_user.isNull("status_in_group"))
					status_in_group=json_user.getInt("status_in_group");
								
				
				replaceGroupUser(context,groupid,userid,status_in_group);								
				
			} catch (Exception e) {
				Log.e(TAG, "replaceGroupUsers json_user="+json_user.toString()+" "+e.toString());				
			}
        }
	}
	
	public static void replaceGroupUser(Context context,int group_server_id, int user_server_id, int status){
		 
		//Log.d(TAG, "addGroupUser group_server_id="+group_server_id);
		
		ContentValues values=new ContentValues();
	 	
	 	values.put(DBContract.GroupUsers.COLUMN_NAME_USERID, user_server_id);
	 	values.put(DBContract.GroupUsers.COLUMN_NAME_GROUPID, group_server_id);
	 	values.put(DBContract.GroupUsers.COLUMN_NAME_STATUS, status);
		
		int affectedRowsCount=context.getContentResolver().update(DBContentProvider.URI_GROUPUSERS, values, "( ( "+user_server_id+" = "+DBContract.GroupUsers.COLUMN_NAME_USERID+" ) AND ( "+group_server_id+" = "+DBContract.GroupUsers.COLUMN_NAME_GROUPID+" ) )", null);	
		if(affectedRowsCount==0){
			context.getContentResolver().insert(Uri.parse(DBContentProvider.URI_GROUPUSERS+"/"+group_server_id+"/"+user_server_id+"/"+status), null);
		}
		
        

	}	
	
//---------------Utilities----------------------------
 	
 	private String timestampToString(long timestamp){
		
	 	Date date=new Date(timestamp);
	 	
	 	SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	 	String asd=format.format(date);
	 	
	 	Log.d(TAG, "timestampToString timestamp="+timestamp+" asd="+asd);
	 	
	 	return asd;
	}
 	
 	private long stringToTimestamp(String date_time_string){
		
	 	long timestamp=Timestamp.valueOf(date_time_string).getTime();
	 	
	 	Log.d(TAG, "stringToTimestamp string="+date_time_string+" timestamp="+timestamp);
	 	
	 	return timestamp;
	}

 	private void toast(String msg){
		Log.d(TAG, msg);
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
   }
}
