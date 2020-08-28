package com.dlogic.ufrndef;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.dlogic.uFCoder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity {

    public static HashMap<String, List<String>> expandableListDetail = new HashMap<>();

    public static PendingIntent pendingIntent = null;
    public static int device = 1; // 1 = uFR Reader
    public static Boolean LOOP = false;
    public static String parsed_payload_data = "";
    IntentFilter writeTagFilters[];

    Context context;

    GifImageView dialogIcon;
    TextView dialogText;
    Button dialogButton;
    ImageView statusIcon;

    static {
        System.loadLibrary("uFCoder"); //Load uFCoder library
    }

    uFCoder uFCoder;

    LinearLayout resultLayout;
    LinearLayout waitGif;
    TextView ndefDataText;
    TextView ndefDataIcon;

    public Boolean isErasing = false;
    boolean writeMode;

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public final static String bytesToHex(byte[] bytes) {
        if (bytes == null) return null;

        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String bytesToASCII(byte[] bytes) {
        return new String(bytes);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uFCoder = new uFCoder(getApplicationContext());

        TabHost tabHost = findViewById(R.id.tabhost);
        tabHost.setup();
        tabHost.addTab(tabHost.newTabSpec("tab0").setIndicator("DEVICE", null).setContent(R.id.device_page));
        tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator("READ", null).setContent(R.id.read_page));
        tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator("WRITE", null).setContent(R.id.write_page));
        tabHost.addTab(tabHost.newTabSpec("tab3").setIndicator("TOOLS", null).setContent(R.id.tools_page));

        TabWidget tw = (TabWidget) tabHost.findViewById(android.R.id.tabs);
        final CheckBox useAdvanced = findViewById(R.id.checkBoxUseAdvanced);
        final LinearLayout advancedContainer = findViewById(R.id.advancedLayout);

        for (int i = 0; i < 4; i++) {
            View tabView = tw.getChildTabViewAt(i);
            tabView.getLayoutParams().height = (int) (40 * this.getResources().getDisplayMetrics().density);
            TextView tv = (TextView) tabView.findViewById(android.R.id.title);
            tv.setTextColor(Color.WHITE);
        }

        useAdvanced.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (useAdvanced.isChecked()) {
                    advancedContainer.setVisibility(View.VISIBLE);
                } else {
                    advancedContainer.setVisibility(View.GONE);
                }
            }
        });


        //final RadioButton phoneRb = findViewById(R.id.androidRB);
        //final RadioButton uFRRb = findViewById(R.id.ufrRB);

        TableRow wifiWrite = findViewById(R.id.wifiRow);
        TableRow urlWrite = findViewById(R.id.urlRow);
        TableRow bluetoothWrite = findViewById(R.id.bluetoothRow);
        TableRow smsWrite = findViewById(R.id.smsRow);
        TableRow locationWrite = findViewById(R.id.geolocationRow);
        TableRow naviWrite = findViewById(R.id.naviRow);
        TableRow emailWrite = findViewById(R.id.emailRow);
        TableRow addressWrite = findViewById(R.id.addressRow);
        TableRow applicationWrite = findViewById(R.id.applicationRow);
        TableRow textWrite = findViewById(R.id.textRow);
        TableRow streetViewWrite = findViewById(R.id.streetviewRow);
        TableRow phoneWrite = findViewById(R.id.phoneRow);
        TableRow contactWrite = findViewById(R.id.contactRow);
        TableRow bitcoinWrite = findViewById(R.id.bitcoinRow);
        TableRow skypeWrite = findViewById(R.id.skypeRow);
        TableRow viberWrite = findViewById(R.id.viberRow);
        TableRow whatsappWrite = findViewById(R.id.whatsappRow);
        final TableRow tagEmulation = findViewById(R.id.tagemulationRow);
        TableRow eraseTag = findViewById(R.id.erasetagRow);
        TableRow ndefData = findViewById(R.id.ndefDatRow);
        ndefDataIcon = findViewById(R.id.ndefDataIconId);
        ndefDataText = findViewById(R.id.ndefDataId);

        LinearLayout header = findViewById(R.id.headerLayoutId);
        final LinearLayout readerOpenOptions = findViewById(R.id.readerOpenLayout);
        final EditText reader_type = findViewById(R.id.readerTypeET);
        final EditText port_name = findViewById(R.id.portNameET);
        final EditText port_interface = findViewById(R.id.portInterfaceET);
        final EditText arg = findViewById(R.id.argET);
        Button btnOpen = findViewById(R.id.btnReaderOpen);
        final Button btnBuy = findViewById(R.id.buttonBuyId);

        resultLayout = findViewById(R.id.readResultsId);
        waitGif = findViewById(R.id.waitGif);

        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*String url = "https://www.d-logic.net/";

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);*/
            }
        });

        btnBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://www.d-logic.net/nfc-rfid-reader-sdk/wireless-nfc-reader-ufr-nano-online/";

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int status = 1;

                try {
                    if (useAdvanced.isChecked()) {
                        String reader_type_str = reader_type.getText().toString().trim();
                        String port_name_str = port_name.getText().toString().trim();
                        String arg_Str = arg.getText().toString().trim();
                        String port_interface_str = port_interface.getText().toString().trim();

                        if (reader_type_str.equals("") || port_name_str.equals("") || port_interface_str.equals("") || arg_Str.equals("")) {
                            Toast.makeText(getApplicationContext(), "You have to fill all parameters for advanced Reader Open", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int port_interface_int = (int) port_interface_str.charAt(0);
                        int int_rtype = Integer.parseInt(reader_type_str);

                        status = uFCoder.ReaderOpenEx(int_rtype, port_name_str, port_interface_int, arg_Str);
                    } else {
                        status = uFCoder.ReaderOpen();
                    }
                } catch (Exception ex) {
                }

                if (status == 0) {
                    Toast.makeText(getApplicationContext(), "Reader connected successfully", Toast.LENGTH_SHORT).show();
                    uFCoder.ReaderUISignal((byte) 1, (byte) 1);
                } else {
                    Toast.makeText(getApplicationContext(), "Reader not connected", Toast.LENGTH_SHORT).show();
                }
            }
        });


        btnBuy.setVisibility(View.VISIBLE);

        TextView instruction = findViewById(R.id.instructionTextId);
        instruction.setText("PUT NFC TAG ON THE READER");

        ImageView deviceImage = findViewById(R.id.devideImageId);
        deviceImage.setBackgroundResource(R.drawable.resize1);

        readerOpenOptions.setVisibility(View.VISIBLE);

        tagEmulation.setVisibility(View.VISIBLE);
        device = 1; // 1 = uFR Reader

        wifiWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WriteWifiActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        urlWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WriteUrlActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        bluetoothWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WriteBluetoothActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        smsWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WriteSMSActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        locationWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WriteLocationActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        naviWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WriteNaviDestinationActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        emailWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WriteEmailActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        addressWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WriteAddressActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        applicationWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WriteApplicationActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        textWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WriteTextActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        streetViewWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WriteStreetViewActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        phoneWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WritePhoneActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        contactWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WriteContactActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        bitcoinWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WriteBitcoinActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        skypeWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WriteSkypeActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        viberWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WriteViberActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        whatsappWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WriteWhatsappActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        eraseTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int status = 0;

                status = uFCoder.erase_all_ndef_records((byte) 1);

                if (status == 0) {
                    Toast.makeText(getApplicationContext(), "Tag erased!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), uFCoder.UFR_Status2String(status), Toast.LENGTH_SHORT).show();
                }

            }
        });

        ndefData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ReadNdefActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {

                if (tabId.equals("tab1")) {
                    waitGif.setVisibility(View.VISIBLE);
                    resultLayout.setVisibility(View.GONE);

                    final TextView ndef_uid = findViewById(R.id.ndefReadUidId);
                    final TextView ndef_card_type = findViewById(R.id.ndefCardType);
                    final TextView scanTime = findViewById(R.id.ndefScanTime);

                    LOOP = true;

                    new Thread() {
                        public void run() {

                            final byte[] sak = new byte[1];
                            final byte[] uid = new byte[10];
                            byte[] temp_uid = new byte[10];
                            temp_uid[0] = (byte) 255;
                            final byte[] uidSize = new byte[1];

                            while (LOOP) {
                                final int get_uid = uFCoder.GetCardIdEx(sak, uid, uidSize);

                                if (get_uid == 0) {
                                    if (!Arrays.equals(temp_uid, uid)) {
                                        final byte[] tnf = new byte[1];
                                        final byte[] type = new byte[40];
                                        final byte[] t_len = new byte[1];
                                        byte[] id = new byte[1];
                                        byte[] id_len = new byte[1];
                                        final byte[] pay = new byte[500];
                                        final int[] pay_len = new int[1];

                                        try {
                                            Thread.sleep(500);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }

                                        final int status = uFCoder.read_ndef_record((byte) 1, (byte) 1, tnf, type, t_len, id, id_len, pay, pay_len);

                                        if (status == 0) {
                                            final byte[] dispUid = new byte[uidSize[0]];
                                            System.arraycopy(uid, 0, dispUid, 0, uidSize[0]);

                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    waitGif.setVisibility(View.GONE);
                                                    resultLayout.setVisibility(View.VISIBLE);

                                                    scanTime.setText(GetScanningTime());
                                                    ndef_uid.setText(bytesToHex(dispUid));

                                                    byte[] card_type = new byte[1];
                                                    uFCoder.GetDlogicCardType(card_type);
                                                    ndef_card_type.setText("Card type : " + uFCoder.UFR_DLCardType2String(card_type[0]));

                                                    parsed_payload_data = uFCoder.ParseNdefMessage(type, t_len[0], pay, pay_len[0]);

                                                    getPayloadDataReader(tnf[0], type, t_len[0], pay, pay_len[0]);
                                                }
                                            });
                                        } else if (status == 0x81) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(getApplicationContext(), uFCoder.UFR_Status2String(status), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }

                                        System.arraycopy(uid, 0, temp_uid, 0, uidSize[0]);
                                    }
                                } else {
                                    Arrays.fill(uid, (byte) 0);
                                    Arrays.fill(temp_uid, (byte) 0);
                                }

                                try {
                                    Thread.sleep(300);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                    }.start();
                } else {
                    LOOP = false;
                }
            }
        });

        final Switch tagEmulationToggle = findViewById(R.id.tagEmulationSwitch);
        tagEmulationToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int status = 0;

                if (tagEmulationToggle.isChecked()) {
                    status = uFCoder.TagEmulationStart();

                    if (status == 0) {
                        tagEmulationToggle.setChecked(true);
                        Toast.makeText(getApplicationContext(), "Tag emulation started", Toast.LENGTH_SHORT).show();
                    } else {
                        tagEmulationToggle.setChecked(false);
                        Toast.makeText(getApplicationContext(), uFCoder.UFR_Status2String(status), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    status = uFCoder.TagEmulationStop();

                    if (status == 0) {
                        tagEmulationToggle.setChecked(true);
                        Toast.makeText(getApplicationContext(), "Tag emulation stopped", Toast.LENGTH_SHORT).show();
                    } else {
                        tagEmulationToggle.setChecked(false);
                        Toast.makeText(getApplicationContext(), uFCoder.UFR_Status2String(status), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public void onResume() {

        super.onResume();

    }

    @Override
    public void onPause() {

        super.onPause();
    }

    @Override
    public void onNewIntent(Intent intent) {

        super.onNewIntent(intent);
    }

    String readRecord(byte[] payload) throws UnsupportedEncodingException {
        return new String(payload);
    }

    public final static String GetScanningTime() {
        Date currentTime = Calendar.getInstance().getTime();
        return currentTime.toString();
    }

    public void getPayloadDataReader(byte tnf, byte[] type, byte type_len, byte[] payload, int payload_len) {
        expandableListDetail.clear();

        List<String> tnf_list = new ArrayList<String>();
        List<String> type_list = new ArrayList<String>();
        List<String> payload_list = new ArrayList<String>();

        tnf_list.add("Hex : " + Integer.toHexString((int) tnf));
        tnf_list.add("Decimal : " + Integer.toString((int) tnf));

        byte[] temp_type = new byte[type_len];
        byte[] temp_payload = new byte[payload_len];

        System.arraycopy(type, 0, temp_type, 0, type_len);
        System.arraycopy(payload, 0, temp_payload, 0, payload_len);

        type_list.add("Length : " + Byte.toString(type_len));
        type_list.add("Hex : " + bytesToHex(temp_type));
        type_list.add("ASCII : " + bytesToASCII(temp_type));

        payload_list.add("Length : " + Integer.toString(payload_len));
        payload_list.add("Hex : " + bytesToHex(temp_payload));
        payload_list.add("ASCII : " + bytesToASCII(temp_payload));

        expandableListDetail.put("TNF", tnf_list);
        expandableListDetail.put("TYPE", type_list);
        expandableListDetail.put("PAYLOAD", payload_list);

        SetPayloadIcon(type[0], new String(temp_payload));
    }


    public void GetPayloadForPhone(Intent intent) {

    }

    public String GetURIPrefix(byte uri_type) {
        String uriPrefixStr = "";

        switch (uri_type) {
            case 1:
                uriPrefixStr = "http://www.";
                break;
            case 2:
                uriPrefixStr = "https://www.";
                break;
            case 3:
                uriPrefixStr = "http://";
                break;
            case 4:
                uriPrefixStr = "https://";
                break;
            case 5:
                uriPrefixStr = "tel:";
                break;
            case 6:
                uriPrefixStr = "mailto:";
                break;
            case 7:
                uriPrefixStr = "ftp://anonymous:anonymous@";
                break;
            case 8:
                uriPrefixStr = "ftp://ftp.";
                break;
            case 9:
                uriPrefixStr = "ftps://";
                break;
            case 10:
                uriPrefixStr = "sftp://";
                break;
            case 11:
                uriPrefixStr = "smb://";
                break;
            case 12:
                uriPrefixStr = "nfs://";
                break;
            case 13:
                uriPrefixStr = "ftp://";
                break;
            case 14:
                uriPrefixStr = "dav://";
                break;
            case 15:
                uriPrefixStr = "news:";
                break;
            case 16:
                uriPrefixStr = "telnet://";
                break;
            case 17:
                uriPrefixStr = "imap:";
                break;
            case 18:
                uriPrefixStr = "rtsp://";
                break;
            case 19:
                uriPrefixStr = "urn:";
                break;
            case 20:
                uriPrefixStr = "pop:";
                break;
            case 21:
                uriPrefixStr = "sip:";
                break;
            case 22:
                uriPrefixStr = "sips:";
                break;
            case 23:
                uriPrefixStr = "tftp:";
                break;
            case 24:
                uriPrefixStr = "btspp://";
                break;
            case 25:
                uriPrefixStr = "btl2cap://";
                break;
            case 26:
                uriPrefixStr = "btgoep://";
                break;
            case 27:
                uriPrefixStr = "tcpobex://";
                break;
            case 28:
                uriPrefixStr = "irdaobex://";
                break;
            case 29:
                uriPrefixStr = "file://";
                break;
            case 30:
                uriPrefixStr = "urn:epc:id:";
                break;
            case 31:
                uriPrefixStr = "urn:epc:tag:";
                break;
            case 32:
                uriPrefixStr = "urn:epc:pat:";
                break;
            case 33:
                uriPrefixStr = "urn:epc:raw:";
                break;
            case 34:
                uriPrefixStr = "urn:epc:";
                break;
            case 35:
                uriPrefixStr = "urn:nfc:";
                break;
            default:
                uriPrefixStr = "";
                break;

        }
        return uriPrefixStr;
    }

    public void SetPayloadIcon(byte type, String payloadStr) {
        if (type == 'U') {
            if (parsed_payload_data.startsWith("Bitcoin")) {
                ndefDataIcon.setBackgroundResource(R.drawable.bitcoin_icon_pattern);
                ndefDataText.setText(parsed_payload_data);
            } else if (parsed_payload_data.startsWith("Address")) {
                ndefDataIcon.setBackgroundResource(R.drawable.homeaddress_icon_pattern);
                ndefDataText.setText(parsed_payload_data);
            } else if (parsed_payload_data.startsWith("Location")) {
                ndefDataIcon.setBackgroundResource(R.drawable.location_icon_pattern);
                ndefDataText.setText(parsed_payload_data);
            } else if (parsed_payload_data.startsWith("StreetView")) {
                ndefDataIcon.setBackgroundResource(R.drawable.streetview_icon_pattern);
                ndefDataText.setText(parsed_payload_data);
            } else if (parsed_payload_data.startsWith("Destination")) {
                ndefDataIcon.setBackgroundResource(R.drawable.navi_icon_pressed);
                ndefDataText.setText(parsed_payload_data);
            } else if (parsed_payload_data.startsWith("Email")) {
                ndefDataIcon.setBackgroundResource(R.drawable.email_icon_pattern);
                ndefDataText.setText(parsed_payload_data);
            } else if (parsed_payload_data.startsWith("Username")) {
                ndefDataIcon.setBackgroundResource(R.drawable.skype_icon_pattern);
                ndefDataText.setText(parsed_payload_data);
            } else if (parsed_payload_data.startsWith("Whatsapp")) {
                ndefDataIcon.setBackgroundResource(R.drawable.whatsapp_icon_pattern);
                ndefDataText.setText(parsed_payload_data);
            } else if (parsed_payload_data.startsWith("Viber")) {
                ndefDataIcon.setBackgroundResource(R.drawable.viber_icon_pattern);
                ndefDataText.setText(parsed_payload_data);
            } else if (parsed_payload_data.startsWith("Phone")) {
                if (parsed_payload_data.contains("Message")) {
                    ndefDataIcon.setBackgroundResource(R.drawable.sms_icon_pattern);
                } else {
                    ndefDataIcon.setBackgroundResource(R.drawable.phone_icon_pattern);
                }

                ndefDataText.setText(parsed_payload_data);
            } else {
                ndefDataIcon.setBackgroundResource(R.drawable.uri_icon_pattern);
                ndefDataText.setText(payloadStr);
            }
        } else {
            if (parsed_payload_data.startsWith("SSID")) {
                ndefDataIcon.setBackgroundResource(R.drawable.wifi_icon_pattern);
            } else if (parsed_payload_data.startsWith("Bluetooth")) {
                ndefDataIcon.setBackgroundResource(R.drawable.bluetooth_icon_pattern);
            } else if (parsed_payload_data.startsWith("Package")) {
                ndefDataIcon.setBackgroundResource(R.drawable.androidapp_icon_pattern);
            } else if (parsed_payload_data.startsWith("Text")) {
                ndefDataIcon.setBackgroundResource(R.drawable.text_icon_pattern);
            } else if (parsed_payload_data.startsWith("BEGIN")) {
                ndefDataIcon.setBackgroundResource(R.drawable.contact_icon_pattern);
            }

            ndefDataText.setText(parsed_payload_data);
        }
    }

    public void displayError() {
        dialogButton.setText("OK");
        dialogIcon.setVisibility(View.GONE);
        statusIcon.setBackgroundResource(R.drawable.error_icon);
        statusIcon.setVisibility(View.VISIBLE);
        dialogText.setText("Tag erasing error!");
    }

    public void displaySuccess() {
        dialogButton.setText("OK");
        dialogIcon.setVisibility(View.GONE);
        statusIcon.setBackgroundResource(R.drawable.successfull_icon);
        statusIcon.setVisibility(View.VISIBLE);
        dialogText.setText("Tag successfully erased");
    }

    public void displayWrite() {
        dialogButton.setText("CANCEL");
        statusIcon.setVisibility(View.GONE);
        dialogIcon.setVisibility(View.VISIBLE);
        dialogText.setText("Put NFC tag on the phone");
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
                    isErasing = false;
                    dismiss();
                default:
                    break;
            }
            dismiss();
        }
    }
}
