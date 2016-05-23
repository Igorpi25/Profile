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
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
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

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.ivanov.tech.profile.Profile;
import com.ivanov.tech.profile.R;
import com.ivanov.tech.profile.provider.DBContentProvider;
import com.ivanov.tech.profile.provider.DBContract;
import com.ivanov.tech.session.Session;
import com.meetme.android.horizontallistview.HorizontalListView;

/**
 * Created by Igor on 09.05.15.
 */
public class FragmentSelectFriends extends FragmentSelectUsers{

    private static final String TAG = FragmentSelectFriends.class
            .getSimpleName();    
        
    public static FragmentSelectFriends newInstance(String tittle,ResultListener resultlistener) {
    	FragmentSelectFriends f = new FragmentSelectFriends();
    	f.tittle=tittle;
    	f.resultlistener=resultlistener;
        return f;
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
        
        String selection= "( ( "+DBContract.User.COLUMN_NAME_STATUS+" = "+Profile.USERSTATUS_FRIEND+" ) AND ( "+DBContract.User.COLUMN_NAME_SERVER_ID+" != "+Session.getUserId()+" ) )";
        		
        CursorLoader cursorLoader = new CursorLoader(getActivity(),
                uri, projection, selection, null, null);

        return cursorLoader;
    }

}
