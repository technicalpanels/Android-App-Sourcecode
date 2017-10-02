package com.example.ntd.tpapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import static android.widget.CompoundButton.OnClickListener;

public class ConfigurePage extends Activity {

 EditText  Distance_Shot;
 EditText  Distance_Medium;
 EditText  Distance_Long;

 EditText  KeyNumberT ;
 EditText  Acceleration ;

    EditText PasswordText;
    ScrollView Scro;
 EditText  vehicle ;
 ToggleButton btn_Distance ;
    Button btn_Cancel ,btn_save,btn_Upload,btn_RTBConfig;

    boolean CommandStatus =false;
    boolean CommandWork = false;
    String dataSend ="";

    private DoorAccess p_instance;

    private RTBConfigureInfo rtb_info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rtb_info = new RTBConfigureInfo();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_configure_page);
        InitButton();
        initextbox();
        InitScroViwe();
        dataSetEditText();

        p_instance=DoorAccess.current_instance;
    }
    private void InitScroViwe()
    {
        Scro = (ScrollView) findViewById(R.id.scrollView3);
    }


    public void InitButton() {
          btn_Distance =(ToggleButton)findViewById(R.id.btn_set_Start_Stop);

          btn_Cancel =(Button)findViewById(R.id.btn_cancel_config);
          btn_save = (Button) findViewById(R.id.btn_save_config);
        btn_Upload =(Button)findViewById(R.id.btn_uploadfm);
        btn_RTBConfig = (Button)findViewById(R.id.btn_rtb_config);
//        final Button btn_set0_position = (Button) findViewById(R.id.btn_set0_position);
//        btn_set0_position.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(ConfigurePage.this, "Set 0 position pressed", Toast.LENGTH_SHORT).show();
//                try {
//                    Intent returnIntent = getIntent();
//                    Protocol protocol = new Protocol();
//                    String data = "";
//                    data = "ACCELEROMETER:0";
//                    SendUART(data);
//                    return;
//                } catch (Exception e) {
//                    Log.d("Exception", e.getMessage());
//                }
//            }
//        });

        btn_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });



        btn_save.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_UP:
                        btn_save.getBackground().setAlpha(255);

                         if((PasswordText.length()<4)||(vehicle.getText().toString().contains("00000")))
                         {
                             PasswordText.setError("Failed");
                             vehicle.setError("Failed");

                         }else
                         {

                             try {
                                 datasetconfig();
                                 //Encrytion data and make file
                                 String HexT = EncryptionClass.StringToHex(vehicle.getText().toString());
                                 String Enc = EncryptionClass.encryptAES(HexT, dataSend);

                                 writeToFile(Enc);



                             } catch (InterruptedException e) {
                                 e.printStackTrace();
                             }

                             if(checkfile())
                             {
                                 Toast.makeText(ConfigurePage.this, "CheckFile...Ok", Toast.LENGTH_SHORT).show();

                                 setResult(RESULT_OK);
                                 ConfigurePage.this.finish();
                             }

                             break;

                         }

                    case MotionEvent.ACTION_DOWN:
                        btn_save.getBackground().setAlpha(128);


                        break;
                }
                return true;
            }
        });


        btn_Distance.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {


                if (btn_Distance.isChecked())
                {
                    SendUART("SET:DIST:START");
                    //Log.d("test","clicked");
                }
                else
                {
                    SendUART("SET:DIST:STOP");
                    //Log.d("test","unclicked");
                }

            }
            });


        btn_Upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SendUART("BOOT");
            }
        });

        btn_RTBConfig.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent rtb_configure_intent = new Intent(ConfigurePage.this, ConfigureRTB.class);

                rtb_configure_intent.putExtra(GlobalVariable.RTB_CONFIG_ACTIVITY_EXTRA_PASSIN_NAME, rtb_info.toString());

                startActivityForResult(rtb_configure_intent, GlobalVariable.RTB_CONFIG_ACTIVITY_REQUEST_CODE);
            }
        });

