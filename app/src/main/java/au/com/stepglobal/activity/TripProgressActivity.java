package au.com.stepglobal.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import au.com.stepglobal.LoginActivity;
import au.com.stepglobal.R;
import au.com.stepglobal.activity.view.UARTBaseActivityView;
import au.com.stepglobal.global.BundleKey;
import au.com.stepglobal.global.TripType;
import au.com.stepglobal.model.Time;
import au.com.stepglobal.model.TripObject;
import au.com.stepglobal.preference.StepGlobalPreferences;
import au.com.stepglobal.utils.GsonFactory;
import au.com.stepglobal.utils.StepGlobalUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static au.com.stepglobal.utils.StepGlobalConstants.SERVER_RESPONSE_WAIT;

/**
 * Created by hiten.bahri on 16/06/2017.
 */

public class TripProgressActivity extends UARTBaseActivityView {

    @BindView(R.id.trip_status_trip_started_value)
    TextView tripStartedValue;
    @BindView(R.id.trip_status_trip_time_value)
    TextView tripTimeValue;
    @BindView(R.id.trip_status_trip_type_value)
    TextView tripTypeTextViewValue;
    @BindView(R.id.trip_status_trip_reason_value)
    TextView tripStatusReasonValue;
    @BindView(R.id.btn_trip_progress_activity_end_trip)
    Button endTripButton;

    private TripObject tripObject;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_status);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getExtras().containsKey(BundleKey.TRIP_OBJECT.key)) {
                tripObject = intent.getExtras().getParcelable(BundleKey.TRIP_OBJECT.key);
                if (tripObject != null) {
                    setProgressState(tripObject);
                }
            }
        }
    }

    @Override
    public void onReceiveMessage(String message) {
        Time time = GsonFactory.getGson().fromJson(message, Time.class);
        messageHandler.removeMessages(MESSAGE_WAIT_TIMEOUT);
        Message msg = new Message();
        msg.what = MESSAGE_END_TRIP;
        msg.obj = time;
        messageHandler.sendMessage(msg);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (tripObject != null) {
            if (tripObject.getStatus().equalsIgnoreCase("Start")) {
                finishAffinity();
            }
        }
    }

    private void setProgressState(TripObject tripObject) {
        tripReasonValue(tripObject.getTripType().display.equalsIgnoreCase(TripType.PRIVATE.toString()));
        tripTypeTextViewValue.setText(tripObject.getTripType().display);
        long startTime = tripObject.getStartTime();
        tripStartedValue.setText(StepGlobalUtils.getDateInFormat(startTime));

        long totalTime = tripObject.getStopTime() - startTime;
        tripTimeValue.setText(StepGlobalUtils.getDateInFormat(totalTime));
    }

    public void tripReasonValue(boolean hide) {
        tripStatusReasonValue.setText(hide ? "N/A" : tripObject.getTripReason());
    }

    @OnClick(R.id.btn_trip_progress_activity_end_trip)
    public void endTripClick() {
        Time time = new Time();
        String timeRequest = GsonFactory.getGson().toJson(time);
        sendMessage(timeRequest);
        messageHandler.sendEmptyMessageDelayed(MESSAGE_WAIT_TIMEOUT, WAIT_TIME);
    }

    int WAIT_TIME = SERVER_RESPONSE_WAIT;
    static final int MESSAGE_WAIT_TIMEOUT = 1;
    static final int MESSAGE_END_TRIP = 2;
    Handler messageHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_WAIT_TIMEOUT:
                    endTrip(System.currentTimeMillis());
                    break;
                case MESSAGE_END_TRIP:
                    Time time = (Time) msg.obj;
                    endTrip(time.getTime());
                    break;
            }
        }
    };

    public void endTrip(long time) {
        tripObject.setStopTime(time);
        tripObject.setStatus("End");
        String trip = GsonFactory.getGson().toJson(tripObject);
        sendMessage(trip);
        StepGlobalPreferences.setTripDetails(this, tripObject);
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
