package com.example.user.project2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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
import android.widget.Spinner;
import android.widget.Toast;

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

import static android.app.Activity.RESULT_OK;

public class FragmentA extends Fragment {
    String str;
    ArrayList<Contact> contactList;
    ArrayList<String> facebookContactList;
    CallbackManager callbackManager;
    ListViewAdapter adapter;
    boolean  buttonExpanded = false;
    FloatingActionButton expandButton;
    FloatingActionButton facebookButton;
    FloatingActionButton addButton;
    FloatingActionButton syncButton;
    static final short REQUEST_CONTACTINFO = 0x721;
    static final int RETURN_OK = 0x722;
    static final int RETURN_EDIT = 0x723;
    static final int RETURN_DELETE = 0x724;
    static final int RETURN_ADD = 0x727;
    static final int REQADD = 0x725;
    static final int REQVIEW = 0x726;
    static final int REQFACEADD = 0x728;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.tab_fragment1, container, false);
        createActionButtons(view);
        MyApplication myApp = (MyApplication) getActivity().getApplication();
        contactList = myApp.getContactList();
        facebookContactList = myApp.getFacebookContactList();
        registerFacebookTask();
        setupListView(view.findViewById(R.id.listview));
        return view;
    }

    public void setupListView(View view)
    {
        if(view == null) return;
        ListView listview = (ListView)view;
        adapter = new ListViewAdapter(contactList);


        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id){
                Intent intent = new Intent(getActivity(), ContactViewer.class);
                Contact item = (Contact)adapter.getItem(position);
                intent.putExtra("request",REQVIEW);
                intent.putExtra("name", item.name);
                intent.putExtra("number", item.number);
                intent.putExtra("email", item.email);
                intent.putExtra("id", position);
                startActivityForResult(intent, REQUEST_CONTACTINFO);
            }
        });

    }
    public void createActionButtons(View view)
    {
        expandButton = view.findViewById(R.id.expand_button);
        facebookButton = view.findViewById(R.id.facebook_button);
        facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                collapseButtons();
                if(AccessToken.getCurrentAccessToken() != null)
                {
                    LoginManager.getInstance().logOut();
                    Toast.makeText(getActivity(), "logout successful", Toast.LENGTH_SHORT).show();
                    facebookButton.setImageResource(R.drawable.com_facebook_favicon_blue);
                }
                else {
                    LoginManager.getInstance().logInWithReadPermissions(FragmentA.this, Arrays.asList("user_friends"));
                }
            }
        });

        addButton = view.findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                collapseButtons();
                if(facebookContactList != null && facebookContactList.size() > 0) {
                    AlertDialog.Builder chooseAlert = new AlertDialog.Builder(getActivity());
                    chooseAlert.setTitle("add user from facebook");
                    chooseAlert.setMessage("Do you want to add user from your facebook friends list?");
                    chooseAlert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                            b.setTitle("Friends list");
                            String[] names = Arrays.copyOf(facebookContactList.toArray(), facebookContactList.size(), String[].class);
                            b.setItems(names, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    Intent intent = new Intent(getActivity(), ContactViewer.class);
                                    intent.putExtra("name", facebookContactList.get(i));
                                    intent.putExtra("request", REQFACEADD);
                                    startActivityForResult(intent, REQUEST_CONTACTINFO);
                                }
                            });
                            b.show();
                        }
                    });
                    chooseAlert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(getActivity(), ContactViewer.class);
                            intent.putExtra("request", REQADD);
                            startActivityForResult(intent, REQUEST_CONTACTINFO);
                        }
                    });
                    chooseAlert.show();
                }
                else {
                    Intent intent = new Intent(getActivity(), ContactViewer.class);
                    intent.putExtra("request", REQADD);
                    startActivityForResult(intent, REQUEST_CONTACTINFO);
                }
            }
        });
        syncButton = view.findViewById(R.id.sync_button);
        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                collapseButtons();
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
                                    if(contactList != null)
                                        contactList.add(new Contact(name, number, email));
                                }
                            }
                            Toast.makeText(getActivity(), "sync finished", Toast.LENGTH_SHORT).show();
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
            }
        });
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
        if(syncButton != null)
            syncButton.setVisibility(visibility);

    }

    public void registerFacebookTask()
    {

        callbackManager = CallbackManager.Factory.create();
        FacebookSdk.addLoggingBehavior(LoggingBehavior.REQUESTS);

        if(AccessToken.getCurrentAccessToken() != null)
        {
            facebookButton.setImageResource(R.drawable.com_facebook_tooltip_black_xout);
            executeFacebookContactTask(AccessToken.getCurrentAccessToken());
        }
        // Callback registration
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken token = loginResult.getAccessToken();
                facebookButton.setImageResource(R.drawable.com_facebook_tooltip_black_xout);
                executeFacebookContactTask(token);
                // App code
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CONTACTINFO)
        {
            if(resultCode == RESULT_OK)
            {
                switch(data.getIntExtra("result", -1))
                {
                    case -1:
                        break;
                    case RETURN_ADD:
                        addContactList(
                                new Contact(
                                        data.getStringExtra("name"),
                                        data.getStringExtra("number"),
                                        data.getStringExtra("email"))
                        );
                        adapter.notifyDataSetChanged();
                        break;
                    case RETURN_OK:
                        Log.d("FRAGA", "OK");
                        break;
                    case RETURN_EDIT:
                        changeContactList(
                                data.getIntExtra("id", -1),
                                new Contact(
                                        data.getStringExtra("name"),
                                        data.getStringExtra("number"),
                                        data.getStringExtra("email")));
                        adapter.notifyDataSetChanged();
                        break;
                    case RETURN_DELETE:
                        removeContactList(data.getIntExtra("id", -1));
                        adapter.notifyDataSetChanged();
                        break;
                }
            }
            else
            {
                Log.d("FRAGA", "CANCEL");
            }

        }
        else {
            if (callbackManager != null)
                callbackManager.onActivityResult(requestCode, resultCode, data);
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    void changeContactList(int id, Contact c)
    {
        if(id >= 0) {
            if (contactList != null)
                contactList.set(id, c);
        }
    }

    void removeContactList(int id)
    {
        if(id >= 0)
        {
            if(contactList != null)
                contactList.remove(id);
        }
    }

    void addContactList(Contact c)
    {
        if(contactList != null)
        {
            contactList.add(c);
        }
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
                                                    if(facebookContactList != null)
                                                        facebookContactList.add(fi.getString("name"));
                                                }
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

