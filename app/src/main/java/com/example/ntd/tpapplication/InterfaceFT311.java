package com.example.ntd.tpapplication;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * Created by ntd on 09/10/2015.
 */
public class InterfaceFT311 implements Serializable {

    public interface ConnectionInterface {
        void aoaAttached();
        void aoaDetached();
    }

    private static InterfaceFT311 g_instance = null;

    public String ManufacturerString = "mManufacturer=FTDI";
    public String ModelString1 = "mModel=FTDIUARTDemo";
    public String ModelString2 = "mModel=Android Accessory FT312D";
    public String VersionString = "mVersion=1.0";
    public ParcelFileDescriptor filedescriptor = null;
    public FileInputStream inputstream = null;
    public FileOutputStream outputstream = null;
    public read_thread readThread;
    public boolean datareceived = false;
    public boolean READ_ENABLE = false;
    public boolean accessory_attached = false;
    public Context global_context;
    public int baud;
    public byte dataBits, stopBits, parity, flowControl;
    public int countOpen = 0;
    int maxnumbytes = 65536;
    UsbManager usbManager;
    PendingIntent mPermissionIntent;
    private String ACTION_USB_PERMISSION = "com.example.ntd.tpapplication.USB_PERMISSION";
    private byte[] usbdata;
    private byte[] writeusbdata;

    private boolean is_attached = false;

    ConnectionInterface connection_interface = null;

