package com.example.schedulemyreminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schedulemyreminder.adapters.AdapterReminders;
import com.example.schedulemyreminder.utility.AlarmReceiver;
import com.example.schedulemyreminder.utility.DBManager;
import com.example.schedulemyreminder.models.ReminderModel;
import com.example.schedulemyreminder.utility.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DBManager dbManager;
    private RecyclerView recyclerView;
    private AdapterReminders adapter;
    private List<ReminderModel> reminderModelList;
    private TextView empty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_about)
                    startActivity(new Intent(MainActivity.this, AboutActivity.class));
                return false;
            }
        });

        dbManager = new DBManager(this);
        empty = findViewById(R.id.empty);
        recyclerView = findViewById(R.id.recyclerView);
        FloatingActionButton add = findViewById(R.id.floatingButton);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addReminder();
            }
        });

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(linearLayoutManager);
        setItemsInRecyclerView();
        listenAlarmReceiver();
    }

    private void addReminder() {
        Utils.buildReminderDialog(this, null, null, new Utils.OnDialogSubmitListener() {
            @Override
            public void onSubmit(String message, String date, Calendar calendar) {
                dbManager.addReminder(message, date);
                int id = dbManager.getLastReminderID();

                Utils.addAlarm(MainActivity.this, id, calendar.getTimeInMillis());
                updateRecyclerView();
                Toast.makeText(MainActivity.this, "Reminder added Successfully", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateRecyclerView(){
        List<ReminderModel> list = dbManager.getAllReminders();
        reminderModelList.clear();
        reminderModelList.addAll(list);
        adapter.notifyDataSetChanged();
        if (reminderModelList.size() > 0) {
            empty.setVisibility(View.INVISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            empty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);
        }
    }

    private void setItemsInRecyclerView() {

        reminderModelList = new ArrayList<>();
        adapter = new AdapterReminders(this, reminderModelList);
        recyclerView.setAdapter(adapter);

        updateRecyclerView();

        adapter.setOnReminderListener(new AdapterReminders.OnReminderListener() {
            @Override
            public void onUpdate(String message, String date, Calendar calendar, int oldId, int position) {
                Utils.removeAlarm(MainActivity.this, oldId);
                dbManager.deleteReminder(oldId);
                dbManager.addReminder(message, date);
                int id = dbManager.getLastReminderID();
                Utils.addAlarm(MainActivity.this, id, calendar.getTimeInMillis());
                Toast.makeText(MainActivity.this, "Updated", Toast.LENGTH_SHORT).show();

                updateRecyclerView();
            }

            @Override
            public void onDelete(int oldId, int position) {
                Utils.removeAlarm(MainActivity.this, oldId);
                dbManager.deleteReminder(oldId);
                reminderModelList.remove(position);
                adapter.notifyItemRemoved(position);
                adapter.notifyItemRangeChanged(position, reminderModelList.size());
                if (reminderModelList.size() > 0) {
                    empty.setVisibility(View.INVISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                } else {
                    empty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void listenAlarmReceiver(){
        registerReceiver(broadcastReceiver, new IntentFilter(AlarmReceiver.ACTION_NOTIFICATION_TRIGGER));
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateRecyclerView();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
}
