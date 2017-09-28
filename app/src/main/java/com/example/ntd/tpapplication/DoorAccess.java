package com.example.ntd.tpapplication;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONStringer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class DoorAccess extends Activity implements  RR2F.RR2FLowLevelInterface,RR2F.RR2FHighLevelInterface,InterfaceFT311.ConnectionInterface,Serializable {
    Protocol protocol = new Protocol();
    static int connecttionType2 = 0;
    final DB myDb = new DB(this);
    public boolean isConnect, waitRead = false;
    public boolean running = false;
    public handler_thread handlerThread;
    public int connectionTypeState = -1; // -1 No connection, 0 Usb , 1 Wifi
    public Context global_context;
    public boolean isStartHandle = false;
    SharedPreferences sp;
    Button btnMode1, btnMode2, btnMode3, btnMode4, btnMode5, btnMode6, btnMode7, btnMode8, btnWifi, btnUSB, btnWifiList
     ,btnNum0,btnNum1,btnNum2,btnNum3,btnNum4,btnNum5,btnNum6,btnNum7,btnNum8,btnNum9,btnNumA,btnNumB,btnNumC,btnNumD,btnNumE,btnNumF
     ,btnNumcl,btnNumok ,but_alarm1,but_alarm2,but_alarm3,but_alarm4,but_alarmEx;
    ToggleButton DistanceL;
    Button btnNumCcl,btnNumCok;
    Button butDison,butDisoff;
    EditText edit_ack, edit_reply ,Pass_config;
    ImageButton btnLog, btnConfig, btnExpand;
    ImageView door1, door2, door3, door4, door5,door6, person1, person2, person3, engine;
    ImageView imgCabLock;
    ImageView imgVaultLock;
    ImageView imgRearLock;
    ImageView imgDrvLock;
    ImageView imgPsgLock;
    RelativeLayout carLayout;
    LinearLayout layoutLogDisplay, layoutParentCar, layoutPassword;
    private CountDownTimer countDownTimer;
    TextView countDownTime;
    Thread readThread;
    boolean isShowLog;
    Animation door1O, door1C, door2O, door2C, door3O, door3C, door4C, door4O, door5O, door5C,door6O, door6C,brilnk;
    int baudRate; /* baud rate */
    byte stopBit; /* 1:1stop bits, 2:2 stop bits */
    byte dataBit; /* 8:8bit, 7: 7bit */
    byte parity; /* 0: none, 1: odd, 2: even, 3: mark, 4: space */
    byte flowControl; /* 0:none, 1: flow control(CTS,RTS) */
    InterfaceFT311 FT311;
    Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    int[] actualNumBytes;

    final static int MAXIMUM_CREW = 4;

    boolean isRuning1,isRuning2,isRuning3,isStatD,isStatP,isStatS,isStatC,isStatV,isStatR;

    int[] countCrewR = new int[MAXIMUM_CREW];
    int[] countCrewOF = new int[MAXIMUM_CREW];

    byte[] readBuffer;
    char[] readBufferToChar;
    int currentMode = 0;
    byte status;

   int ModeRTB =0;
    ProgressDialog progress;
    int percent = 0;
    boolean buttonClickable = true;
    String replyNum;
    String buferDataBase="";
    int currentClick;
    TableLayout table;
    String dataOut = "";
    private int connectionType; //USB = 2 Wifi = 1
    private boolean isUSBConnect;
    private Socket socket = null;
    private BufferedReader bufferRead = null;
    private Object _locker = new Object();
    private UsbDevice device = null;
    private UsbEndpoint usbEndpointIn = null;
    private UsbManager mUsbManager;
    private boolean statusExpandText = false;
    private int securityLevel = 0;
    String[] dataReadFile;

    final private int MAXIMUM_LOG_VIEW_COUNT = 500;

     Dialog pup ;
     Dialog pupCon ;

    RR2F rr2f = new RR2F();
    public static DoorAccess current_instance;

    //RR2F Debug
    TextView debugText;
    int count=0;

    private void DEBUG_PRINT(String tag, String fmt, Object... args)
    {
        Log.d(tag, String.format(fmt, args));
    }

    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            byte[] data = new byte[actualNumBytes[0]];
            System.arraycopy(readBuffer,0,data,0,actualNumBytes[0]);

            String dbg_msg = String.format("RR2F_INPUT %d bytes : ", data.length);
            for (int i=0; i<data.length; i++) {
                dbg_msg += String.format("%x ", data[i]);
            }
            DEBUG_PRINT("RR2F_INPUT", dbg_msg);

            rr2f.dataInput(data);
        }
    };

    private void setViewVisibility_CrossThread(final View view, final int visibility)
    {
        view.post(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(visibility);
            }
        });
    }

    private void ConnectionTypeCheck(int type) {
        GlobalVariable.connectionType = type;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        //  getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        //         WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_door_access);
        sp = getSharedPreferences("DoorAccessPreference", Context.MODE_PRIVATE);
        InitButtonDeclare();
        InitImageDeclare();
        InitLayoutDeclare();

        InitAnimation();
        InitSharedPreferences();
        ConnectionTypeCheck(-1);
        // ConnectionOpen();
        ConnectionUSB();
        LogRefresh();
        ButtonColor(0);
        initIs();

        rr2f.registerHighlevelCallback(this);
        rr2f.registerLowlevelCallback(this);


        isStartHandle = true;
        handlerThread = new handler_thread(handler);
        handlerThread.start();


        if (GlobalVariable.runProgramFirst == true) {
            CheckSharedPreferences();
        }
        ShowTextState(1);
        current_instance=this;

        setDoorLock(GlobalVariable.DOOR_LOCK_DRIVER, false);
        setDoorLock(GlobalVariable.DOOR_LOCK_PASSENGER, false);
        setDoorLock(GlobalVariable.DOOR_LOCK_SIDE, false);
        setDoorLock(GlobalVariable.DOOR_LOCK_CAB, false);
        setDoorLock(GlobalVariable.DOOR_LOCK_VAULT, false);
        setDoorLock(GlobalVariable.DOOR_LOCK_REAR, false);
    }

    public void SendUartFromExtClass(String data){
        current_instance.SendUART(data);
    }

    public void InitSharedPreferences() {
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("isUnplug", false);
        editor.commit();
    }

    public void CheckSharedPreferences() {
        if (sp.getInt("ConnectType", -1) == 1)
        {
            GlobalVariable.connectionType = 1;
            FT311.ResumeConnection();
        }
        currentClick = sp.getInt("Mode", -1);
        modeClick(currentClick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    String result = data.getStringExtra("result");
                    //    SendUART(result);
                } catch (Exception e) {
                    Log.d("Exception", e.getMessage());
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    private void RandomACK() {
        Random r = new Random();
        int i1 = r.nextInt(999999 - 100000) + 100000;
        int i2 = (i1 * 255) + 31;
        String replyString = Integer.toString(i2);
        replyNum = replyString.substring(replyString.length() - 6, replyString.length());
        //  UIToast(replyNum);
        edit_ack.setText("" + i1);
        edit_reply.setText("");
       // AddToDatabase("Pass" + i1);

    }


//    private void RunThread(String message) {
//        if (GlobalVariable.connectionType == 1) {
//            if (!running) {
//                running = true;
//                Thread communicate = new Thread(new SendThread(message));
//                communicate.start();
//            }
//        } else if (GlobalVariable.connectionType == 2) {
//            bulkDataSent(message);
//        }
//    }

    public void ConnectionUSB() {
      //  baudRate = 9600;
        baudRate = 115200;
        stopBit = 1;
        dataBit = 8;
        parity = 0;
        flowControl = 0;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        mUsbManager = (UsbManager) this.getSystemService(this.USB_SERVICE);
//        UsbAccessory[] accessories = mUsbManager.getAccessoryList();
//        UsbAccessory accessory = (accessories == null ? null : accessories[0]);
//        if (accessory != null) {
//
//        } else {
        FT311 = new InterfaceFT311(this);
        FT311.setConnectionInterface(this);
        readBuffer = new byte[4096];
        readBufferToChar = new char[4096];
        actualNumBytes = new int[1];
//        }
//        handlerThread = new handler_thread(handler);
//        handlerThread.start();

    }
/*
    public void SendUART(String writeText) {
        byte[] writeBuffer;
        writeBuffer = new byte[64];
        int numBytes;
        String srcStr = writeText + new String(new char[]{13});
//        String srcStr = writeText;
        String destStr = srcStr;
        numBytes = destStr.length();
        for (int i = 0; i < numBytes; i++) {
            writeBuffer[i] = (byte) destStr.charAt(i);
        }
        FT311.SendData(numBytes, writeBuffer);

    }
*/

    /****************************************
     * Implement SendUart with RR2F Protocol
     ****************************************/

    public void SendUART(String writeText) {

        //int len = writeText.length()+1;
        //byte a[] = new byte[len];

        DEBUG_PRINT("RR2F_TX", "Tx %d bytes :%s.\r\n ", writeText.length(), writeText);

        rr2f.write(writeText+"\r");

    }


    private void InitLayoutDeclare() {
        carLayout = (RelativeLayout) findViewById(R.id.carLayout);
        layoutLogDisplay = (LinearLayout) findViewById(R.id.layoutLogDisplay);
        layoutParentCar = (LinearLayout) findViewById(R.id.layoutParentCar);
        ///PupUpKeyPass
        pup = new Dialog(this);
        pup.setContentView(R.layout.pupuppass);

        pupCon = new Dialog(this);
        pupCon.setContentView(R.layout.passconfiglayout);



       // layoutPassword = (LinearLayout) findViewById(R.id.layoutPassword);
    }

    private void initIs()
    {
        isRuning1=false;
        isRuning2=false;
        isStatC=false;
        isStatD=false;
        isStatP=false;
        isStatS=false;
        isStatV=false;
        isStatR=false;

    }
    private void InitAnimation() {
        door1O = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.door2o);
        door1C = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.door2c);
        door2O = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.door1o);
        door2C = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.door1c);
        door3O = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.door3o);
        door3C = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.door3c);
        door4O = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.door4o);
        door4C = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.door4c);
        door5O = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.door5o);
        door5C = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.door5c);
        door6O = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.door6o);
        door6C = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.door6c);

        brilnk = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.brlink);

        door1O.setFillAfter(true);

        door1C.setFillAfter(true);
        door2O.setFillAfter(true);
        door2C.setFillAfter(true);
        door3O.setFillAfter(true);
        door3C.setFillAfter(true);
        door4O.setFillAfter(true);
        door4C.setFillAfter(true);
        door5O.setFillAfter(true);
        door5C.setFillAfter(true);
        door6O.setFillAfter(true);
        door6C.setFillAfter(true);
        brilnk.setFillAfter(true);
    }

    private void InitImageDeclare() {
      // door1 = (ImageView) findViewById(R.id.door1);
//        door2 = (ImageView) findViewById(R.id.door2);
//        door3 = (ImageView) findViewById(R.id.door3);
//        door4 = (ImageView) findViewById(R.id.door4);
//        person1 = (ImageView) findViewById(R.id.person1);
//        person2 = (ImageView) findViewById(R.id.person2);
//        person3 = (ImageView) findViewById(R.id.person3);
        door1 = (ImageView) findViewById(R.id.door1);
        door2 = (ImageView) findViewById(R.id.door2);
        door3 = (ImageView) findViewById(R.id.door3);
        door4 = (ImageView) findViewById(R.id.door4);
        door5 = (ImageView) findViewById(R.id.door5);
        door6 = (ImageView) findViewById(R.id.door6);

        engine = (ImageView) findViewById(R.id.engine);

        imgCabLock = (ImageView)findViewById(R.id.imgCabLock);
        imgVaultLock = (ImageView)findViewById(R.id.imgVaultLock);
        imgRearLock = (ImageView)findViewById(R.id.imgRearLock);
        imgDrvLock = (ImageView)findViewById(R.id.imgDrvLock);
        imgPsgLock = (ImageView)findViewById(R.id.imgPsgLock);
    }

