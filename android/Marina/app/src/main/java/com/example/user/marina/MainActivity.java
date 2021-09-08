/*
 * Developed by: Bar Ofner, Hadar Eliyahu
 *
 * This is the main activity file. Here the conversation with the LUIS api will be made.
 * Other than that, the request making and answer parsing will also be done.
 * This file will also include the main feature functions themselves.
 * */

package com.example.user.marina;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.text.format.Time;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;


public class MainActivity extends AppCompatActivity {

    // LUIS parsing
    private Map<String, Command> f = new HashMap<String, Command>();
    private static final String INTENT_FIELD = "intent";
    private static final String ENTITIES_FIELD = "entities";

    // components on layout
    private ImageButton btnSendRequest;
    private EditText requestText;
    private TextView responseText;
    private TextView signInButton;
    private TextView signOutButton;
    private Button LogInActivity;

    //Fire Base Variables
    private FirebaseDatabase DataBaseRef;
    private FirebaseAuth AuthRef;
    private DatabaseReference PCMessageRef;


    // volley variables
    private RequestQueue mRequestQueue;
    private StringRequest stringRequest;



    // LUIS details
    private final String luisAppId = "d859fc53-3e70-49ea-9f59-9fc7b9f3f8fd";
    private final String subscriptionKey = "86a2dc227c144d59b6d10a9896e4bb8b";
    private final String url = "https://westus.api.cognitive.microsoft.com/luis/v2.0/apps/" +
            luisAppId + "?subscription-key=" + subscriptionKey + "&q=";


    // User
    private boolean isSigned;

    // PERMISSIONS
    private static final int PERMISSIONS_REQUEST_PHONE_CALL = 1;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 2;
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        signInButton = findViewById(R.id.SignInButton);
        signOutButton = findViewById(R.id.SignOutButton);
        PCMessageRef = null;
        responseText = findViewById(R.id.ShowResponse);
        DataBaseRef = FirebaseDatabase.getInstance();
        AuthRef = FirebaseAuth.getInstance();
        isSigned = false;


        if(AuthRef.getCurrentUser() != null)
        {
            responseText.setText("hello " + AuthRef.getCurrentUser().getEmail());
            PCMessageRef = DataBaseRef.getReference("Users/" + AuthRef.getInstance().getCurrentUser().getUid() + "/PC/message");
            signInButton.setVisibility(View.INVISIBLE);
            signOutButton.setVisibility(View.VISIBLE);
            isSigned = true;
        }
        else
        {
            responseText.setText("disconnected");
        }

