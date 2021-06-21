package com.example.schedulemyreminder.utility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.schedulemyreminder.models.ReminderModel;

import java.util.Date;
import java.util.List;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//       Re-subscribe on device reboot.
        Utils.bootUpdateReminders(context);
    }
}