    private final int BAUDRATE = 115200;

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (accessory != null) {
                            MessageBox("After Permission");
                            OpenAccessory(accessory);
                            SetConfigAfterPermission();
                            GlobalVariable.connectionType = 1;

                            Log.d("USB", "USB permit.\r\n");
                            if (connection_interface != null) {
                                connection_interface.aoaAttached();
                            }
                        }
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                Log.d("USB", "USB attached.\r\n");
                is_attached = true;
                MessageBox("ACTION_USB_DEVICE_ATTACHED");
                //ResumeConnection();
            } else if (Intent.ACTION_POWER_CONNECTED.equals(action)) {
                // ResumeConnection();
            } else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
                Log.d("USB", "USB detached.\r\n");
                MessageBox("ACTION_USB_ACCESSORY_DETACHED");
                //is_attached = false;
                //System.exit(0);
                //GlobalVariable.connectionType = -1;
                //DestroyAccessory(true);
                //System.exit(0);
            }
        }
    };
    private byte[] readBuffer; /*circular buffer*/
    private int readcount;
    private int totalBytes;
    private int writeIndex;
    private int readIndex;
    private byte status;

    public InterfaceFT311() {

    }

    public static InterfaceFT311 instance(Context ctx) {
        if (g_instance == null) {
            g_instance = new InterfaceFT311(ctx);
        } else {

        }

        return g_instance;
    }

    InterfaceFT311(Context context) {

        this.global_context = context;
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        usbdata = new byte[1024];
        writeusbdata = new byte[256];
        /*128(make it 256, but looks like bytes should be enough)*/
        readBuffer = new byte[maxnumbytes];


        readIndex = 0;
        writeIndex = 0;
        mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
//        try {
//            context.unregisterReceiver(mUsbReceiver);
//        } catch (Exception e) {
//            //Log.d("Exception", e.getMessage());
//        }

        context.registerReceiver(mUsbReceiver, filter);
        filedescriptor = null;
        inputstream = null;
        outputstream = null;
        // ResumeConnection();
    }

    public void setConnectionInterface(ConnectionInterface iface)
    {
        connection_interface = iface;
    }

    public boolean isAttached()
    {
        return is_attached;
    }

    public void SetConfigAfterPermission() {
        baud = BAUDRATE;
        stopBits = 1;
        dataBits = 8;
        parity = 0;
        flowControl = 0;
        writeusbdata[0] = (byte) baud;
        writeusbdata[1] = (byte) (baud >> 8);
        writeusbdata[2] = (byte) (baud >> 16);
        writeusbdata[3] = (byte) (baud >> 24);
        writeusbdata[4] = dataBits;
        writeusbdata[5] = stopBits;
        writeusbdata[6] = parity;
        writeusbdata[7] = flowControl;
        SendPacket(8);
    }

    public void SetConfig(int baud, byte dataBits, byte stopBits, byte parity, byte flowControl) {
        this.baud = baud;
        this.dataBits = dataBits;
        this.stopBits = stopBits;
        this.parity = parity;
        this.flowControl = flowControl;
        writeusbdata[0] = (byte) baud;
        writeusbdata[1] = (byte) (baud >> 8);
        writeusbdata[2] = (byte) (baud >> 16);
        writeusbdata[3] = (byte) (baud >> 24);
        writeusbdata[4] = dataBits;
        writeusbdata[5] = stopBits;
        writeusbdata[6] = parity;
        writeusbdata[7] = flowControl;
        SendPacket(8);
    }

    public void MessageBox(String msg) {
        Toast.makeText(global_context, msg, Toast.LENGTH_SHORT).show();
    }

    public void SendUART(String writeText) {
        try {
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
            SendData(numBytes, writeBuffer);
        }
        catch(Exception e) {

        }
    }

    public byte SendData(int numBytes, byte[] buffer) {
        status = 0x00; /*success by default*/
        /*
         * if num bytes are more than maximum limit
		 */

        try {
            if (numBytes < 1) {
            /*return the status with the error in the command*/
                return status;
            }

		/*check for maximum limit*/
            if (numBytes > 256) {
                numBytes = 256;
            }
        /*prepare the packet to be sent*/
            for (int count = 0; count < numBytes; count++) {
                writeusbdata[count] = buffer[count];
            }

            if (numBytes != 64) {
                SendPacket(numBytes);
            } else {
                byte temp = writeusbdata[63];
                SendPacket(63);
                writeusbdata[0] = temp;
                SendPacket(1);
            }
        } catch (Exception e) {

        }

        return status;
    }

    public byte ReadData(int numBytes, byte[] buffer, int[] actualNumBytes) {
        try {
            status = 0x00; /*success by default*/

		/*should be at least one byte to read*/
            if ((numBytes <= 1) || (totalBytes == 0)) {
                actualNumBytes[0] = 0;
                status = 0x01;
                return status;
            }

		/*check for max limit*/
            if (numBytes > totalBytes)
                numBytes = totalBytes;

		/*update the number of bytes available*/
            totalBytes -= numBytes;

            actualNumBytes[0] = numBytes;

		/*copy to the user buffer*/
            for (int count = 0; count < numBytes; count++) {
                buffer[count] = readBuffer[readIndex];
                readIndex++;
            /*shouldnt read more than what is there in the buffer,
             * 	so no need to check the overflow
			 */
                readIndex %= maxnumbytes;
            }
            return status;
        } catch (Exception e) {
            return 0;
        }
    }

    private void SendPacket(int numBytes) {
        try {
            if (outputstream != null) {
                outputstream.write(writeusbdata, 0, numBytes);
            }
        } catch (IOException e) {
            MessageBox("DD"+e.getMessage());
        }
    }


    public void stopMyThread() {
        Thread tmpThread = readThread;
        readThread = null;
        if (tmpThread != null) {
            tmpThread.interrupt();
        }
    }

    public void OpenAccessory(UsbAccessory accessory)
    {
//        if(GlobalVariable.connectionType!=1)
        filedescriptor = usbManager.openAccessory(accessory);
//        }
        if (filedescriptor != null)
        {
            FileDescriptor fd = filedescriptor.getFileDescriptor();

            inputstream = new FileInputStream(fd);
            outputstream = new FileOutputStream(fd);
			/*check if any of them are null*/
            if (inputstream == null || outputstream == null) {
                Log.d("USB", "Inputstream or Outputstream null.\r\n");
                return;
            }
            try {
                READ_ENABLE = false;
                Thread.sleep(100);
            } catch (Exception e)
            {
            }
            if (READ_ENABLE == false)
            {
                READ_ENABLE = true;
                readThread = new read_thread(inputstream);
                readThread.start();


            }
        } else {
            MessageBox("filescriptor is null");
        }
    }

    public void DestroyAccessory(boolean bConfiged) {
        try {
            GlobalVariable.connectionType = -1;
            if (true == bConfiged) {
                READ_ENABLE = false;  // set false condition for handler_thread to exit waiting data loop
//                writeusbdata[0] = 0;  // send dummy data for instream.read going
//                SendPacket(1);
                SendUART("<END>");
                //  global_context.unregisterReceiver(mUsbReceiver);
            } else {
                SetConfig(BAUDRATE, (byte) 1, (byte) 8, (byte) 0, (byte) 0);  // send default setting data for config
                try {
                    Thread.sleep(10);
                } catch (Exception e) {
                }

                READ_ENABLE = false;  // set false condition for handler_thread to exit waiting data loop
                writeusbdata[0] = 0;  // send dummy data for instream.read going
                SendPacket(1);
                if (true == accessory_attached) {
                }
            }
            try {
                Thread.sleep(10);
            } catch (Exception e) {
            }
            CloseAccessory();
        } catch (Exception e) {
        }
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        }
    }

    public boolean ResumeConnection() {
        UsbAccessory[] accessories = usbManager.getAccessoryList();
        if (accessories != null) {
            Toast.makeText(global_context, "Accessory Attached", Toast.LENGTH_SHORT).show();
        }

        UsbAccessory accessory = (accessories == null ? null : accessories[0]);
        if (accessory != null)
        {
            if (usbManager.hasPermission(accessory))
            {
                // DestroyAccessory(true);  //Read à¸ªà¸±à¸à¸•à¸±à¸§ à¸à¹ˆà¸­à¸™à¸–à¸¶à¸‡à¸ˆà¸° Open à¹„à¸”à¹‰
                OpenAccessory(accessory);
                SetConfigAfterPermission();
                GlobalVariable.connectionType = 1;

                if (connection_interface != null) {
                    connection_interface.aoaAttached();
                }
            } else {
                synchronized (mUsbReceiver)
                {
                    usbManager.requestPermission(accessory, mPermissionIntent);
                }
            }
            return true;
        } else {
            MessageBox("Accessory Null");
            GlobalVariable.connectionType = -1;
            return false;
        }

    }

    private void CloseAccessory() {
        try {
            if (filedescriptor != null)
                filedescriptor.close();

        } catch (IOException e) {
        }
        try {
            if (inputstream != null)
                inputstream.close();
        } catch (IOException e) {
        }

        try {
            if (outputstream != null)
                outputstream.close();

        } catch (IOException e) {
        }

		/*FIXME, add the notfication also to close the application*/

        inputstream = null;
        outputstream = null;
        filedescriptor = null;

        //System.exit(0);
    }

    private class read_thread extends Thread {
        FileInputStream instream;

        read_thread(FileInputStream stream) {
            instream = stream;
            this.setPriority(Thread.MAX_PRIORITY);
        }

        public void run() {
            while (READ_ENABLE == true) {
                while (totalBytes > (maxnumbytes - 1024)) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if (instream != null) {
                        readcount = instream.read(usbdata, 0, 1024);
                        if (readcount > 0) {
                            for (int count = 0; count < readcount; count++) {
                                readBuffer[writeIndex] = usbdata[count];
                                writeIndex++;
                                writeIndex %= maxnumbytes;
                            }

                            if (writeIndex >= readIndex)
                                totalBytes = writeIndex - readIndex;
                            else
                                totalBytes = (maxnumbytes - readIndex) + writeIndex;
                        }
                    } else {
                    }
                } catch (IOException e) {
                    MessageBox("Read thread " + e.getMessage());
                }
            }
        }
    }
}
