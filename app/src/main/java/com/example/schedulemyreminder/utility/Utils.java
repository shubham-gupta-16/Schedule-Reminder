package com.example.schedulemyreminder.utility;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.schedulemyreminder.R;
import com.example.schedulemyreminder.models.ReminderModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static android.content.Context.ALARM_SERVICE;

public class Utils {

    public static void addAlarm(Context context, int id, long timeInMillis) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, _getPendingIntentForAlarm(context, id));
    }

    public static void removeAlarm(Context context, int id) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager.cancel(_getPendingIntentForAlarm(context, id));
    }

    private static PendingIntent _getPendingIntentForAlarm(Context context, int id) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("id", id);
        return PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @SuppressLint("SetTextI18n")
    public static void buildReminderDialog(Context context, String message, String date, OnDialogSubmitListener onDialogSubmitListener) {
        Dialog dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_popup);

        final TextView textView = dialog.findViewById(R.id.date);
        View select = dialog.findViewById(R.id.selectDate);
        Button add = dialog.findViewById(R.id.addButton);
        final EditText messageText = dialog.findViewById(R.id.message);

        if (message != null)
            messageText.setText(message);
        if (date != null){
            textView.setText(date);
            textView.setTextColor(context.getResources().getColor(R.color.textPrimary));
        }

        if (date != null || message != null)
            add.setText("UPDATE");

        final Calendar newCalender = Calendar.getInstance();
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, final int year, final int month, final int dayOfMonth) {

                        final Calendar newDate = Calendar.getInstance();
                        Calendar newTime = Calendar.getInstance();
                        TimePickerDialog time = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                                newDate.set(year, month, dayOfMonth, hourOfDay, minute, 0);
                                Calendar tem = Calendar.getInstance();
                                Log.w("TIME", System.currentTimeMillis() + "");
                                if (newDate.getTimeInMillis() - tem.getTimeInMillis() > 0) {
                                    textView.setText(newDate.getTime().toString());
                                    textView.setTextColor(context.getResources().getColor(R.color.textPrimary));
                                } else
                                    Toast.makeText(context, "Invalid date and time", Toast.LENGTH_SHORT).show();

                            }
                        }, newTime.get(Calendar.HOUR_OF_DAY), newTime.get(Calendar.MINUTE), true);
                        time.show();
                    }
                }, newCalender.get(Calendar.YEAR), newCalender.get(Calendar.MONTH), newCalender.get(Calendar.DAY_OF_MONTH));

                dialog.getDatePicker().setMinDate(System.currentTimeMillis());
                dialog.show();
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String msg = messageText.getText().toString().trim();
                String date = textView.getText().toString().trim();


                if (date.equals("")) {
                    Toast.makeText(context, "Please pick date and time", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (msg.isEmpty()) {
                    Toast.makeText(context, "Please enter any message", Toast.LENGTH_SHORT).show();
                    return;
                }
                final long currentTime = Calendar.getInstance().getTimeInMillis();
                Date d = getDate(date);
                if (d == null) {
                    Toast.makeText(context, "Invalid date and time", Toast.LENGTH_SHORT).show();
                    return;
                }
                Calendar calendar = Utils.getCalender(d);
                if (calendar.getTimeInMillis() - currentTime <= 0) {
                    Toast.makeText(context, "Invalid date and time", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (onDialogSubmitListener != null)
                    onDialogSubmitListener.onSubmit(msg, date, getCalender(d));
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public static void bootUpdateReminders(Context context){
        Toast.makeText(context, "Reminders Updated", Toast.LENGTH_SHORT).show();
        DBManager dbManager = new DBManager(context);
        List<ReminderModel> reminderModelList = dbManager.getAllReminders();
        for (ReminderModel reminder : reminderModelList) {
            Date d = Utils.getDate(reminder.getDate());
            if (d == null)
                continue;
            Utils.addAlarm(context, reminder.getId(), Utils.getCalender(d).getTimeInMillis());
        }
    }

    public interface OnDialogSubmitListener {
        void onSubmit(String message, String date, Calendar calendar);
    }

    public static Date getDate(String date) {
        SimpleDateFormat format =
                new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
        try {
            return format.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Calendar getCalender(Date d) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+5:30"));
        calendar.setTime(d);
        calendar.set(Calendar.SECOND, 0);
        return calendar;
    }
}
