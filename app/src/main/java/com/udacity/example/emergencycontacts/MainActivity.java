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
            //String query = args.getString(QUERY_KEY);
            Uri uri = ContactsContract.CommonDataKinds.Contactables.CONTENT_URI;


            String[] projection = new String[]{
                    ContactsContract.CommonDataKinds.Contactables.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Email.ADDRESS,
                    ContactsContract.CommonDataKinds.Relation.TYPE,
                    ContactsContract.CommonDataKinds.Contactables.MIMETYPE,
                    ContactsContract.CommonDataKinds.Contactables.LOOKUP_KEY
            };

            // Easy way to limit the query to contacts with phone numbers.
            String selection = ContactsContract.CommonDataKinds.Contactables.HAS_PHONE_NUMBER + " = " + 1;

            // Sort results such that rows for the same contact stay together.
            String sortBy = ContactsContract.CommonDataKinds.Contactables.LOOKUP_KEY;

            mCursor = getContentResolver().query(
                    uri,       // URI representing the table/resource to be queried
                    projection,      // projection - the list of columns to return.  Null means "all"
                    selection, // selection - Which rows to return (condition rows must match)
                    null,      // selection args - can be provided separately and subbed into selection.
                    sortBy);   // string specifying sort order

            String lookupKeyOld = "";
            while (mCursor.moveToNext()) {
                String name = mCursor.getString(0);
                String number = mCursor.getString(1);
                String email = mCursor.getString(2);
                int relationshipInt = mCursor.getInt(3);
                String mimeType = mCursor.getString(4);
                String lookupKey = mCursor.getString(5);

                if (!lookupKeyOld.equals(lookupKey)) {
                    lookupKeyOld = lookupKey;
                    mOutputTextView.append(name + "\n" );
                }

                if (mimeType.equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
                    mOutputTextView.append("\tPhone number: " + number + "\n");
                }

                if (mimeType.equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {
                    mOutputTextView.append("\tEmail: " + email + "\n");
                }

                if (mimeType.equals(ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE)) {
                    CharSequence relation = ContactsContract.CommonDataKinds.Relation.getTypeLabel(getResources(), relationshipInt, "");
                    mOutputTextView.append("\tRelationship: " + relation + "\n");
                }
            }
        }
    }


    public int getRelationshipType(long contactId) {
        Uri uri = ContactsContract.Data.CONTENT_URI;
        String where = String.format(
                "%s = ? AND %s = ?",
                ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.Relation.CONTACT_ID);

        String[] whereParams = new String[]{
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
        try {
            if (relationCursor.moveToFirst()) {
                type = relationCursor.getInt(
                        relationCursor.getColumnIndex(ContactsContract.CommonDataKinds.Relation.TYPE));
            }
        } finally {
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
