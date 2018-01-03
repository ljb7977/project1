package com.example.user.project2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.regions.Regions;

public class LoginActivity extends Activity implements Button.OnClickListener{

    private static CognitoUserPool userPool;
    private static CognitoDevice device;
    private final String TAG="LoginActivity";
    private static CognitoUserSession userSession;

    // User Details
    private String username="hpthrd";  //TODO
    private String password="Trx55555@@";

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button login_button = findViewById(R.id.login_button);
        login_button.setOnClickListener(this);

        userPool = new CognitoUserPool(this,
                getString(R.string.pool_id),
                getString(R.string.cognito_client_id),
                getString(R.string.cognito_client_secret),
                Regions.US_EAST_2);
    }

    @Override
    public void onClick(View view) {
        //showWaitDialog("Signing in...");
        CognitoUser u = userPool.getUser(username); //.getSessionInBackground(authenticationHandler);
        Log.i(TAG, u.getUserId());
        u.getSessionInBackground(authenticationHandler);
    }

    private void launchUser(String token) {
        MyApplication myApp = (MyApplication)getApplication();
        myApp.id_token = token;
        Log.i(TAG, "launch!");
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("name", username);
        startActivityForResult(intent, 4);
    }

    // Sign in the user
    AuthenticationHandler authenticationHandler = new AuthenticationHandler() {

        @Override
        public void onSuccess(CognitoUserSession cognitoUserSession, CognitoDevice newDevice) {
            // Sign-in was successful, cognitoUserSession will contain tokens for the user
            Log.d(TAG, " -- Auth Success");
            userSession = cognitoUserSession;
            device = newDevice;

            String idToken = userSession.getIdToken().getJWTToken();
            launchUser(idToken);
        }

        @Override
        public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
            // The API needs user sign-in credentials to continue

            AuthenticationDetails authenticationDetails = new AuthenticationDetails(username, password, null);
            authenticationContinuation.setAuthenticationDetails(authenticationDetails);
            authenticationContinuation.continueTask();
        }

        @Override
        public void getMFACode(MultiFactorAuthenticationContinuation multiFactorAuthenticationContinuation) {
            /*
            // Multi-factor authentication is required; get the verification code from user
            multiFactorAuthenticationContinuation.setMfaCode(mfaVerificationCode);
            // Allow the sign-in process to continue
            multiFactorAuthenticationContinuation.continueTask();
            */
        }

        @Override
        public void authenticationChallenge(ChallengeContinuation continuation) {

        }

        @Override
        public void onFailure(Exception exception) {
            // Sign-in failed, check exception for the cause
            Log.e(TAG, "Login Failure");
        }
    };
}
