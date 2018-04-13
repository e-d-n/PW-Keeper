package com.androidstackoverflow.pwkeeper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

import static com.androidstackoverflow.pwkeeper.MainActivity.str;
import static com.androidstackoverflow.pwkeeper.MainActivity.THE_PATH;

public class DBHelper extends SQLiteOpenHelper{
    public static final String DB_NAME = THE_PATH + "PassWord";
    public static final Integer DB_VERSION = 1;

    public static final String TABLE_PW = "masterPW";
    public static final String Col_IDI = "IDI";
    public static final String Col_MPW = "mpw";

    public static final String TABLE_INFO = "webINFO";
    public static final String Col_ID = "ID";
    public static final String Col_WS = "website";
    public static final String Col_UN = "username";
    public static final String Col_PW = "password";
    public static final String Col_SQ = "question";
    public static final String Col_SA = "answer";
    public static final String Col_NOTES = "notes";

    private static final String MAKE_TABLE_PW = "CREATE TABLE IF NOT EXISTS " +  TABLE_PW +
            "(" + Col_IDI + " INTEGER PRIMARY KEY," + Col_MPW + " TEXT " + ")";

    private static final String MAKE_TABLE = "CREATE TABLE IF NOT EXISTS  " +  TABLE_INFO +
            "(" + Col_ID + " INTEGER PRIMARY KEY," + Col_WS + " TEXT, " + Col_UN + " TEXT, " + Col_PW + " TEXT, " + Col_SQ + " TEXT, " + Col_SA + " TEXT, " + Col_NOTES +" TEXT "+ ")";

    static SQLiteDatabase db;

    public DBHelper(Context context){
        super(context,DB_NAME,null,DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL( MAKE_TABLE_PW );
        db.execSQL( MAKE_TABLE );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PW);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INFO);
        onCreate(db);
    }
    /*All the CODE above is the design for the Database and its Tables */
    /*=================================================================*/
    /* Code Below are the Helper CRUD Functions */
    /*==========================================*/

    public String getCol_MPW(){

        db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_PW;
        Cursor cursor = db.rawQuery(query,null);
        if(cursor.moveToFirst()){
            str = cursor.getString(cursor.getColumnIndex(Col_MPW));
        }
        cursor.close();
        db.close();
        return str;
        // This routine called from MainActivity determine's if a
        // password has been entered in the db table named "TABLE_PW"
        // See onLoad() method in MainActivity
    }

    /* Update Record in Database*/
    public void updateDBRow(String rowid,String website, String username, String password, String question,String answer, String notes){

        db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(Col_WS,website);
        cv.put(Col_UN,username);
        cv.put(Col_PW,password);
        cv.put(Col_SQ,question);
        cv.put(Col_SA,answer);
        cv.put(Col_NOTES,notes);

        /*NOTE WHERE THE quotation MARKS ARE */
        db.update(TABLE_INFO,cv, Col_ID + " = ?",new String[] { rowid });
        db.close();
    }

    /* Insert into database table named "TABLE_INFO" */
    public void insertIntoDB(String website, String username, String password, String question,String answer, String notes){

        // 1. get reference to writable DB
        db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues cv = new ContentValues();

        cv.put(Col_WS,website);
        cv.put(Col_UN,username);
        cv.put(Col_PW,password);
        cv.put(Col_SQ,question);
        cv.put(Col_SA,answer);
        cv.put(Col_NOTES,notes);

        // 3. insert
        db.insert(TABLE_INFO, null, cv);
        // 4. close
        db.close();
    }

    /* Retrieve ALL data from database table named "TABLE_INFO" */
    public List<DatabaseModel> getDataFromDB(){

        List<DatabaseModel> dbList = new ArrayList<>();

        String query = "SELECT * FROM " + TABLE_INFO;

        db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query,null);

        if (cursor.moveToFirst()){
            do {
                DatabaseModel model = new DatabaseModel();
                model.setRowid(cursor.getString(0));
                model.setWebsite(cursor.getString(1));
                model.setUsernane(cursor.getString(2));
                model.setPassword(cursor.getString(3));
                model.setQuestion(cursor.getString(4));
                model.setAnswer(cursor.getString(5));
                model.setNotes(cursor.getString(6));

                dbList.add(model);
            }while (cursor.moveToNext());
        }
        db.close();
        cursor.close();
        return dbList;
    }

    /* Delete a record from database table named "TABLE_INFO" */
    /* based on the selected records id in Col_ID*/
    public void deleteDBRow(String rowid){

        db = this.getWritableDatabase();
        db.delete(TABLE_INFO, Col_ID + " = ?", new String[] { rowid });
        db.close();
    }

}// END CLASS
