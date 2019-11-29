package com.example.rehber;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_READ_CONTACTS=1;
    ListView rehber;
    ArrayList<Contact> contacts = new ArrayList<>();

    Cursor cursor =null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rehber = findViewById(R.id.rehber);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
        {
            tumRehber();
        }
        else{
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_CONTACTS},REQUEST_READ_CONTACTS);
        }
    }

    public void tumRehber(){
        ContentResolver contentResolver =getContentResolver();

        try{
            cursor=contentResolver.query(ContactsContract.Contacts.CONTENT_URI,
                    null,
                    null,
                    null,
                    null);
        }
        catch (Exception ignored){}

        if(cursor !=null && cursor.getCount()>0){
            while(cursor.moveToNext()){
                Contact contact = new Contact();
                String contact_id=cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                contact.name=cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                int numara=Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
                if (numara>0){
                    Cursor phoneCursor=contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +"= ?",
                            new String[] {contact_id},
                            null);
                    if (phoneCursor !=null){
                        phoneCursor.moveToNext();
                        contact.phone=phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    }
                    if (phoneCursor != null){phoneCursor.close();}
                }
                contact.image= ContactPhoto(contact_id);
                contacts.add(contact);
            }
            Adapter adapter = new Adapter(this,contacts);
            rehber.setAdapter(adapter);
        }
    }
    public Bitmap ContactPhoto(String contactId){
        Uri contactUri= ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.valueOf(contactId));
        Uri photoUri= Uri.withAppendedPath(contactUri,ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        Cursor cursor=getContentResolver().query(photoUri,
                new String[] {ContactsContract.Contacts.Photo.PHOTO},null,null,null);
        if (cursor !=null && cursor.getCount()>0){
            cursor.moveToNext();
            byte[] data= cursor.getBlob(0);
            if (data != null)
            {
                return BitmapFactory.decodeStream(new ByteArrayInputStream(data));

            }
            else
                return null;
        }
        cursor.close();
        return null;
    }

    public class Contact{

        String name="";
        String phone="";
        Bitmap image=null;
    }
    public class Adapter extends BaseAdapter{
        Adapter(Context context, List<Contact> contactList){
            this.context=context;
            this.contactList=contactList;
        }

        Context context;
        List<Contact> contactList;

        public int getCount(){
            return contactList.size();
        }
        public Object getItem(int position){
            return contactList.get(position);
        }
        public long getItemId(int position){
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent){
           @SuppressLint("ViewHolder") View view=View.inflate(context,R.layout.kisi,null);
            TextView ad= view.findViewById(R.id.ad);
            TextView numara=view.findViewById(R.id.telefon);
            ImageView resim = view.findViewById(R.id.resim);

            ad.setText(contactList.get(position).name);
            numara.setText(contactList.get(position).phone);
            if (contactList.get(position).image !=null)
            {
                resim.setImageBitmap(contactList.get(position).image);
            }
            else
            {
                resim.setImageResource(R.drawable.ic_launcher_background);
            }
            view.setTag(contactList.get(position).name);
            return view;
        }

    }
}
