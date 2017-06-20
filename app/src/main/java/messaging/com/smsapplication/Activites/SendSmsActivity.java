package messaging.com.smsapplication.Activites;

import android.Manifest;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import messaging.com.smsapplication.Adapters.ChatRoomThreadAdapter;
import messaging.com.smsapplication.Models.Message;
import messaging.com.smsapplication.R;
import messaging.com.smsapplication.Views.SoftKeyboard;

public class SendSmsActivity extends AppCompatActivity {


    private String selectedPhoneNumber = "";
    private String chatRoomId;
    private RecyclerView recyclerView;
    private ChatRoomThreadAdapter mAdapter;
    private ArrayList<Message> messageArrayList;
    private EditText inputMessage;
    private Button btnSend;
    private List<String> mobileNumbers = new ArrayList<>();
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int REQ_CODE_SPEECH_INPUT = 2;
    private Cursor cursor;
    private TextView AiResult;
    private Boolean fromIntent = false;
    private Button clickMe;
    private EditText queryEditext;
    private ImageView mic;
    SoftKeyboard softKeyboard;
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
        checkIntentData();

    }

    public void checkIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("number")) {
                String phonenumber = intent.getStringExtra("number");
                if (phonenumber != null && !phonenumber.isEmpty()) {
                    selectedPhoneNumber = phonenumber;
                    autoCompleteTextView.setFocusable(false);
                    autoCompleteTextView.setText(intent.getStringExtra("name"));
                    contactPicker.setVisibility(View.INVISIBLE);
                    messageArrayList.addAll(getAllSmsModel(selectedPhoneNumber));
                }
            }

        }
    }

    public void elementsIntilization() {

        //AUTO COMPLETE TEXTVIEW INTIZLIATION
        autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.to);
        autoCompleteTextView.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.list_autocomplete_item_contact, R.id.tv_ContactName, getAllContactNames()));
        autoCompleteTextView.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() == 0) {
                    selectedPhoneNumber = "";
                    messageArrayList.clear();
                    mAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.length() == 0) {
                    selectedPhoneNumber = "";
                    messageArrayList.clear();
                    mAdapter.notifyDataSetChanged();
                }


            }
        });
        //IMAGEVIEW INTILIZATION
        contactPicker = (ImageView) findViewById(R.id.getContact);
        mic = (ImageView) findViewById(R.id.mic);

        //EDITEXT INTILIZATION
        inputMessage = (EditText) findViewById(R.id.message);

        //BUTTON INTILIZATION
        btnSend = (Button) findViewById(R.id.btn_send);

        //RECYCLERVIEW INTILIZATION
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        messageArrayList = new ArrayList<>();
        mAdapter = new ChatRoomThreadAdapter(this, messageArrayList, "1");
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);


        inputMessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    //got focus
                    mic.setVisibility(View.VISIBLE);
                } else {
                    //lost focus
                    mic.setVisibility(View.GONE);
                }
            }
        });

        RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.main); // You must use your root layout
        InputMethodManager im = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);

