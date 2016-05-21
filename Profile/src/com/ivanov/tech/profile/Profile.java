package com.ivanov.tech.profile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.ivanov.tech.connection.Connection;
import com.ivanov.tech.profile.provider.DBContentProvider;
import com.ivanov.tech.profile.provider.DBContract;
import com.ivanov.tech.profile.service.CommunicatorService;
import com.ivanov.tech.profile.service.TransportProfile;
import com.ivanov.tech.profile.ui.FragmentContacts;
import com.ivanov.tech.profile.ui.FragmentDetailsUser;
import com.ivanov.tech.profile.ui.FragmentGroup;
import com.ivanov.tech.profile.ui.FragmentMe;
import com.ivanov.tech.profile.ui.FragmentSelectUsers;
import com.ivanov.tech.profile.ui.FragmentSelectUsers.ResultListener;
import com.ivanov.tech.profile.ui.FragmentPhoto;
import com.ivanov.tech.profile.ui.FragmentSearchContact;
import com.ivanov.tech.profile.ui.FragmentSelectFriends;
import com.ivanov.tech.profile.ui.FragmentSelectGroupAddUsers;
import com.ivanov.tech.profile.ui.FragmentText;
import com.ivanov.tech.session.Session;

public class Profile {
	
	private static final String TAG = "Profile";
	
	public static final int  TRANSPORT_PROFILE=3;
		
	public static final int FRIENDOPERATION_ADD=0;
	public static final int FRIENDOPERATION_CANCEL=1;
	public static final int FRIENDOPERATION_CONFIRM=2;
	public static final int FRIENDOPERATION_DECLINE=3;
	public static final int FRIENDOPERATION_BLOCK=4;
	public static final int FRIENDOPERATION_UNLOCK=5;
	public static final int FRIENDOPERATION_DELETE=6;

	public static final int USERSTATUS_DEFAULT=0;
	public static final int USERSTATUS_INVITE_OUTGOING=1;
	public static final int USERSTATUS_INVITE_INCOMING=2;
	public static final int USERSTATUS_FRIEND=3;
	public static final int USERSTATUS_BLOCK_OUTGOING=4;
	public static final int USERSTATUS_BLOCK_INCOMING=5;
	
	public static final int GROUPOPERATION_ADD_USERS=0;
	public static final int GROUPOPERATION_SAVE=1;
	public static final int GROUPOPERATION_CREATE=2;
	public static final int GROUPOPERATION_USER_STATUS=4;
	
	public static final int GROUPSTATUS_COMMON_USER=0;
	public static final int GROUPSTATUS_ADMIN_CREATER=1;
	public static final int GROUPSTATUS_ADMIN=2;
	public static final int GROUPSTATUS_BANNED=3;
	public static final int GROUPSTATUS_MISSING=4;
	public static final int GROUPSTATUS_LEAVE=5;
	public static final int GROUPSTATUS_REMOVED=6;
	public static final int GROUPSTATUS_NOT_IN_GROUP=7;
	
	public static final int[]  AVATAR_UPLOAD_SIZE= {600,600};
    public static final String AVATAR_UPLOAD_FILE_PART_NAME = "image";
    
    //----------------Preferences-----------------------------
    
    static private SharedPreferences preferences=null;
    
	private static final String PREF = "Profile";
    
    public static final String PREF_URL_SEARCH_CONTACT="PREF_URL_SEARCH_CONTACT";
    public static final String PREF_URL_AVATAR_UPLOAD="PREF_URL_AVATAR_UPLOAD";
    public static final String PREF_URL_GROUP_PANORAMA_UPLOAD="PREF_URL_GROUP_PANORAMA_UPLOAD";
    public static final String PREF_URL_CREATE_GROUP="PREF_URL_CREATE_GROUP";
		
    public static String getUrlSearchContact(){
    	return preferences.getString(PREF_URL_SEARCH_CONTACT, null);    	
    }
    
    public static String getUrlAvatarUpload(){
    	return preferences.getString(PREF_URL_AVATAR_UPLOAD, null);    	
    }
    
    public static String getUrlGroupPanoramaUpload(){
    	return preferences.getString(PREF_URL_GROUP_PANORAMA_UPLOAD, null);    	
    }
    
    public static String getUrlCreateGroup(){
    	return preferences.getString(PREF_URL_CREATE_GROUP, null);    	
    }
    