public void AnimaDoor(int i)
{

    switch (i){

        case 1 :

            door1.startAnimation(door1O);
           AddToDatabase("Driver door open");
            break;

        case 2 :
            door2.startAnimation(door2O);
            AddToDatabase("Passenger door open");
            break;

        case 3 :
            door3.startAnimation(door3O);
            AddToDatabase("Side door open");
            break;

        case 4 :
            door4.startAnimation(door4O);
            AddToDatabase("Cab door open");
            break;

        case 5 :
            door5.startAnimation(door5O);
          AddToDatabase("Vault door open");
            break;

        case 6 :
            door1.startAnimation(door1C);
            AddToDatabase("Driver door close");
            break;
        case 7 :
            door2.startAnimation(door2C);
            AddToDatabase("Passenger door close");
            break;
        case 8 :
            door3.startAnimation(door3C);
            AddToDatabase("Side door close");
            break;
        case 9 :
            door4.startAnimation(door4C);
            AddToDatabase("Cab door close");
            break;
        case 10 :
            door5.startAnimation(door5C);
            AddToDatabase("Vault door close");
            break;
        case 11:
            but_alarm4.setVisibility(View.VISIBLE);
            but_alarm4.setText("Please Close and lock Door");
            but_alarm4.startAnimation(brilnk);
            break;
        case 12:
            but_alarm3.setVisibility(View.VISIBLE);
            but_alarm3.setText("Not enough crew");
            but_alarm3.startAnimation(brilnk);
            break;

        case 13:
            if(but_alarm3.isShown()) {
                but_alarm3.setVisibility(View.INVISIBLE);
                but_alarm3.clearAnimation();
            }
            break;
        case 14:
            if(but_alarm4.isShown()) {
                but_alarm4.setVisibility(View.INVISIBLE);
                but_alarm4.clearAnimation();
            }
            break;

        case 15:
            door6.startAnimation(door6O);
            AddToDatabase("Rear door open");
            break;
        case 16:
            door6.startAnimation(door6C);
            AddToDatabase("Rear door close");
            break;


        default:
            break;


    }


}

    private void InitButtonDeclare() {
        btnMode1 = (Button) findViewById(R.id.btn_mode1);
        btnMode2 = (Button) findViewById(R.id.btn_mode2);
        btnMode3 = (Button) findViewById(R.id.btn_mode3);
        btnMode4 = (Button) findViewById(R.id.btn_mode4);
        btnMode5 = (Button) findViewById(R.id.btn_mode5);
        btnMode6 = (Button) findViewById(R.id.btn_mode6);
        btnMode7 = (Button) findViewById(R.id.btn_mode7);
        btnMode8 = (Button) findViewById(R.id.btn_mode8);


        btnLog = (ImageButton) findViewById(R.id.btn_log);
        btnWifi = (Button) findViewById(R.id.btn_wifi);
        btnWifiList = (Button) findViewById(R.id.btn_wifilist);
        btnUSB = (Button) findViewById(R.id.btn_usb);
        btnExpand = (ImageButton) findViewById(R.id.btn_expand);
        btnConfig = (ImageButton) findViewById(R.id.btn_config);

        but_alarm1 =(Button) findViewById(R.id.but_alarmA);
        but_alarm2 =(Button) findViewById(R.id.but_alarmB);
        but_alarm3 =(Button) findViewById(R.id.but_alarmC);
        but_alarm4 =(Button) findViewById(R.id.but_alarmD);
        but_alarmEx = (Button) findViewById(R.id.but_alarmEX);

        but_alarm2.setVisibility(View.VISIBLE);
        but_alarm1.setVisibility(View.VISIBLE);
        but_alarmEx.setVisibility(View.VISIBLE);
        but_alarm3.setVisibility(View.INVISIBLE);
        but_alarm4.setVisibility(View.INVISIBLE);
        but_alarm2.setText("Crew on 0");
        but_alarm1.setText("Crew Reg 0");
        setOdometer(false);

       // DistanceL =(ToggleButton) findViewById(R.id.toggleDistance);


            btnMode1.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    modeClick(1);
                }
            });
            btnMode2.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    modeClick(2);
                }
            });
            btnMode3.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    modeClick(3);
                }
            });
            btnMode4.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    modeClick(4);
                }
            });
            btnMode5.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    modeClick(5);
                }
            });
            btnMode6.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    modeClick(6);
                }
            });
            btnMode7.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    modeClick(7);
                }
            });
            btnMode8.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ConfigMenuPopupRTB();
               //     modeClick(8);
                }
            });


        btnLog.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                 if (isShowLog)
                    ShowTextState(1);
                else {
                    ShowTextState(3);
                    statusExpandText = false;
                }
                isShowLog = !isShowLog;
            }
        });
        btnWifi.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (connectionType != 1) {
                    Thread clientThread = new Thread(new ClientThread());
                    clientThread.start();
                    connectionType = 1;
                }
            }
        });
        btnUSB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    //ConnectionUSB();

                    boolean check = FT311.ResumeConnection();
                    if (!check) {
                        ConnectionUSB();
                    }
                    //  FT311.SetConfig(baudRate, dataBit, stopBit, parity, flowControl);
                } catch (Exception a) {
                }
            }
        });
