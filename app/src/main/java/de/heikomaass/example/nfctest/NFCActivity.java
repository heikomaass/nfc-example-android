package de.heikomaass.example.nfctest;

import android.app.Activity;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class NFCActivity extends Activity {

    private final static String TAG = NFCActivity.class.getSimpleName();
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] filters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(de.heikomaass.example.nfctest.R.layout.activity_nfc);
        this.nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (this.nfcAdapter == null) {
            Toast.makeText(this, de.heikomaass.example.nfctest.R.string.nfc_not_available, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Intent ndefDiscovered = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        this.pendingIntent = PendingIntent.getActivity(this, 0, ndefDiscovered, 0);

        IntentFilter ndefIntentFilter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);

        try {
            ndefIntentFilter.addDataType("text/plain");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException(e);
        }
        filters = new IntentFilter[]{ndefIntentFilter};

        if (savedInstanceState == null) {
            NFCFragment nfcFragment = new NFCFragment();
            getFragmentManager()
                    .beginTransaction()
                    .add(de.heikomaass.example.nfctest.R.id.container, nfcFragment)
                    .commit();
        }
    }

    protected void onResume() {
        super.onResume();
        this.nfcAdapter.enableForegroundDispatch(this, pendingIntent, filters, new String[][]{});
    }

    protected void onPause() {
        super.onPause();
        this.nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        /**
         * This method gets called, when a new Intent gets associated with the current activity instance.
         * Instead of creating a new activity, onNewIntent will be called. For more information have a look
         * at the documentation.
         *
         * In our case this method gets called, when the user attaches a Tag to the device.
         */
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        Log.i(TAG, "Discovered tag with intent: " + intent);
        NFCFragment fragmentById = (NFCFragment) this.getFragmentManager().findFragmentById(de.heikomaass.example.nfctest.R.id.container);

        Parcelable[] parcelableArrayExtra = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage[] msgs;
        if (parcelableArrayExtra != null) {
            msgs = new NdefMessage[parcelableArrayExtra.length];

            for (int i = 0; i < parcelableArrayExtra.length; i++) {
                msgs[i] = (NdefMessage) parcelableArrayExtra[i];
            }

            for (NdefMessage message : msgs) {
                String text = this.extractText(message);
                if (text != null) {
                    fragmentById.ndefMessage.setText(text);
                    return;
                }
            }
            Toast.makeText(this, "This tag is not a Text Tag", Toast.LENGTH_LONG).show();

        }
    }

    private String extractText(NdefMessage ndefMessage) {
        NdefRecord[] records = ndefMessage.getRecords();
        for (NdefRecord record : records) {
            if (NdefRecord.TNF_WELL_KNOWN == record.getTnf()) {
                TextRecord textRecord = new TextRecord.Builder(record).build();
                return textRecord.getText();
            }
        }
        return null;
    }


    public static class NFCFragment extends Fragment {

        private TextView ndefMessage;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(de.heikomaass.example.nfctest.R.layout.fragment_nfc, container, false);
            this.ndefMessage = (TextView) rootView.findViewById(de.heikomaass.example.nfctest.R.id.ndefMessage);
            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            NFCActivity nfcActivity = (NFCActivity) getActivity();
            if (nfcActivity.nfcAdapter.isEnabled()) {
                this.ndefMessage.setText(de.heikomaass.example.nfctest.R.string.introduction);
            } else {
                this.ndefMessage.setText(de.heikomaass.example.nfctest.R.string.nfc_not_available);
            }
        }
    }
}
