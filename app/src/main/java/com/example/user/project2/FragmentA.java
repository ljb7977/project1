package com.example.user.project2;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;

public class FragmentA extends Fragment {
    String str;
    ArrayList<Contact> ContactList;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_fragment1, container, false);

        ListView listview = view.findViewById(R.id.listview);
        final ListViewAdapter adapter = new ListViewAdapter();

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
}
