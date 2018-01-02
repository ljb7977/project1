package com.example.user.project2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import java.util.EnumMap;


enum TextType {
    NAME, NUMBER, EMAIL
}

public class ContactViewer extends AppCompatActivity {
    private EnumMap<TextType, EditText> viewerText = new EnumMap<>(TextType.class);
    private EnumMap<TextType, Integer> viewerXML = new EnumMap<>(TextType.class);
    Intent returnIntent = new Intent();
    int contactId = -1;
    boolean is_edited = false;
    AlertDialog deleteAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address2);
        initViewXML();
        createDeleteDialog();
        Intent intent = getIntent();

        String name = intent.getStringExtra("name");
        String number = intent.getStringExtra("number");
        String email = intent.getStringExtra("email");
        contactId = intent.getIntExtra("id", -1);
        getViewerText(TextType.NAME).setText(name);
        getViewerText(TextType.NUMBER).setText(number);
        getViewerText(TextType.EMAIL).setText(email);
        setAllEditable(false);
    }

    void setAllEditable(boolean flag){
        for(TextType t : TextType.values())
        {
            setEditable(getViewerText(t), flag);
        }
    }

    void initViewXML()
    {
        viewerXML.put(TextType.NAME, R.id.nameText);
        viewerXML.put(TextType.NUMBER, R.id.phoneText);
        viewerXML.put(TextType.EMAIL, R.id.emailText);
    }

    void createDeleteDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this );
        builder.setMessage("Delete Contact?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                returnIntent.putExtra("result", FragmentA.RETURN_DELETE);
                returnIntent.putExtra("id", contactId);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        deleteAlert = builder.create();
    }
    void setEditable(View view, boolean flag)
    {
        view.setFocusableInTouchMode(flag);
        view.setFocusable(flag);
        view.setClickable(flag);
    }

    EditText getViewerText(TextType e)
    {
        EditText retval = viewerText.get(e);
        if(retval == null)
        {
            Integer retid = viewerXML.get(e);
            if(retid == null)
                return null;
            retval = findViewById(retid);
            viewerText.put(e, retval);
        }
        return retval;
    }

    public void onContactEditButtonClick(View view)
    {
        setAllEditable(true);
        is_edited = true;
    }

    public void onContactDeleteButtonClick(View view)
    {
        if(deleteAlert == null)
            createDeleteDialog();
        deleteAlert.show();
    }

    public void onContactOkButtonClick(View view)
    {
        if(is_edited) {
            returnIntent.putExtra("result", FragmentA.RETURN_EDIT);
            returnIntent.putExtra("name", getViewerText(TextType.NAME).getText().toString());
            returnIntent.putExtra("number", getViewerText(TextType.NUMBER).getText().toString());
            returnIntent.putExtra("email", getViewerText(TextType.EMAIL).getText().toString());
        }
        else {
            returnIntent.putExtra("result", FragmentA.RETURN_OK);
        }
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}