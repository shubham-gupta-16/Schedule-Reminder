package com.example.schedulemyreminder.utility;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.schedulemyreminder.models.ReminderModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DBManager extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "carDB2";
    private static final String REMINDERS = "reminders";
//    private XStore xStore;

    public DBManager(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
        assert context != null;
//        xStore = new XStore(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + REMINDERS + " (id INTEGER PRIMARY KEY AUTOINCREMENT, message VARCHAR, date VARCHAR);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void addReminder(String message, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("message", message);
        contentValues.put("date", date);
        db.insert(REMINDERS, null, contentValues);
    }

    public int getLastReminderID() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("select max(id) from " + REMINDERS, null);
        if (c.moveToFirst())
            return c.getInt(0);
        c.close();
        return 0;
    }

    public void deleteReminder(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(REMINDERS, "id=?", new String[]{"" + id});
    }

    public ReminderModel getReminder(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.query(REMINDERS, new String[]{"id", "message", "date"}, "id=" + id, null, null, null, "date");
        if (c.moveToFirst()) {
            return new ReminderModel(c.getInt(0), c.getString(1), c.getString(2));
        } else {
            c.close();
            return null;
        }
    }

    public List<ReminderModel> getAllReminders() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.query(REMINDERS, new String[]{"id", "message", "date"}, null, null, null, null, null);
        List<ReminderModel> models = new ArrayList<>();
        final long currentTime = Calendar.getInstance().getTimeInMillis();
        while (c.moveToNext()) {
            String date = c.getString(2);
            Date d = Utils.getDate(date);
            if (d == null){
                deleteReminder(c.getInt(0));
                continue;
            }
            Calendar calendar = Utils.getCalender(d);
            if (calendar.getTimeInMillis() - currentTime <= 0) {
                deleteReminder(c.getInt(0));
            } else
                models.add(new ReminderModel(c.getInt(0), c.getString(1), date));
        }
        c.close();
        return models;
    }
}