    public static void Initialize(Context context, String url_searchcontact, String url_avatarupload, String url_grouppanoramaupload, String url_creategroup){
    	if(preferences==null){
    		preferences=context.getApplicationContext().getSharedPreferences(PREF, 0);
    	}
    	
    	preferences.edit().
	    putString(PREF_URL_SEARCH_CONTACT, url_searchcontact).
	    putString(PREF_URL_AVATAR_UPLOAD, url_avatarupload).
    	putString(PREF_URL_GROUP_PANORAMA_UPLOAD, url_grouppanoramaupload).
    	putString(PREF_URL_CREATE_GROUP, url_creategroup).commit();
	    
    }
    	
    //-----------------UI Fragments----------------------
    
	public static void showSearchContact(final Context context, final FragmentManager fragmentManager, final int container){

		if( (fragmentManager.findFragmentByTag("SearchContact")!=null) && (fragmentManager.findFragmentByTag("SearchContact").isVisible()) )return;
			
		FragmentSearchContact fragment=FragmentSearchContact.newInstance();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(container, fragment, "SearchContact");
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.addToBackStack("SearchContact");
        fragmentTransaction.commit();
	}
	
	public static void showSelectUsers(final String tittle, FragmentSelectUsers.ResultListener listener, final Context context, final FragmentManager fragmentManager, final int container){

		if( (fragmentManager.findFragmentByTag("SelectUsers")!=null) && (fragmentManager.findFragmentByTag("SelectUsers").isVisible()) )return;
			
		FragmentSelectUsers fragment=FragmentSelectUsers.newInstance(tittle, listener);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(container, fragment, "SelectUsers");
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.addToBackStack("SelectUsers");
        fragmentTransaction.commit();
	}
	
	public static void showSelectFriends(final String tittle, FragmentSelectUsers.ResultListener listener, final Context context, final FragmentManager fragmentManager, final int container){

		if( (fragmentManager.findFragmentByTag("SelectFriends")!=null) && (fragmentManager.findFragmentByTag("SelectFriends").isVisible()) )return;
			
		FragmentSelectFriends fragment=FragmentSelectFriends.newInstance(tittle, listener);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(container, fragment, "SelectFriends");
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.addToBackStack("SelectFriends");
        fragmentTransaction.commit();
	}
	
	public static void selectGroupAddUsers(final String tittle, int groupid, FragmentSelectUsers.ResultListener listener, final Context context, final FragmentManager fragmentManager, final int container){

		if( (fragmentManager.findFragmentByTag("SelectGroupAddUsers")!=null) && (fragmentManager.findFragmentByTag("SelectGroupAddUsers").isVisible()) )return;
			
		FragmentSelectGroupAddUsers fragment=FragmentSelectGroupAddUsers.newInstance(tittle,groupid, listener);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(container, fragment, "SelectGroupAddUsers");
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.addToBackStack("SelectGroupAddUsers");
        fragmentTransaction.commit();
	}
	
	public static void showDetailsUser(final int userid,final Context context, final FragmentManager fragmentManager, final int container){
    	
		if( (fragmentManager.findFragmentByTag("DetailsUser")!=null) && (fragmentManager.findFragmentByTag("DetailsUser").isVisible()) )return;
		
		FragmentDetailsUser fragment=FragmentDetailsUser.newInstance(userid);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(container, fragment, "DetailsUser");
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.addToBackStack("DetailsUser");
        fragmentTransaction.commit();
	}
	
	public static void showGroup(final int groupid,final Context context, final FragmentManager fragmentManager, final int container){
    	
		if( (fragmentManager.findFragmentByTag("Group")!=null) && (fragmentManager.findFragmentByTag("Group").isVisible()) )return;
		
		FragmentGroup fragment=FragmentGroup.newInstance(groupid);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(container, fragment, "Group");
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.addToBackStack("Group");
        fragmentTransaction.commit();
	}
	
	public static void showMe(final Context context, final FragmentManager fragmentManager, final int container){
    	
		if( (fragmentManager.findFragmentByTag("Me")!=null) && (fragmentManager.findFragmentByTag("Me").isVisible()) )return;
		
		FragmentMe fragment=FragmentMe.newInstance();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(container, fragment, "Me");
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.addToBackStack("Me");
        fragmentTransaction.commit();
	}
	
	public static void showText(String key, String value, FragmentText.ResultListener listener, final Context context, final FragmentManager fragmentManager, final int container){
    	
		if( (fragmentManager.findFragmentByTag("Text")!=null) && (fragmentManager.findFragmentByTag("Text").isVisible()) )return;
		
		FragmentText fragment=FragmentText.newInstance(key,value,listener);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(container, fragment, "Text");
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.addToBackStack("Text");
        fragmentTransaction.commit();
	}
	
