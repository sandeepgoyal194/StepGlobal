package au.com.stepglobal.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ButtonBarLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.UUID;

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

public class TripConfirmationActivity extends AppCompatActivity{

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
    private TripObject tripObject = new TripObject();
    private String uniqueID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_confirmation);
        ButterKnife.bind(this);
        uniqueID = UUID.randomUUID().toString();

        Intent intent = getIntent();
        if(intent != null) {
            userId = intent.getExtras().getString(BundleKey.USER_ID.key);
            tripType = intent.getExtras().getParcelable(BundleKey.TRIP_TYPE.key);
        }

        if(userId != null) {
            tripConfirmationUserId.setText(userId.toString().toUpperCase());
        }
        initAdapter();
        initReasonAdapter();
    }

    public void initAdapter() {
        String[] list;
        if(tripType.display.equalsIgnoreCase(TripType.PRIVATE.toString())) {
            list = new String[] {tripType.display.toUpperCase(), TripType.BUSINESS.toString()};
        } else {
            list = new String[] {tripType.display.toUpperCase(), TripType.PRIVATE.toString()};
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
        ArrayAdapter<CharSequence> reasonAdapter = ArrayAdapter
                .createFromResource(this, R.array.reason_array,
                        android.R.layout.simple_spinner_item);

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
        tripObject.setGUID(uniqueID);
        tripObject.setUserId(userId);
        tripObject.setTripType(TripType.toTripType(spinnerItem));
        tripObject.setTripReason(spinnerItem.equalsIgnoreCase(TripType.BUSINESS.toString()) ? reasonSpinnerItem : "");
        tripObject.setStatus("Start");
        StepGlobalPreferences.setTripDetails(this, tripObject);
        Intent tripActivityIntent = new Intent(this, TripProgressActivity.class);
        tripActivityIntent.putExtra(BundleKey.TRIP_OBJECT.key, tripObject);
        startActivity(tripActivityIntent);

    }
}
