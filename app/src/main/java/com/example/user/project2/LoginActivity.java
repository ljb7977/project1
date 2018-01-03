package com.example.user.project2;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.amazonaws.mobileconnectors.cognitoauth.Auth;
import com.amazonaws.mobileconnectors.cognitoauth.AuthUserSession;
import com.amazonaws.mobileconnectors.cognitoauth.handlers.AuthHandler;

public class LoginActivity extends Activity implements Button.OnClickListener{

    private Auth auth;
    private Uri appRedirect;
    private AlertDialog userDialog;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Auth.Builder builder = new Auth.Builder().setAppClientId(getString(R.string.cognito_client_id))
                .setAppCognitoWebDomain(getString(R.string.cognito_web_domain))
                .setApplicationContext(getApplicationContext())
                .setAuthHandler(new callback())
                .setSignInRedirect(getString(R.string.app_redirect_in))
                .setSignOutRedirect(getString(R.string.app_redirect_out));
        this.auth = builder.build();
        appRedirect = Uri.parse(getString(R.string.app_redirect_in));

        Button login_button = findViewById(R.id.login_button);
        login_button.setOnClickListener(this);

        //auth.signOut();
    }

    /*
    @Override
    protected void onResume() {
        super.onResume();
        Intent activityIntent = getIntent();
        //  -- Call Auth.getTokens() to get Cognito JWT --
        if (activityIntent.getData() != null &&
                appRedirect.getHost().equals(activityIntent.getData().getHost())) {
            auth.getTokens(activityIntent.getData());
        }
    }
    */

    @Override
    public void onClick(View view) {
        this.auth.getSession();
    }

    class callback implements AuthHandler {

        @Override
        public void onSuccess(AuthUserSession authUserSession) {
            // Show tokens for the authenticated user

            MyApplication myApp = (MyApplication) getApplication();
            myApp.id_token = authUserSession.getIdToken().getJWTToken();
            Log.i("myapp IDTOKEN", myApp.id_token);

            /*Intent i = new Intent(mContext, MainActivity.class);
            startActivity(i);*/
        }

        @Override
        public void onSignout() {
            // Back to new user screen.
            //setNewUserFragment();
        }

        @Override
        public void onFailure(Exception e) {
            Log.i("error", e.getMessage());
            showDialogMessage("error", e.getMessage());
        }

    }
    private void showDialogMessage(String title, String body) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(body).setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    userDialog.dismiss();
                } catch (Exception e) {
                    // Log failure
                    Log.e("Login","Dialog failure", e);
                }
            }
        });
        userDialog = builder.create();
        userDialog.show();
    }
}