/*
Instantiate and pass a callback
*/

        softKeyboard = new SoftKeyboard(mainLayout, im);
        softKeyboard.setSoftKeyboardCallback(new SoftKeyboard.SoftKeyboardChanged() {

            @Override
            public void onSoftKeyboardHide() {
                // Code here

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mic.setVisibility(View.GONE);

                    }
                });
            }

            @Override
            public void onSoftKeyboardShow() {
                // Code here
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mic.setVisibility(View.VISIBLE);

                    }
                });
            }
        });

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

        //BUTTON ONCLICK LISTENER
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inputMessage.getText().toString().isEmpty()) {
                    Toast.makeText(SendSmsActivity.this, "Please enter some message", Toast.LENGTH_SHORT).show();
                } else if (autoCompleteTextView.getText().toString().isEmpty()) {
                    Toast.makeText(SendSmsActivity.this, "Please enter contact number", Toast.LENGTH_SHORT).show();
                } else {
                    if (selectedPhoneNumber.isEmpty()) {
                        if (isValidPhoneNumber(autoCompleteTextView.getText().toString())) {


                            if (!fromIntent) {
                                softKeyboard.closeSoftKeyboard();
                                messageArrayList.clear();
                                Message message = new Message();
                                message.setMessage(inputMessage.getText().toString());
                                message.setUser("2");
                                messageArrayList.add(message);
                                messageArrayList.addAll(getAllSmsModel(autoCompleteTextView.getText().toString()));

                                mAdapter.notifyDataSetChanged();
                                recyclerView.scrollToPosition(mAdapter.getItemCount() - 1);


                                try {

                                    SmsManager smsManager = SmsManager.getDefault();
                                    smsManager.sendTextMessage(autoCompleteTextView.getText().toString(), null, inputMessage.getText().toString(), null, null);
                                    Toast.makeText(getApplicationContext(), "SMS sent.",
                                            Toast.LENGTH_LONG).show();
                                    inputMessage.setText("");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }


                        } else {
                            Toast.makeText(SendSmsActivity.this, "Please enter valid mobile number", Toast.LENGTH_SHORT).show();
                        }
                    } else {


                        if (!fromIntent) {
                            softKeyboard.closeSoftKeyboard();

                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(selectedPhoneNumber, null, inputMessage.getText().toString(), null, null);
                            Toast.makeText(getApplicationContext(), "SMS sent.",
                                    Toast.LENGTH_LONG).show();
                            inputMessage.setText("");
                            messageArrayList.clear();
                            Message message = new Message();
                            message.setMessage(inputMessage.getText().toString());
                            message.setUser("2");
                            messageArrayList.add(message);
                            messageArrayList.addAll(getAllSmsModel(selectedPhoneNumber));

                            mAdapter.notifyDataSetChanged();
                            recyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
                        }
                    }


                }
            }
        });

        //IMAGEVIEW ONCLICK LISTENER
        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ActivityCompat.checkSelfPermission(SendSmsActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(SendSmsActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_CODE);

                    return;
                }

                promptSpeechInput();
            }
        });

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                selectedPhoneNumber = extractMobileNumberFromString(autoCompleteTextView.getText().toString());
            }
        });


    }


    public List<Message> getAllSmsModel(String selectedPhoneNumber) {


        List<Message> lstSmsModel = new ArrayList<Message>();

        Message objSmsModel = new Message();
        Uri message = Uri.parse("content://sms/");
        ContentResolver cr = getContentResolver();

        //  cursor = cr.query(message, null, "address='" + selectedPhoneNumber + "'", null, null);
        cursor = cr.query(message, null, null, null, null);
        startManagingCursor(cursor);
        if (cursor != null) {
            int totalSmsModel = cursor.getCount();

            if (cursor.moveToFirst()) {
                for (int i = 0; i < totalSmsModel; i++) {

                    objSmsModel = new Message();

                    objSmsModel.setMessage(cursor.getString(cursor.getColumnIndexOrThrow("body")));

                    objSmsModel.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("date")));
                    objSmsModel.setUser("1");


                    if (cursor.getString(cursor.getColumnIndexOrThrow("type")).contains("1")) {
                        objSmsModel.setUser("1");
                    } else {
                        objSmsModel.setUser("2");
                    }


                 /*   if (cursor.getString(cursor
                            .getColumnIndexOrThrow("address")).equals(selectedPhoneNumber))*/

                    String contactNumberTemp = cursor.getString(cursor
                            .getColumnIndexOrThrow("address"));
                    if (contactNumberTemp!=null && contactNumberTemp.substring(0, 0).replaceAll("\\s+", "").equals("+")) {
                        // assign value string value expect first character
                        contactNumberTemp = contactNumberTemp.substring(1);
                    }


                    if (contactNumberTemp!=null && contactNumberTemp.equals(selectedPhoneNumber))
                        lstSmsModel.add(objSmsModel);


                    cursor.moveToNext();
                }

            }


        } else {
            Log.d("null", "true");
        }


        return lstSmsModel;


    }

    private boolean isValidPhoneNumber(CharSequence phoneNumber) {
        if (!TextUtils.isEmpty(phoneNumber)) {
            return Patterns.PHONE.matcher(phoneNumber).matches();
        }
        return false;
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

                case REQ_CODE_SPEECH_INPUT: {
                    if (resultCode == RESULT_OK && null != data) {

                        ArrayList<String> result = data
                                .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        // txtSpeechInput.setText(result.get(0));
                        inputMessage.setText(result.get(0));
                    }
                    break;

                }
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
            selectedPhoneNumber = phoneNo;
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
                    mobileNumbers.add(lPeople.getString(indexNumber));

                }
            }
        } catch (NullPointerException e) {
            Log.e("getAllContactNames()", e.getMessage());
        }
        return lContactNamesList;
    }


    public String extractMobileNumberFromString(String contactText) {
        try {
            Pattern regex = Pattern.compile("(?:\\d+\\s*)+");
            Matcher regexMatcher = regex.matcher(contactText);
            if (regexMatcher.find()) {
                return regexMatcher.group(0);
            }
        } catch (PatternSyntaxException ex) {
            // Syntax error
            return "";
        }

        return "";
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        softKeyboard.unRegisterSoftKeyboardCallback();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        inputMessage.clearFocus();
        mic.setVisibility(View.GONE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    promptSpeechInput();

                } else {

                    // Snackbar.make(view,"Permission Denied, You cannot access location data.",Snackbar.LENGTH_LONG).show();
                    Toast.makeText(SendSmsActivity.this, "Please provide us permission to record your voice", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    /**
     * Showing google speech input dialog
     */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }


}
