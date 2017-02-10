package nikhil.com.smsapplication.Activites;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import nikhil.com.smsapplication.Adapters.SmsAdapter;
import nikhil.com.smsapplication.Models.SmsModel;
import nikhil.com.smsapplication.R;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {


    private static final int PERMISSION_SMS_READ_REQUEST_CODE = 1;
    private static final int PERMISSION_SMS_SEND_REQUEST_CODE = 2;
    private static final int REQUEST_CODE_RESOLUTION = 3;
    private static final int REQUEST_CODE_CREATOR = 4;
    private static final String TAG = "Main activitgy";
    private RecyclerView recyclerView;
    private SmsAdapter smsAdapter;
    private Cursor cursor;
    private FloatingActionButton sendSms;
    private EditText search;
    private GoogleApiClient mGoogleApiClient;
    private static MainActivity inst;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }

    public static MainActivity instance() {
        return inst;
    }

    public void updateList(final String smsMessage) {

    }

    public void elementsIntilization() {


        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS, Manifest.permission.RECEIVE_SMS}, PERMISSION_SMS_READ_REQUEST_CODE);


            return;
        }

        //RECYCLERVIEW INTILIZATION
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        smsAdapter = new SmsAdapter(this, getAllSmsModel());
        recyclerView.setAdapter(smsAdapter);


        //FLOATING ACTION BUTTON INTILIZATION
        sendSms = (FloatingActionButton) findViewById(R.id.sendSms);

        //EDITTEXT INTILIZATION
        search = (EditText) findViewById(R.id.searcheMessage);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (charSequence.length() == 0) {
                    smsAdapter = new SmsAdapter(MainActivity.this, getAllSmsModel());
                    recyclerView.setAdapter(smsAdapter);
                } else {
                    smsAdapter = new SmsAdapter(MainActivity.this, getAllSmsModelWithSearch(charSequence.toString()));
                    recyclerView.setAdapter(smsAdapter);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        onClickListeners();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.search:

                search.setVisibility(View.VISIBLE);
                return true;

            case R.id.backup:

                if (mGoogleApiClient == null) {

                    /**
                     * Create the API client and bind it to an instance variable.
                     * We use this instance as the callback for connection and connection failures.
                     * Since no account name is passed, the user is prompted to choose.
                     */
                    mGoogleApiClient = new GoogleApiClient.Builder(this)
                            .addApi(Drive.API)
                            .addScope(Drive.SCOPE_FILE)
                            .addConnectionCallbacks(MainActivity.this)
                            .addOnConnectionFailedListener(this)
                            .build();
                }

                mGoogleApiClient.connect();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {

            case REQUEST_CODE_CREATOR:
                // Called after a file is saved to Drive.
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "Image successfully saved.");

                }
                break;
        }
    }


    public void showMessage(String message) {
        Log.d("message", message);
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    final private ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback = new
            ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult result) {
                    if (!result.getStatus().isSuccess()) {
                        showMessage("Error while trying to create new file contents");
                        return;
                    }
                    final DriveContents driveContents = result.getDriveContents();

                    // Perform I/O off the UI thread.
                    new Thread() {
                        @Override
                        public void run() {
                            // write content to DriveContents
                            OutputStream outputStream = driveContents.getOutputStream();
                            Writer writer = new OutputStreamWriter(outputStream);
                            try {
                                List<SmsModel> smslist = getAllSmsModelWithoutFilter();
                                writer.write("Messages");
                                if (smslist != null && smslist.size() > 0) {
                                    for (int i = 0; i < smslist.size(); i++) {
                                        writer.append("From: " + smslist.get(i).getAddress() + "\n");
                                        writer.append("At: " + smslist.get(i).getTime() + "\n");
                                        writer.append("Message: " + smslist.get(i).getMsg() + "\n\n");

                                    }


                                }

                                writer.close();
                            } catch (IOException e) {
                                Log.e(TAG, e.getMessage());
                            }

                            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                    .setTitle("smsapplication_backup")
                                    .setMimeType("text/plain")
                                    .setStarred(true).build();

                            // create a file on root folder
                            Drive.DriveApi.getRootFolder(mGoogleApiClient)
                                    .createFile(mGoogleApiClient, changeSet, driveContents)
                                    .setResultCallback(fileCallback);
                        }
                    }.start();
                }
            };

    final private ResultCallback<DriveFolder.DriveFileResult> fileCallback = new
            ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(DriveFolder.DriveFileResult result) {
                    if (!result.getStatus().isSuccess()) {
                        showMessage("Error while trying to create the file");
                        return;
                    }
                    showMessage("Created a file with content: " + result.getDriveFile().getDriveId());
                }
            };

    public void onClickListeners() {
        //FLOATING ACTION BUTTON ONCLICK LISTENERS
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS}, PERMISSION_SMS_SEND_REQUEST_CODE);


            return;
        }

        sendSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SendSmsActivity.class));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {


            case PERMISSION_SMS_READ_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    elementsIntilization();

                } else {


                    Toast.makeText(MainActivity.this, "Please allow us to read messages", Toast.LENGTH_SHORT).show();

                }
            }
            break;

            case PERMISSION_SMS_SEND_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivity(new Intent(MainActivity.this, SendSmsActivity.class));

                } else {


                    Toast.makeText(MainActivity.this, "Please allow us to read messages", Toast.LENGTH_SHORT).show();

                }
            }
            break;


        }

    }


    public List<SmsModel> getAllSmsModel() {
        List<SmsModel> lstSmsModel = new ArrayList<SmsModel>();
        HashMap<String, SmsModel> smsMaps = new HashMap<>();
        SmsModel objSmsModel = new SmsModel();
        Uri message = Uri.parse("content://sms/");
        ContentResolver cr = getContentResolver();

        cursor = cr.query(message, null, null, null, null);
        startManagingCursor(cursor);
        if (cursor != null) {
            int totalSmsModel = cursor.getCount();

            if (cursor.moveToFirst()) {
                for (int i = 0; i < totalSmsModel; i++) {

                    objSmsModel = new SmsModel();
                    objSmsModel.setId(cursor.getString(cursor.getColumnIndexOrThrow("_id")));
                    objSmsModel.setAddress(cursor.getString(cursor
                            .getColumnIndexOrThrow("address")));
                    objSmsModel.setMsg(cursor.getString(cursor.getColumnIndexOrThrow("body")));
                    objSmsModel.setReadState(cursor.getString(cursor.getColumnIndex("read")));
                    objSmsModel.setTime(cursor.getString(cursor.getColumnIndexOrThrow("date")));
                    if (cursor.getString(cursor.getColumnIndexOrThrow("type")).contains("1")) {
                        objSmsModel.setFolderName("inbox");
                    } else {
                        objSmsModel.setFolderName("sent");
                    }


                    if (objSmsModel.getAddress() != null && !objSmsModel.getAddress().isEmpty()) {
                        if (!smsMaps.containsKey(objSmsModel.getAddress()))
                            smsMaps.put(objSmsModel.getAddress(), objSmsModel);
                    } else {

                        lstSmsModel.add(objSmsModel);
                    }


                    cursor.moveToNext();
                }


                lstSmsModel.addAll(new ArrayList<SmsModel>(smsMaps.values()));
                Collections.sort(lstSmsModel, new Comparator<SmsModel>() {
                    public int compare(SmsModel s1, SmsModel s2) {
                        return s2.getTime().compareToIgnoreCase(s1.getTime());
                    }
                });
            }


        } else {
            Log.d("null", "true");
        }


        return lstSmsModel;


    }


    public List<SmsModel> getAllSmsModelWithoutFilter() {
        List<SmsModel> lstSmsModel = new ArrayList<SmsModel>();
        HashMap<String, SmsModel> smsMaps = new HashMap<>();
        SmsModel objSmsModel = new SmsModel();
        Uri message = Uri.parse("content://sms/");
        ContentResolver cr = getContentResolver();

        cursor = cr.query(message, null, null, null, null);
        startManagingCursor(cursor);
        if (cursor != null) {
            int totalSmsModel = cursor.getCount();

            if (cursor.moveToFirst()) {
                for (int i = 0; i < totalSmsModel; i++) {

                    objSmsModel = new SmsModel();
                    objSmsModel.setId(cursor.getString(cursor.getColumnIndexOrThrow("_id")));
                    objSmsModel.setAddress(cursor.getString(cursor
                            .getColumnIndexOrThrow("address")));
                    objSmsModel.setMsg(cursor.getString(cursor.getColumnIndexOrThrow("body")));
                    objSmsModel.setReadState(cursor.getString(cursor.getColumnIndex("read")));
                    objSmsModel.setTime(cursor.getString(cursor.getColumnIndexOrThrow("date")));
                    if (cursor.getString(cursor.getColumnIndexOrThrow("type")).contains("1")) {
                        objSmsModel.setFolderName("inbox");
                    } else {
                        objSmsModel.setFolderName("sent");
                    }


                    lstSmsModel.add(objSmsModel);


                    cursor.moveToNext();
                }

            }


        } else {
            Log.d("null", "true");
        }


        return lstSmsModel;


    }

    public List<SmsModel> getAllSmsModelWithSearch(String query) {
        List<SmsModel> lstSmsModel = new ArrayList<SmsModel>();
        HashMap<String, SmsModel> smsMaps = new HashMap<>();
        SmsModel objSmsModel = new SmsModel();
        Uri message = Uri.parse("content://sms/");
        ContentResolver cr = getContentResolver();

        cursor = cr.query(message, null, null, null, null);
        startManagingCursor(cursor);
        if (cursor != null) {
            int totalSmsModel = cursor.getCount();

            if (cursor.moveToFirst()) {
                for (int i = 0; i < totalSmsModel; i++) {

                    objSmsModel = new SmsModel();
                    objSmsModel.setId(cursor.getString(cursor.getColumnIndexOrThrow("_id")));
                    objSmsModel.setAddress(cursor.getString(cursor
                            .getColumnIndexOrThrow("address")));
                    objSmsModel.setMsg(cursor.getString(cursor.getColumnIndexOrThrow("body")));
                    objSmsModel.setReadState(cursor.getString(cursor.getColumnIndex("read")));
                    objSmsModel.setTime(cursor.getString(cursor.getColumnIndexOrThrow("date")));
                    if (cursor.getString(cursor.getColumnIndexOrThrow("type")).contains("1")) {
                        objSmsModel.setFolderName("inbox");
                    } else {
                        objSmsModel.setFolderName("sent");
                    }
                    if (objSmsModel.getMsg().matches("(?i:.*" + query + ".*)") || objSmsModel.getAddress().matches("(?i:.*" + query + ".*)")) {
                        lstSmsModel.add(objSmsModel);
                    }


                    cursor.moveToNext();
                }

            }


        } else {
            Log.d("null", "true");
        }


        return lstSmsModel;


    }


    @Override
    protected void onResume() {
        super.onResume();
        elementsIntilization(); // function where you can find all elements intilziated
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null) {

            // disconnect Google API client connection
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {


        if (!result.hasResolution()) {

            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
            return;
        }

        /**
         *  The failure has a resolution. Resolve it.
         *  Called typically when the app is not yet authorized, and an  authorization
         *  dialog is displayed to the user.
         */

        try {

            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);

        } catch (IntentSender.SendIntentException e) {


        }
    }

    /**
     * It invoked when Google API client connected
     *
     * @param connectionHint
     */
    @Override
    public void onConnected(Bundle connectionHint) {

        Log.d("coonection", "coonected");
        Drive.DriveApi.newDriveContents(mGoogleApiClient)
                .setResultCallback(driveContentsCallback);

    }

    /**
     * It invoked when connection suspended
     *
     * @param cause
     */
    @Override
    public void onConnectionSuspended(int cause) {

        Log.d("coonection", "suspended" + cause);


    }
}
