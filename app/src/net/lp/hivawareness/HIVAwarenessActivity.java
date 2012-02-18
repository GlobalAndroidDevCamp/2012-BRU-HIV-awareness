package net.lp.hivawareness;

import java.nio.charset.Charset;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class HIVAwarenessActivity extends Activity implements OnClickListener, CreateNdefMessageCallback {
    private NfcAdapter mNfcAdapter;
    private int caught=0;
    private boolean ran=false;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity);
        
        Button b = (Button) findViewById(R.id.button1);
        b.setOnClickListener(this);

        // Check for available NFC Adapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
            //finish();
            //return;
        }else{
        	// Register callback
        	mNfcAdapter.setNdefPushMessageCallback(this, this);
        }
    }

	@Override
	public void onClick(View v) {
		if(v.getId()==R.id.button1){
	        FragmentManager fragmentManager = getFragmentManager();
	        FragmentTransaction transaction = fragmentManager.beginTransaction();
	
	        // Replace whatever is in the fragment_container view with this fragment,
	        // and add the transaction to the back stack
	        transaction.replace(R.id.fragment_container, fragmentManager.findFragmentById(R.id.top_fragment));//TODO
	        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
	        transaction.addToBackStack(null);
	
	        // Commit the transaction
	        transaction.commit();
	        
	        if(!ran){
	        	caught = (int)Math.floor(Math.random()*2);
	        }
		}
	}
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
	    return true;
	}
	
    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        String text = ""+caught ;
        NdefMessage msg = new NdefMessage(
                new NdefRecord[] { createMimeRecord(
                        "application/net.lp.hivawareness.beam", text.getBytes())
         /**
          * The Android Application Record (AAR) is commented out. When a device
          * receives a push with an AAR in it, the application specified in the AAR
          * is guaranteed to run. The AAR overrides the tag dispatch system.
          * You can add it back in to guarantee that this
          * activity starts when receiving a beamed message. For now, this code
          * uses the tag dispatch system.
          */
          //,NdefRecord.createApplicationRecord("com.example.android.beam")
        });
        return msg;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
    }

    /**
     * Parses the NDEF Message from the intent and prints to the TextView
     */
    void processIntent(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        // record 0 contains the MIME type, record 1 is the AAR, if present

        double random = Math.random()*2;
    	caught = (int)Math.floor(((double)caught)* (1-random) + Double.parseDouble(new String(msg.getRecords()[0].getPayload()))*random);
    }

    /**
     * Creates a custom MIME type encapsulated in an NDEF record
     */
    public NdefRecord createMimeRecord(String mimeType, byte[] payload) {
        byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));
        NdefRecord mimeRecord = new NdefRecord(
                NdefRecord.TNF_MIME_MEDIA, mimeBytes, new byte[0], payload);
        return mimeRecord;
    }
    
}