package com.example.user.project2;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by user on 2018-01-01.
 */

public class HTTPJSONRequest {
    HttpURLConnection con;
    String url;
    String option;
    byte[] data;
    HTTPJSONRequestHandler handler;
    public HTTPJSONRequest(String url, String option)
    {
        this.url = url;
        this.option = option;
    }
    public HTTPJSONRequest(String url, String option, byte[] data)
    {
        this.url = url;
        this.option = option;
        this.data = data;
    }

    public HTTPJSONRequest setHandler(HTTPJSONRequestHandler h)
    {
        this.handler = h;
        return this;
    }

    public void execAsync() {
        AsyncTask<Void, Void, JSONObject> oneTask = new AsyncTask<Void, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(Void... voids) {
                try {
                    URL target = new URL(url);
                    con = (HttpURLConnection) target.openConnection();
                    con.setRequestMethod(option);
                    if (data != null) {
                        con.setDoOutput(true);
                        OutputStream os = con.getOutputStream();
                        os.write(data);
                        os.flush();
                        os.close();
                    }
                    DataInputStream x = new DataInputStream(con.getInputStream());
                    byte[] response = new byte[con.getContentLength()];
                    x.read(response, 0, con.getContentLength());
                    JSONObject jsonob = new JSONObject(new String(response));
                    return jsonob;
                }
                catch(IOException | JSONException e)
                {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(JSONObject jsonObject) {
                super.onPostExecute(jsonObject);
                if(handler != null)
                {
                    if(jsonObject != null)
                    {
                        handler.on_response(jsonObject);
                    }
                    else
                    {
                        handler.on_fail();
                    }
                }
            }
        }.execute();
    }


}

interface HTTPJSONRequestHandler {
    void on_response(JSONObject response);
    void on_fail();
}
