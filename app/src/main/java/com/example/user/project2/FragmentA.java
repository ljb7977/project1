package com.example.user.project2;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.LoggingBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class FragmentA extends Fragment {
    String str;
    ArrayList<Contact> ContactList;
    CallbackManager callbackManager;
    ListViewAdapter adapter;
    boolean  buttonExpanded = false;
    FloatingActionButton expandButton;
    FloatingActionButton facebookButton;
    FloatingActionButton addButton;
    FloatingActionButton removeButton;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.tab_fragment1, container, false);
        expandButton = view.findViewById(R.id.expand_button);
        facebookButton = view.findViewById(R.id.facebook_button);
        facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                collapseButtons();
                if(AccessToken.getCurrentAccessToken() != null)
                {
                    LoginManager.getInstance().logOut();
                }
                else {
                    LoginManager.getInstance().logInWithReadPermissions(FragmentA.this, Arrays.asList("user_friends"));
                }
            }
        });

        addButton = view.findViewById(R.id.add_button);
        removeButton = view.findViewById(R.id.remove_button);
        setButtonsInvisible(View.GONE);
        facebookButton.setVisibility(View.GONE);
        expandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(buttonExpanded)
                {
                    collapseButtons();
                }
                else
                {
                    expandButtons();
                }
            }
        });
        ListView listview = view.findViewById(R.id.listview);
        callbackManager = CallbackManager.Factory.create();


        FacebookSdk.addLoggingBehavior(LoggingBehavior.REQUESTS);

        if(AccessToken.getCurrentAccessToken() != null)
        {
            executeFacebookContactTask(AccessToken.getCurrentAccessToken());
        }
        Log.d("FLOGIN","BEFORE");
        // Callback registration
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken token = loginResult.getAccessToken();
                executeFacebookContactTask(token);

                Log.d("FLOGIN","SUCCESS");
                // App code
            }

            @Override
            public void onCancel() {
                Log.d("FLOGIN","FAIL");
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                Log.d("FLOGIN",exception.toString());
                // App code
            }
        });
        adapter = new ListViewAdapter();

        MyApplication myApp = (MyApplication) getActivity().getApplication();
        ContactList = myApp.getContactList();

        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id){
                Intent intent = new Intent(getActivity(), ContactViewer.class);
                ListViewItem item = (ListViewItem)adapter.getItem(position);
                intent.putExtra("name", item.getName());
                intent.putExtra("number", item.getNumber());
                intent.putExtra("email", item.getEmail());
                startActivity(intent);
            }
        });

        boolean s = readJson();
        if(s){
            try{
                JSONArray jarray = new JSONArray(str);
                for(int i=0; i<jarray.length(); i++){
                    JSONObject jobject = jarray.getJSONObject(i);
                    String name = jobject.getString("name");
                    String number = jobject.getString("number");
                    String email = jobject.getString("email");

                    adapter.addItem(name, number, email);
                }
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
        for(int i=0; i<ContactList.size(); i++){
            Contact c = ContactList.get(i);
            adapter.addItem(c.name, c.number, c.email);
        }

        new HTTPJSONRequest("http://143.248.36.226:3000/contacts","GET").setHandler(new HTTPJSONRequestHandler() {
            @Override
            public void on_response(JSONObject response) {
                try {
                    JSONArray f = response.getJSONArray("content");
                    int le = f.length();
                    for(int i = 0; i < le; i++)
                    {
                        JSONObject fi = f.getJSONObject(i);
                        if(fi != null)
                        {
                            String name = "";
                            String number = "";
                            String email = "";
                            if(fi.has("name"))
                                name = fi.getString("name");
                            if(fi.has("phone"))
                                number = fi.getString("phone");
                            if(fi.has("email"))
                                email = fi.getString("email");
                            if(adapter != null)
                                adapter.addItem(name, number, email);
                        }
                    }
                    if(adapter != null)
                    {
                        adapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void on_fail() {
                Log.d("JSON", "fail");
            }
        }).execAsync();
        return view;
    }

    public void collapseButtons()
    {
        setButtonsInvisible(View.GONE);
        buttonExpanded = false;
    }

    public void expandButtons()
    {
        setButtonsInvisible(View.VISIBLE);
        buttonExpanded = true;
    }

    public void setButtonsInvisible(int visibility)
    {
        if(facebookButton != null)
            facebookButton.setVisibility(visibility);
        if(addButton != null)
            addButton.setVisibility(visibility);
        if(removeButton != null)
            removeButton.setVisibility(visibility);

    }
    public boolean readJson(){
        AssetManager am = getResources().getAssets();
        InputStream is;
        try{
            is = getResources().openRawResource(R.raw.address);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            str  = new String(buffer, "UTF-8");
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(callbackManager != null)
            callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void executeFacebookContactTask(AccessToken token)
    {
        GraphRequest request =
                new GraphRequest(token,
                        "/me/taggable_friends",
                        null,
                        HttpMethod.GET,
                        new PagingRequestCallback(
                                new GraphRequest.Callback() {
                                    public void onCompleted(GraphResponse response)
                                    {
                                        JSONObject x = response.getJSONObject();
                                        try {
                                            JSONArray f = x.getJSONArray("data");
                                            if(x.has("data"))
                                                if(f == null) return;
                                            int le = f.length();
                                            for(int i = 0; i < le; i++)
                                            {
                                                JSONObject fi = f.getJSONObject(i);
                                                if(fi != null && fi.has("name"))
                                                {
                                                    if(adapter != null)
                                                        adapter.addItem(fi.getString("name"), "", "");
                                                }
                                            }
                                            if(adapter != null)
                                            {
                                                adapter.notifyDataSetChanged();
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                })
                );
        Bundle parameters = new Bundle();
        parameters.putString("fields", "name");
        request.setParameters(parameters);
        request.executeAsync();
    }


}

class PagingRequestCallback implements GraphRequest.Callback {
    private GraphRequest.Callback x;
    public PagingRequestCallback(GraphRequest.Callback x)
    {
        this.x = x;
    }
    @Override
    public void onCompleted(GraphResponse response) {
        x.onCompleted(response);
        GraphRequest nextResultsRequests = response.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);
        if(nextResultsRequests != null)
        {
            nextResultsRequests.setCallback(this);
            nextResultsRequests.executeAsync();
        }
    }
}

