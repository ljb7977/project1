package com.example.user.project2;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by user on 2017-12-28.
 */

public class Contact {
    String name, number, email, id;
    Contact(String name, String number, String email){
        this.name = name;
        this.number = number;
        this.email = email;
    }
    Contact(String name, String number, String email, String uuid){
        this.name = name;
        this.number = number;
        this.email = email;
        this.id = uuid;
    }

    @Override
    public boolean equals(Object obj) {
        boolean isSame = false;
        if (obj != null && obj instanceof Contact)
        {
            Contact objC = (Contact)obj;
            isSame = name.equals(objC.name);
        }
        return isSame;
    }

    public JSONObject toJSONObject(boolean includeUuid)
    {
        try {
            JSONObject retval = new JSONObject();
            if (includeUuid)
                retval.put("uuid", id);
            retval.put("name", name);
            retval.put("phone", number);
            retval.put("email", email);
            return retval;
        } catch (JSONException e) {
            return null;
        }

    }
}
