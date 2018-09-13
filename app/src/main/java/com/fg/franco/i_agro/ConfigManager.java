package com.fg.franco.i_agro;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.text.InputType;
import android.widget.EditText;

class ConfigManager {
    MainActivity context;
    SharedPreferences.Editor editor;
    SharedPreferences pref;
    
    public ConfigManager(MainActivity mainActivity) {
        context = mainActivity;
        pref = context.getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        editor = pref.edit();
    }

    public void setConfig() {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("URL configuration");

        // Set up the input
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(pref.getString("url",null));
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                editor.putString("url", input.getText().toString());
                editor.commit();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}
