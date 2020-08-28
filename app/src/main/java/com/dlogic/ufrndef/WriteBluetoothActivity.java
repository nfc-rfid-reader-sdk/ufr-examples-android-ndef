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

import javax.crypto.Mac;

import pl.droidsonroids.gif.GifImageView;

import static com.dlogic.ufrndef.MainActivity.device;

public class WriteBluetoothActivity extends AppCompatActivity {

    static {
        System.loadLibrary("uFCoder"); //Load uFCoder library
    }

    uFCoder uFCoder;
    byte storage = 0;

    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];
    boolean writeMode;
    Context context;
    EditText ndefText;
    public Boolean isPhone = false;

    GifImageView dialogIcon;
    TextView dialogText;
    Button dialogButton;
    ImageView statusIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_bluetooth);

        uFCoder = new uFCoder(getApplicationContext());

        //---------------------------------------------------------------------------------
        //---------------------------------------------------------------------------------
        context = this;

        //---------------------------------------------------------------------------------
        //---------------------------------------------------------------------------------

        LinearLayout bigLayout = findViewById(R.id.writeBluetoothLayout);
        LinearLayout storageLayout = findViewById(R.id.writeBluetoothStorage);

        if(device == 0)
        {
            storageLayout.setVisibility(View.GONE);
            ViewGroup.LayoutParams params = bigLayout.getLayoutParams();
            final float scale = getResources().getDisplayMetrics().density;
            params.height = (int)(50 * scale);
            bigLayout.setLayoutParams(params);
        }
        else
        {
            bigLayout.setVisibility(View.VISIBLE);
        }

        LinearLayout back = findViewById(R.id.back_from_bluetooth);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Spinner spnStoreInto = findViewById(R.id.bluetooth_store_into);
        ArrayAdapter<CharSequence> spnStoreModes = ArrayAdapter.createFromResource(getApplicationContext(),
                R.array.store_into_modes,
                R.layout.dl_spinner_textview);
        spnStoreInto.setAdapter(spnStoreModes);
        spnStoreInto.setSelection(0);
        spnStoreInto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                storage = (byte)pos;
            }

            public void onNothingSelected(AdapterView<?> parent) { }
        });

        Button btnWriteBluetooth = findViewById(R.id.buttonWriteBluetooth);
        btnWriteBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText macET = findViewById(R.id.bluetoothMACEditTextId);

                String MacStr = macET.getText().toString().trim();

                if(MacStr.length() != 12)
                {
                    Toast.makeText(getApplicationContext(), "Invalid MAC address", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(device == 0)
                {
                    isPhone = true;
                    CustomDialogClass cdd = new CustomDialogClass(WriteBluetoothActivity.this);
                    cdd.show();
                    displayWrite();
                }
                else if(device == 1)
                {
                    int status;

                    uFCoder.erase_all_ndef_records((byte)1);

                    status = uFCoder.WriteNdefRecord_Bluetooth(storage, MacStr);

                    if(status == 0)
                    {
                        Toast.makeText(getApplicationContext(), "Tag successfully written", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), uFCoder.UFR_Status2String(status), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    public void displayError()
    {
        dialogButton.setText("OK");
        dialogIcon.setVisibility(View.GONE);
        statusIcon.setBackgroundResource(R.drawable.error_icon);
        statusIcon.setVisibility(View.VISIBLE);
        dialogText.setText("Tag writing error!");
    }

    public void displaySuccess()
    {
        dialogButton.setText("OK");
        dialogIcon.setVisibility(View.GONE);
        statusIcon.setBackgroundResource(R.drawable.successfull_icon);
        statusIcon.setVisibility(View.VISIBLE);
        dialogText.setText("Tag successfully written");
    }

    public void displayWrite()
    {
        dialogButton.setText("CANCEL");
        statusIcon.setVisibility(View.GONE);
        dialogIcon.setVisibility(View.VISIBLE);
        dialogText.setText("Put NFC tag on the phone");
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onResume(){
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

    public static byte[] hexStringToByteArray(String paramString) throws IllegalArgumentException {
        int j = paramString.length();

        if (j % 2 == 1) {
            throw new IllegalArgumentException("Hex string must have even number of characters");
        }

        byte[] arrayOfByte = new byte[j / 2];
        int hiNibble, loNibble;

        for (int i = 0; i < j; i += 2) {
            hiNibble = Character.digit(paramString.charAt(i), 16);
            loNibble = Character.digit(paramString.charAt(i + 1), 16);
            if (hiNibble < 0) {
                throw new IllegalArgumentException("Illegal hex digit at position " + i);
            }
            if (loNibble < 0) {
                throw new IllegalArgumentException("Illegal hex digit at position " + (i + 1));
            }
            arrayOfByte[(i / 2)] = ((byte) ((hiNibble << 4) + loNibble));
        }
        return arrayOfByte;
    }

}
