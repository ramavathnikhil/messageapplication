package nikhil.com.smsapplication.Activites;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nikhil.com.smsapplication.Adapters.SmsAdapter;
import nikhil.com.smsapplication.Models.SmsModel;
import nikhil.com.smsapplication.R;

public class MainActivity extends AppCompatActivity {


    private static final int PERMISSION_SMS_READ_REQUEST_CODE = 1;
    private RecyclerView recyclerView;
    private SmsAdapter smsAdapter;
    private Cursor cursor;
    private FloatingActionButton sendSms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        elementsIntilization(); // function where you can find all elements intilziated
        onClickListeners();
    }

    public void elementsIntilization() {


        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS}, PERMISSION_SMS_READ_REQUEST_CODE);


            return;
        }

        //RECYCLERVIEW INTILIZATION
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        smsAdapter = new SmsAdapter(this, getAllSmsModel());
        recyclerView.setAdapter(smsAdapter);


        //FLOATING ACTION BUTTON INTILIZATION
        sendSms = (FloatingActionButton) findViewById(R.id.sendSms);
    }


    public void onClickListeners() {
        //FLOATING ACTION BUTTON ONCLICK LISTENERS
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
            }


        } else {
            Log.d("null", "true");
        }


        return lstSmsModel;


    }

    @Override
    protected void onPause() {
        super.onPause();

    }


    @Override
    protected void onResume() {
        super.onResume();

    }
}
