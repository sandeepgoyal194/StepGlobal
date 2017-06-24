package au.com.stepglobal.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import au.com.stepglobal.LoginActivity;
import au.com.stepglobal.R;
import au.com.stepglobal.activity.view.UARTBaseActivityView;
import au.com.stepglobal.global.BundleKey;
import au.com.stepglobal.mapper.TripObjectResponseMapper;
import au.com.stepglobal.model.SaveTripModel;
import au.com.stepglobal.model.SaveTripResponseModel;
import au.com.stepglobal.model.TimeAndLocation;
import au.com.stepglobal.model.TripObject;
import au.com.stepglobal.model.TripStatus;
import au.com.stepglobal.preference.StepGlobalPreferences;
import au.com.stepglobal.utils.GsonFactory;
import au.com.stepglobal.utils.StepGlobalConstants;
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
        switch (TripStatus.getInstance().getStatus()) {
            case TripStatus.DEVICE_STATUS_GETTING_TIME: {
                TimeAndLocation timeAndLocation = GsonFactory.getGson().fromJson(message, TimeAndLocation.class);
                messageHandler.removeMessages(MESSAGE_GET_TIME_TIMEOUT);
                Message msg = new Message();
                msg.what = MESSAGE_GET_TIME;
                msg.obj = timeAndLocation;
                messageHandler.sendMessage(msg);
                break;
            }
            case TripStatus.DEVICE_STATUS_SAVING_TRIP: {
                SaveTripResponseModel saveTripResponse = GsonFactory.getGson().fromJson(message, SaveTripResponseModel.class);
                messageHandler.removeMessages(MESSAGE_SAVE_TRIP_TIMEOUT);
                Message msg = new Message();
                msg.what = MESSAGE_SAVE_TRIP;
                msg.obj = saveTripResponse;
                messageHandler.sendMessage(msg);
                break;
            }
        }
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
        tripReasonValue(tripObject.getTripType().equalsIgnoreCase("P"));
        tripTypeTextViewValue.setText(tripObject.getTripType().equalsIgnoreCase("P") ? "PRIVATE" : "BUSINESS");
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
        sendMessage(StepGlobalConstants.REQUEST_TYPE_TIME);
        messageHandler.sendEmptyMessageDelayed(MESSAGE_GET_TIME_TIMEOUT, WAIT_TIME);
        TripStatus.getInstance().setStatus(TripStatus.DEVICE_STATUS_GETTING_TIME);
    }

    int WAIT_TIME = SERVER_RESPONSE_WAIT;

    static final int MESSAGE_GET_TIME = 1;
    static final int MESSAGE_GET_TIME_TIMEOUT = 2;
    static final int MESSAGE_SAVE_TRIP = 3;
    static final int MESSAGE_SAVE_TRIP_TIMEOUT = 4;

    TimeAndLocation timeAndLocation = new TimeAndLocation();
    Handler messageHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_GET_TIME:
                    timeAndLocation = (TimeAndLocation) msg.obj;
                    TripStatus.getInstance().setStatus(TripStatus.DEVICE_STATUS_NONE);
                    endTrip(timeAndLocation.getTime());
                    break;
                case MESSAGE_GET_TIME_TIMEOUT:
                    timeAndLocation = TripObjectResponseMapper.getTimeAndLocation(GsonFactory.fromTimeAndLocation(getApplicationContext(), "timeandlocation"));
                    TripStatus.getInstance().setStatus(TripStatus.DEVICE_STATUS_GETTING_TIME_FAIL);
                    TripStatus.getInstance().setStatus(TripStatus.DEVICE_STATUS_NONE);
                    endTrip(timeAndLocation.getTime());
                    break;
                case MESSAGE_SAVE_TRIP:
                    SaveTripResponseModel saveTripResponse = (SaveTripResponseModel) msg.obj;
                    checkResponse(saveTripResponse);
                    TripStatus.getInstance().setStatus(TripStatus.DEVICE_STATUS_NONE);
                    //TODO on save trip success or fail need to check
                    break;
                case MESSAGE_SAVE_TRIP_TIMEOUT:
                    SaveTripResponseModel responseModel = new SaveTripResponseModel();
                    responseModel.setStatusCode(SaveTripResponseModel.STATUS_CODE_UNKNOWN_ERROR);
                    checkResponse(responseModel);
                    TripStatus.getInstance().setStatus(TripStatus.DEVICE_STATUS_NONE);
                    break;
            }
        }
    };

    private void checkResponse(SaveTripResponseModel saveTripResponse) {
        if (saveTripResponse.getStatus() != null && saveTripResponse.getStatus().equals("OK")) {
            switch (saveTripResponse.getStatusCode()) {
                case SaveTripResponseModel.STATUS_CODE_NO_ERROR:
                    saveTripToLocal();
                    finishTrip();
                    break;
                case SaveTripResponseModel.STATUS_CODE_INAVLID_TRIP_TIME:
                    break;
                case SaveTripResponseModel.STATUS_CODE_INVALID_API:
                    break;
                case SaveTripResponseModel.STATUS_CODE_INVALID_DEVICE_ID:
                    break;
                case SaveTripResponseModel.STATUS_CODE_INVALID_DRIVER_ID:
                    break;
                case SaveTripResponseModel.STATUS_CODE_INVALID_KEY:
                    break;
                case SaveTripResponseModel.STATUS_CODE_INVALID_METHOD:
                    break;
                case SaveTripResponseModel.STATUS_CODE_INVALID_TRIP_REASON:
                    break;
                case SaveTripResponseModel.STATUS_CODE_INVALID_TRIP_TYPE:
                    break;
                case SaveTripResponseModel.STATUS_CODE_TEMP_SYSTEM_FAILURE:
                    break;
                case SaveTripResponseModel.STATUS_CODE_UNKNOWN_ERROR:
                    saveTripToLocal();
                    finishTrip();
                    Toast.makeText(this, "Update To Server Fails", Toast.LENGTH_LONG).show();
                    break;

            }
        } else {
            saveTripToLocal();
            finishTrip();
            Toast.makeText(this, "Update To Server Fails", Toast.LENGTH_LONG).show();
        }
    }

    public void endTrip(long time) {
        tripObject.setTripReason(StepGlobalUtils.getTripReasonCode(tripObject.getTripReason()));
        tripObject.setStopTime(time);
        tripObject.setDeviceId(tripObject.getDeviceId());
        tripObject.setStatus("End");
        //Save Trip Object
        SaveTripModel saveTripModel = new SaveTripModel();
        saveTripModel.setApi("lmuLogbookApi");
        saveTripModel.setMethod("savetrip");
        saveTripModel.setKey("“key123”");
        saveTripModel.setTripObject(tripObject);
        String saveTrip = GsonFactory.getGson().toJson(saveTripModel);
        sendMessage(saveTrip);
        messageHandler.sendEmptyMessageDelayed(MESSAGE_SAVE_TRIP_TIMEOUT, WAIT_TIME);

    }
    private void saveTripToLocal() {
        StepGlobalPreferences.setTripDetails(this, tripObject);
    }

    private void finishTrip() {

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
