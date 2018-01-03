package com.example.user.project2;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by user on 2018-01-01.
 */

public class HTTPJSONRequest {
    HttpURLConnection con;
    String url;
    String option;
    String data;
    HTTPJSONRequestHandler handler;
    public HTTPJSONRequest(String url, String option)
    {
        this.url = url;
        this.option = option;
    }
    public HTTPJSONRequest(String url, String option, String data)
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
                    con.setConnectTimeout(10000);
                    con.setReadTimeout(10000);
                    con.setRequestMethod(option);
                    con.setRequestProperty("Content-Type", "application/json");

                    if (data != null) {
                        con.setDoOutput(true);
                        OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
                        osw.write(data);
                        osw.flush();
                        osw.close();
                    }
                    //Log.d("REQ", Integer.toString(con.getResponseCode()));
                    DataInputStream x = new DataInputStream(con.getInputStream());
                    byte[] response = new byte[con.getContentLength()];
                    int recv_byte = con.getContentLength();
                    int off = 0;
                    while(recv_byte > 0) {
                        int read_len = x.read(response, off, recv_byte);
                        off += read_len;
                        recv_byte -= read_len;
                    }
                    x.close();
                    con.getInputStream().close();
                    JSONObject jsonob = new JSONObject(new String(response));
                    return jsonob;
                }
                catch(IOException | JSONException e)
                {
                    e.printStackTrace();
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
