package com.example.schedulemyreminder.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.schedulemyreminder.R;
import com.example.schedulemyreminder.models.ReminderModel;
import com.example.schedulemyreminder.utility.Utils;

import java.util.Calendar;
import java.util.List;

public class AdapterReminders extends RecyclerView.Adapter<AdapterReminders.MyViewHolder> {

    private final Context context;
    private final List<ReminderModel> reminderModelList;
    private OnReminderListener onReminderListener;

    public void setOnReminderListener(OnReminderListener onReminderListener) {
        this.onReminderListener = onReminderListener;
    }

    public AdapterReminders(Context context, List<ReminderModel> reminderModelList) {
        this.reminderModelList = reminderModelList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.reminder_item, viewGroup, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int i) {

        ReminderModel reminder = reminderModelList.get(i);
        if (!reminder.getMessage().equals(""))
            holder.messageText.setText(reminder.getMessage());
        else
            holder.messageText.setHint("No Message");
        holder.dateText.setText(reminder.getDate());

        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.buildReminderDialog(context, reminder.getMessage(), reminder.getDate(), new Utils.OnDialogSubmitListener() {
                    @Override
                    public void onSubmit(String message, String date, Calendar calendar) {
                        if (onReminderListener != null)
                            onReminderListener.onUpdate(message, date, calendar, reminder.getId(), i);
                    }
                });
            }
        });
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(reminder.getMessage());
                builder.setMessage("Do your want to remove this reminder");
                builder.setNegativeButton("NO", null);
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (onReminderListener != null)
                            onReminderListener.onDelete(reminder.getId(), i);
                    }
                });
                builder.create().show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return reminderModelList.size();
    }

    public interface OnReminderListener {
        void onUpdate(String message, String date, Calendar calendar, int id, int position);

        void onDelete(int id, int position);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private final TextView messageText, dateText;
        private final ImageButton editButton, deleteButton;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.textView1);
            dateText = itemView.findViewById(R.id.textView2);
            editButton = itemView.findViewById(R.id.edit);
            deleteButton = itemView.findViewById(R.id.delete);
        }
    }
}
