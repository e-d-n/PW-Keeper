package com.androidstackoverflow.pwkeeper;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

import static com.androidstackoverflow.pwkeeper.DBHelper.TABLE_PW;
import static com.androidstackoverflow.pwkeeper.DBHelper.Col_IDI;
import static com.androidstackoverflow.pwkeeper.DBHelper.Col_MPW;
import static com.androidstackoverflow.pwkeeper.RecyclerAdapter.dbList;

public class DetailsActivity extends AppCompatActivity  {

    TextView tvDA;
    String tORf;
    int position;
    String str;
    Button btnSave, btnDelete, btnUpdate;
    EditText etWebSite, etUN, etPW, etSecQuestion, etSecAnswer, etNotes;
    ImageView imageTB;

    private DBHelper helper;
    private SQLiteDatabase db;
    private Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent intentSP = getIntent();
        Bundle bundle = intentSP.getExtras();
        tORf = bundle.getString("FROM_LIST_ACTIVITY");

        Intent intentN = getIntent();
        Bundle extras = intentN.getExtras();
        position = extras.getInt("POSITION");
        tORf = extras.getString("FROM_LIST_ACTIVITY");

        tvDA = (TextView) findViewById(R.id.tvDA);
        btnSave = (Button) findViewById(R.id.btnSave);
        btnDelete = (Button) findViewById(R.id.btnDelete);
        btnUpdate = (Button) findViewById(R.id.btnUpdate);

        etWebSite = (EditText) findViewById(R.id.etWebSite);
        etUN = (EditText) findViewById(R.id.etUN);
        etPW = (EditText) findViewById(R.id.etPW);
        etSecQuestion = (EditText) findViewById(R.id.etSecQuestion);
        etSecAnswer = (EditText) findViewById(R.id.etSecAnswer);
        etNotes = (EditText) findViewById(R.id.etNotes);
        imageTB = (ImageView) findViewById(R.id.imageTB);

        addListenerOnButtonSave();
        addListenerOnButtonDelete();
        addListenerOnButtonUpdate();
        addTextChangedListener();

        hint_text_listener();
        //Works with METHOD TO PERMIT Notes Editing
        //==========================================

