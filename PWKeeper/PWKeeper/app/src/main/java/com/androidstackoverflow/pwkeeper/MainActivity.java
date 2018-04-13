package com.androidstackoverflow.pwkeeper;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.androidstackoverflow.pwkeeper.DBHelper.Col_MPW;
import static com.androidstackoverflow.pwkeeper.DBHelper.TABLE_PW;

public class MainActivity extends AppCompatActivity {

    Button btnSave;
    Button btnEnter;
    TextView tvPW;
    TextView tvCPW;
    EditText etPW;
    EditText etCPW;
    ImageView imageView;

    static DBHelper helper;
    SQLiteDatabase db;

    public static String str;
    public static  String THE_PATH;
    public static String writeVALUE;

    static final int READ_BLOCK_SIZE = 100;
    String stringREAD = "";
    String result = "false";
    public static final String Name = "nameKey";
    public static int STORAGE_LOCATION;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        tvPW = (TextView) findViewById(R.id.tvPW);
        tvCPW = (TextView) findViewById(R.id.tvCPW);
        etPW = (EditText) findViewById(R.id.etPW);
        etCPW = (EditText) findViewById(R.id.etCPW);

        btnEnter = (Button) findViewById(R.id.btnEnter);
        btnSave = (Button) findViewById(R.id.btnSave);
        addListenerOnButtonSave();
        imageView = (ImageView) findViewById(R.id.imageView);

        setTitle("");
        Toolbar topToolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(topToolBar);
        //topToolBar.setLogo(R.drawable.lockgold);
        // Added image to topToolBar to display larger image for Tablets

        readFROM_TEXT_FILE();

       /* String secStore = System.getenv("SECONDARY_STORAGE");
        File secs = new File(secStore);
        String gt = String.valueOf(secs);
        System.out.println("$$$$$$$$$$$$$$$$ gt "+gt);
        // out put /storage/extSdCard

        String extStore = System.getenv("EXTERNAL_STORAGE");
        File exts = new File(extStore);
        String tg = String.valueOf(exts);
        System.out.println("################## tg "+tg);
        // out put /sdcard*/

        File fi = new File("storage/");
        File[] lst = fi.listFiles();//
        String top = String.valueOf(lst[1]);
        String bot = String.valueOf(lst[0]);

        if(bot.contains("-")){
            STORAGE_LOCATION = 1;
        }
        if(top.contains("storage/enc_emulated")){
            STORAGE_LOCATION = 0;
        }

        if(stringREAD.matches("EMPTY") && STORAGE_LOCATION == 1){
            storageDIALOG();
        }
        if(stringREAD.matches("EMPTY") && STORAGE_LOCATION == 0) {
            internalDIALOG();
        }
        SharedPreferences prefPW = getSharedPreferences("MyPref", MODE_PRIVATE);
        str = prefPW.getString(Name,"");// Get Stored MyOref VALUE so onLoad() is not used till DB created

        if (str == null || str.isEmpty()) {
            etPW.requestFocus();
            btnSave.setVisibility(View.VISIBLE);

        } else {

            tvCPW.setVisibility(View.INVISIBLE);
            etCPW.setVisibility(View.INVISIBLE);
            btnSave.setVisibility(View.INVISIBLE);
            btnEnter.setVisibility(View.VISIBLE);
        }

        // This Code Below Maintains the variable THE_PATH
        if(stringREAD.matches("EXTERNAL")) {
            STORAGE_LOCATION = 1;
            getThePath();
        }
        if(stringREAD.matches("INTERNAL")){
            STORAGE_LOCATION = 0;
            getThePath();
        }

        theMSG();

