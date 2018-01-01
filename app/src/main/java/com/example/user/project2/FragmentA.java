package com.example.user.project2;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
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
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.tab_fragment1, container, false);

        ListView listview = view.findViewById(R.id.listview);
        callbackManager = CallbackManager.Factory.create();

        LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);
        // If using in a fragment
        loginButton.setFragment(this);
        loginButton.setReadPermissions(Arrays.asList("user_friends"));
        FacebookSdk.addLoggingBehavior(LoggingBehavior.REQUESTS);
        Log.d("FLOGIN","BEFORE");
        // Callback registration
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken token = loginResult.getAccessToken();
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
                                if(x.has("data"))
                                    try {
                                    JSONArray f = x.getJSONArray("data");
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
                parameters.putString("fields", "name,email");
                request.setParameters(parameters);
                request.executeAsync();
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
        return view;
    }

    public boolean readJson(){
        AssetManager am = getResources().getAssets();
        InputStream is;
        try{
            //is = am.open("address.json");
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
