package net.lp.hivawareness.v4;

import java.nio.charset.Charset;

import net.lp.hivawareness.R;
import net.lp.hivawareness.domain.Gender;
import net.lp.hivawareness.domain.Probability;
import net.lp.hivawareness.domain.Region;

import org.openintents.intents.AboutIntents;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class HIVAwarenessActivity extends FragmentActivity implements
		OnClickListener {
	private static final String PREFS_GENDER = "gender";
	private static final String PREFS_REGION = "region";
	private static final String PREFS_INFECTED = "infected";
	private static final String PREFS_HISTORY = "history";
	private static final String PREFS_HISTORY_INFECTED = "history_infected";

	private NfcAdapter mNfcAdapter;
	private int caught = 0;
	private boolean ran = false;
	private IntentFilter[] mIntentFiltersArray;
	private String[][] mTechListsArray;
	private PendingIntent mPendingIntent;
	public Gender mGender;
	public Region mRegion;

	final static private boolean DEBUG = false;

	/**
	 * Dialog ids
	 */
	public static final int HELP_DIALOG_ID = 0;
	public static final int SMOKING_DIALOG_ID = 1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity);

		// initialize values
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		mGender = Gender.valueOf(prefs.getString(PREFS_GENDER, "male"));

		String region = prefs.getString(PREFS_REGION, null);
		if (region == null) {
			mRegion = null;
		}
		caught = prefs.getInt(PREFS_INFECTED, -1);

		if (caught == -1) {
			calculateInitial(mRegion == null);
		}

		// Check for available NFC Adapter
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (mNfcAdapter == null) {
			Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG)
					.show();
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
			int genderPos = ((Spinner) findViewById(R.id.spinner_gender))
					.getSelectedItemPosition();

			mNfcAdapter.disableForegroundNdefPush(this);
			mNfcAdapter.enableForegroundNdefPush(this, createNdefMessage());

			FragmentManager fragmentManager = getSupportFragmentManager();
			android.support.v4.app.FragmentTransaction transaction = fragmentManager
					.beginTransaction();

			StartedFragment sf = new StartedFragment();
			// Replace whatever is in the fragment_container view with this
			// fragment,
			// and add the transaction to the back stack
			transaction.remove(fragmentManager
					.findFragmentById(R.id.start_fragment));
			transaction.add(R.id.fragment_container, sf);
			transaction
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
			transaction.addToBackStack("started");

			// Commit the transaction
			transaction.commit();

			// use new values
			mGender = Gender.values()[genderPos];

			int regionPos = ((Spinner) findViewById(R.id.spinner_region))
					.getSelectedItemPosition();
			mRegion = Region.values()[regionPos];

			calculateInitial(false);

			// output values
			if (DEBUG) {
				TextView tv = ((TextView) findViewById(R.id.debug));
				if (tv != null) {
					tv.setText("caught=" + caught + ", Gender="
							+ mGender.toString() + ", Region="
							+ mRegion);
				}
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.about: {

			// Show the about dialog for this app.
			showAboutDialog();
			return true;
		}
		case R.id.feedback: {

			// Send out the feedback intent with a chooser
			startActivity(new Intent(Intent.ACTION_VIEW,
					Uri.parse(getString(R.string.about_feedback))));
			return true;
		}
		case R.id.share: {

			// Send out the send/share_app intent with a chooser, and with a
			// template text
			startActivity(Intent
					.createChooser(
							new Intent(Intent.ACTION_SEND)
									.putExtra(
											Intent.EXTRA_TEXT,
											getString(R.string.template_share_app))
									.putExtra(
											Intent.EXTRA_SUBJECT,
											getString(R.string.template_share_app_subject))
									.setType("text/plain"),
							getString(R.string.chooser_send_action)));
			return true;
		}
		case R.id.start_over: {

			FragmentManager fragmentManager = getSupportFragmentManager();
			fragmentManager.popBackStack();

			mGender = Gender.male;
			mRegion = null;

			calculateInitial(true);
			return true;
		}
		case R.id.help: {
			showDialog(HELP_DIALOG_ID);
		}
		}
		// TODO add
		return super.onOptionsItemSelected(item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case HELP_DIALOG_ID:
			return createHelpDialog();
			// break;
		case SMOKING_DIALOG_ID:
			return createSmokingDialog();
			// break;
		}
		return null;
	}

	/**
	 * Prepare dialog for help.
	 */
	public Dialog createHelpDialog() {

		// Launch dialog to ask for action
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.dialog_help_title);
		builder.setCancelable(true);
		builder.setMessage(R.string.dialog_help_message);

		final AlertDialog dialog = builder.create();
		return dialog;
	}

	/**
	 * Prepare dialog for afterwards (smoking).
	 */
	public Dialog createSmokingDialog() {
		Dialog dialog = new Dialog(this);

		dialog.setContentView(R.layout.smoking_toast);
		dialog.setTitle(R.string.dialog_smoking_title);
		dialog.setCancelable(true);

		return dialog;
	}
    
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		//Close cursor and free memory

		try {
			removeDialog(HELP_DIALOG_ID);
		} catch (IllegalArgumentException e) {
			//nothing
		}
		try {
			removeDialog(SMOKING_DIALOG_ID);
		} catch (IllegalArgumentException e) {
			//nothing
		}
		
	}
	
	private void showAboutDialog() {
		Intent intent = new Intent(AboutIntents.ACTION_SHOW_ABOUT_DIALOG);

		// Supply the image name and package.
		intent.putExtra(AboutIntents.EXTRA_ICON_RESOURCE, getResources()
				.getResourceName(R.drawable.logo));
		intent.putExtra(AboutIntents.EXTRA_PACKAGE_NAME, getPackageName());

		intent.putExtra(AboutIntents.EXTRA_APPLICATION_LABEL,
				getString(R.string.app_name));

		// Get the app version
		String version = "?";
		try {
			PackageInfo pi = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			version = pi.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			// analytics error
		}
		;
		intent.putExtra(AboutIntents.EXTRA_VERSION_NAME, version);
		intent.putExtra(AboutIntents.EXTRA_COMMENTS,
				getString(R.string.about_comments));
		intent.putExtra(AboutIntents.EXTRA_COPYRIGHT,
				getString(R.string.about_copyright));
		intent.putExtra(AboutIntents.EXTRA_WEBSITE_LABEL,
				getString(R.string.about_website_label));
		intent.putExtra(AboutIntents.EXTRA_WEBSITE_URL,
				getString(R.string.about_website_url));
		intent.putExtra(AboutIntents.EXTRA_EMAIL,
				getString(R.string.about_feedback));
		// intent.putExtra(AboutIntents.EXTRA_AUTHORS,
		// getResources().getStringArray(R.array.about_authors));
		// intent.putExtra(AboutIntents.EXTRA_DOCUMENTERS,
		// getResources().getStringArray(R.array.about_documenters));
		// intent.putExtra(AboutIntents.EXTRA_ARTISTS,
		// getResources().getStringArray(R.array.about_artists));

		// Create string array of translators from translated string from
		// Launchpad or (for English) from the array.
		/*
		 * String translatorsString=getString(R.string.about_translators);
		 * if(translatorsString.equals("translator-credits")){
		 * intent.putExtra(AboutIntents.EXTRA_TRANSLATORS,
		 * getResources().getStringArray(R.array.about_translators)); }else{
		 * String[] translatorsArray=translatorsString.replaceFirst(
		 * "Launchpad Contributions: ", "").split("(; )|(;)");
		 * intent.putExtra(AboutIntents.EXTRA_TRANSLATORS, translatorsArray); }
		 */

		// Supply resource name of raw resource that contains the license:
		intent.putExtra(AboutIntents.EXTRA_LICENSE_RESOURCE, getResources()
				.getResourceName(R.raw.license_short));
		// mIntent.putExtra(AboutIntents.EXTRA_WRAP_LICENSE, false);

		try {
			startActivityForResult(intent, 0);
		} catch (ActivityNotFoundException e) {
			try {
				// if (!Collectionista.DEBUG)
				// FlurryAgent.onError("CollectionsListWindow:showAboutDialog2",
				// getString(R.string.about_backup), e.getMessage());
				Toast.makeText(this, getString(R.string.about_backup),
						Toast.LENGTH_LONG).show();
				startActivity(new Intent(Intent.ACTION_VIEW,
						Uri.parse(getString(R.string.link_about_dialog))));
			} catch (ActivityNotFoundException e2) {
				// if (!Collectionista.DEBUG)
				// FlurryAgent.onError("CollectionsListWindow:showAboutDialog3",
				// getString(R.string.market_backup), e2.getMessage());
				Toast.makeText(this, getString(R.string.market_backup),
						Toast.LENGTH_LONG).show();
			}
		}
	}

	private void storePreferences() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		Editor editor = prefs.edit();
		editor.putString(PREFS_GENDER, mGender.name());

		if (mRegion != null) {
			editor.putString(PREFS_REGION, mRegion.name());
		} else {
			editor.putString(PREFS_REGION, null);
		}

		editor.putInt(PREFS_INFECTED, caught);
		editor.commit();

	}

	/**
	 * 
	 */
	protected void calculateInitial(boolean worldCitizen) {
		double prob = 0;
		if (!ran) {// TODO: add to preferences
			if (worldCitizen) {
				prob = Probability.worldwide * Probability.scale;
			} else {
				prob = Probability.fromData(mGender, mRegion)
						* Probability.scale;
			}
			caught = (int) Math.floor(Math.random() + Math.min(1, prob));
		}

		TextView tv = ((TextView) findViewById(R.id.debug));
		if (DEBUG) {
			if (tv != null) {
				tv.setText("caught=" + caught + ", Gender="
						+ mGender.toString() + ", Region="
						+ (mRegion == null ? "world" : mRegion.toString())
						+ ", Prob=" + prob);
			}
		}
		storePreferences();
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

		// TODO sending messages at the same time do not work !race condition
		mNfcAdapter.enableForegroundNdefPush(this, createNdefMessage());
		mNfcAdapter.enableForegroundDispatch(this, mPendingIntent,
				mIntentFiltersArray, mTechListsArray);

		NotificationManager nm = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(1);
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
		Log.v("HIV", "old status " + caught + " " + data);

		double partnerInfected = Double.parseDouble(parts[0]);
		Gender partnerGender = Gender.valueOf(parts[1]);
		updateInfectionStatus(partnerInfected, partnerGender);

		Log.v("HIV", "new status " + caught);

		showDialog(SMOKING_DIALOG_ID);

	}

	private void updateInfectionStatus(double partnerInfected, Gender gender) {
		int caughtOld = caught;

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
			caught = (int) Math.floor(random + (factor * Probability.scale));// TODO

			storePreferences();

			// output values
			if (DEBUG) {
				TextView tv = ((TextView) findViewById(R.id.debug));
				if (tv != null) {
					tv.setText("caught=" + caught + ", Gender="
							+ mGender.toString() + ", Region="
							+ (mRegion == null ? "world" : mRegion.toString()));
				}
			}

		}

		// store history
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		Editor editor = prefs.edit();
		int history = prefs.getInt(PREFS_HISTORY, 0);
		history++;
		editor.putInt(PREFS_HISTORY, history);

		if (caughtOld == 0 && caught == 1) {
			// remember when you were infected
			int historyInfected = history;
			editor.putInt(PREFS_HISTORY_INFECTED, historyInfected);
		}
		editor.commit();

		// game over after 10 touches
		if (history > 1) {
			finishGame(caught == 1, prefs.getInt(PREFS_HISTORY_INFECTED, 0));
		}

	}

	private void finishGame(boolean infected, int whenInfected) {

		String msg;
		if (infected && whenInfected == 0) {
			msg = getString(R.string.infected_at_beginning);
		} else if (infected) {
			msg = getString(R.string.infected_when, whenInfected);
		} else {
			msg = getString(R.string.not_infected);
		}

		Intent intent = new Intent(this, AlertReceiver.class);
		intent.putExtra("message", msg);
		PendingIntent operation = PendingIntent.getBroadcast(this, 0, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);

		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC, System.currentTimeMillis(), operation);
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

	/*
	 * public static void showFormattedImageToast(Context context, int id,
	 * Drawable drawable, Object... args) {
	 * 
	 * final View view =
	 * LayoutInflater.from(context).inflate(R.layout.book_notification, null);
	 * ((TextView) view.findViewById(R.id.message)).setText(
	 * String.format(context.getText(id).toString(), args)); ((ImageView)
	 * view.findViewById(R.id.cover)).setImageDrawable(drawable);
	 * 
	 * Toast toast = new Toast(context); toast.setDuration(Toast.LENGTH_LONG);
	 * toast.setView(view);
	 * 
	 * toast.show(); }
	 */

}