        //adjust buttons
        // create an event listener of make request
        btnSendRequest = findViewById(R.id.MakeRequest);
        btnSendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestText = findViewById(R.id.RequestText);
                makeRequest(requestText.getText().toString());
            }
        });

        // pass to sign in activity

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AuthRef.signOut();
                responseText.setText("disconnected");
                signInButton.setVisibility(View.VISIBLE);
                signOutButton.setVisibility(View.INVISIBLE);
                isSigned = false;
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getBaseContext(), LogInScreen.class);
                startActivity(i);
                finish();
            }
        });



        /*
         *   Load all available functions to the map,
         *   every function has an outer name that allows to call it.
         */
        f.put("Time.What", new Command() {
            public String execute(Map<String, Entity> entityList) {
                //calls the time constructor and returns the current time zone's time

                Calendar rightNow = Calendar.getInstance();
                return String.valueOf(rightNow.get(Calendar.HOUR_OF_DAY)) + ":" + String.valueOf(rightNow.get(Calendar.MINUTE));
            }
        });


        f.put("Search", new Command() {
            public String execute(Map<String, Entity> entityList) {
                //Sends a query to google search and opens the results in the default browser

                //check if the entity list is not empty and contains the right entity type required
                if (entityList.isEmpty() || !entityList.containsKey("Query")){
                    return "I can't understand what should I search. Please rephrase that.";
                }

                String query = entityList.get("Query").getEntity();

                // if the user asked to that on pc
                if(entityList.containsKey("OnPC") && isSigned)
                {
                    PCMessageRef.setValue("search for " + query);
                    return "I sent it";
                }
                // regular ask
                else
                {
                    Intent viewIntent = new Intent("android.intent.action.VIEW",
                            Uri.parse("http://www.google.com/search?q=" + query));
                    startActivity(viewIntent);
                    return "Searching for " + query;
                }
            }
        });

        f.put("Hello", new Command() {
            public String execute(Map<String, Entity> entityList) {
                String greets[] = new String[3];
                greets[0] = "Hi";
                greets[1] = "Greetings";
                greets[2] = "Bye";
                return greets[((int)(Math.random() * 2))];
            }
        });

        f.put("PlayMusic", new Command() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            public String execute(Map<String, Entity> entityList) {
                //check if the entity list is not empty and contains the right entity type required
                if (entityList.isEmpty() || !entityList.containsKey("Entertainment.Title"))
                {
                    return "I can't find the name of the song you want to play...";
                }

                String requestedSong = entityList.get("Entertainment.Title").getEntity();

                // if the user asks to do that on pc sends the request to pc
                if(entityList.containsKey("OnPC") && isSigned)
                {
                    PCMessageRef.setValue("Play " + requestedSong);
                    return "I sent it";
                }


                String fullpath = "";


                // find the right song path and play it
                ContentResolver contentResolver = getContentResolver();
                Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                Cursor songCursor;
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    return "I don't have permission for this";
                }

                else
                {
                    songCursor = contentResolver.query(songUri, null, null, null, null);
                }


                if(songCursor != null && songCursor.moveToFirst())
                {
                    int songId = songCursor.getColumnIndex(MediaStore.Audio.Media._ID);
                    int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);

                    do {

                        if(songCursor.getString(songTitle).toLowerCase().contains(requestedSong))
                        {
                            fullpath = songCursor.getString(songCursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                            break;
                        }

                    } while(songCursor.moveToNext());
                }

                if(fullpath != "")
                {
                    try {
                        Intent intent = new Intent();
                        intent.setAction(android.content.Intent.ACTION_VIEW);
                        File file = new File(fullpath);
                        intent.setDataAndType(Uri.fromFile(file), "audio/*");
                        startActivity(intent);
                        return "playing: " + requestedSong;
                    }
                    catch(Exception e){
                        return "Could not launch the music player D:";
                    }
                }

                return "Could not find the song";
            }
        });

        // Open App by name
        f.put("OpenApp", new Command() {
            public String execute(Map<String, Entity> entityList) {
                if (entityList.isEmpty() || !entityList.containsKey("OnDevice.AppName")){
                    return "I can't find the name of the app that you wanted to open...";
                }

                String requestedApp = entityList.get("OnDevice.AppName").getEntity();


                if(entityList.containsKey("OnPC") && isSigned){
                    PCMessageRef.setValue("open " + requestedApp);
                    return "I sent it";
                }
                else {

                    final PackageManager pm = getPackageManager();
                    String rightAppName = "";
                    List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

                    for (ApplicationInfo packageInfo : packages) {
                        if (packageInfo.packageName.toLowerCase().contains(requestedApp.toLowerCase())) {
                            rightAppName = packageInfo.packageName;
                            break;
                        }
                    }

                    if (rightAppName != "") {
                        Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(rightAppName);
                        startActivity(LaunchIntent);
                        String s[] = rightAppName.split("\\.");
                        return "opening: " + s[s.length - 1];
                    }

                    return "didn't find";
                }
            }
        });

        // call contact by name
        f.put("Call", new Command() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            public String execute(Map<String, Entity> entityList) {
                if (entityList.isEmpty() || !entityList.get(0).getType().equals("Communication.ContactName")){
                    return "I can't find the name of the contact which you wanted to call...";
                }
                String contactName = entityList.get(0).getEntity();
                String number="";
                String name = "";

                // requesting Phone call Permission
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
                    requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, PERMISSIONS_REQUEST_PHONE_CALL);

                // requesting Read Contact Permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);


                // requesting list of contacts
                Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

                String[] projection = new String[] {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER};

                Cursor contact;
                if (ActivityCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    contact = getContentResolver().query(uri, projection, null, null, null);
                }
                else
                {
                    return "I don't have permission to do that";
                }

                // find the match contact
                int indexName = contact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int indexNumber = contact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                contact.moveToFirst();


                do {
                    String Name = contact.getString(indexName);
                    String Number = contact.getString(indexNumber);
                    if(Name.equalsIgnoreCase(contactName)) // contact found
                    {

                        number = Number.replace("-", "");
                        name = Name;
                        break;
                    }
                } while (contact.moveToNext());

                // close cursor
                contact.close();

                //if there is a number
                if(!number.equalsIgnoreCase(""))
                {
                    number = number.replace("-", "");
                    // check if have permission
                    if (ActivityCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                        Intent phoneIntent = new Intent(Intent.ACTION_CALL);
                        phoneIntent.setData(Uri.parse("tel:"+ number));
                        startActivity(phoneIntent);
                        return "Calling: " + name;
                    }
                    else
                    {
                        return "I don't have permission to do that";
                    }
                }
                else
                {
                    return "Couldn't find the number...";
                }
            }
        });
    }


    public String SendToFunc(String r, Map<String, Entity> entityList) {

        Command cmd;

        // find matched function to do
        cmd = f.get(r);

        if(cmd != null){
            return cmd.execute(entityList);
        }

        return "Sorry, I can't understand that.";
    }

    private void makeRequest(String request) {

        btnSendRequest.setEnabled(false);
        mRequestQueue = Volley.newRequestQueue(this);

        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url + request.replaceAll(" ", "+"),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // split to JSON Object
                            JSONObject jObject = new JSONObject(response);

                            // get the Intent
                            String intent = jObject.getJSONObject("topScoringIntent").getString(INTENT_FIELD);
                            String typeTemp;
                            JSONArray entities = jObject.getJSONArray(ENTITIES_FIELD);
                            Map<String, Entity> entitiesList = new HashMap<>();
                            for (int i = 0; i < entities.length(); i++) {
                                typeTemp = entities.getJSONObject(i).getString("type");
                                entitiesList.put(typeTemp, new Entity(entities.getJSONObject(i).getString("entity"), typeTemp));
                            }

                            responseText.setText(SendToFunc(intent, entitiesList));

                        } catch (JSONException e) {
                            responseText.setText("An error occurred. Please try again.");
                        }
                        btnSendRequest.setEnabled(true);
                    }
                }, new Response.ErrorListener() {
            // on error occurred
            @Override
            public void onErrorResponse(VolleyError error) {
                String text = error.toString();
                error.printStackTrace();
                responseText.setText(text);
                btnSendRequest.setEnabled(true);
            }
        });
        mRequestQueue.add(stringRequest);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_PHONE_CALL : {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
            }
            break;
            case PERMISSIONS_REQUEST_READ_CONTACTS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted

                }
                break;
            case PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE : {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
            }
            break;
        }
    }
}