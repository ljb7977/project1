package com.example.user.project2;

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
}
