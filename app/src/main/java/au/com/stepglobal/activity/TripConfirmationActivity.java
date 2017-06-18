package au.com.stepglobal.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.UUID;

import au.com.stepglobal.R;
import au.com.stepglobal.activity.view.UARTBaseActivityView;
import au.com.stepglobal.global.BundleKey;
import au.com.stepglobal.global.TripType;
import au.com.stepglobal.model.ReasonCodes;
import au.com.stepglobal.model.Time;
import au.com.stepglobal.model.TripObject;
import au.com.stepglobal.preference.StepGlobalPreferences;
import au.com.stepglobal.utils.GsonFactory;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static au.com.stepglobal.utils.StepGlobalConstants.REQUEST_TYPE_REASON_CODE;
import static au.com.stepglobal.utils.StepGlobalConstants.REQUEST_TYPE_TIME;
import static au.com.stepglobal.utils.StepGlobalConstants.SERVER_RESPONSE_WAIT;

/**
 * Created by hiten.bahri on 16/06/2017.
 */

public class TripConfirmationActivity extends UARTBaseActivityView {

    int WAIT_TIME = SERVER_RESPONSE_WAIT;
    static final int MESSAGE_WAIT_TIMEOUT = 1;
    static final int MESSAGE_START_TRIP = 2;
    static final int MESSAGE_WAIT_REASON_UPDATE_TIMEOUT = 3;
    static final int MESSAGE_REASON_UPDATE = 4;

    @BindView(R.id.trip_confirmation_user_id)
    TextView tripConfirmationUserId;
    @BindView(R.id.trip_confirmation_trip_type_spinner)
    Spinner tripTypeSpinner;
    @BindView(R.id.trip_confirmation_trip_reason_label)
    TextView tripReasonLabel;
    @BindView(R.id.trip_confirmation_trip_reason_spinner)
    Spinner tripTypeReasonSpinner;
    @BindView(R.id.trip_confirmation_button)
    Button startTripButton;

    private String userId;
    private TripType tripType;
    private String spinnerItem;
    private String reasonSpinnerItem;

    private String uniqueID;

    private String[] reasonCodes = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_confirmation);
        ButterKnife.bind(this);
        uniqueID = UUID.randomUUID().toString();

        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getExtras().getString(BundleKey.USER_ID.key);
            tripType = intent.getExtras().getParcelable(BundleKey.TRIP_TYPE.key);
        }

        if (userId != null) {
            tripConfirmationUserId.setText(userId.toString().toUpperCase());
        }
        initAdapter();
        ReasonCodes reasonCodes = new ReasonCodes();
        String reasonCode = GsonFactory.getGson().toJson(reasonCodes);
        messageHandler.sendEmptyMessageDelayed(MESSAGE_WAIT_REASON_UPDATE_TIMEOUT, WAIT_TIME);
        sendMessage(reasonCode);
    }

    @Override
    public void onReceiveMessage(String message) {
        Time time = GsonFactory.getGson().fromJson(message, Time.class);
        if (time.getRequestType().equalsIgnoreCase(REQUEST_TYPE_TIME)) {
            messageHandler.removeMessages(MESSAGE_WAIT_TIMEOUT);
            Message msg = new Message();
            msg.what = MESSAGE_START_TRIP;
            msg.obj = time;
            messageHandler.sendMessage(msg);
        } else if (time.getRequestType().equalsIgnoreCase(REQUEST_TYPE_REASON_CODE)) {
            ReasonCodes[] reasonCodes = GsonFactory.getGson().fromJson(message, ReasonCodes[].class);
            messageHandler.removeMessages(MESSAGE_WAIT_REASON_UPDATE_TIMEOUT);
            Message msg = new Message();
            msg.what = MESSAGE_REASON_UPDATE;
            msg.obj = reasonCodes;
            messageHandler.sendMessage(msg);
        }
    }

    public void initAdapter() {
        String[] list;
        if (tripType.display.equalsIgnoreCase(TripType.PRIVATE.toString())) {
            list = new String[]{tripType.display.toUpperCase(), TripType.BUSINESS.toString()};
        } else {
            list = new String[]{tripType.display.toUpperCase(), TripType.PRIVATE.toString()};
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tripTypeSpinner.setAdapter(adapter);
        tripTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinnerItem = (String) parent.getItemAtPosition(position);
                hideTripReason(spinnerItem.equalsIgnoreCase(TripType.PRIVATE.toString()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void initReasonAdapter() {
        ArrayAdapter<CharSequence> reasonAdapter;
        if (reasonCodes == null) {
            reasonAdapter = ArrayAdapter
                    .createFromResource(this, R.array.reason_array,
                            android.R.layout.simple_spinner_item);
        } else {
            reasonAdapter = new ArrayAdapter(this,
                    android.R.layout.simple_spinner_item,
                    reasonCodes);
        }

        // Specify the layout to use when the list of choices appears
        reasonAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        tripTypeReasonSpinner.setAdapter(reasonAdapter);
        tripTypeReasonSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                reasonSpinnerItem = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void hideTripReason(boolean hide) {
        tripReasonLabel.setVisibility(hide ? View.GONE : View.VISIBLE);
        tripTypeReasonSpinner.setVisibility(hide ? View.GONE : View.VISIBLE);
    }

    @OnClick(R.id.trip_confirmation_button)
    public void startTripClicked() {
        Time time = new Time();
        String timeRequest = GsonFactory.getGson().toJson(time);
        sendMessage(timeRequest);
        messageHandler.sendEmptyMessageDelayed(MESSAGE_WAIT_TIMEOUT, WAIT_TIME);
    }

    public void sendStartTrip(long time) {
        TripObject tripObject = new TripObject();
        tripObject.setGUID(uniqueID);
        tripObject.setUserId(userId);
        tripObject.setTripType(TripType.toTripType(spinnerItem));
        tripObject.setTripReason(spinnerItem.equalsIgnoreCase(TripType.BUSINESS.toString()) ? reasonSpinnerItem : "");
        tripObject.setStartTime(time);
        tripObject.setStatus("Start");
        String trip = GsonFactory.getGson().toJson(tripObject);

        //SENDING START TRIP TO SERVER
        sendMessage(trip);

        StepGlobalPreferences.setTripDetails(this, tripObject);
        Intent tripActivityIntent = new Intent(this, TripProgressActivity.class);
        tripActivityIntent.putExtra(BundleKey.TRIP_OBJECT.key, tripObject);
        startActivity(tripActivityIntent);
        finish();
    }

    Handler messageHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_WAIT_TIMEOUT:
                    sendStartTrip(System.currentTimeMillis());
                    break;
                case MESSAGE_START_TRIP:
                    Time time = (Time) msg.obj;
                    sendStartTrip(time.getTime());
                    break;
                case MESSAGE_WAIT_REASON_UPDATE_TIMEOUT:
                    initReasonAdapter();
                    break;
                case MESSAGE_REASON_UPDATE:
                    ReasonCodes resReasonCodes = (ReasonCodes) msg.obj;
                    reasonCodes = resReasonCodes.getReasonCodes();
                    initReasonAdapter();
                    break;
            }
        }
    };
}
