package thewhispererinc.door12072016;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.nfc.tech.Ndef;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.firebase.database.FirebaseDatabase;

import java.nio.charset.Charset;


public class MainActivity extends AppCompatActivity {

    private NfcAdapter mNfcAdapter;
    private RadioButton radio_button;

    private NdefMessage mNdefMessage;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        setContentView(R.layout.activity_main);
        Log.d(MainActivity.class.getSimpleName(), "***********************onCreate() ********************************");
        FirebaseDatabase database = FirebaseDatabase.getInstance(); ///working here***********************************************


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
                    Firebase myFireBase = new Firebase("https://securitydoorfacca-afc17.firebaseio.com/");
                    myFireBase.child("connect").setValue("Opening Door");

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
}
