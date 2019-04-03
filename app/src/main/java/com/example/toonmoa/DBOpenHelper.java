package com.example.toonmoa;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOpenHelper {
    private static final String DATABASE_NAME = "webtoon.db";
    private static final int DATABASE_VERSION = 1;
    public static SQLiteDatabase mDB;
    private DatabaseHelper mDBHelper;
    private Context mCtx;

    private class DatabaseHelper extends SQLiteOpenHelper{

        public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db){
            db.execSQL(Database.CreateDB._CREATE0);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + Database.CreateDB._TABLENAME0);
            onCreate(db);
        }
    }

    public DBOpenHelper(Context context){
        this.mCtx = context;
    }

    public DBOpenHelper open() throws SQLException{
        mDBHelper = new DatabaseHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
        mDB = mDBHelper.getWritableDatabase();
        return this;
    }

    public void create(){
        mDBHelper.onCreate(mDB);
    }

    public void upgrade(){
        mDBHelper.onUpgrade(mDB, 1, 2);
    }

    public void close(){
        mDB.close();
    }

    // Insert DB
    public long insertColumn(int id, String newest_title){
        ContentValues values = new ContentValues();
        values.put(Database.CreateDB.ID, id);
        values.put(Database.CreateDB.NEWEST_TITLE, newest_title);
        return mDB.insert(Database.CreateDB._TABLENAME0, null, values);
    }

    // Update DB
    public boolean updateColumn(int id, String newest_title){
        ContentValues values = new ContentValues();
        values.put(Database.CreateDB.ID, id);
        values.put(Database.CreateDB.NEWEST_TITLE, newest_title);
        return mDB.update(Database.CreateDB._TABLENAME0, values, "id=" + id, null) > 0;
    }

    // Delete All
    public void deleteAllColumns() {
        mDB.delete(Database.CreateDB._TABLENAME0, null, null);
    }

    // Delete DB
    public boolean deleteColumn(int id){
        return mDB.delete(Database.CreateDB._TABLENAME0, "id="+id, null) > 0;
    }
    // Select DB
    public Cursor selectColumns(){
        return mDB.query(Database.CreateDB._TABLENAME0, null, null, null, null, null, null);
    }

    public Cursor selectColumn(int id){
        Cursor c = mDB.rawQuery("SELECT * FROM " + Database.CreateDB._TABLENAME0 + " where id=" + id +";", null);
        return c;
    }

    // sort by column
    public Cursor sortColumn(String sort){
        Cursor c = mDB.rawQuery( "SELECT * FROM usertable ORDER BY " + sort + ";", null);
        return c;
    }
}
