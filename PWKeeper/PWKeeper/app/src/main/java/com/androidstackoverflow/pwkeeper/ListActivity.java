package com.androidstackoverflow.pwkeeper;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import static com.androidstackoverflow.pwkeeper.DBHelper.DB_NAME;
import static com.androidstackoverflow.pwkeeper.MainActivity.STORAGE_LOCATION;

public class ListActivity extends AppCompatActivity {

    static DBHelper helper;
    TextView tvNoData;
    static final int READ_BLOCK_SIZE = 100;
    String readstring;
    String stringREAD;
    Button btnAdd;

    static List<DatabaseModel> dbList;
    RecyclerView mRecyclerView;
    private static RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        addListenerOnButtonAdd();
        tvNoData = (TextView)findViewById(R.id.tvNoData);
        btnAdd = findViewById(R.id.btnAdd);
        setTitle("");// This sets the title of the toolbar
        Toolbar topToolBar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(topToolBar);
        //topToolBar.setLogo(R.drawable.keyss);// See Notes in MainActivity

        setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        // Line of code above is used when you navigate back from DetailsActivity with
        // soft keyboard visible on DetailActivity WILL NOT BECOME VISIBLE ON ListActivity

        helper = new DBHelper(this);
        dbList = new ArrayList<>();
        dbList = helper.getDataFromDB();

        mRecyclerView = (RecyclerView)findViewById(R.id.recycleview);
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Code below defines the adapter
        mAdapter = new RecyclerAdapter(this,dbList);
        mRecyclerView.setAdapter(mAdapter);

        int sz = dbList.size();

        if(sz == 0){
            tvNoData.setVisibility(View.VISIBLE);
            tvNoData.setText("No Data");
        }

    }// End of onCreate Bundle


    // This method is called from DetailsActivity and notifies Recycler View that the DB was changed
    // and the method makes the same changes to the Recycler View kind of a sync of DB and Recycler View
    public static void removeListRow(int position) {
        dbList.remove(position);
        mAdapter.notifyItemRemoved(position);
        mAdapter.notifyItemRangeChanged(position, dbList.size());
    }

    /* this BUTTON is on the ToolBar click to ADD new record */
    private void addListenerOnButtonAdd() {
        // Navigate to DetailsActivity to ADD new DATA
        Toolbar tb = (Toolbar) findViewById( R.id.toolbar );
        setSupportActionBar( tb );

        tb.findViewById( R.id.btnAdd ).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 666);  // Comment 26
                        Intent intent = new Intent(ListActivity.this, MainActivity.class);
                        startActivity(intent);

                    } else {

                        Intent intentSP = new Intent(ListActivity.this, DetailsActivity.class);
                        Bundle extras = new Bundle();
                        extras.putString("FROM_LIST_ACTIVITY", "true");
                        intentSP.putExtras(extras);
                        startActivity(intentSP);
                    }
                }
            }
        } );
    }

    public void needTOREAD(){

        try {
            FileInputStream fileIn = openFileInput("STORAGE_TYPE_FILE.TXT");
            InputStreamReader InputRead= new InputStreamReader(fileIn);
            char[] inputBuffer= new char[READ_BLOCK_SIZE];
            stringREAD="";
            int charRead;

            while ((charRead = InputRead.read(inputBuffer))>0) {
                // char to string conversion
                readstring = String.copyValueOf(inputBuffer,0,charRead);
                stringREAD += readstring;
            }
            InputRead.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void make_BACK_UP(View view){
        int buSize = dbList.size();
        if(buSize == 0){
            Toast.makeText(getApplicationContext(), "No Data in DB ", Toast.LENGTH_LONG).show();
            return;
        }

        needTOREAD();

        if(stringREAD.equals("EXTERNAL")){
            File sdLocation = ContextCompat.getExternalFilesDirs(this, null)[STORAGE_LOCATION];
            String DBname = DB_NAME;
            File currentPATH_TO_DB  = getDatabasePath(DBname).getAbsoluteFile();
            String currentDBPath = String.valueOf(currentPATH_TO_DB );
            FileChannel source;
            FileChannel destination;
            String backupDBName = "EXTERNAL_BACK_UP_PassWord";

            File currentLOCATION = new File(String.valueOf(currentDBPath));
            File backupLOCATION = new File(sdLocation,backupDBName);

            try {
                source = new FileInputStream(currentLOCATION).getChannel();
                destination = new FileOutputStream(backupLOCATION).getChannel();
                destination.transferFrom(source, 0, source.size());
                source.close();
                destination.close();
                Toast.makeText(this, "Back UP on SD CARD", Toast.LENGTH_LONG).show();
            } catch(IOException e) {
                e.printStackTrace();
            }

        }else if(stringREAD.equals("INTERNAL")){
            File sdLocation = ContextCompat.getExternalFilesDirs(this, null)[STORAGE_LOCATION];
            String DBname = DB_NAME;// GET exact database name from DBHelper
            File currentPATH_TO_DB   = getDatabasePath(DBname).getAbsoluteFile();
            String currentDBPath = String.valueOf(currentPATH_TO_DB );

            FileChannel source;
            FileChannel destination;
            String backupDBName;

            backupDBName = "INTERNAL_BACK_UP_PassWord";

            File currentLOCATION = new File(currentDBPath);
            File backupLOCATION = new File(sdLocation, backupDBName);

            try {
                source = new FileInputStream(currentLOCATION).getChannel();
                destination = new FileOutputStream(backupLOCATION).getChannel();
                destination.transferFrom(source, 0, source.size());
                source.close();
                destination.close();
                Toast.makeText(this, "Back UP is INTERNAL", Toast.LENGTH_LONG).show();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void onBackPressed(){
        Intent intentSP = new Intent(ListActivity.this, DetailsActivity.class );
        Bundle extras = new Bundle();
        extras.putString("FROM_LIST_ACTIVITY","true" );
        intentSP.putExtras(extras);
        startActivity( intentSP );
    }

}// END CLASS
