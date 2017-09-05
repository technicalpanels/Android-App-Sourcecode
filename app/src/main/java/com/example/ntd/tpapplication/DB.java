package com.example.ntd.tpapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DB extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "TPDatabase";
    private static final String TABLE_MEMBER = "DataLog";

    public DB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("Create Table DataLog " +
                "(ID INTEGER PRIMARY KEY," +
                " dateInsert datetime," +
                " messageData TEXT(100));");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    public long DeleteAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            long result = db.delete(TABLE_MEMBER, "", null);
            db.close();
            return result;
        } catch (Exception e) {
            db.close();
            return -1;
        }
    }

    public long InsertData(String data) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues value = new ContentValues();
            value.put("dateInsert", getDateTime());
            value.put("messageData", data);
            long result = db.insert(TABLE_MEMBER, null, value);
            db.close();
            return result;
        } catch (Exception e) {
            db.close();
            return -1;
        }
    }

    public LogItems SelectLastRecord() {
        SQLiteDatabase db;
        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from DataLog order by dateInsert desc limit 1", null);
        try {
            LogItems item = new LogItems();

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        item.id = cursor.getInt(0);
                        item.date = df.parse(cursor.getString(1));
                        item.message = cursor.getString(2);
                    }
                    while (cursor.moveToNext());
                }
            }
            cursor.close();
            db.close();
            return item;
        } catch (Exception e) {
            cursor.close();
            db.close();
            return null;
        }
    }

    public List<LogItems> SelectLastRecord(int n)
    {
        SQLiteDatabase db;
        db = this.getReadableDatabase();
        String sql = String.format("select * from Datalog order by ID desc limit %d", n);
        Cursor cursor = db.rawQuery(sql, null);

        try {
            List<LogItems> items = new ArrayList<LogItems>();

            if (cursor == null) {
                cursor.close();
                db.close();
                return null;
            }

            cursor.moveToLast();

            do {
                LogItems item = new LogItems();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                item.id = cursor.getInt(0);
                item.date = df.parse(cursor.getString(1));
                item.message = cursor.getString(2);

                items.add(item);
            } while (cursor.moveToPrevious());

            cursor.close();
            db.close();
            return items;
        } catch (Exception e) {
            cursor.close();
            db.close();
            return null;
        }
    }

    public List<LogItems> SelectData() {
        SQLiteDatabase db;
        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from DataLog", null);
        try {
            List<LogItems> itemList = new ArrayList<LogItems>();

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        LogItems item = new LogItems();
                        item.id =  cursor.getInt(0);
                        item.date = df.parse(cursor.getString(1));
                        item.message = cursor.getString(2);
                        itemList.add(item);
                    }
                    while (cursor.moveToNext());
                }
            }
            cursor.close();
            db.close();
            return itemList;
        } catch (Exception e) {
            cursor.close();
            db.close();
            return null;
        }
    }
}
