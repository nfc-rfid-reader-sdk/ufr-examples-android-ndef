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

public class WriteContactActivity extends AppCompatActivity {

    static {
        System.loadLibrary("uFCoder"); //Load uFCoder library
    }

    uFCoder uFCoder;
    byte storage = 0;

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
        setContentView(R.layout.activity_write_contact);

        uFCoder = new uFCoder(getApplicationContext());

        //---------------------------------------------------------------------------------
        //---------------------------------------------------------------------------------
        context = this;

        //---------------------------------------------------------------------------------
        //---------------------------------------------------------------------------------

        LinearLayout bigLayout = findViewById(R.id.writeContactLayout);
        LinearLayout storageLayout = findViewById(R.id.writeContactStorage);

        if (device == 0) {
            storageLayout.setVisibility(View.GONE);
            ViewGroup.LayoutParams params = bigLayout.getLayoutParams();
            final float scale = getResources().getDisplayMetrics().density;
            params.height = (int) (250 * scale);
            bigLayout.setLayoutParams(params);
        } else {
            bigLayout.setVisibility(View.VISIBLE);
        }

        Spinner spnStoreInto = findViewById(R.id.contact_store_into);
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

        LinearLayout back = findViewById(R.id.back_from_contact);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button btnWriteContact = findViewById(R.id.buttonWriteContact);
        btnWriteContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText WifiSsidName = findViewById(R.id.ssidNameId);
                EditText WifiPassword = findViewById(R.id.passwordId);

                int status = 0;

                EditText nameET = findViewById(R.id.contactNameET);
                EditText companyET = findViewById(R.id.contactCompanyET);
                EditText addressET = findViewById(R.id.contactAddressET);
                EditText phoneET = findViewById(R.id.contactPhoneET);
                EditText emailET = findViewById(R.id.contactEmailET);
                EditText websiteET = findViewById(R.id.contactWebsiteET);

                String nameStr = nameET.getText().toString().trim();
                String companyStr = companyET.getText().toString().trim();
                String addressStr = addressET.getText().toString().trim();
                String phoneStr = phoneET.getText().toString().trim();
                String emailStr = emailET.getText().toString().trim();
                String websiteStr = websiteET.getText().toString().trim();

                uFCoder.erase_all_ndef_records((byte) 1);

                status = uFCoder.WriteNdefRecord_Contact(storage, nameStr, companyStr, addressStr, phoneStr, emailStr, websiteStr);

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
