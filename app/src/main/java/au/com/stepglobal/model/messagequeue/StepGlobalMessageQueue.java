package au.com.stepglobal.model.messagequeue;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;

import au.com.stepglobal.connector.IUARTDataConnector;
import au.com.stepglobal.connector.UARTDataConnector;
import au.com.stepglobal.model.BaseModel;
import au.com.stepglobal.utils.GsonFactory;

/**
 * Created by Sandeep on 09/07/2017.
 */

public class StepGlobalMessageQueue implements IUARTDataConnector.IUARTDataReceiver {
    private static StepGlobalMessageQueue instance = null;
    ArrayList<String> messageQueue = new ArrayList<>();
    Context mContext;
    IUARTDataConnector connector;
    AlarmManager alarmManager;

    private StepGlobalMessageQueue(Context context) {
        mContext = context;
        connector = new UARTDataConnector(this);
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        startQueue();
    }

    private void startQueue() {
        Intent intent = new Intent(mContext, StepGlobalMessageQueueHandler.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);

// 20 minutes.
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+(1000 * 60 * 20),
                1000 * 60 * 20, alarmIntent);
    }

    public static StepGlobalMessageQueue getInstance(Context context) {
        if (instance == null) {
            instance = new StepGlobalMessageQueue(context);

        }

        return instance;
    }

    public void addMessageWait(String message) {
        synchronized (this) {
            BaseModel baseModel = GsonFactory.getGson().fromJson(message, BaseModel.class);
            if (baseModel.getTimestart() != -1 || baseModel.getTimestamp() != -1)
                messageQueue.add(message);
        }
    }

    public void checkMessageWaitLocked(String message) {
        synchronized (this) {
            int i = 0;
            int size = messageQueue.size();
            while (i < size) {
                String messages = messageQueue.get(i);
                BaseModel baseModel = GsonFactory.getGson().fromJson(message, BaseModel.class);
                BaseModel inQueue = GsonFactory.getGson().fromJson(messages, BaseModel.class);
                if (baseModel.getTimestamp() != -1 && inQueue.getTimestamp() == baseModel.getTimestamp()) {
                    messageQueue.remove(i);
                    size--;
                } else if (baseModel.getTimestart() != -1 && inQueue.getTimestart() == baseModel.getTimestart()) {
                    messageQueue.remove(i);
                    size--;
                } else {
                    i++;
                }
            }
        }
    }

    public void sendPendingMessages() {
        for (String message : messageQueue) {
            connector.sendData(message);
        }
    }


    @Override
    public void onDataReceive(String data) {
        checkMessageWaitLocked(data);
    }
}
