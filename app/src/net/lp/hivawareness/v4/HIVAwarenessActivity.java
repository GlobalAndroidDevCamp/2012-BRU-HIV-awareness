package net.lp.hivawareness.v4;

import java.nio.charset.Charset;

import net.lp.hivawareness.R;
import net.lp.hivawareness.domain.Gender;
import net.lp.hivawareness.domain.Probability;
import net.lp.hivawareness.domain.Region;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class HIVAwarenessActivity extends FragmentActivity implements
		OnClickListener {
	private NfcAdapter mNfcAdapter;
	private int caught = 0;
	private boolean ran = false;
	private IntentFilter[] mIntentFiltersArray;
	private String[][] mTechListsArray;
	private PendingIntent mPendingIntent;	
	private Gender mGender = Gender.male;
	private Region mEthnic;
	private boolean mUseWorld = true;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity);

		// Check for available NFC Adapter
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (mNfcAdapter == null) {
			Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG)
					.show();
			// finish();
			// return;
		} else {
			// Register callback

		}

		mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		try {
			ndef.addDataType("application/net.lp.hivawareness.beam");
		} catch (MalformedMimeTypeException e) {
			throw new RuntimeException("fail", e);
		}
		mIntentFiltersArray = new IntentFilter[] { ndef };
		mTechListsArray = new String[][] { new String[] { NfcF.class.getName() } };
	}

	public void onClick(View v) {
		if (v.getId() == R.id.button1) {
			FragmentManager fragmentManager = getSupportFragmentManager();
			FragmentTransaction transaction = fragmentManager
					.beginTransaction();

			StartedFragment sf = new StartedFragment();
			// Replace whatever is in the fragment_container view with this
			// fragment,
			// and add the transaction to the back stack
			transaction.remove(fragmentManager.findFragmentById(R.id.start_fragment));
			transaction.add(R.id.fragment_container, sf);
			transaction
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
			transaction.addToBackStack("started");

			// Commit the transaction
			transaction.commit();

			if (!ran) {
				if (mUseWorld) {
					caught = (int) Math.floor(Math.random()
							+ (Probability.worldwide * Probability.scale));
				} else {
					caught = (int) Math
							.floor(Math.random()
									+ (Probability.fromData(mGender, mEthnic) * Probability.scale));
				}
			}
		}else if (v.getId() == R.id.startover_button) {
			FragmentManager fragmentManager = getSupportFragmentManager();
			fragmentManager.popBackStack();
			
			caught = 0;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	public NdefMessage createNdefMessage() {
		String text = "" + caught + "|" + mGender.name();
		NdefMessage msg = new NdefMessage(new NdefRecord[] { createMimeRecord(
				"application/net.lp.hivawareness.beam", text.getBytes())
		/**
		 * The Android Application Record (AAR) is commented out. When a device
		 * receives a push with an AAR in it, the application specified in the
		 * AAR is guaranteed to run. The AAR overrides the tag dispatch system.
		 * You can add it back in to guarantee that this activity starts when
		 * receiving a beamed message. For now, this code uses the tag dispatch
		 * system.
		 */
		// ,NdefRecord.createApplicationRecord("net.lp.hivawareness")
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
		mNfcAdapter.enableForegroundNdefPush(this, createNdefMessage());
		mNfcAdapter.enableForegroundDispatch(this, mPendingIntent,
				mIntentFiltersArray, mTechListsArray);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mNfcAdapter != null) {
			mNfcAdapter.disableForegroundNdefPush(this);
			mNfcAdapter.disableForegroundDispatch(this);
		}

	}

	@Override
	public void onNewIntent(Intent intent) {
		// onResume gets called after this to handle the intent
		setIntent(intent);
		processIntent(intent);
	}

	/**
	 * Parses the NDEF Message from the intent and prints to the TextView
	 */
	void processIntent(Intent intent) {
		Parcelable[] rawMsgs = intent
				.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
		// only one message sent during the beam
		NdefMessage msg = (NdefMessage) rawMsgs[0];

		String data = new String(msg.getRecords()[0].getPayload());
		String[] parts = data.split("\\|");
		
		// record 0 contains the MIME type, record 1 is the AAR, if present
		Log.v("HIV", "old status " + caught);

		double partnerInfected = Double.parseDouble(parts[0]);
		Gender partnerGender = Gender.valueOf(parts[1]);
		updateInfectionStatus(partnerInfected, partnerGender);

		Log.v("HIV", "new status " + caught);

	}

	private void updateInfectionStatus(double partnerInfected, Gender gender) {
		if (caught == 0d && partnerInfected > 0) {

			double factor;

			if (mGender == Gender.male) {

				if (gender == Gender.male) {
					factor = Probability.male_male; // male not infected - male
													// infected
				} else {
					factor = Probability.male_female; // male not infected -
														// female infected
				}
			} else {
				if (gender == Gender.male) {
					factor = Probability.female_male; // female not infected -
														// male infected
				} else {
					factor = Probability.female_female; // female not infected -
														// female infected
				}
			}

			double random = Math.random();
			caught = (int) Math.floor(random + (factor * Probability.scale));
		}
	}

	/**
	 * Creates a custom MIME type encapsulated in an NDEF record
	 */
	public NdefRecord createMimeRecord(String mimeType, byte[] payload) {
		byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));
		NdefRecord mimeRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
				mimeBytes, new byte[0], payload);
		return mimeRecord;
	}

}