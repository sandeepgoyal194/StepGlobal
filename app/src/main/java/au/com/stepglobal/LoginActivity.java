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

import java.util.ArrayList;
import java.util.List;

import au.com.stepglobal.activity.TripConfirmationActivity;
import au.com.stepglobal.activity.TripProgressActivity;
import au.com.stepglobal.global.BundleKey;
import au.com.stepglobal.global.TripType;
import au.com.stepglobal.model.TripObject;
import au.com.stepglobal.preference.StepGlobalPreferences;
import au.com.stepglobal.utils.StepGlobalUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * A dummy authentication store containing known user names and passwords.
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
//        if (mAuthTask != null) {
//            return;
//        }

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

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    /*public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String userId = null;

        UserLoginTask(String userId) {
            userId = userId;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {

                if (credential.equalsIgnoreCase(userId)) {
                    // Account exists, return true if the password matches.
                    return true;
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                callMainActivity();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
*/

}

