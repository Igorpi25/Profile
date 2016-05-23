package com.ivanov.tech.profile.provider;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

public final class DBContract {

    private static final String TAG = "DBContract";

    public DBContract(){}

    public static final String DATABASE_NAME = "profile.db";
    public static final int DATABASE_VERSION = 1;
    
        
    public static abstract class User implements BaseColumns {

        public static final String TABLE_NAME = "user";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_STATUS = "status";
        public static final String COLUMN_NAME_CAR = "car";
        public static final String COLUMN_NAME_URL_ICON = "url_icon";
        public static final String COLUMN_NAME_URL_AVATAR = "url_avatar";
        public static final String COLUMN_NAME_URL_FULL = "url_full";
        public static final String COLUMN_NAME_SERVER_ID = "server_id";
        public static final String COLUMN_NAME_CHANGED_AT = "changed_at";
        
        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                		COLUMN_NAME_SERVER_ID + " INTEGER DEFAULT 0, " +
                        COLUMN_NAME_NAME + " STRING DEFAULT NULL, " +
                        COLUMN_NAME_STATUS + " INTEGER DEFAULT NULL, " +
                        COLUMN_NAME_CAR+ " STRING DEFAULT NULL ," +
                        COLUMN_NAME_URL_ICON+ " STRING DEFAULT NULL ," +
                        COLUMN_NAME_URL_AVATAR+ " STRING DEFAULT NULL ," +
                        COLUMN_NAME_URL_FULL+ " STRING DEFAULT NULL ," +
                        COLUMN_NAME_CHANGED_AT+ " TIMESTAMP DEFAULT NULL" +
                 ");";
    }
    
    public static abstract class Group implements BaseColumns {

        public static final String TABLE_NAME = "groups";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_STATUS = "status";
        public static final String COLUMN_NAME_URL_ICON = "url_icon";
        public static final String COLUMN_NAME_URL_AVATAR = "url_avatar";
        public static final String COLUMN_NAME_URL_FULL = "url_full";
        public static final String COLUMN_NAME_SERVER_ID = "server_id";
        public static final String COLUMN_NAME_CHANGED_AT = "changed_at";
        

        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                		COLUMN_NAME_SERVER_ID + " INTEGER DEFAULT 0, " +
                        COLUMN_NAME_NAME + " STRING DEFAULT NULL, " +
                        COLUMN_NAME_STATUS + " INTEGER DEFAULT NULL, " +
                        COLUMN_NAME_URL_ICON+ " STRING DEFAULT NULL ," +
                        COLUMN_NAME_URL_AVATAR+ " STRING DEFAULT NULL ," +
                        COLUMN_NAME_URL_FULL+ " STRING DEFAULT NULL ," +
                        COLUMN_NAME_CHANGED_AT+ " TIMESTAMP DEFAULT NULL" +
                 ");";
    }
    
    public static abstract class GroupUsers implements BaseColumns {

        public static final String TABLE_NAME = "groupusers";        
        public static final String COLUMN_NAME_GROUPID = "groupid";
        public static final String COLUMN_NAME_USERID = "userid";
        public static final String COLUMN_NAME_STATUS = "status";
        public static final String COLUMN_NAME_CHANGED_AT = "changed_at";
        

        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_NAME_GROUPID + " INTEGER DEFAULT 0, " +
                        COLUMN_NAME_USERID + " INTEGER DEFAULT 0, " +
                        COLUMN_NAME_STATUS + " INTEGER DEFAULT NULL, " +
                        COLUMN_NAME_CHANGED_AT+ " TIMESTAMP DEFAULT NULL" +
                 ");";
    }

    public static void onCreate(SQLiteDatabase db) {
        Log.w(TAG, "onCreate");

        db.execSQL(User.CREATE_TABLE);
        db.execSQL(Group.CREATE_TABLE);
        db.execSQL(GroupUsers.CREATE_TABLE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

    }

}