        imageTB.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                etNotes.setEnabled(true);
                etNotes.requestFocus();
                etNotes.setFocusableInTouchMode(true);
                Toast.makeText(DetailsActivity.this, "Now You Can Edit", Toast.LENGTH_LONG).show();
                return false;
            }
        });

        if (tORf.equals("true")) {
            tvDA.setText("Add New Data");

        } else {
            tvDA.setText("Detail View");
            btnDelete.setVisibility(View.VISIBLE);
            btnUpdate.setVisibility(View.VISIBLE);

            if(tvDA.getText().toString().equals("Add New Data")){
                etNotes.setHint(R.string.hint_edit);
            }else{
                etNotes.setHint("");
                // This IF statement decides if HINT text is shown or NOT
                // DO NOT WANT Hint Text with an EDIT of Data
            }
        }

        setTitle("");
        // We do not want a title on DetailActivity toolbar this removes the title
        // Title is set in the ABOVE if statements based on what was selected on List View
        //=================================================================================
        Toolbar topToolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(topToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        helper = new DBHelper(this);
        dbList = new ArrayList<>();
        dbList = helper.getDataFromDB();

        if (dbList.size() > 0 && tORf.equalsIgnoreCase("false")) {

            btnSave.setVisibility(View.INVISIBLE);

            String Nwhat = dbList.get(position).getRowid();
            String Nwebsite = dbList.get(position).getWebsite();
            String Nusername = dbList.get(position).getUsernane();
            String Npassword = dbList.get(position).getPassword();
            String Nquestion = dbList.get(position).getQuestion();
            String Nanswer = dbList.get(position).getAnswer();
            String Nnotes = dbList.get(position).getNotes();

            etWebSite.setText(Nwebsite);
            etUN.setText(Nusername);
            etPW.setText(Npassword);
            etSecQuestion.setText(Nquestion);
            etSecAnswer.setText(Nanswer);
            etNotes.setText(Nnotes);
        }

    }// END onCreate Bundle

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void hint_text_listener() {
        /*
        This listener is set in the onCreate Bundle section of the program
        It fires when the EditText field etSecAnswer gains focus WHY ?
        etSecAnswer is a required field when SAVEING where as etNotes is not required
         */
        etSecAnswer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    etNotes.setHint(null);
                }
            }
        });
    }

    /* CODE below manages the etNotes by limiting the etNotes to 3 lines */
    /* and it removes the 4th line when the ENTER key is pressed it also renders */
    /* the etNotes DISABLED hence the need for the OnLongClickListener on the Image Keys */

    private void addTextChangedListener() {
        etNotes.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int L = etNotes.getLineCount();
                if (L > 3) {
                    etNotes.getText().delete(etNotes.getSelectionEnd() - 1, etNotes.getSelectionStart());
                    //etNotes.append("\b"); Used for TESTING line of code above
                    etNotes.setEnabled(false);
                    Toast.makeText(DetailsActivity.this, "Only 3 Lines Permitted\n\nLong Press on the Keys to Edit", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void addListenerOnButtonDelete() {
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Calls the Method deleteDBRow in DatabaseHelper
                // which acts on the TABLE_INFO to remove a record by getting the record ID
                helper.deleteDBRow(String.valueOf(dbList.get(position).getRowid()));
                ListActivity.removeListRow(position);
                // Code line above calls Method in ListActivity to notify recycler view of changes
                // NOTICE the List keeps items by position not record ID <== READ
                etWebSite.setText("");
                etUN.setText("");
                etPW.setText("");
                etSecQuestion.setText("");
                etSecAnswer.setText("");
                etNotes.setText("");

                Intent intent = new Intent(DetailsActivity.this, ListActivity.class);
                startActivity(intent);
            }
        });
    }

    private void addListenerOnButtonUpdate() {
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String website = etWebSite.getText().toString();
                String username = etUN.getText().toString();
                String password = etPW.getText().toString();
                String question = etSecQuestion.getText().toString();
                String answer = etSecAnswer.getText().toString();
                String notes = etNotes.getText().toString();

                if (etWebSite.length() == 0) {
                    Toast.makeText(DetailsActivity.this, "Enter Web-Site Name", Toast.LENGTH_LONG).show();
                    etWebSite.requestFocus();
                    return;
                }
                if (etUN.length() == 0) {
                    Toast.makeText(DetailsActivity.this, "Enter Username", Toast.LENGTH_LONG).show();
                    etUN.requestFocus();
                    return;
                }
                if (etPW.length() == 0) {
                    Toast.makeText(DetailsActivity.this, "Enter Password", Toast.LENGTH_LONG).show();
                    etPW.requestFocus();
                    return;
                }
                if (etSecQuestion.length() == 0) {
                    Toast.makeText(DetailsActivity.this, "Enter Security Question", Toast.LENGTH_LONG).show();
                    etSecQuestion.requestFocus();
                    return;
                }
                if (etSecAnswer.length() == 0) {
                    Toast.makeText(DetailsActivity.this, "Enter Security Answer", Toast.LENGTH_LONG).show();
                    etSecAnswer.requestFocus();
                    return;
                }

                String rowid = dbList.get(position).getRowid();
                helper.updateDBRow(rowid, website, username, password, question, answer, notes);

                etWebSite.setText("");
                etUN.setText("");
                etPW.setText("");
                etSecQuestion.setText("");
                etSecAnswer.setText("");
                etNotes.setText("");

                Intent intentTO = new Intent(DetailsActivity.this, ListActivity.class);
                startActivity(intentTO);
                Toast.makeText(DetailsActivity.this, "Record Updated", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addListenerOnButtonSave() {
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String website = etWebSite.getText().toString();
                String username = etUN.getText().toString();
                String password = etPW.getText().toString();
                String question = etSecQuestion.getText().toString();
                String answer = etSecAnswer.getText().toString();
                String notes = etNotes.getText().toString();

                if (etWebSite.length() == 0) {
                    Toast.makeText(DetailsActivity.this, "Enter Web-Site Name", Toast.LENGTH_LONG).show();
                    etWebSite.requestFocus();
                    return;
                }
                if (etUN.length() == 0) {
                    Toast.makeText(DetailsActivity.this, "Enter Username", Toast.LENGTH_LONG).show();
                    etUN.requestFocus();
                    return;
                }
                if (etPW.length() == 0) {
                    Toast.makeText(DetailsActivity.this, "Enter Password", Toast.LENGTH_LONG).show();
                    etPW.requestFocus();
                    return;
                }
                if (etSecQuestion.length() == 0) {
                    Toast.makeText(DetailsActivity.this, "Enter Security Question", Toast.LENGTH_LONG).show();
                    etSecQuestion.requestFocus();
                    return;
                }
                if (etSecAnswer.length() == 0) {
                    Toast.makeText(DetailsActivity.this, "Enter Security Answer", Toast.LENGTH_LONG).show();
                    etSecAnswer.requestFocus();
                    return;
                }

                helper.insertIntoDB(website, username, password, question, answer, notes);

                etWebSite.setText("");
                etUN.setText("");
                etPW.setText("");
                etSecQuestion.setText("");
                etSecAnswer.setText("");
                etNotes.setText("");

                Intent intentTO = new Intent(DetailsActivity.this, ListActivity.class);
                startActivity(intentTO);

                Toast.makeText(DetailsActivity.this, "Record Added", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {// Little BACK <== arrow key on ToolBar
            Intent intent = new Intent(DetailsActivity.this, ListActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_ReSetPW) {
            // This is the menu ReSetPW button for Master Password
            // It lives in the menu_main.xml file
            doCustom();
        }
        return super.onOptionsItemSelected(item);
    }

    private void doCustom(){
        /* This method uses the custom_dialog.xml file created for greater control over
           the styling of the Custom Alert Dialog for various screen sizes and to be
           able to set the text size of the dialog message text
         */
        final Dialog openDialog = new Dialog(context);
        openDialog.setContentView(R.layout.custom_dialog);
        Button btnYES = openDialog.findViewById(R.id.btnYES);
        Button btnNO = openDialog.findViewById(R.id.btnNO);

        // if YES delete Master Password from TABLE_MPW
        btnYES.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences pref = getSharedPreferences("MyPref", MODE_PRIVATE);
                pref.edit().remove("nameKey").apply();

                db = helper.getReadableDatabase();
                String q = "SELECT * FROM "+ TABLE_PW;
                Cursor cursor = db.rawQuery(q,null);
                // Above query gets TABLE_PW data from Col_IDI
                // TABLE_PW will only ever have one row of data

                int rowID = 99;
                if(cursor.moveToFirst()){
                    rowID = cursor.getInt(cursor.getColumnIndex(Col_IDI));
                    str = cursor.getString(cursor.getColumnIndex(Col_MPW));
                }
                cursor.close();

                // Line of code below WORKS deletes entire TABLE <=====
                // Not a recommended way to re-set the master password
                // db.delete(TABLE_PW, null, null);

                String num = Integer.toString(rowID);

                db.delete(TABLE_PW, Col_IDI + " = ?", new String[] { num });
                db.close();
                openDialog.dismiss();

                Intent intentYY = new Intent(DetailsActivity.this, MainActivity.class );
                startActivity( intentYY );

                Toast.makeText(getApplicationContext(), "Changed the Password", Toast.LENGTH_SHORT).show();
            }
        });

        btnNO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog.dismiss();

                Intent intent = new Intent( DetailsActivity.this, ListActivity.class );
                startActivity( intent );

                Toast.makeText(getApplicationContext(), "Password NOT Changed", Toast.LENGTH_SHORT).show();
            }
        });
        openDialog.show();
    }

    public void onBackPressed(){
        Intent intent = new Intent( DetailsActivity.this, ListActivity.class );
        startActivity( intent );
    }
}