//        btnWifiList.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                SendDataToServer();
//            }
//        });
        btnExpand.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                statusExpandText = !statusExpandText;
                if (statusExpandText) {
                    ShowTextState(1);
                } else {
                    ShowTextState(2);
                }

            }
        });
        btnConfig.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ConfigMenuPopup();
            }
        });
        /*

        DistanceL.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (DistanceL.isChecked()) {

                    SendUART("SET:DIST:ON");
                } else {
                    SendUART("SET:DIST:OFF");
                }

            }
        });
*/
    }

//    public void readJSONFeed(String http) {
//        String response = null;
//        try {
//            DefaultHttpClient httpClient = new DefaultHttpClient();
//            HttpEntity httpEntity = null;
//            HttpResponse httpResponse = null;
//            HttpPost httpGet = new HttpPost(http);
//            httpResponse = httpClient.execute(httpGet);
//            httpEntity = httpResponse.getEntity();
//            response = EntityUtils.toString(httpEntity);
//            UIToast(response);
//        } catch (Exception e) {
//            UIToast(e.getMessage());
//        }
//    }


    public void setOdometer(boolean state){

        if(state){
            but_alarmEx.setText(R.string.odometer_active);
            but_alarmEx.setBackgroundResource(R.drawable.circle_green);
        }else {
            but_alarmEx.setText(R.string.odometer_inactive);
            but_alarmEx.setBackgroundResource(R.drawable.alarmred);
        }

    }



    public void UploadBar() {
        progress = new ProgressDialog(this);
        progress.setCancelable(false);
        progress.setMessage("Uploading Logfiles");
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setProgress(0); // call these two methods before setting progress.
        //    progress.setIndeterminate(true);
        progress.setMax(100);
        progress.show();
        Thread background = new Thread(new Runnable() {
            public void run() {
                try {
                    while (percent < 100) {
                        progress.setProgress(percent);
                    }
                    progress.dismiss();
                } catch (Exception e) {

                }
            }
        });
        background.start();
    }

    public void writeData(String http) {
        List<LogItems> itemList = myDb.SelectData();
        final int countItem = itemList.size();
        int i = 0;
        percent = 0;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UploadBar();
            }
        });
        String response;
        for (LogItems item : itemList) {
            try {
                i++;
                percent = (int) Math.ceil(((double) i / countItem) * 100);
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpEntity httpEntity = null;
                HttpResponse httpResponse = null;
                HttpPost request = new HttpPost(http);
                JSONStringer userJson = new JSONStringer()
                        .object()
                        .key("item").object().key("ID").value(item.id).key("Message").value(item.message).key("DateTime").value(formatter.format(item.date)).endObject()
                        .endObject();
                StringEntity entity = new StringEntity(userJson.toString(), HTTP.UTF_8);
                entity.setContentType("application/json;charset=UTF-8");
                request.setEntity(entity);
                httpResponse = httpClient.execute(request);
                httpEntity = httpResponse.getEntity();
                response = EntityUtils.toString(httpEntity);
                //  UIToast(response);
            } catch (Exception e) {
                //   UIToast(e.getMessage());
            }
        }

    }

    private void SendDataToServer() {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    writeData("http://10.52.60.225/androidservice/service1.svc/insert");
                } catch (Exception e) {
                    // UIToast(e.getMessage());
                }
            }
        });
        thread.start();
    }

    private void ButtonColor(int buttonState) {


            btnMode1.setBackgroundResource(R.drawable.button01);
            btnMode2.setBackgroundResource(R.drawable.button01);
            btnMode3.setBackgroundResource(R.drawable.button01);
            btnMode4.setBackgroundResource(R.drawable.button01);
            btnMode5.setBackgroundResource(R.drawable.button01);
            btnMode6.setBackgroundResource(R.drawable.button01);
            btnMode7.setBackgroundResource(R.drawable.button01);
            btnMode8.setBackgroundResource(R.drawable.button01);

            if (buttonState == 1) {
                btnMode1.setBackgroundResource(R.drawable.button02);
            }
            if (buttonState == 2) {
                btnMode2.setBackgroundResource(R.drawable.button02);

            }
            if (buttonState == 3) {
                btnMode3.setBackgroundResource(R.drawable.button02);

            }
            if (buttonState == 4) {
                btnMode4.setBackgroundResource(R.drawable.button02);

            }
            if (buttonState == 5) {
                btnMode5.setBackgroundResource(R.drawable.button02);

            }
            if (buttonState == 6) {
                btnMode6.setBackgroundResource(R.drawable.button02);

            }
            if (buttonState == 7) {
                btnMode7.setBackgroundResource(R.drawable.button02);

            }
            if (buttonState >= 8) {
                btnMode8.setBackgroundResource(R.drawable.button02);
                btnMode8.setText(protocol.GetMode(buttonState));

            }
    }

    private Boolean AnswerPasswordConfig()
    {
        String PaasConfig = Pass_config.getText().toString();
        if(PaasConfig.equals(GlobalVariable.PassworkConfig))
        {

            return true ;
        }
        else {
            UIToast("Wrong Password");
            return false;
        }
    }
    private Boolean AnswerPassword() {
     String inpucode =GlobalVariable.KeyNumber + protocol.GetMode(currentMode)+protocol.GetMode(currentClick)+edit_ack.getText().toString();// = currentMode + currentClick+gen 6 digi
     String secrerkey =EncryptionClass.StringToHex(GlobalVariable.VehicleNo); //vehiclee

        String Encode = EncryptionClass.encryptBlowFish(secrerkey,inpucode).toUpperCase();
        String shoustr = Encode.substring(Encode.length() - 6);
        String reply_key = edit_reply.getText().toString();
          if (reply_key.equals(shoustr))
          {

            AddToDatabase("Pass OK Reply:" + edit_reply.getText() + " ASC: " + replyNum);
            SendUART(protocol.CodePass(protocol.GetMode(currentClick))); //send code pass
             //  modeChange();
              return  true ;
        }
          else {

            UIToast("Wrong Password");
            AddToDatabase("Wrong Password");
            RestorePassword();
              return  false;
        }
    }
    private void RestorePassword() {
     //   replyNum = "9999999";
     //   RandomACK("ChangePassword");
        edit_reply.setText("");
    }
    public void passwordOnClick(View v) {
        final int id = v.getId();
        if (edit_reply.getText().length() < 6) {
            switch (id) {
                case R.id.but_0:
                    edit_reply.setText(edit_reply.getText() + "0");
             //       edit_reply.setSelection(editText.getText().length());

                    break;
                case R.id.but_1:
                    edit_reply.setText(edit_reply.getText() + "1");
                    break;
                case R.id.but_2:
                    edit_reply.setText(edit_reply.getText() + "2");
                    break;
                case R.id.but_3:
                    edit_reply.setText(edit_reply.getText() + "3");
                    break;
                case R.id.but_4:
                    edit_reply.setText(edit_reply.getText() + "4");
                    break;
                case R.id.but_5:
                    edit_reply.setText(edit_reply.getText() + "5");
                    break;
                case R.id.but_6:
                    edit_reply.setText(edit_reply.getText() + "6");
                    break;
                case R.id.but_7:
                    edit_reply.setText(edit_reply.getText() + "7");
                    break;
                case R.id.but_8:
                    edit_reply.setText(edit_reply.getText() + "8");
                    break;
                case R.id.but_9:
                    edit_reply.setText(edit_reply.getText() + "9");
                    break;
                case R.id.but_A:
                    edit_reply.setText(edit_reply.getText() + "A");
                    break;
                case R.id.but_B:
                    edit_reply.setText(edit_reply.getText() + "B");
                    break;
                case R.id.but_C:
                    edit_reply.setText(edit_reply.getText() + "C");
                    break;
                case R.id.but_D:
                    edit_reply.setText(edit_reply.getText() + "D");
                    break;
                case R.id.but_E:
                    edit_reply.setText(edit_reply.getText() + "E");
                    break;
                case R.id.but_F:
                    edit_reply.setText(edit_reply.getText() + "F");
                    break;
                case R.id.but_cl:
                    RestorePassword();
                    break;
                default:
                    break;
            }
        } else {

            switch (id) {
                    case R.id.but_cl:
                    edit_reply.setText("");
                    break;


                default:
                    break;
            }
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Toast.makeText(this, item.getTitle(), Toast.LENGTH_LONG).show();
        return true;
    }

    public void ConfigMenuPopupRTB() {
        PopupMenu popupRTB = new PopupMenu(DoorAccess.this, btnMode8);
        popupRTB.getMenuInflater()
                .inflate(R.menu.menu_rtb, popupRTB.getMenu());

                 popupRTB.getMenu().getItem(ModeRTB).setChecked(true);

                popupRTB.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {


                        switch (item.getItemId())

                        {
                            case R.id.menu_RTB1:
                                modeClick(8);
                                break;
                            case R.id.menu_RTB2:

                                modeClick(9);

                                break;
                            case R.id.menu_RTB3:

                                modeClick(10);

                                break;
                            case R.id.menu_RTB4:

                                modeClick(11);
                                break;
                            case R.id.menu_RTB5:

                                modeClick(12);
                                break;
                            case R.id.menu_RTB6:

                                modeClick(13);

                                break;

                            default:

                                break;
                        }

                        return true;
                    }
                });

        popupRTB.show();
    }


    public void ConfigMenuPopup() {
        PopupMenu popup = new PopupMenu(DoorAccess.this, btnConfig);
        popup.getMenuInflater()
                .inflate(R.menu.menu_config, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.hasSubMenu()) {   //radio sub menu here
                    SubMenu sub = item.getSubMenu();

                    MenuItem subMenu;
                    if (GlobalVariable.connectionType == 1) { //Usb
                        subMenu = sub.getItem(1);
                        subMenu.setChecked((GlobalVariable.connectionType == 1));

                    } else if (GlobalVariable.connectionType == 2) { //Wifi
                        subMenu = sub.getItem(2);
                        subMenu.setChecked(true);
                    }
                }
                switch (item.getItemId()) {
                    case R.id.menu_usb:
                        try {
                            //  boolean check =
                            if ((GlobalVariable.connectionType != 1)) {
                                FT311.ResumeConnection();
                            } else {
                                rr2f.startPing();
                                if (GlobalVariable.SetFist == true) {
                                    if ((GlobalVariable.VehicleNo != "") && (!(GlobalVariable.VehicleNo.contains("00000")))) {

                                        SendUART("SET:VEHN:" + GlobalVariable.VehicleNo + "");
                                    } else {
                                        SendUART("SET:VEHN:?");
                                    }
                                } else {
                                    SendUART("SET:VEHN:?");
                                }
                            }

                        } catch (Exception a) {
                        }
                        break;
                    case R.id.menu_wifi:

                        break;
                    case R.id.menu_senddata:
                        SendDataToServer();
                        break;
                    case R.id.menu_savetxtfile:
                        writeToFile();
                        break;
                    case R.id.menu_configure:
                        if ((GlobalVariable.SetFist == false) && (GlobalVariable.ReadFileConfig == true)) {
                            pupConclassini();
                        } else
                        {
                           OpenConfigure();
                        }


                        break;
                    default:
                        GlobalVariable.connectionType = GlobalVariable.connectionType;
                        break;
                }
                return true;
            }
        });

        popup.show();
    }

    private void writeToFile() {
        try {
            List<LogItems> itemList = myDb.SelectData();
            String data = "";
            if (itemList == null) {
                UIToast("Not found data");
            }
             else
            {
                for (LogItems item : itemList) {
                    data = data + formatter.format(item.date) + "\t" + item.message + "\r\n";
                }
            }
            File myFile = new File(Environment.getExternalStorageDirectory()+"/TPsecurity/logfiletp.txt");
            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile);
            OutputStreamWriter myOutWriter =
                    new OutputStreamWriter(fOut);
            myOutWriter.write(data);
            myOutWriter.close();
            fOut.close();
            Log.d("File", "OK");
        } catch (IOException e) {
            Log.d("File", e.getMessage());
        }
    }

    private String fileToRead(String name)
    {
        File myFile = new File(Environment.getExternalStorageDirectory()+"/TPsecurity/Config_"+name+".txt");

        String mline ="";
      if(!(name.contains("00000")))
      {
          if (myFile.isFile()) {

              try {
                  FileInputStream fileIn = new FileInputStream(myFile);

                  BufferedReader reader = new BufferedReader(new InputStreamReader(fileIn));
                  String line;

                  while ((line = reader.readLine()) != null)
                      mline = mline + line;

              } catch (IOException e) {
                  e.printStackTrace();
              }
          } else {
              mline = "null";
          }
      }else
      {
         // myFile.delete();
          mline = "null";
      }
        return mline;
    }

    public void AppendLogfile(String text) {
        TableRow row = (TableRow) LayoutInflater.from(this).inflate(R.layout.list_row_data, null);
        ((TextView) row.findViewById(R.id.row_data_date)).setText(getDateTime());
        ((TextView) row.findViewById(R.id.row_data_message)).setText(text);
        table.addView(row);

        int row_count = table.getChildCount();

        if (row_count > MAXIMUM_LOG_VIEW_COUNT) {
            table.removeViewAt(0);
        }

        LogFoscusDown();
    }

    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }


    public void LogFoscusDown() {
        findViewById(R.id.scrollView).post(new Runnable() {
            public void run() {
                ((ScrollView) findViewById(R.id.scrollView)).fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private void LogRefresh() {
        try {

            Date dateOne = new Date();


            table = (TableLayout) findViewById(R.id.tableDisplay);
            table.removeAllViews();
            List<LogItems> itemList = myDb.SelectLastRecord(MAXIMUM_LOG_VIEW_COUNT);
            if (itemList == null) {
                UIToast("Not found data");
            } else {
                for (LogItems item : itemList) {
                    TableRow row = (TableRow) LayoutInflater.from(this).inflate(R.layout.list_row_data, null);
                    ((TextView) row.findViewById(R.id.row_data_date)).setText(formatter.format(item.date));
                    ((TextView) row.findViewById(R.id.row_data_message)).setText(item.message);
                    table.addView(row);
                }
                Date dateTwo = new Date();
                long timeDiff = Math.abs(dateOne.getTime() - dateTwo.getTime());
                Log.d("ER", "Dif2" + timeDiff);
                LogFoscusDown();
            }
        } catch (Exception e) {
            Log.d("ER", e.getMessage());
        }
    }

    private void AddToDatabase(final String text) {

        AppendLogfile(text);
        Runnable r = new DbRunAble(text);
        new Thread(r).start();

//        long flg = myDb.InsertData(text);
//        if (flg > 0) {
//            AppendLogfile();
//            buferDataBase = "";
//        } else {
//            ToastMessageShort(" Fail Add data to Database");
//        }

    }

    private void DeleteAllLog() {
        myDb.DeleteAllData();
        LogRefresh();
    }


    //======== Implement AsyncTask Use For DB Insert ========

    private class DbRunAble implements Runnable {

        private String g_text;

        @Override
        public void run() {
            synchronized (myDb) {
                long flg = myDb.InsertData(g_text);
                if (flg > 0) {
                    buferDataBase = "";
                } else {
                }
            }
        }

        public DbRunAble(String text) {
            g_text = text;

        }

    }


public void pupupclassini()
{


    btnNum0 = (Button) pup.findViewById(R.id.but_0);
    btnNum1 = (Button) pup.findViewById(R.id.but_1);
    btnNum2 = (Button) pup.findViewById(R.id.but_2);
    btnNum3 = (Button) pup.findViewById(R.id.but_3);
    btnNum4 = (Button) pup.findViewById(R.id.but_4);
    btnNum5 = (Button) pup.findViewById(R.id.but_5);
    btnNum6 = (Button) pup.findViewById(R.id.but_6);
    btnNum7 = (Button) pup.findViewById(R.id.but_7);
    btnNum8 = (Button) pup.findViewById(R.id.but_8);
    btnNum9 = (Button) pup.findViewById(R.id.but_9);
    btnNumA = (Button) pup.findViewById(R.id.but_A);
    btnNumB = (Button) pup.findViewById(R.id.but_B);
    btnNumC = (Button) pup.findViewById(R.id.but_C);
    btnNumD = (Button) pup.findViewById(R.id.but_D);
    btnNumE = (Button) pup.findViewById(R.id.but_E);
    btnNumF = (Button) pup.findViewById(R.id.but_F);

    btnNumcl = (Button) pup.findViewById(R.id.but_cl);
    btnNumok = (Button) pup.findViewById(R.id.but_ok);



    btnNum0.setBackgroundResource(R.drawable.buttonnum);
    btnNum1.setBackgroundResource(R.drawable.buttonnum);
    btnNum2.setBackgroundResource(R.drawable.buttonnum);
    btnNum3.setBackgroundResource(R.drawable.buttonnum);
    btnNum4.setBackgroundResource(R.drawable.buttonnum);
    btnNum5.setBackgroundResource(R.drawable.buttonnum);
    btnNum6.setBackgroundResource(R.drawable.buttonnum);
    btnNum7.setBackgroundResource(R.drawable.buttonnum);
    btnNum8.setBackgroundResource(R.drawable.buttonnum);
    btnNum9.setBackgroundResource(R.drawable.buttonnum);
    btnNumA.setBackgroundResource(R.drawable.buttonnum);
    btnNumB.setBackgroundResource(R.drawable.buttonnum);
    btnNumC.setBackgroundResource(R.drawable.buttonnum);
    btnNumD.setBackgroundResource(R.drawable.buttonnum);
    btnNumE.setBackgroundResource(R.drawable.buttonnum);
    btnNumF.setBackgroundResource(R.drawable.buttonnum);

    btnNumcl.setBackgroundResource(R.drawable.buttonnum);
    btnNumok.setBackgroundResource(R.drawable.buttonnum);

    btnNum0.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            passwordOnClick(v);
        }
    });
    btnNum1.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            passwordOnClick(v);
        }
    });
    btnNum2.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            passwordOnClick(v);
        }
    });
    btnNum3.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            passwordOnClick(v);
        }
    });
    btnNum4.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            passwordOnClick(v);
        }
    });
    btnNum5.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            passwordOnClick(v);
        }
    });
    btnNum6.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            passwordOnClick(v);
        }
    });
    btnNum7.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            passwordOnClick(v);
        }
    });
    btnNum8.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            passwordOnClick(v);
        }
    });
    btnNum9.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            passwordOnClick(v);
        }
    });
    btnNumA.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            passwordOnClick(v);
        }
    });
    btnNumB.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            passwordOnClick(v);
        }
    });
    btnNumC.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            passwordOnClick(v);
        }
    });
    btnNumD.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            passwordOnClick(v);
        }
    });
    btnNumE.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            passwordOnClick(v);
        }
    });
    btnNumF.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            passwordOnClick(v);
        }
    });
    btnNumcl.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            passwordOnClick(v);
        }
    });
    btnNumok.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            if(AnswerPassword())
            {
                timex.cancel();
                pup.dismiss();
            }

        }
    });

    edit_ack = (EditText) pup.findViewById(R.id.edit_ack);
    edit_reply = (EditText) pup.findViewById(R.id.edit_reply);
    countDownTime =(TextView) pup.findViewById(R.id.textView9);


    edit_ack.setOnFocusChangeListener(new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean b) {
            if (b) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edit_ack.getApplicationWindowToken(), 0);

            }

        }
    });

    edit_reply.setOnFocusChangeListener(new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean b) {
            if (b) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edit_reply.getApplicationWindowToken(), 0);

            }

        }
    });

    edit_ack.setCursorVisible(false);
    edit_reply.setCursorVisible(false);

        if(pup.isShowing())
        {
          //  countDownTimer.cancel();
            timex.cancel();
           pup.dismiss();

        }
    else
        {

            timex.cancel();
            timex.start();
            pup.show();
            //////////*******
         }

   }

    CountDownTimer timex = new CountDownTimer(125000,1000) {
        @Override
        public void onTick(long l) {
            countDownTime.setText("" + (l+1000)/1000);
        }
        @Override
        public void onFinish() {
            pup.dismiss();
            this.cancel();
        }
    };
    CountDownTimer tiDoor = new CountDownTimer(10000,1000) {
        @Override
        public void onTick(long l) {

        }
        @Override
        public void onFinish() {
          AnimaDoor(14);
            isRuning2=false;
            this.cancel();
        }
    };
    CountDownTimer tiCrew = new CountDownTimer(10000,1000) {
        @Override
        public void onTick(long l) {

        }
        @Override
        public void onFinish() {
            AnimaDoor(13);
            isRuning1=false;
            this.cancel();
        }
    };
    CountDownTimer tiCrewEQ = new CountDownTimer(300000,6000) {
        @Override
        public void onTick(long l) {
            SendUART("SET:CREWREG:?");
        }
        @Override
        public void onFinish() {

            isRuning3=false;
            this.cancel();
        }
    };


    public void pupConclassini()
    {

        btnNumCcl = (Button) pupCon.findViewById(R.id.Pass_butcl);
        btnNumCok = (Button) pupCon.findViewById(R.id.Pass_butok);

        btnNumCok.setBackgroundResource(R.drawable.buttonnum);
        btnNumCcl.setBackgroundResource(R.drawable.buttonnum);


        btnNumCcl.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               Pass_config.setText("");
            }
        });
        btnNumCok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(AnswerPasswordConfig())
                {

                    if(GlobalVariable.connectionType == 1) {
                        OpenConfigure();
                    }

                    pupCon.dismiss();

                }

            }
        });


        Pass_config = (EditText) pupCon.findViewById(R.id.PassC_EditText);

        Pass_config.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(Pass_config.getApplicationWindowToken(), 0);

                }

            }
        });
        Pass_config.setCursorVisible(false);
       if(pup.isShowing())
        {
            pupCon.dismiss();

        }
        else
        {
            Pass_config.setText("");
            pupCon.show();
            //////////*******
        }

    }
    public void UIReceive(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (message.contains("ACT:C")) {
                        modeClick(1);
                    } else if (message.contains("ACT:B")) {
                        modeClick(2);
                    }
                    if (message.contains("ACT:A")) {
                        modeClick(3);
                    }


                } catch (Exception e) {
                }
            }
        });
    }
    private void ShowTextState(int state) {
        LinearLayout.LayoutParams layoutLogP = (LinearLayout.LayoutParams) layoutLogDisplay.getLayoutParams();
        LinearLayout.LayoutParams layoutCar = (LinearLayout.LayoutParams) layoutParentCar.getLayoutParams();
      //  LinearLayout.LayoutParams layoutPass = (LinearLayout.LayoutParams) layoutPassword.getLayoutParams();
      //  layoutPassword.setVisibility(View.INVISIBLE);
      //  layoutPass.weight = 0f;
        if (state == 1) {
//            layoutLogP.weight = 0.1f;
//            layoutCar.weight = 0.8f;
            layoutLogP.height = 70;
            layoutCar.height = LinearLayout.LayoutParams.FILL_PARENT;
            layoutParentCar.setVisibility(View.VISIBLE);
            layoutLogDisplay.setVisibility(View.VISIBLE);
            btnLog.setBackgroundResource(R.drawable.logdisable);
            btnExpand.setBackgroundResource(R.drawable.zoomin);
        }
        if (state == 2) {
            layoutLogP.height = LinearLayout.LayoutParams.FILL_PARENT;
            layoutCar.height = 0;
//            layoutLogP.weight = 0.9f;
//            layoutCar.weight = 0f;
            layoutLogDisplay.setVisibility(View.VISIBLE);
            layoutParentCar.setVisibility(View.VISIBLE);
            btnLog.setBackgroundResource(R.drawable.logdisable);
            btnExpand.setBackgroundResource(R.drawable.zoomout);
        }
        if (state == 3) {
            layoutLogP.height = 70;
            layoutCar.height = LinearLayout.LayoutParams.FILL_PARENT;
//            layoutLogP.weight = 0.1f;
//            layoutCar.weight = 0.8f;
            layoutLogDisplay.setVisibility(View.INVISIBLE);
            layoutParentCar.setVisibility(View.VISIBLE);
            btnLog.setBackgroundResource(R.drawable.logenable);
            btnExpand.setBackgroundResource(R.drawable.zoomin);
        }
        if (state == 4) {
            layoutLogP.height = 70;
            layoutCar.height = 100;
          //  layoutPass.height= 250;
            layoutParentCar.setVisibility(View.VISIBLE);
         //   layoutPassword.setVisibility(View.VISIBLE);
        }
       layoutLogDisplay.setLayoutParams(layoutLogP);
       layoutParentCar.setLayoutParams(layoutCar);
     //  layoutPassword.setLayoutParams(layoutPass);
    }
    public void chendmode(String datacode)
    {

        if(datacode.contains("PARKING"))
        {
            currentClick = 3;}
        else if(datacode.contains("MAINTEN"))
        {
            currentClick = 6;}
        else if(datacode.contains("DEPO"))
        {
            currentClick = 2;}
        else if(datacode.contains("SECURE"))
        {
            currentClick = 1;}
        else if(datacode.contains("S2S"))
        {
            currentClick = 4;}

        else if(datacode.contains("RTB1"))
        {

            currentClick = 8;
            ModeRTB =1;
        }
        else if(datacode.contains("RTB2"))
        {

            currentClick = 9;
            ModeRTB =2;
        }
        else if(datacode.contains("RTB3"))
        {

            currentClick = 10;
            ModeRTB =3;
        }
        else if(datacode.contains("RTB4"))
        {

            currentClick = 11;
            ModeRTB =4;
                  }
        else if(datacode.contains("RTB5"))
        {

            currentClick = 12;
            ModeRTB =4;
                    }
        else if(datacode.contains("RTB6"))
        {

            currentClick = 13;
            ModeRTB =6;
                   }
        else if(datacode.contains("SOS"))
        {

            currentClick = 7;

        }
        else if(datacode.contains("CREW"))
        {

            currentClick = 5;

        }
    }
    public void ReceiveData(String datacode) {
        String[] strSplit = datacode.split(":");
        String bufAction = strSplit[0].toUpperCase().toString();
        String bufmode1 = strSplit[1].toUpperCase().toString();
        String bufValue1 = strSplit[2].toString();

          switch (bufAction) {
                case "MODE":
                    MODERec(bufmode1.toString(), bufValue1.toString());
                    break;
                case "SET":
                    setVariableGlobleR(bufmode1, bufValue1);
                    break;
                case "GET":
                    break;
            }

      //  Strdata.clear();
    }
    public String Spitvalue(String value)
    {
        String[] strSplit = value.split(":");
        String bufValue =strSplit[2].replace(" ", "").toString();
   return bufValue ;
    }
    public String Spitvalue2(String value)
    {
        String[] strSplit = value.split(":");
        String bufValue =strSplit[1].replace(" ", "").toString();
        return bufValue ;
    }
    public String[] Spitvalue3(String value)
    {
        String[] strSplit = value.split(",");

        return strSplit ;
    }
    public String[] Spitvalue4(String value)
    {
        String[] strSplit = value.split("#");

        return strSplit ;
    }



    private void MODERec(String mode,String valus)
    {
        switch (valus) {
            case "CHEC" :
                permissionMode();
                buferdata(false);
                break;

            case  "CHEP" :
                modeChange();
                buferdata(true);
                break;

            case "ACKP" :
                chendmode(mode);
                modeChange();
                buferdata(false);
                break;

            case "NACK" :
                modeNotChange();
                buferdata(false);
                break;

            default:
                if ( mode.contains("SOS") )
                {
                       ToastMessageShort("ADDdata");
                        AddToDatabase(valus.toString());
                }
                break;
        }
    }
    public void setVariableGlobleR( String mode ,String valus)//Read from fire
    {
        switch (mode)
        {

            case "ACC0" :

                if((valus.contains("ACKP")))
                {
                    GlobalVariable.Acceleration_l = Spitvalue(dataReadFile[GlobalVariable.CONFIG_FILE_ACCELERATION_INDEX].toString());
                    GlobalVariable.PassworkConfig = Spitvalue2(dataReadFile[GlobalVariable.CONFIG_FILE_PASSWORD_INDEX].toString());
                    GlobalVariable.ReadFileConfig = true;
                    GlobalVariable.SetFist = false;

                    SendUART("MODE:?");

                    ToastMessageShort("Success..setup");
                }
                else if(valus.contains("NACK"))
                {
                    if(GlobalVariable.Acceleration_l=="") {

                        SendUART(dataReadFile[GlobalVariable.CONFIG_FILE_ACCELERATION_INDEX].toString());
                    }
                }
                else{
                    setVariableGlobleG(1,valus.toString());
                }

                break;

            case  "VEHN":

                if(( valus.contains("ACKP"))||(valus.contains("NACK")))
                {
                    if(( valus.contains("ACKP")))
                    {
                        if(GlobalVariable.VehicleNo=="")
                        {
                            GlobalVariable.VehicleNo = Spitvalue(dataReadFile[GlobalVariable.CONFIG_FILE_VEHICLE_NUMNER_INDEX].toString());
                            SendUART(dataReadFile[GlobalVariable.CONFIG_FILE_DISTANCE_LONG_INDEX].toString());
                        }
                        else
                        {
                            String buferR = fileToRead( GlobalVariable.VehicleNo.toString());
                            String buferD = "";
                            if (buferR != "null")  //find File
                            {
                                String Hex = EncryptionClass.StringToHex(GlobalVariable.VehicleNo.toString());
                                buferD = EncryptionClass.decryptAES(Hex, buferR);
                                dataReadFile = buferD.split(";");
                                SendUART(dataReadFile[GlobalVariable.CONFIG_FILE_DISTANCE_LONG_INDEX].toString());
                            }
                            else
                            {
                                ToastMessageShort("@Error read file ");
                            }
                        }
                    }
                    else if((valus.contains("NACK")))
                    {

                        if(GlobalVariable.VehicleNo != "") {
                            SendUART("SET:VEHN:" + GlobalVariable.VehicleNo + "");
                        }
                        else{
                            SendUART(dataReadFile[GlobalVariable.CONFIG_FILE_ACCELERATION_INDEX].toString());
                        }
                    }
                }
                else
                {
                    GlobalVariable.VehicleNo = valus.replace(" ", "").replace(new char[]{13}.toString(), "").replace("\r", "").replace("\n", "").toUpperCase().toString();

                    if(GlobalVariable.VehicleNo.contains("00000"))
                    {

                        ToastMessageShort("Setup..first");
                        GlobalVariable.ReadFileConfig = false;
                        GlobalVariable.SetFist = true;
                    }else
                    {

                        String buferR = fileToRead(valus.toUpperCase());
                        String buferD = "";
                        if (buferR != "null")  //find File
                        {
                            String Hex = EncryptionClass.StringToHex(GlobalVariable.VehicleNo.toString());
                            buferD = EncryptionClass.decryptAES(Hex, buferR);
                            dataReadFile = buferD.split(";");

                            SendUART(dataReadFile[GlobalVariable.CONFIG_FILE_DISTANCE_LONG_INDEX].toString());
                        } else  //find No File
                        {
                            setVariableGlobleG(2,valus.toString());
                        }
                    }

                }

                break;
            case "DISH" :

                if(valus.contains("ACKP")) {
                    GlobalVariable.Distance_s = Spitvalue(dataReadFile[4].toString());
                    SendUART(dataReadFile[GlobalVariable.CONFIG_FILE_KEY_INDEX].toString());
                }
                else if(valus.contains("NACK"))
                {
                    SendUART(dataReadFile[GlobalVariable.CONFIG_FILE_DISTANCE_SHORT_INDEX].toString());
                }
                else
                {
                    setVariableGlobleG(3,valus.toString());
                }

                break;
            case "DIME" :

                if(valus.contains("ACKP")) {
                    GlobalVariable.Distance_m = Spitvalue(dataReadFile[3].toString());
                    SendUART(dataReadFile[GlobalVariable.CONFIG_FILE_DISTANCE_SHORT_INDEX].toString());
                }
                else if(valus.contains("NACK"))
                {
                    SendUART(dataReadFile[GlobalVariable.CONFIG_FILE_DISTANCE_MEDIUM_INDEX].toString());
                }
                else{
                    setVariableGlobleG(4,valus.toString());
                }

                break;
            case "DILO":

                if(valus.contains("ACKP")) {
                    GlobalVariable.Distance_l = Spitvalue(dataReadFile[2].toString());
                    SendUART(dataReadFile[GlobalVariable.CONFIG_FILE_DISTANCE_MEDIUM_INDEX].toString());
                }
                else if(valus.contains("NACK"))
                {
                    SendUART(dataReadFile[GlobalVariable.CONFIG_FILE_DISTANCE_LONG_INDEX].toString());
                }else
                {
                    setVariableGlobleG(5,valus.toString());
                }

                break;
            case "VKEY":

                if(valus.contains("ACKP")) {
                    GlobalVariable.KeyNumber = Spitvalue(dataReadFile[GlobalVariable.CONFIG_FILE_KEY_INDEX].toString());
                    SendUART(dataReadFile[GlobalVariable.CONFIG_FILE_RTB_INDEX].toString());
                }
                else if(valus.contains("NACK")) {
                    SendUART(dataReadFile[GlobalVariable.CONFIG_FILE_KEY_INDEX].toString());
                }
                else{
                    setVariableGlobleG(6,valus.toString());
                }
                break;
            case "RTB":
                if(valus.contains("ACKP")) {
                    GlobalVariable.RTBInfo = Spitvalue(dataReadFile[GlobalVariable.CONFIG_FILE_RTB_INDEX].toString());
                    SendUART(dataReadFile[GlobalVariable.CONFIG_FILE_ACCELERATION_INDEX].toString());
                }
                else if(valus.contains("NACK")) {
                    SendUART(dataReadFile[GlobalVariable.CONFIG_FILE_RTB_INDEX].toString());
                }
                else{
                    setVariableGlobleG(6,valus.toString());
                }

                break;
            case "CREW" :
                crewNOT(valus.toString());
                break;

            case "CREWREG" :

                crewR(valus.toString());

                break;
            case "CREWST":
                crewOF(valus.toString());
                break;

            case "DOORLOCK":
                setDoorLockHandler(valus);
                break;

            case "DOOR":
                DoorOpen(valus.toString());
                break;

            case "DOORALARM" :


                     DooralarmAct(valus.toString());

                break;

            case "ENGI":
                ENGi(valus.toUpperCase().toString());
                break;

            case "ODMA":
                if(valus.contains("ACTV")){
                    setOdometer(true);
                }else if(valus.contains("INAC")){
                    setOdometer(false);
                }
                break;

           default: ToastMessageShort("Error..Receive..Commamd ");

                break;

        }
    }
    private void setVariableGlobleG(int mode,String valus)// Get from control borad
    {
        if(mode==1)
        {
            GlobalVariable.Acceleration_l = valus.toString();


        }
        else if(mode==GlobalVariable.CONFIG_FILE_VEHICLE_NUMNER_INDEX)
        {
          GlobalVariable.VehicleNo = valus.toUpperCase().toString();
            SendUART("SET:DISH:?");
        }
        else if(mode==GlobalVariable.CONFIG_FILE_DISTANCE_SHORT_INDEX)
        {
            GlobalVariable.Distance_s = valus.toString();
            SendUART("SET:DIME:?");

        }
        else if(mode==GlobalVariable.CONFIG_FILE_DISTANCE_MEDIUM_INDEX)
        {
            GlobalVariable.Distance_m = valus.toString();
            SendUART("SET:DILO:?");
        }
        else if(mode==GlobalVariable.CONFIG_FILE_DISTANCE_LONG_INDEX)
        {
            GlobalVariable.Distance_l = valus.toString();
            SendUART("SET:VKEY:?");
        }
        else if(mode==6)
        {
            GlobalVariable.KeyNumber = valus.toString();
            GlobalVariable.SetFist=false;
            GlobalVariable.ReadFileConfig =false;
            SendUART("MODE:?");
            ToastMessageShort("Please..Config.Application ");
           // SendUART("SET:ACC0:?");
        }
        else if(mode==7)
        {

        }
        else if(mode==8)
        {


        }

    }

    private void crewR(String data)
    {
        int i = 0;
        int crewR_count = 0;

        String[] dataSpit = Spitvalue3(data);
        String[] crew = new String[MAXIMUM_CREW];

        for (i=0; i<MAXIMUM_CREW; i++) {
            countCrewR[i] = 0;
            crew[i] = dataSpit[i];
        }

        for (i=0; i<MAXIMUM_CREW; i++) {
            if (crew[i].contains("1")) {
                countCrewR[i] = 1;
            } else if (crew[i].contains("0")) {
                countCrewR[i] = 0;
            } else {

            }
        }

        for (i=0; i<MAXIMUM_CREW; i++) {
            crewR_count += countCrewR[i];
        }

        but_alarm1.setText("Crew Reg " + crewR_count);

        SendUART("SET:CREWST:?");
    }

    private void crewOF(String data)
    {
        int i = 0;
        int crewOF_count = 0;

        String[] dataSpit = Spitvalue3(data);
        String[] crew = new String[MAXIMUM_CREW];

        for (i=0; i<MAXIMUM_CREW; i++) {
            countCrewOF[i] = 0;
            crew[i] = dataSpit[i];
        }

        for (i=0; i<MAXIMUM_CREW; i++) {
            if (crew[i].contains("1")) {
                countCrewOF[i] = 1;
            } else if (crew[i].contains("0")) {
                countCrewOF[i] = 0;
            } else {

            }
        }

        for (i=0; i<MAXIMUM_CREW; i++) {
            crewOF_count += countCrewOF[i];
        }

        but_alarm2.setText("Crew ON "+crewOF_count);


        SendUART("SET:DOOR:?");

    }
    private void crewNOT (String data)
    {
        if(data.contains("NOT") && isRuning1==false)
        {
            isRuning1 = true;
            tiCrew.start();
            AnimaDoor(12);
        }
        else{
            isRuning1=false;
        }
        SendUART("SET:CREW:ACK");
    }

    private void setDoorLockHandler(String lock_str)
    {
        DoorLockInfo info = new DoorLockInfo(lock_str);

        setDoorLock(GlobalVariable.DOOR_LOCK_DRIVER, info.getDriverDoorLocked());
        setDoorLock(GlobalVariable.DOOR_LOCK_PASSENGER, info.getPassengerDoorLocked());
        setDoorLock(GlobalVariable.DOOR_LOCK_SIDE, info.getSideDoorLocked());
        setDoorLock(GlobalVariable.DOOR_LOCK_CAB, info.getCabinDoorLocked());
        setDoorLock(GlobalVariable.DOOR_LOCK_VAULT, info.getVaultDoorLocked());
        setDoorLock(GlobalVariable.DOOR_LOCK_REAR, info.getRearDoorLocked());
    }

    private  void DoorOpen(String door)
    {
      String[] dataSpit =  Spitvalue3(door);

        /* d1 is Passenger
           d2 is driver
         */
        String d2  = dataSpit[0];
        String d1  = dataSpit[1];

        String d3  = dataSpit[2];
        String d4  = dataSpit[3];
        String d5  = dataSpit[4];
        String d6  = dataSpit[5];

        if(d1.contains("1")&&(isStatD==true))
        {
            isStatD=false;
            AnimaDoor(6);

        }else if(d1.contains("0")&&(isStatD==false))
        {
            isStatD=true;
            AnimaDoor(1);
        }

        if(d2.contains("1")&&(isStatP==true))
        {
            isStatP=false;
            AnimaDoor(7);
        }else if(d2.contains("0")&&((isStatP==false)))
        {
            isStatP=true;
            AnimaDoor(2);
        }

        if(d3.contains("1")&&(isStatS==true))
        {
            isStatS=false;
            AnimaDoor(8);
        }else if(d3.contains("0")&&(isStatS==false))
        {
            isStatS=true;
            AnimaDoor(3);
        }

        if(d4.contains("1")&&(isStatC==true))
        {
            isStatC=false;
            AnimaDoor(9);
        }else if(d4.contains("0")&&(isStatC==false))
        {
            isStatC=true;
            AnimaDoor(4);
        }

        if(d5.contains("1")&&(isStatV==true))
        {
            isStatV=false;
            AnimaDoor(10);
        }else if(d5.contains("0")&&(isStatV==false))
        {
            isStatV=true;
            AnimaDoor(5);
        }
        if(d6.contains("1")&&(isStatR==true))
        {
            isStatR=false;
            AnimaDoor(16);
        }else if(d6.contains("0")&&(isStatR==false))
        {
            isStatR=true;
            AnimaDoor(15);
        }

    }

    private void setDoorLock(int target_door, boolean status)
    {
        switch (target_door) {
            case GlobalVariable.DOOR_LOCK_DRIVER :
                setViewVisibility_CrossThread(imgDrvLock, status ? View.VISIBLE : View.INVISIBLE);
                break;
            case GlobalVariable.DOOR_LOCK_PASSENGER :
                setViewVisibility_CrossThread(imgPsgLock, status ? View.VISIBLE : View.INVISIBLE);
                break;
            case GlobalVariable.DOOR_LOCK_SIDE :
                /* There are no lock on SideDoor. */
                break;
            case GlobalVariable.DOOR_LOCK_CAB :
                setViewVisibility_CrossThread(imgCabLock, status ? View.VISIBLE : View.INVISIBLE);
                break;
            case GlobalVariable.DOOR_LOCK_VAULT :
                setViewVisibility_CrossThread(imgVaultLock, status ? View.VISIBLE : View.INVISIBLE);
                break;
            case GlobalVariable.DOOR_LOCK_REAR :
                setViewVisibility_CrossThread(imgRearLock, status ? View.VISIBLE : View.INVISIBLE);
                break;
        }
    }

    private void DooralarmAct(String data)
    {
       if(data.contains("PCLOSE")&&isRuning2==false) {
           isRuning2=true;
           tiDoor.start();
           AnimaDoor(11);

       }
        else{
           isRuning2=false;
       }
        SendUART("SET:DOORALARM:ACK");

    }

    private void ENGi(String data)
    {

        if(data.contains("RED"))
        {
            engine.setBackgroundResource(R.drawable.engred);
        }
        else if(data.contains("ORG"))
        {
            engine.setBackgroundResource(R.drawable.engorange);

        }
        else if(data.contains("GRE"))
        {
            engine.setBackgroundResource(R.drawable.enggreen);

        }
        SendUART("SET:ENGI:ACK");
    }

    public void permissionMode() {
        try {
            ShowTextState(1);
            pupupclassini();
            RandomACK();

        } catch (Exception e)
        {
          UIToast(e.getMessage());
        }
    }

    public void  modeChange() {
        currentMode = currentClick;
          if(currentMode !=1)
          {
              AnimaDoor(14);
          }
        //String data = "";
       // data = protocol.RequestMode(protocol.GetMode(modeNo));
        //ButtonColor(modeNo);


        ButtonColor(currentMode);
        ShowTextState(1);
        AddToDatabase("MODE:" + protocol.GetMode(currentClick));

        Handler handlerGet = new Handler();
        handlerGet.postDelayed(new Runnable() {
            public void run() {
                if (currentMode == 1 || currentMode == 3 || currentMode == 4 || currentMode == 5 || currentMode >= 8) {
                    if (isRuning3 == false) {
                        isRuning3 = true;
                        tiCrewEQ.start();
                    } else {
                        tiCrewEQ.cancel();
                        tiCrewEQ.start();
                    }
                } else {
                    if (isRuning3 == true) {
                        tiCrewEQ.cancel();
                    }
                }
            }
        }, 2000);

       // return data;
    }
    public void  modeNotChange() {
       // currentMode = currentClick;
        //String data = "";
        // data = protocol.RequestMode(protocol.GetMode(modeNo));
        //ButtonColor(modeNo);
        ShowTextState(1);
        AddToDatabase(protocol.GetMode(currentMode));
        // return data;
    }


    public void modeClick(int buttonNo) {
        try {

            if ((buttonNo!= currentMode || buttonNo != currentClick)&&(GlobalVariable.ReadFileConfig == true)) {

                currentClick = buttonNo;
                   SendUART(protocol.RequestMode(protocol.GetMode(buttonNo)));
           }
        } catch (Exception e) {

        }
    }

    public boolean buferdata(boolean i)
    {

        String data = "";
        if(i==false)
        {
            data = protocol.RequestMode(protocol.GetMode(currentClick));
        }
        else
        {
            data = protocol.CodePass(protocol.GetMode(currentClick));
        }
        AddToDatabase(data);
        return  true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isStartHandle = false;
        FT311.DestroyAccessory(true);
        UIToast("Destroy");
    }

    private void ToastMessageShort(String message) {

        Toast.makeText(DoorAccess.this, message, Toast.LENGTH_SHORT).show();
    }


    public void OpenConfigure() {
        try {

            Intent myIntent = new Intent(DoorAccess.this, ConfigurePage.class);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("isLogin", true);
            editor.putInt("Mode", currentClick);
            editor.putInt("ConnectType", GlobalVariable.connectionType);
            editor.commit();
            GlobalVariable.runProgramFirst = true;
            GlobalVariable.FTDIGlob = FT311;
            DoorAccess.this.startActivity(myIntent);


        } catch (Exception e) {
            Log.e("Exception", "message", e);
        }
    }

    private void UIToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    ToastMessageShort(message);
                } catch (Exception e) {

                }
            }
        });
    }

    @Override
    public void onBackPressed() {
//        new AlertDialog.Builder(this)
//                .setTitle("Delete entry").setMessage("TEST").show();
    }

    @Override
    public void onPause() {
        super.onPause();
        SendUART("AAA");
        Log.d("test","PPP");
    }


    public class ReadThread implements Runnable {
        String input;

        @Override
        public void run() {
            try {
                bufferRead = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                input = bufferRead.readLine();
                UIReceive(input);
                waitRead = false;

            } catch (Exception e) {

            }
        }
    }

    public class SendThread implements Runnable {
        String message;

        SendThread(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            try {
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                isConnect = false;
                running = false;
                waitRead = false;
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {

            }
        }
    }

    public class ClientThread implements Runnable {
        @Override
        public void run() {
            while (GlobalVariable.connectionType == 2) {  //While true
                if (!isConnect) {
                    try {
                        String HOST = "172.20.10.12";
                        InetAddress serverAddr = InetAddress.getByName(HOST);
                        int PORT = 6000;
                        socket = new Socket(serverAddr, PORT);
                        isConnect = true;
                    } catch (Exception e) {

                    }
                }
                if (!waitRead) {
                    waitRead = true;
                    readThread = new Thread(new ReadThread());
                    readThread.start();

                }
            }
        }
    }

    private class handler_thread extends Thread {
        Handler mHandler;

        /* constructor */
        handler_thread(Handler h)
        {
            mHandler = h;
        }

        public void run() {
            Message msg;


            while (isStartHandle) {
                if (GlobalVariable.connectionType == 1) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                    }
                    try {
                        byte status = FT311.ReadData(4096, readBuffer, actualNumBytes);

                        if (status == 0x00 && actualNumBytes[0] > 0) {
                            synchronized (_locker) {
                                msg = mHandler.obtainMessage();
                                mHandler.sendMessage(msg);
                            }
                        }
                    } catch (Exception e) {
                        UIToast("Can't Read" + e.getMessage());
                    }
                }
            }
        }
    }

    /************************************************************
     *       RR2F.RR2FLowLevelInterface implementation.
     ************************************************************/
    @Override
    public void rr2fOutput(byte[] data) {
        String dbg_msg = String.format("RR2F_OUTPUT %d bytes : ", data.length);
        for (int i=0; i<data.length; i++) {
            dbg_msg += String.format("%x ", data[i]);
        }
        DEBUG_PRINT("RR2F_OUTPUT", dbg_msg);

        FT311.SendData(data.length,data);
    }

    /************************************************************
     *       RR2F.RR2FHighLevelInterface implementation.
     ************************************************************/
    @Override
    public void rr2fDataReceive(byte[] data) {
        String dbg_msg = String.format("Rx %d bytes : %s.\r\n", data.length, new String(data));
        DEBUG_PRINT("RR2F_RX", dbg_msg);

        ReceiveData(new String(data));
    }

    @Override
    public void rr2fWriteFinish(int status) {
        Log.d("debug",String.format("WrFin:%d",status));
    }

    @Override
    public void rr2fConnected() {
        //testDebug();
    }

    @Override
    public void rr2fDisconnected() {
        //testDebug("Disconnect");
        //ToastMessageShort("USB Has Disconnect");
    }

/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_door_access, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */

    public void aoaAttached()
    {
        DEBUG_PRINT("USB", "AOA Attached.\r\n");

        Handler hndl = new Handler();

        hndl.postDelayed(new Runnable() {
            @Override
            public void run() {
                rr2f.startPing();
                if (GlobalVariable.SetFist == true) {
                    if ((GlobalVariable.VehicleNo != "") && (!(GlobalVariable.VehicleNo.contains("00000")))) {

                        SendUART("SET:VEHN:" + GlobalVariable.VehicleNo + "");
                    } else {
                        SendUART("SET:VEHN:?");
                    }
                } else {
                    SendUART("SET:VEHN:?");
                }
            }
        }, 500);

    }

    public void aoaDetached()
    {

    }
}
