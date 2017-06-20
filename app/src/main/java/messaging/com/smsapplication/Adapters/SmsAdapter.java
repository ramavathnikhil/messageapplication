package messaging.com.smsapplication.Adapters;


import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import messaging.com.smsapplication.Activites.SendSmsActivity;
import messaging.com.smsapplication.Models.ContactModel;
import messaging.com.smsapplication.Models.SmsModel;
import messaging.com.smsapplication.R;
import messaging.com.smsapplication.Views.CircularImageview;


public class SmsAdapter extends RecyclerView.Adapter {
    private List<SmsModel> mDataSet = new ArrayList<>();
    private LayoutInflater mInflater;
    private final ViewBinderHelper binderHelper = new ViewBinderHelper();
    public Context mContext;

    public SmsAdapter(Context context, List<SmsModel> dataSet) {
        mDataSet = dataSet;
        mInflater = LayoutInflater.from(context);
        mContext = context;

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
                Log.e("Alert", "Lets See if it Works !!!");
                paramThrowable.printStackTrace();
            }
        });

        // uncomment if you want to open only one row at a time
        // binderHelper.setOpenOnlyOne(true);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.row_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder h, int position) {
        final ViewHolder holder = (ViewHolder) h;

        if (mDataSet != null && 0 <= position && position < mDataSet.size()) {
            final SmsModel data = mDataSet.get(position);

            // Use ViewBindHelper to restore and save the open/close state of the SwipeRevealView
            // put an unique string id as value, can be any string which uniquely define the data

            binderHelper.bind(holder.swipeLayout, data.getMsg());

            // Bind your data here
            ContactModel contactModel = getContactName(mContext, data.getAddress());

            holder.bind(data, contactModel, data.getAddress());
        }
    }

    public ContactModel getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null, null, null);
        if (cursor == null) {
            return null;
        }
        ContactModel contactModel = new ContactModel();
        String contactName = null;
        String contactId = null;
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));

            contactModel.setContactName(contactName);
            Bitmap photo = BitmapFactory.decodeResource(context.getResources(),
                    R.mipmap.ic_launcher);

            try {
                InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(),
                        ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(contactId)));

                if (inputStream != null) {
                    photo = BitmapFactory.decodeStream(inputStream);
                    contactModel.setProfilePic(photo);
                    inputStream.close();
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return contactModel;
    }


    @Override
    public int getItemCount() {
        if (mDataSet == null)
            return 0;
        return mDataSet.size();
    }

    /**
     * Only if you need to restore open/close state when the orientation is changed.
     * Call this method in {@link android.app.Activity#onSaveInstanceState(Bundle)}
     */
    public void saveStates(Bundle outState) {
        binderHelper.saveStates(outState);
    }

    /**
     * Only if you need to restore open/close state when the orientation is changed.
     * Call this method in {@link android.app.Activity#onRestoreInstanceState(Bundle)}
     */
    public void restoreStates(Bundle inState) {
        binderHelper.restoreStates(inState);
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        private SwipeRevealLayout swipeLayout;
        private View deleteLayout;
        private TextView title, message, createdAt;
        private CircularImageview profilePic;
        private RelativeLayout relativeLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            swipeLayout = (SwipeRevealLayout) itemView.findViewById(R.id.swipe_layout);
            deleteLayout = itemView.findViewById(R.id.delete_layout);
            title = (TextView) itemView.findViewById(R.id.title);
            message = (TextView) itemView.findViewById(R.id.message);
            profilePic = (CircularImageview) itemView.findViewById(R.id.profilePic);
            relativeLayout = (RelativeLayout) itemView.findViewById(R.id.relativelayout);
            createdAt = (TextView) itemView.findViewById(R.id.createdAt);
        }

        public void bind(SmsModel data, final ContactModel contactModel, final String address) {
            deleteLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDataSet.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());
                }
            });


            if (contactModel != null && contactModel.getContactName() != null && !contactModel.getContactName().isEmpty())
                title.setText(contactModel.getContactName());
            else
                title.setText(data.getAddress());

            if (contactModel != null && contactModel.getProfilePic() != null) {
                profilePic.setImageBitmap(contactModel.getProfilePic());
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    profilePic.setImageDrawable(mContext.getDrawable(R.drawable.ic_user));
                } else {
                    profilePic.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_user
                    ));
                }
            }

            message.setText(data.getMsg());


            swipeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mContext.startActivity(new Intent(mContext, SendSmsActivity.class).putExtra("name", title.getText().toString()).putExtra("number", address));
                }
            });
            relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mContext.startActivity(new Intent(mContext, SendSmsActivity.class).putExtra("name", title.getText().toString()).putExtra("number", address));

                }
            });


            // String timestamp = getTimeStamp(message.getCreatedAt());
            DateFormat df = new SimpleDateFormat("d MMM");
            if (data.getTime() != null && !data.getTime().isEmpty()) {
                String timestamp = df.format(Long.parseLong(data.getTime()));

                createdAt.setText(timestamp);
            } else {


                createdAt.setText("");
            }
        }


    }
}
