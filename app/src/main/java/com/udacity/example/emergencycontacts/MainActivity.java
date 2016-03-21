package com.udacity.example.emergencycontacts;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Cursor mCursor;
    TextView mOutputTextView;
    // Request code for READ_CONTACTS. It can be any number > 0.
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;

    private static final int[] acceptedRelationships = {
            ContactsContract.CommonDataKinds.Relation.TYPE_FATHER,
            ContactsContract.CommonDataKinds.Relation.TYPE_MOTHER,
            ContactsContract.CommonDataKinds.Relation.TYPE_BROTHER,
            ContactsContract.CommonDataKinds.Relation.TYPE_RELATIVE,
            ContactsContract.CommonDataKinds.Relation.TYPE_SISTER,
            ContactsContract.CommonDataKinds.Relation.TYPE_SPOUSE,
            ContactsContract.CommonDataKinds.Relation.TYPE_DOMESTIC_PARTNER
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mOutputTextView = (TextView) findViewById(R.id.contactsList);

        // Read and show the contacts
        showContacts();
    }


        /**
         * Show the contacts in the ListView.
         */
    private void showContacts() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            // Android version is lesser than 6.0 or the permission is already granted.


            Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

            String[] projection = new String[]{
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
            };

//            Uri uri = ContactsContract.Data.CONTENT_URI;
//
//            String[] projection = new String[]{
//                    ContactsContract.CommonDataKinds.Relation.DATA3,
//                    ContactsContract.CommonDataKinds.Relation.DISPLAY_NAME,
//                    ContactsContract.CommonDataKinds.Relation.TYPE
//            };

//            String where = String.format(
//                    "%s = ?",
//                    ContactsContract.Data.MIMETYPE);
//
//            String[] whereParams = new String[] {
//                    ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE
//            };


            mCursor = getContentResolver().query(
                    uri,   // The content URI of the words table
                    projection,                        // The columns to return for each row
                    null,                    // Selection criteria
                    null,                     // Selection criteria
                    null);                        // The sort order for the returned rows

            String text = "";
            while (mCursor.moveToNext()) {
            //mCursor.moveToNext();
                int id = mCursor.getInt(0);
                String name = mCursor.getString(1);
                String number = mCursor.getString(2);
                int relationshipInt =  getRelationshipType(id);

                boolean isRelative = false;
                for (int i : acceptedRelationships) {
                    if (i == relationshipInt) {
                        isRelative = true;
                        break;
                    }
                }
                if (isRelative) text += "\n" + id + " : " + name + " (" + number + ")";

            }
            mOutputTextView.setText(text);

        }
    }


    public int getRelationshipType(long contactId) {
        Uri uri = ContactsContract.Data.CONTENT_URI;
        String where = String.format(
                "%s = ? AND %s = ?",
                ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.Relation.CONTACT_ID);

        String[] whereParams = new String[] {
                ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE,
                Long.toString(contactId),
        };

        String[] selectColumns = new String[]{
                ContactsContract.CommonDataKinds.Relation.TYPE,
                // add additional columns here
        };


        Cursor relationCursor = this.getContentResolver().query(
                uri,
                selectColumns,
                where,
                whereParams,
                null);

        int type = -1;
        try{
            if (relationCursor.moveToFirst()) {
                type = relationCursor.getInt(
                        relationCursor.getColumnIndex(ContactsContract.CommonDataKinds.Relation.TYPE));
            }
        }finally{
            relationCursor.close();
        }
        return type;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                showContacts();
            } else {
                Toast.makeText(this, "Until you grant the permission, we canot display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
