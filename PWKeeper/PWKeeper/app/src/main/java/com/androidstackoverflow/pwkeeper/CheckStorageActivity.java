package com.androidstackoverflow.pwkeeper;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class CheckStorageActivity extends AppCompatActivity {

    Button btnNext;

    static final int READ_BLOCK_SIZE = 100;
    String stringREAD = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_storage);

        btnNext = findViewById(R.id.btnNext);

        SharedPreferences preferences = getSharedPreferences("MyPrefrence", MODE_PRIVATE);
        if (!preferences.getBoolean("isFirstTime", false)) {

            try {
                writeTO_TEXT_FILE();
            } catch (IOException e) {
                e.printStackTrace();
            }

            final SharedPreferences pref = getSharedPreferences("MyPrefrence", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("isFirstTime", true);
            editor.apply();
        }

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            } else {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 666);  // Comment 26
            }
        }

        try {
            readFROM_TEXT_FILE();
        } catch (IOException e) {
            e.printStackTrace();
        }

        chkHere();

    }//END onCreate


    public void chkHere(){

        if(stringREAD.matches("EXTERNAL") || stringREAD.matches("INTERNAL")){
            Intent intent = new Intent(CheckStorageActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    public void onNEXT(View view) {
        /* If user did not manually set STORAGE PERMISSION no leaving the page */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && stringREAD.equals("EMPTY")) {
                Intent intentCHK = new Intent(CheckStorageActivity.this, MainActivity.class);
                startActivity(intentCHK);
            } else {
                Intent intentCHK = new Intent(CheckStorageActivity.this, CheckStorageActivity.class);
                startActivity(intentCHK);
            }
        }
    }

    public void writeTO_TEXT_FILE() throws IOException {
        /* Creates the STORAGE_TYPE_FILE.TXT and writes EMPTY to the file */
        FileOutputStream fileout = openFileOutput("STORAGE_TYPE_FILE.TXT", MODE_PRIVATE);
        OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);
        outputWriter.write("EMPTY");
        outputWriter.close();
    }

    public void readFROM_TEXT_FILE() throws IOException {

        try {
            FileInputStream fileIn = openFileInput("STORAGE_TYPE_FILE.TXT");
            InputStreamReader InputRead= new InputStreamReader(fileIn);
            char[] inputBuffer= new char[READ_BLOCK_SIZE];
            stringREAD="";
            int charRead;

            while ((charRead = InputRead.read(inputBuffer))>0) {
                // char to string conversion
                String readstring = String.copyValueOf(inputBuffer,0,charRead);
                stringREAD += readstring;
            }
            InputRead.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {

            case 666: // User selected Allowed  Permission Granted
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Snackbar s = Snackbar.make(findViewById(android.R.id.content), "Permission Granted", Snackbar.LENGTH_LONG);
                    View snackbarView = s.getView();
                    snackbarView.setBackgroundColor(Color.LTGRAY);
                    TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(Color.BLUE);
                    textView.setTextSize(18);
                    textView.setMaxLines(6);
                    s.show();

                    // Go to MainActivity Permission Granted
                    Intent intent = new Intent(CheckStorageActivity.this, MainActivity.class);
                    startActivity(intent);

                    // User selected the Never Ask Again Option OR Changed settings in app settings manually
                } else if (Build.VERSION.SDK_INT >= 23 && !shouldShowRequestPermissionRationale(permissions[0])) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
                    alertDialogBuilder.setTitle("Change Permissions in Settings");
                    alertDialogBuilder
                            .setMessage("Click SETTINGS to Manually Set\n\n" + "Permissions to use Database Storage")
                            .setCancelable(false)
                            .setPositiveButton("SETTINGS", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    startActivityForResult(intent, 1000);
                                    btnNext.setVisibility(View.VISIBLE);
                                }

                            });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                } else {
                    // User selected Deny Dialog to UN-INSTALL App ==> OR <== RETRY to have a second chance to Allow Permissions
                    if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
                        alertDialogBuilder.setTitle("Second Chance");
                        alertDialogBuilder
                                .setMessage("Click RETRY to Allow Permissions\n\n" + "Click UN-INSTALL to Remove App")
                                .setCancelable(false)
                                .setPositiveButton("RETRY", new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent i = new Intent(CheckStorageActivity.this, CheckStorageActivity.class);
                                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(i);
                                    }
                                })
                                .setNegativeButton("UN-INSTALL", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        finish();
                                        dialog.cancel();
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                                        intent.setData(uri);
                                        startActivityForResult(intent, 1000);
                                    }
                                });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }
                }
                break;
        }}

}// END CLASS
