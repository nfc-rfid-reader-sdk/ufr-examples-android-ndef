package com.dlogic.ufrndef;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dlogic.uFCoder;

import java.io.IOException;

import pl.droidsonroids.gif.GifImageView;

import static com.dlogic.ufrndef.MainActivity.device;

public class WriteUrlActivity extends AppCompatActivity {

    static {
        System.loadLibrary("uFCoder"); //Load uFCoder library
    }

    uFCoder uFCoder;
    byte storage = 0;
    byte uri_type = 0;

    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];
    boolean writeMode;
    Context context;
    public Boolean isPhone = false;

    GifImageView dialogIcon;
    TextView dialogText;
    Button dialogButton;
    ImageView statusIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_url);

        uFCoder = new uFCoder(getApplicationContext());

        //---------------------------------------------------------------------------------
        //---------------------------------------------------------------------------------
        context = this;

        //---------------------------------------------------------------------------------
        //---------------------------------------------------------------------------------
        LinearLayout urlLayout = findViewById(R.id.writingURLLayout);
        LinearLayout urlStorage = findViewById(R.id.writeURIStorage);


        urlLayout.setVisibility(View.VISIBLE);


        LinearLayout back = findViewById(R.id.back_from_url);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Spinner spnStoreInto = findViewById(R.id.url_store_into);
        ArrayAdapter<CharSequence> spnStoreModes = ArrayAdapter.createFromResource(getApplicationContext(),
                R.array.store_into_modes,
                R.layout.dl_spinner_textview);
        spnStoreInto.setAdapter(spnStoreModes);
        spnStoreInto.setSelection(0);
        spnStoreInto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                storage = (byte) pos;
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        final Spinner spnURIMode = findViewById(R.id.uri_id_code_spinner);
        ArrayAdapter<CharSequence> spnAuthModes = ArrayAdapter.createFromResource(getApplicationContext(),
                R.array.uri_types,
                R.layout.dl_spinner_textview);
        spnURIMode.setAdapter(spnAuthModes);
        spnURIMode.setSelection(0);
        spnURIMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                uri_type = (byte) (pos + 1);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Button btnWriteURI = findViewById(R.id.buttonWriteURIId);
        btnWriteURI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText uriField = findViewById(R.id.uri_field_id);

                String uriStr = uriField.getText().toString().trim();

                int status = 0;

                uFCoder.erase_all_ndef_records((byte) 1);

                byte[] tnf = {1};
                byte[] type_record = {85};
                byte[] type_length = {1};
                byte[] id = new byte[1];
                byte[] id_length = {1};
                byte[] payload = new byte[1 + uriStr.length()];
                payload[0] = uri_type;
                int[] payload_length = {1 + uriStr.length()};
                byte[] card_formatted = new byte[1];
                System.arraycopy(uriStr.getBytes(), 0, payload, 1, uriStr.length());


                if (storage == 1) {
                    status = uFCoder.write_ndef_record((byte) 1, tnf, type_record, type_length, id, id_length, payload, payload_length, card_formatted);
                } else {
                    status = uFCoder.WriteEmulationNdef(tnf[0], type_record, type_length[0], id, id_length[0], payload, (byte) payload_length[0]);
                }
                if (status == 0) {
                    Toast.makeText(getApplicationContext(), "Tag successfully written", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), uFCoder.UFR_Status2String(status), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    public void displayError() {
        dialogButton.setText("OK");
        dialogIcon.setVisibility(View.GONE);
        statusIcon.setBackgroundResource(R.drawable.error_icon);
        statusIcon.setVisibility(View.VISIBLE);
        dialogText.setText("Tag writing error!");
    }

    public void displaySuccess() {
        dialogButton.setText("OK");
        dialogIcon.setVisibility(View.GONE);
        statusIcon.setBackgroundResource(R.drawable.successfull_icon);
        statusIcon.setVisibility(View.VISIBLE);
        dialogText.setText("Tag successfully written");
    }

    public void displayWrite() {
        dialogButton.setText("CANCEL");
        statusIcon.setVisibility(View.GONE);
        dialogIcon.setVisibility(View.VISIBLE);
        dialogText.setText("Put NFC tag on the phone");
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    public class CustomDialogClass extends Dialog implements
            android.view.View.OnClickListener {

        public Activity c;
        public Dialog d;
        public Button dialogBtn;

        public CustomDialogClass(Activity a) {
            super(a);
            // TODO Auto-generated constructor stub
            this.c = a;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.custom_dialog);
            dialogButton = findViewById(R.id.dialogButtonId);
            dialogIcon = findViewById(R.id.dialogIconId);
            dialogText = findViewById(R.id.dialogTextId);
            statusIcon = findViewById(R.id.statusWriteiconId);
            dialogButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.dialogButtonId:
                    isPhone = false;
                    dismiss();
                default:
                    break;
            }
            dismiss();
        }
    }
}
