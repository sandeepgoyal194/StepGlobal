package au.com.stepglobal;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import au.com.stepglobal.activity.TripConfirmationActivity;
import au.com.stepglobal.activity.TripProgressActivity;
import au.com.stepglobal.activity.view.UARTBaseActivityView;
import au.com.stepglobal.global.BundleKey;
import au.com.stepglobal.global.TripType;
import au.com.stepglobal.json.TripObjectResponse;
import au.com.stepglobal.mapper.TripObjectResponseMapper;
import au.com.stepglobal.model.TripObject;
import au.com.stepglobal.preference.StepGlobalPreferences;
import au.com.stepglobal.utils.GsonFactory;
import au.com.stepglobal.utils.StepGlobalUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends UARTBaseActivityView {

    /**
     * A dummy authentication store containing known user Id.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "test12", "user12"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
//    private UserLoginTask mAuthTask = null;

    // UI references.

    @BindView(R.id.login_activity_user_id)
    EditText userIdView;
    @BindView(R.id.login_activity_private_button)
    Button privateButton;
    @BindView(R.id.login_activity_business_button)
    Button businessButton;
    @BindView(R.id.login_activity_form)
    LinearLayout loginFormView;
    @BindView(R.id.login_activity_progress)
    View progressView;
    @BindView(R.id.login_activity_button_container)
    LinearLayout buttonContainer;

    private TripObject tripDetails;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
    }

    @Override
    public void onReceiveMessage(String message) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        tripDetails = StepGlobalPreferences.getTripDetails(this);
        if(tripDetails.getGUID() != null && tripDetails.getStatus().equalsIgnoreCase("Start")) {
            showProgress(true);
            callTripProgress(tripDetails);
        } else {
            showProgress(false);
        }
    }

    private void callTripProgress(TripObject tripDetails) {
        Intent intent = new Intent(this, TripProgressActivity.class);
        intent.putExtra(BundleKey.TRIP_OBJECT.key, tripDetails);
        startActivity(intent);
    }

    @OnClick(R.id.login_activity_private_button)
    public void privateButtonClicked() {
        attemptLogin(TripType.PRIVATE);
    }

    @OnClick(R.id.login_activity_business_button)
    public void businessButtonClicked() {
        attemptLogin(TripType.BUSINESS);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     * @param tripType
     */
    private void attemptLogin(TripType tripType) {
        userIdView.setError(null);

        // Store values at the time of the login attempt.
        String userIdValue = userIdView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(userIdValue)) {
            userIdView.setError(getString(R.string.error_field_required));
            focusView = userIdView;
            cancel = true;
        } else if (!StepGlobalUtils.isValidUserId(userIdValue)) {
            userIdView.setError(getString(R.string.error_invalid_user_id));
            focusView = userIdView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            callTripConfirmation(tripType);
//            mAuthTask = new UserLoginTask(userIdValue);
//            mAuthTask.execute((Void) null);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            loginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });
            buttonContainer.setVisibility(show ? View.GONE : View.VISIBLE);
            buttonContainer.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    buttonContainer.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            progressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });

        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            buttonContainer.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void callTripConfirmation(TripType tripType) {
        Intent tripConfirmationIntent = new Intent(LoginActivity.this, TripConfirmationActivity.class);
        tripConfirmationIntent.putExtra(BundleKey.USER_ID.key, userIdView.getText().toString());
        tripConfirmationIntent.putExtra(BundleKey.TRIP_TYPE.key, (Parcelable) tripType);
        startActivity(tripConfirmationIntent);
    }
}

