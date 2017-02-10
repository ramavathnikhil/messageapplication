package nikhil.com.smsapplication.Activites;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import nikhil.com.smsapplication.R;

public class SendSmsActivity extends AppCompatActivity {


    private ImageView contactPicker;
    private AutoCompleteTextView autoCompleteTextView;
    private static final int RESULT_PICK_CONTACT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_sms);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(" ");
        // getSupportActionBar().setIcon(R.mipmap.alvoff_logo_wb);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        elementsIntilization();
        onClickListeners();

    }


    public void elementsIntilization() {

        //AUTO COMPLETE TEXTVIEW INTIZLIATION
        autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.to);
        autoCompleteTextView.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.list_autocomplete_item_contact, R.id.tv_ContactName, getAllContactNames()));

        //IMAGEVIEW INTILIZATION
        contactPicker = (ImageView) findViewById(R.id.getContact);
    }


    public void onClickListeners() {
        //IMAGEVIEW ONCLICK LISTENER
        contactPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // check whether the result is ok
        if (resultCode == RESULT_OK) {
            // Check for the request code, we might be usign multiple startActivityForReslut
            switch (requestCode) {
                case RESULT_PICK_CONTACT:
                    contactPicked(data);
                    break;
            }
        } else {
            Log.e("MainActivity", "Failed to pick contact");
        }
    }

    /**
     * Query the Uri and read contact details. Handle the picked contact data.
     *
     * @param data
     */
    private void contactPicked(Intent data) {
        Cursor cursor = null;
        try {
            String phoneNo = null;
            String name = null;
            // getData() method will have the Content Uri of the selected contact
            Uri uri = data.getData();
            //Query the content uri
            cursor = getContentResolver().query(uri, null, null, null, null);
            cursor.moveToFirst();
            // column index of the phone number
            int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            // column index of the contact name
            int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            phoneNo = cursor.getString(phoneIndex);
            name = cursor.getString(nameIndex);
            // Set the value to the textviews
            autoCompleteTextView.setText(name);
            // textView2.setText(phoneNo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Get All the Contact Names
     *
     * @return
     */
    private List<String> getAllContactNames() {
        List<String> lContactNamesList = new ArrayList<String>();
        try {
            // Get all Contacts
            String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER};

            Cursor lPeople = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, null, null, null);
            if (lPeople != null) {
                while (lPeople.moveToNext()) {
                    // Add Contact's Name into the List
                    int indexName = lPeople.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                    int indexNumber = lPeople.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                    lContactNamesList.add(lPeople.getString(indexName) + " ( " + lPeople.getString(indexNumber) + " )");

                }
            }
        } catch (NullPointerException e) {
            Log.e("getAllContactNames()", e.getMessage());
        }
        return lContactNamesList;
    }


}