        //if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && !stringREAD.equals("EMPTY"))  {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 666);
            }
        }

    }// END onCreate(Bundle
    /* #################### */

    public void internalDIALOG(){

        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        builder.setCancelable(false);

        builder.setTitle("SD Card NOT Mounted ");
        builder.setMessage("Click OK to use INTERNAL Device Storage");
        builder.setNeutralButton("OK",new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                STORAGE_LOCATION = 0;
                getThePath();

                try {
                    FileOutputStream fileout = openFileOutput("STORAGE_TYPE_FILE.TXT", MODE_PRIVATE);
                    OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);
                    outputWriter.write("INTERNAL");
                    writeVALUE = "INTERNAL";
                    outputWriter.close();
                    //display file saved message
                    Toast.makeText(getApplicationContext(), "Data IS Stored " + writeVALUE, Toast.LENGTH_LONG).show();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        builder.show();
    }


    public void storageDIALOG(){

        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        builder.setCancelable(false);

        builder.setTitle("Select Data Storage Location ");
        builder.setMessage("EXTERNAL Use SD CARD\n\n"+"INTERNAL Device Storage");

        builder.setPositiveButton("EXTERNAL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                STORAGE_LOCATION = 1;
                getThePath();

                try {
                    FileOutputStream fileout = openFileOutput("STORAGE_TYPE_FILE.TXT", MODE_PRIVATE);
                    OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);
                    outputWriter.write("EXTERNAL");
                    writeVALUE = "EXTERNAL";
                    outputWriter.close();
                    //display where file saved message
                    Toast.makeText(getApplicationContext(), "Data IS Stored " + writeVALUE, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        builder.setNegativeButton("INTERNAL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                STORAGE_LOCATION = 0;
                getThePath();

                try {
                    FileOutputStream fileout = openFileOutput("STORAGE_TYPE_FILE.TXT", MODE_PRIVATE);
                    OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);
                    outputWriter.write("INTERNAL");
                    writeVALUE = "INTERNAL";
                    outputWriter.close();
                    //display where file saved message
                    Toast.makeText(getApplicationContext(), "Data IS Stored " + writeVALUE, Toast.LENGTH_LONG).show();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        builder.show();
    }

    public String getThePath(){

        /*File removable = ContextCompat.getExternalFilesDirs(this,null)[0];*/
        /* Line of CODE above is for TESTING on EMULATOR only */
        File removable = ContextCompat.getExternalFilesDirs(this,null)[STORAGE_LOCATION];
        /* Line of CODE above is for PRODUCTION ONLY it will not run on Emulator */

        if(STORAGE_LOCATION == 1){
            THE_PATH = String.valueOf(removable);
            THE_PATH = THE_PATH + "/Documents/";
        }
        if(STORAGE_LOCATION == 0){
            THE_PATH = String.valueOf(removable);
            THE_PATH = THE_PATH + "/INTERNAL/";
            Toast.makeText(getApplicationContext(),"NO SD CARD", Toast.LENGTH_LONG).show();
        }
        return THE_PATH;
    }
    public void theMSG(){// IF TRUE CODE DOES NOT RUN
        if(result.equals("false")){
            if(stringREAD.equalsIgnoreCase("INTERNAL")){
                Toast.makeText(getApplicationContext(), "Data is Stored " + stringREAD, Toast.LENGTH_LONG).show();
            }
            if(stringREAD.equalsIgnoreCase("EXTERNAL")) {
                Toast.makeText(getApplicationContext(), "Data is Stored " + stringREAD, Toast.LENGTH_LONG).show();
            }
        }
    }

    public void readFROM_TEXT_FILE() {

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

    private void addListenerOnButtonSave() {
        btnSave.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(etPW.getText().length() < 8 || etPW.getText().length() >10){
                    Toast.makeText(getApplicationContext(), "Password Max Length is 10 Characters\n"
                            +"\nPassword Min Length is  8 Characters", Toast.LENGTH_LONG ).show();
                    return;
                }
                //String tstr = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
                String tstr = "^(?!.*\\s)(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[$@!%*?&])";
                String astr = etPW.getText().toString().trim();
                Pattern regex = Pattern.compile(tstr);
                Matcher regexMatcher = regex.matcher(astr);

                boolean foundMatch = regexMatcher.find();

                if(foundMatch == false){

                    Toast.makeText( getApplicationContext(), "Password must have one Numeric Value\n"
                            + "\nOne Upper & Lower Case Letters\n"
                            + "\nOne Special Character $ @ ! % * ? &\n"
                            +"\nNO Spaces in the PASSWORD", Toast.LENGTH_LONG ).show();
                    //etPW.setText("");
                    //etCPW.setText("");
                    // Two lines of code above are optional
                    // Also by design these fields MUST be set to input type Password in the XML file
                    etPW.requestFocus();

                    return ;
                }

                if(!etPW.getText().toString().equals(etCPW.getText().toString())){
                    Toast.makeText(getApplicationContext(),"Password Does NOT\n"+"Match Confirm Password",Toast.LENGTH_LONG).show();
                    etPW.setText("");
                    etCPW.setText("");
                    etPW.requestFocus();
                    return;
                }
                doAdd();
            }
        } );
    }

    public void doAdd() {

        // Adds Master Password to TABLE_PW after THE_PATH is set
        helper = new DBHelper(this);
        db = helper.getWritableDatabase();

        String password = etPW.getText().toString().trim();
        ContentValues cv = new ContentValues();
        cv.put(Col_MPW,password);
        db.insert( TABLE_PW,null,cv);
        etPW.setText("");
        etCPW.setText("");
        onLoad();/* CALLED to manage view for onEnter method */
        db.close();
    }

    private void onLoad() {
    /* Gets variable str from TABLE_PW and manages View to SAVE PW or CONFIRM PW */
    /* This CODE can NOT be called before THE_PATH is defined and used when the DB is CREATED */
    /* ###################################################################################### */
        helper = new DBHelper(this);
        str = helper.getCol_MPW();
        // Line of code above calls getCol_MPW method in DBHelper
        // And DBHwlper RETURNS str Variable with the Master Password
        // onLoad method is called in the  doAdd method
        //============================================================
        if (str == null || str.isEmpty()) {
            etPW.requestFocus();
            btnSave.setVisibility(View.VISIBLE);

        } else {

            tvCPW.setVisibility(View.INVISIBLE);
            etCPW.setVisibility(View.INVISIBLE);
            btnSave.setVisibility(View.INVISIBLE);
            btnEnter.setVisibility(View.VISIBLE);
        }
    }

    /*========================================================================================
        Notice the btnEnter has <=NO=> addListenerOnButtonEnter() defined
        so how does it know what to do ? How to respond to the click event
        Look at btnEnter in the XML file take note of this property android:onClick="onEnter"
        So the btnEnter when clicked executes the code in the method onEnter below
     ==========================================================================================*/
    public void onEnter(View view)  {

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 666);  // Comment 26
                etPW.setText("");
                return;
            }
        }

        onLoad();

        if(str.equals(etPW.getText().toString().trim())){
            SharedPreferences pref = getSharedPreferences("MyPref", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(Name, str);
            editor.apply();
            /* Code above stores the password in a key-value pair this is used
             *  to reload that value if the app is closed and then restarted */
            /* THIS HELPS PREVENTING onLoad() FROM RUNNING BEFORE db is created */
            /* %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% */

            etPW.setText( "" );
            str = "";
            Intent intent = new Intent( MainActivity.this, ListActivity.class );
            startActivity( intent );
        } else {
            Toast.makeText( getApplicationContext(), "Incorrect Password", Toast.LENGTH_LONG ).show();
            etPW.setText( "" );
            etPW.requestFocus();
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

                    // User selected the Never Ask Again Option Change settings in app settings manually
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
                                }

                            });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                } else {
                    // User selected Deny Dialog to EXIT App ==> OR <== RETRY to have a second chance to Allow Permissions
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
                        alertDialogBuilder.setTitle("Second Chance");
                        alertDialogBuilder
                                .setMessage("Click RETRY to Allow Permissions\n\n" + "Click UN-INSTALL to Remove App")
                                .setCancelable(false)
                                .setPositiveButton("RETRY", new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent i = new Intent(MainActivity.this, MainActivity.class);
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
        }};

    public void onBackPressed(){
        Toast.makeText(getApplicationContext(), "This EXIT Closed", Toast.LENGTH_LONG ).show();
    }
}
