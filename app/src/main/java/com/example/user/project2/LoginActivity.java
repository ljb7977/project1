package com.example.user.project2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.NewPasswordContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.regions.Regions;

public class LoginActivity extends Activity implements Button.OnClickListener{

    private final String TAG="LoginActivity";

    private AlertDialog userDialog;
    private ProgressDialog waitDialog;

    // User Details
    private String username="hpthrd";  //TODO
    private String password="Trx55555@@";

    public EditText inUsername;
    public EditText inPassword;

    private NewPasswordContinuation newPasswordContinuation;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button login_button = findViewById(R.id.login_button);
        login_button.setOnClickListener(this);
        Button create_account_button = findViewById(R.id.create_account_button);
        create_account_button.setOnClickListener(this);

        AppHelper.init(getApplicationContext());

        inUsername = (EditText) findViewById(R.id.editTextUserId);
        inPassword = (EditText) findViewById(R.id.editTextPassword);

        /*Auto login
        CognitoUser user = AppHelper.getPool().getCurrentUser();
        username = user.getUserId();
        if(username != null) {
            AppHelper.setUser(username);
            inUsername.setText(user.getUserId());
            user.getSessionInBackground(authenticationHandler);
        }
        */
    }

    @Override
    public void onClick(View view) {
        //showWaitDialog("Signing in...");

        switch(view.getId()){
            case R.id.login_button:
                username = inUsername.getText().toString();
                if(username == null || username.length() < 1) {
                    TextView label = (TextView) findViewById(R.id.textViewUserNameLabel);
                    label.setText("Username cannot be empty");
                    inUsername.setBackground(getDrawable(R.drawable.text_border_error)); //TODO
                    return;
                }

                AppHelper.setUser(username);

                password = inPassword.getText().toString();
                if(password == null || password.length() < 1) {
                    TextView label = (TextView) findViewById(R.id.textViewUserPasswordLabel);
                    label.setText("Password cannot be empty");
                    inPassword.setBackground(getDrawable(R.drawable.text_border_error));
                    return;
                }

                AppHelper.getPool().getUser(username).getSessionInBackground(authenticationHandler);
                return;
            case R.id.create_account_button:
                Intent registerActivity = new Intent(this, RegisterUser.class);
                startActivity(registerActivity);
                return;
        }
    }

    private void launchUser(String token) {
        MyApplication myApp = (MyApplication)getApplication();
        myApp.id_token = token;
        Log.i(TAG, "launch!");
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("name", username);
        startActivity(intent);
        finish();
    }

    // Sign in the user
    AuthenticationHandler authenticationHandler = new AuthenticationHandler() {

        @Override
        public void onSuccess(CognitoUserSession cognitoUserSession, CognitoDevice newDevice) {
            // Sign-in was successful, cognitoUserSession will contain tokens for the user
            Log.d(TAG, " -- Auth Success");
            AppHelper.setCurrSession(cognitoUserSession);
            AppHelper.newDevice(newDevice);

            String idToken = cognitoUserSession.getIdToken().getJWTToken();
            launchUser(idToken);
        }

        @Override
        public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
            // The API needs user sign-in credentials to continue

            closeWaitDialog();

            AuthenticationDetails authenticationDetails = new AuthenticationDetails(username, password, null);
            authenticationContinuation.setAuthenticationDetails(authenticationDetails);
            authenticationContinuation.continueTask();
        }

        @Override
        public void getMFACode(MultiFactorAuthenticationContinuation multiFactorAuthenticationContinuation) {}

        @Override
        public void authenticationChallenge(ChallengeContinuation continuation) {

        }

        @Override
        public void onFailure(Exception exception) {
            // Sign-in failed, check exception for the cause
            closeWaitDialog();
            TextView label = (TextView) findViewById(R.id.textViewUserNameLabel);
            label.setText("Sign-in failed");
            inPassword.setBackground(getDrawable(R.drawable.text_border_error));

            showDialogMessage("Sign-in failed", AppHelper.formatException(exception));
            Log.e(TAG, "Login Failure");
        }
    };

    private void showWaitDialog(String message) {
        closeWaitDialog();
        waitDialog = new ProgressDialog(this);
        waitDialog.setTitle(message);
        waitDialog.show();
    }

    private void showDialogMessage(String title, String body) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(body).setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    userDialog.dismiss();
                } catch (Exception e) {
                    //
                }
            }
        });
        userDialog = builder.create();
        userDialog.show();
    }

    private void closeWaitDialog() {
        try {
            waitDialog.dismiss();
        }
        catch (Exception e) {
            //
        }
    }
}
