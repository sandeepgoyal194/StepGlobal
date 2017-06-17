package au.com.stepglobal.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import au.com.stepglobal.LoginActivity;
import au.com.stepglobal.R;
import au.com.stepglobal.global.BundleKey;
import au.com.stepglobal.global.TripType;
import au.com.stepglobal.model.TripObject;
import au.com.stepglobal.preference.StepGlobalPreferences;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by hiten.bahri on 16/06/2017.
 */

public class TripProgressActivity extends AppCompatActivity{

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
        if(intent != null) {
            if(intent.getExtras().containsKey(BundleKey.TRIP_OBJECT.key)) {
                tripObject = intent.getExtras().getParcelable(BundleKey.TRIP_OBJECT.key);
                if(tripObject != null){
                    setProgressState(tripObject);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(tripObject != null) {
            if(tripObject.getStatus().equalsIgnoreCase("Start")) {
                finishAffinity();
            }
        }
    }

    private void setProgressState(TripObject tripObject) {
//        tripStartedValue.setText((int) tripObject.getStartTime());
        tripReasonValue(tripObject.getTripType().display.equalsIgnoreCase(TripType.PRIVATE.toString()));
        tripTypeTextViewValue.setText(tripObject.getTripType().display);
    }

    public void tripReasonValue(boolean hide) {
        tripStatusReasonValue.setText(hide ? "N/A" : tripObject.getTripReason());
    }

    @OnClick(R.id.btn_trip_progress_activity_end_trip)
    public void endTripClick() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        tripObject.setStatus("End");
        StepGlobalPreferences.setTripDetails(this, tripObject);
        finish();
    }
}
