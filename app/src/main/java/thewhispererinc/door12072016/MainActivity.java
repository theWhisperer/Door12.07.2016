package thewhispererinc.door12072016;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.nfc.tech.Ndef;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.nio.charset.Charset;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private NfcAdapter mNfcAdapter;
    private RadioButton radio_button;
    SignInButton signInButton;
    GoogleApiClient mGoogleApiClient;
    private NdefMessage mNdefMessage;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        setContentView(R.layout.activity_main);
        Log.d(MainActivity.class.getSimpleName(), "***********************onCreate() ********************************");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();

        signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(this);



        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        radio_button= (RadioButton) findViewById(R.id.radioButton5);

        //NdefRecord uriRecord = NdefRecord.createUri(Uri.encode("working"));
        //mNdefMessage = new NdefMessage(new NdefRecord[]{uriRecord});
        NdefRecord mimeRecord = NdefRecord.createMime("security/door",
                "Beam me up, Android".getBytes(Charset.forName("US-ASCII")));
        mNdefMessage = new NdefMessage(mimeRecord);
        //mFileUris[0]=Uri.parse("phoneNum");
        //mNfcAdapter.setBeamPushUris(new Uri[]{Uri.parse("phoneNum")},this);


        if(mNfcAdapter != null)
        {
            Toast.makeText(MainActivity.this,"Tap to unlock door", Toast.LENGTH_SHORT).show();
            radio_button.setEnabled(true);
        }

        infoToSend();





    }

    @Override
    public  void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.sign_in_button:
                signIn();
                break;

        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, 9001);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 9001) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d("sign in event", "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            Toast.makeText(getApplicationContext(), acct.getDisplayName() + " signed in", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        Toast.makeText(getApplicationContext(), "sensed lock ", Toast.LENGTH_SHORT).show();
        if(mNfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()))
        {
            NdefMessage[] msg = null;
            Parcelable[] rawMsg = intent.getParcelableArrayExtra(mNfcAdapter.EXTRA_NDEF_MESSAGES);
            if(rawMsg != null)
            {
                msg = new NdefMessage[rawMsg.length];
                for (int i=0;i<rawMsg.length;i++)
                {
                    msg[i]=(NdefMessage) rawMsg[i];
                }
                if(msg[0] != null)
                {
                    String text="";
                    byte[] payload = msg[0].getRecords()[0].getPayload();
                    for (int i = 0; i<payload.length; i++)
                    {
                        text += (char) payload[i];
                    }
                    Toast.makeText(getApplicationContext(), "Door : "+ text, Toast.LENGTH_SHORT).show();
                    radio_button= (RadioButton) findViewById(R.id.radioButton);
                    radio_button.setEnabled(true);

                   DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
                    Log.d("testing", "*********************** writing to database ********************************");
                    Firebase myFireBase = new Firebase("https://securitydoorfacca-afc17.firebaseio.com/");
                    DatabaseReference myRef = databaseRef.child("message");

                    //myRef.push("working now!!!");


                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        // User is signed in
                        Toast.makeText(getApplicationContext(), "signed in", Toast.LENGTH_SHORT).show();
                    } else {
                        // No user is signed in
                        Toast.makeText(getApplicationContext(), "not signed in", Toast.LENGTH_SHORT).show();
                    }





                    //need to fix checkDoorPermission()
                    checkDoorPermission(text);

                }
            }
        }
    }


    private void checkDoorPermission(String door)
    {
        Log.d("testing", "***********************checking door permission ********************************");
        TelephonyManager deviceId = (TelephonyManager)getSystemService(this.TELEPHONY_SERVICE);
        String phoneNum = deviceId.getLine1Number();
        new ReadPermissionFile().execute(phoneNum);
        radio_button= (RadioButton) findViewById(R.id.radioButton3);
        radio_button.setEnabled(true);


    }

    private void infoToSend()
    {
        TelephonyManager deviceId = (TelephonyManager)getSystemService(this.TELEPHONY_SERVICE);
        String phoneNum = deviceId.getLine1Number();
        Toast.makeText(MainActivity.this,""+phoneNum, Toast.LENGTH_SHORT).show();



    }

    @Override
    public void onResume() {
        super.onResume();

        if (mNfcAdapter != null)
        {
            mNfcAdapter.setNdefPushMessage(mNdefMessage,this);
            radio_button= (RadioButton) findViewById(R.id.radioButton5);
            radio_button.setEnabled(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        radio_button= (RadioButton) findViewById(R.id.radioButton);
        radio_button.setEnabled(false);
        radio_button= (RadioButton) findViewById(R.id.radioButton3);
        radio_button.setEnabled(false);
        radio_button= (RadioButton) findViewById(R.id.radioButton4);
        radio_button.setEnabled(false);
        radio_button= (RadioButton) findViewById(R.id.radioButton5);
        radio_button.setEnabled(false);

        //if (mNfcAdapter != null)
            //mNfcAdapter.setNdefPushMessage(mNdefMessage,this);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