	public static void showPhoto(String url, final Context context, final FragmentManager fragmentManager, final int container){
    	
		if( (fragmentManager.findFragmentByTag("Photo")!=null) && (fragmentManager.findFragmentByTag("Photo").isVisible()) )return;
		
		FragmentPhoto fragment=FragmentPhoto.newInstance(url);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(container, fragment, "Photo");
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.addToBackStack("Photo");
        fragmentTransaction.commit();
	}
	
	public static void showContacts(final Context context, final FragmentManager fragmentManager, final int container,final boolean backStack){
		
		if( (fragmentManager.findFragmentByTag("Contacts")!=null) && (fragmentManager.findFragmentByTag("Contacts").isVisible()) )return;
		
        FragmentContacts fragmentdetails=FragmentContacts.newInstance();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(container, fragmentdetails, "Contacts");
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_NONE);
        if(backStack)
           fragmentTransaction.addToBackStack("Contacts");           
        fragmentTransaction.commit();
    	
    }
	
	//----------Messages and Requests------------------------------
  	
   public static void searchContactRequest(final String value,final Context context, final SearchResultListener searchstatus) {

      	final String tag = TAG+" searchContactRequest ";  
      	        
      	Log.d(TAG,tag);
      	
      	final ProgressDialog pDialog = new ProgressDialog(context);
      	pDialog.setMessage("Search contact ...");
      	pDialog.setCancelable(true);      	
      	
      	pDialog.show();
      	
      	final StringRequest request = new StringRequest(Method.POST,
      			getUrlSearchContact(),
      	                new Response.Listener<String>() {
      	 
      	                    @Override
      	                    public void onResponse(String response) {
      	                        Log.d(TAG, tag+" onResponse " + response);
      	                        
      	                        JSONObject json;
      	                        
      	                        try{
      	                        	json=new JSONObject(response);
      	                        
      	                        	if(!json.isNull("success")){
      	                        		
      	                        		if(json.getInt("success")==0)throw new JSONException(json.getString("message"));
      	                        		
      	                        		int userid=json.getInt("userid");
      	                        		
      	                        		JSONArray json_users=json.getJSONArray("users");
      	                        		
      	                        		TransportProfile.replaceUsers(context, json_users);
      	                        		
      	                        		if(searchstatus!=null)searchstatus.onSuccess(userid);
      	                        		
  	    	                        }else throw (new JSONException("success=null"));
  	    	                        
      	                        }catch(JSONException e){
      	                        	Log.e(TAG,tag+"onResponse JSONException "+e.toString());
      	                        	
      	                        	if(searchstatus!=null)searchstatus.onFailed(e.toString());
      	                        }finally{
      	                        	pDialog.hide();
      	                        }
      	                        
      	                    }
      	                    
      	                }, new Response.ErrorListener() {
      	 
      	                    @Override
      	                    public void onErrorResponse(VolleyError error) {
      	                    	//Log.e(TAG,tag+"onErrorResponse "+error.toString());
      	                        pDialog.hide();
      	                        
      	                      if(searchstatus!=null)searchstatus.onFailed(error.toString());
      	                    }
      	                }){
      		
      		
      		@Override
              public Map<String, String> getHeaders() throws AuthFailureError {
                  HashMap<String, String> headers = new HashMap<String, String>();
                  
                  headers.put("Content-Type", "application/x-www-form-urlencoded");
                  headers.put("Api-Key", Session.getApiKey());
                  
                  return headers;
              }
      		
      		@Override
            public Map<String, String> getParams(){
    			Map<String, String> params = new HashMap<String, String>();
    			
    			params.put("value", value);
    			
                return params;
            }
      		
      		
      	};
      	 
      	pDialog.setOnCancelListener(new OnCancelListener(){

			@Override
			public void onCancel(DialogInterface dialog) {
				request.cancel();
			}
      		
      	});
      	
      	request.setTag(tag);
      	Volley.newRequestQueue(context.getApplicationContext()).add(request);

      }
  	
   public static void friendOperation(final Context context, final int friendid,final int operationid) {
	   
	   Log.d(TAG,"friendOperationRequest friendid="+friendid+" operationid="+operationid);
	   
	   JSONObject json=new JSONObject();
	   
	   try {
		   json.put("type", TransportProfile.OUTGOING_TYPE_FRIENDOPERATION);
		   json.put("friendid", friendid);
		   json.put("operationid", operationid);
	   } catch (JSONException e) {
		   Log.d(TAG,"friendOperationRequest JSONException e="+e);		   
	   }
	   
	   sendCommunicatorMessage(context,json);
	  
   }
   
   public static void meOperation(final Context context, JSONObject me_json) {
	   
	   Log.d(TAG,"meOperation user_json="+me_json);
	   
	   //Берем исходный JSON  и дополняем
	   JSONObject json=me_json;
	   
	   try {
		   json.put("type", TransportProfile.OUTGOING_TYPE_MEOPERATION);
	   } catch (JSONException e) {
		   Log.d(TAG,"meOperation JSONException e="+e);		   
	   }
	   
	   sendCommunicatorMessage(context,json);
	  
   }
   
   public static void groupOperation(final Context context, JSONObject operation_json) {
	   
	   Log.d(TAG,"groupOperation operation_json="+operation_json);
	   
	   //Берем исходный JSON  и дополняем
	   JSONObject json=operation_json;
	   
	   try {
		   json.put("type", TransportProfile.OUTGOING_TYPE_GROUPOPERATION);
	   } catch (JSONException e) {
		   Log.d(TAG,"meOperation JSONException e="+e);		   
	   }
	   
	   sendCommunicatorMessage(context,json);
	  
   }
   
   public static void createGroupRequest(final JSONArray users, final Context context, final CreateGroupResultListener createstatus) {

     	final String tag = TAG+" createGroupRequest ";
     	        
     	//Log.e(TAG,tag);
     	
     	final ProgressDialog pDialog = new ProgressDialog(context);
     	pDialog.setMessage("Creating group ...");
     	pDialog.setCancelable(false);
     	
     	pDialog.show();
     	
     	StringRequest request = new StringRequest(Method.POST,
     			getUrlCreateGroup(),
     	                new Response.Listener<String>() {
     	 
     	                    @Override
     	                    public void onResponse(String response) {
     	                        Log.d(TAG, tag+" onResponse " + response);
     	                        
     	                        JSONObject json;
     	                        
     	                        try{
     	                        	json=new JSONObject(response);
     	                        
     	                        	if(!json.isNull("success")){
     	                        		
     	                        		if(json.getInt("success")==0)throw new JSONException(json.getString("message"));
     	                        		
     	                        		int groupid=json.getInt("groupid");
     	                        		if(createstatus!=null)createstatus.onCreated(groupid);
     	                        		
 	    	                        }else throw (new JSONException("success=null"));
 	    	                        
     	                        }catch(JSONException e){
     	                        	Log.e(TAG,tag+"onResponse JSONException "+e.toString());
     	                        	
     	                        	if(createstatus!=null)createstatus.onFailed(e.toString());
     	                        }finally{
     	                        	pDialog.hide();
     	                        }
     	                        
     	                    }
     	                    
     	                }, new Response.ErrorListener() {
     	 
     	                    @Override
     	                    public void onErrorResponse(VolleyError error) {
     	                    	//Log.e(TAG,tag+"onErrorResponse "+error.toString());
     	                        pDialog.hide();
     	                        
     	                      if(createstatus!=null)createstatus.onFailed(error.toString());
     	                    }
     	                }){     		
     		
     		@Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                 HashMap<String, String> headers = new HashMap<String, String>();
                 
                 headers.put("Content-Type", "application/x-www-form-urlencoded");
                 headers.put("Api-Key", Session.getApiKey());
                 
                 return headers;
            }
     		
     		@Override
            public Map<String, String> getParams(){
    			Map<String, String> params = new HashMap<String, String>();
    			
    			params.put("users", users.toString());
    			
                return params;
            }
     		
     		
     	};
     	 
     	request.setTag(tag);
     	Volley.newRequestQueue(context.getApplicationContext()).add(request);

     }
 	
   //---------------CommunicatorService------------
   
   public static void sendCommunicatorMessage(Context context, JSONObject json) {
		
		Log.d(TAG, "sendCommunicatorMessage json="+json.toString());
		
	    Intent intent=new Intent(context,CommunicatorService.class);
	    intent.putExtra("userid", Session.getUserId());
	    intent.putExtra("transport", Profile.TRANSPORT_PROFILE);
	    intent.putExtra("json", json.toString());
		
	    context.startService(intent);
	}
   
   public static void startCommunicatorService(Context context){
	   Log.d(TAG,"startCommunicatorService userid="+Session.getUserId());
	   
     	Intent intent=new Intent(context,CommunicatorService.class);
     	intent.putExtra("userid", Session.getUserId());
     	
     	context.startService(intent);
   }
      
   //--------------Listeners-------------------------

   public interface SearchResultListener{
  		void onSuccess(int userid);
  		void onFailed(String message);
   }
  	
   public interface FriendOperationListener{
  		void onSuccess(int friendid,int operationid, int status);
  		void onFailed(int friendid,int operationid, String message);
   }
   
   public interface CreateGroupResultListener{
		void onCreated(int groupid);
		void onFailed(String message);
	}
   
   public interface CloseListener{
	   void onClose();
   }

}