//        btn_save.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
    }

    /* This function will be called when child Activity finished. */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String rtb_info_str;

        if (requestCode == GlobalVariable.RTB_CONFIG_ACTIVITY_REQUEST_CODE) {
            Log.d("RTB", "finished");

            if ((resultCode == RESULT_OK) && (data != null)) {
                Log.d("RTB", "Result OK.");
                rtb_info_str = data.getStringExtra(GlobalVariable.RTB_CONFIG_ACTIVITY_EXTRA_RETURN_NAME);
                rtb_info.fromString(rtb_info_str);
            } else {
                Log.d("RTB", "Result fail.");
            }
        }
    }

    private  void initextbox()
    {
        vehicle = (EditText) findViewById(R.id.edit_vehicle_number);
        Distance_Shot =(EditText) findViewById(R.id.edit_short_distance);
        Distance_Medium =(EditText)findViewById(R.id.edit_medium_distance);
        Distance_Long =(EditText)findViewById(R.id.edit_long_distance);
        KeyNumberT = (EditText)findViewById(R.id.editNumber);
        Acceleration =(EditText)findViewById(R.id.edit_acceleration_limit);
        PasswordText = (EditText)findViewById(R.id.edit_Password);






    }

    private void datasetconfig() throws InterruptedException {

            dataSend = "CommandToWriteFile;";
            dataSend = dataSend+ "SET:VEHN:" + vehicle.getText().toString()+";";


            dataSend = dataSend +"SET:DILO:" + Distance_Long.getText().toString()+";";

            dataSend = dataSend +"SET:DIME:" + Distance_Medium.getText().toString()+";";

            dataSend = dataSend +"SET:DISH:" + Distance_Shot.getText().toString()+";";

            dataSend = dataSend +"SET:VKEY:" + KeyNumberT.getText().toString()+";";

            dataSend = dataSend +"SET:ACC0:" + Acceleration.getText().toString()+";";

           // dataSend = dataSend +"SET::ACC0:?;";

            dataSend = dataSend +"PASS:" + PasswordText.getText().toString()+";";

            dataSend = dataSend + "SET:RTB:" + rtb_info.toString() + ";";
    }

    private void dataSetEditText()
    {
        vehicle.setText(GlobalVariable.VehicleNo);
        Distance_Long.setText(GlobalVariable.Distance_l);
        Distance_Medium.setText(GlobalVariable.Distance_m);
        Distance_Shot.setText(GlobalVariable.Distance_s);
        KeyNumberT.setText(GlobalVariable.KeyNumber);
        Acceleration.setText(GlobalVariable.Acceleration_l);
        PasswordText.setText(GlobalVariable.PassworkConfig);

        rtb_info.fromString(GlobalVariable.RTBInfo);
    }
    private void writeToFile(String DataSave) {
        try {

            String data = DataSave;

            GlobalVariable.VehicleNo = vehicle.getText().toString();
            File myFile = new File(Environment.getExternalStorageDirectory()+"/TPsecurity");
            if(!myFile.exists())
            {
                myFile.mkdir();
            }
            myFile = new File(Environment.getExternalStorageDirectory()+"/TPsecurity/Config_"+vehicle.getText().toString()+".txt");
            myFile.createNewFile();

            FileOutputStream fOut = new FileOutputStream(myFile);
            OutputStreamWriter myOutWriter =
                    new OutputStreamWriter(fOut);
            myOutWriter.write(data);
            myOutWriter.close();
            fOut.close();

            if(myFile.isFile()) {
                Log.d("File", "OK");
                Toast.makeText(ConfigurePage.this, "WriteFile-ok", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Log.d("File", "wait");
                Toast.makeText(ConfigurePage.this, "File-writting", Toast.LENGTH_SHORT).show();
            }
           //Toast.makeText(ConfigurePage.this, "WriteFile-ok", Toast.LENGTH_SHORT).show();




        } catch (IOException e) {
            Log.d("File", e.getMessage());
        }
    }
    private String fileToRead(String name)
    {
        File myFile = new File(Environment.getExternalStorageDirectory()+"/TPsecurity/Config_"+name.toUpperCase().toString()+".txt");

        String mline ="";

        if(myFile.isFile()){

            try {
                FileInputStream fileIn = new FileInputStream(myFile);

                BufferedReader reader = new BufferedReader(new InputStreamReader(fileIn));
                String line;

                while ((line = reader.readLine()) != null)
                    mline = mline + line ;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            mline ="null";
        }
        return mline;
    }

    private boolean checkfile()
    {
        Toast.makeText(ConfigurePage.this, "CheckFile....", Toast.LENGTH_SHORT).show();

        String datacheck = fileToRead(vehicle.getText().toString());
        String buferD = "";

        if (datacheck != "null")  //find File
        {

            String Hex = EncryptionClass.StringToHex(vehicle.getText().toString());
            buferD = EncryptionClass.decryptAES(Hex, datacheck);
            String[] dataReadFile = buferD.split(";");

           if(!dataReadFile[GlobalVariable.CONFIG_FILE_VEHICLE_NUMNER_INDEX].toString().contains(vehicle.getText().toString()))
            {
            Toast.makeText(ConfigurePage.this, "Error-vehicle", Toast.LENGTH_SHORT).show();
            return false  ;
            }
            if(!(dataReadFile[GlobalVariable.CONFIG_FILE_DISTANCE_LONG_INDEX].toString().contains(Distance_Long.getText().toString())))
            {
                Toast.makeText(ConfigurePage.this, "Error-Distance_Long", Toast.LENGTH_SHORT).show();
                return false  ;
            }
            if(!(dataReadFile[GlobalVariable.CONFIG_FILE_DISTANCE_MEDIUM_INDEX].toString()).contains(Distance_Medium.getText().toString()))
            {
                Toast.makeText(ConfigurePage.this, "Error-Distance_Medium", Toast.LENGTH_SHORT).show();
                return false  ;
            }
            if(!(dataReadFile[GlobalVariable.CONFIG_FILE_DISTANCE_SHORT_INDEX].toString()).contains(Distance_Shot.getText().toString()))
            {
                Toast.makeText(ConfigurePage.this, "Error-Distance_Shot", Toast.LENGTH_SHORT).show();
                return false  ;
            }
            if(!(dataReadFile[GlobalVariable.CONFIG_FILE_KEY_INDEX].toString()).contains(KeyNumberT.getText().toString()))
            {
                Toast.makeText(ConfigurePage.this, "Error-KeyNumber", Toast.LENGTH_SHORT).show();
                return false  ;
            }
            if(!(dataReadFile[GlobalVariable.CONFIG_FILE_ACCELERATION_INDEX].toString()).contains(Acceleration.getText().toString()))
            {
                Toast.makeText(ConfigurePage.this, "Error-Acceleration", Toast.LENGTH_SHORT).show();
                return false  ;
            }
            if(!(dataReadFile[GlobalVariable.CONFIG_FILE_PASSWORD_INDEX].toString()).contains(PasswordText.getText().toString()))
            {
                Toast.makeText(ConfigurePage.this, "Error-Password", Toast.LENGTH_SHORT).show();
                return false  ;
            }
            if(!(dataReadFile[GlobalVariable.CONFIG_FILE_RTB_INDEX].toString()).contains(rtb_info.toString()))
            {
                Toast.makeText(ConfigurePage.this, "Error-RTB", Toast.LENGTH_SHORT).show();
                return false  ;
            }


        } else  //find No File
        {
            Toast.makeText(ConfigurePage.this, "File-Error"+vehicle.getText().toString(), Toast.LENGTH_SHORT).show();
            return false  ;

        }

        return  true;
    }


    public void SendUART(String writeText) {
//        byte[] writeBuffer;
//        writeBuffer = new byte[64];
//        int numBytes;
//        String srcStr = writeText + new String(new char[]{13});
////        String srcStr = writeText;
//        String destStr = srcStr;
//        numBytes = destStr.length();
//        for (int i = 0; i < numBytes; i++) {
//            writeBuffer[i] = (byte) destStr.charAt(i);
//        }
//        GlobalVariable.FTDIGlob.SendData(numBytes, writeBuffer);
        p_instance.SendUartFromExtClass(writeText);
      //  GlobalVariable.DoorClass.SendUART(writeText);

    }





//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_configure_page, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }



}
