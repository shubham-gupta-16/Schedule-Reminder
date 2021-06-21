package com.example.schedulemyreminder.utility;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;

import androidx.annotation.DrawableRes;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import com.example.schedulemyreminder.MainActivity;
import com.example.schedulemyreminder.R;
import com.example.schedulemyreminder.models.ReminderModel;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String ACTION_NOTIFICATION_TRIGGER = "notification triggered";

    @Override
    public void onReceive(Context context, Intent intent) {

        DBManager dbManager = new DBManager(context);
        ReminderModel reminder = dbManager.getReminder(intent.getIntExtra("id", 0));
        if (reminder == null)
            return;
        dbManager.deleteReminder(reminder.getId());

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        Intent intent1 = new Intent(context, MainActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
        taskStackBuilder.addParentStack(MainActivity.class);
        taskStackBuilder.addNextIntent(intent1);

        PendingIntent intent2 = taskStackBuilder.getPendingIntent(1, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "my_channel_01");

        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel("my_channel_01", "hello", NotificationManager.IMPORTANCE_HIGH);
            channel.setSound(alarmSound, new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build());
        }

        Notification notification = builder.setContentTitle("Reminder")
                .setContentText(reminder.getMessage()).setAutoCancel(true)
                .setSound(alarmSound)
                .setSmallIcon(R.drawable.ic_reminder_outline)
                .setLargeIcon(drawableToBitmap(context, R.drawable.ic_reminder))
                .setColor(context.getResources().getColor(R.color.purple_500))
                .setContentIntent(intent2)
                .setPriority(Notification.PRIORITY_HIGH)
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(1, notification);

        context.sendBroadcast(new Intent(ACTION_NOTIFICATION_TRIGGER));

    }

    public static Bitmap drawableToBitmap (Context context, @DrawableRes int res) {

        Drawable drawable = ContextCompat.getDrawable(context, res);
        if (drawable == null)
            return null;
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}
