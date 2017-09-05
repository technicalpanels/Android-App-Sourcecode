package com.example.ntd.tpapplication;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class RR2F {
    /************************************************************
     * Private Debug method definition for this class.
     ************************************************************/
    static private void DEBUG_PRINT(String msg, Object... args)
    {
        //System.out.println("RR2F DEBUG> " + String.format(msg, args));
    }

    /************************************************************
     * Public constant definition.
     ************************************************************/
    static final public int WRITE_OK = 0;
    static final public int WRITE_ERROR = -1;

    /************************************************************
     * Private constant definition.
     ************************************************************/
    static final private int TX_DATA_TIMEOUT = 1000;
    static final private int RX_DATA_TIMEOUT = 1000;
    static final private int RX_PRTCL_TIMEOUT = 200;

    static final private int PING_TIMER_TIME = 150;
    static final private int PING_TIMEOUT_TIME = 500;

    static final private int TX_STATE_IDLE = 0;
    static final private int TX_STATE_WRITING = 1;

    static final private int PRTCL_STATE_FIND_HD = 0;
    static final private int PRTCL_STATE_FIND_FC = 1;
    static final private int PRTCL_STATE_RECEIVE_DATA = 2;

    static final private int MAXIMUM_DATA_LEN = 128;
    static final private int MAXIMUM_DATA_LEN_PER_MESSAGE = 31;
    static final private int MAXIMUM_TX_QUEUE_LEN = 10;

    static final private int PRTCL_OVERHEAD = 2;
    static final private byte PRTCL_ID = (byte)0x60;
    static final private byte PRTCL_HEAD_ID_MASK = (byte)0xf0;
    static final private byte PRTCL_HEAD_ECD_MASK = (byte)0x0f;
    static final private byte PRTCL_FC_MASK = (byte)0xe0;
    static final private byte PRTCL_FC_FLAG_ACK = (byte)0x20;
    static final private byte PRTCL_FC_FLAG_PIN = (byte)0x40;
    static final private byte PRTCL_FC_FLAG_DR = (byte)0x80;
    static final private byte PRTCL_LEN_MASK = (byte)0x1f;

    /************************************************************
     * Interface declaration.
     ************************************************************/
    public interface RR2FHighLevelInterface {
        void rr2fDataReceive(byte[] data);
        void rr2fWriteFinish(int status);
        void rr2fConnected();
        void rr2fDisconnected();
    }

    public interface RR2FLowLevelInterface {
        void rr2fOutput(byte[] data);
    }

    /************************************************************
     * Public variable.
     ************************************************************/


    /************************************************************
     * Private variable.
     ************************************************************/
    private RR2FLowLevelInterface LowLevelCallback;
    private RR2FHighLevelInterface HighLevelCallback;

    private Timer PingTimer;
    private TimerTask PingTimerTask;
    private Timer PingTimeoutTimer;
    private TimerTask PingTimeoutTimerTask;

    private boolean ConnectionStatus = false;

    private int TxState = TX_STATE_IDLE;
    private byte[] TxBuf = new byte[0];
    private int TxBufHead;
    private int TxBufTail;
    private int TxPieceSize;
    private RR2FPrtcl TxMsg;
    private Timer TxDataTimeoutTimer;
    TimerTask TxDataTimeoutTimerTask;
    List<byte[]> TxQueue = new ArrayList<byte[]>();

    private byte[] RxDataBuf;
    private Timer RxDataTimeoutTimer;
    TimerTask RxDataTimeoutTimerTask;
    private byte[] RxBuf;
    private int RxBufPos;
    private RR2FPrtcl RxMsg;
    private Timer RxPrtclTimeoutTimer;
    TimerTask RxPrtclTimeoutTimerTask;

    private int PrtclReadState = PRTCL_STATE_FIND_HD;

    /************************************************************
     * Public method.
     ************************************************************/
    /* Constructor */
    public RR2F() {
        TxState = TX_STATE_IDLE;
        PrtclReadState = PRTCL_STATE_FIND_HD;

        TxMsg = new RR2FPrtcl();

        RxMsg = new RR2FPrtcl();
        RxBuf = new byte[0];
        RxBufPos = 0;
        RxDataBuf = new byte[0];

        TxPieceSize = MAXIMUM_DATA_LEN_PER_MESSAGE;

        DEBUG_PRINT("RR2F inited.");
    }

    public void setMaximumSizePerMessage(int size)
    {
        TxPieceSize = size;
    }

    public void startPing()
    {
        startPingTimer();
    }

    public void stopPing()
    {
        stopPingTimer();
    }

    public boolean isConnected()
    {
        return ConnectionStatus;
    }

    public void registerLowlevelCallback(RR2FLowLevelInterface callback)
    {
        LowLevelCallback = callback;
    }

    public void registerHighlevelCallback(RR2FHighLevelInterface callback)
    {
        HighLevelCallback = callback;
    }

    /********************High-Level API*********************/
    public boolean write(byte[] data)
    {
        RR2FPrtcl msg = new RR2FPrtcl();

        if (TxQueue.size() >= MAXIMUM_TX_QUEUE_LEN) {
            return false;
        }

        if (TxQueue.size() == 0) {
            TxQueue.add(data);

            TxState = TX_STATE_WRITING;
            /* Process writing start here. */
            sendDataInQueue();
        } else {
            TxQueue.add(data);
        }
        return true;
    }

    public boolean write(String data)
    {
        return write(data.getBytes());
    }

    /********************Low-Level API*********************/
    public void dataInput(byte data_byte)
    {
        switch (PrtclReadState) {
            case PRTCL_STATE_FIND_HD:
                if (PRTCL_ID == PRTCL_HEAD_GET_ID(data_byte)) {
                    RxMsg.setECD(data_byte);
                    DEBUG_PRINT("PRTCL HEAD founded : ECD=%x", RxMsg.getECD());
                    PrtclReadState = PRTCL_STATE_FIND_FC;

                /* Start Prtcl timeout timer. */
                    restartRxPrtclTimer();
                }
                break;
            case PRTCL_STATE_FIND_FC:
                RxMsg.setFC(data_byte);

                DEBUG_PRINT("PRTCL FC received : DR=%b, PIN=%b, ACK=%b, LEN=%d",
                        RxMsg.getFlag(RR2FPrtcl.PRTCL_FC_FLAG_DR),
                        RxMsg.getFlag(RR2FPrtcl.PRTCL_FC_FLAG_PIN),
                        RxMsg.getFlag(RR2FPrtcl.PRTCL_FC_FLAG_ACK),
                        RxMsg.getLen());

                /* Restart Prtcl timeout timer. */
                restartRxPrtclTimer();

                if (RxMsg.getLen() > 0) {
                    RxBuf = new byte[RxMsg.getLen()];
                    RxBufPos = 0;
                    PrtclReadState = PRTCL_STATE_RECEIVE_DATA;
                } else {
                    DEBUG_PRINT("There are no data contain in this mesage.");

                    PrtclReadState = PRTCL_STATE_FIND_HD;

                    /* Stop Prtcl timeout timer. */
                    stopRxPrtclTimer();

                    if (RxMsg.checkECD() == true) {
                        DEBUG_PRINT("ECD is correct.");

                        /* This message is ping acknowledge.*/
                        if ((RxMsg.getFlag(PRTCL_FC_FLAG_PIN) == true) &&
                                (RxMsg.getFlag(PRTCL_FC_FLAG_ACK) == true)) {
                            DEBUG_PRINT("This message is ping acknowledge message.");
                            pingAckHandler(RxMsg);
                        }

                        /* This message is data acknowledge. */
                        if ((RxMsg.getFlag(PRTCL_FC_FLAG_PIN) == false) &&
                                (RxMsg.getFlag(PRTCL_FC_FLAG_ACK) == true)) {
                            DEBUG_PRINT("This message is data acknowledge message.");
                            dataAckHandler();
                        }
                    } else {
                        DEBUG_PRINT("ECD is incorrect.");
                        dataAckHandler();
                    }
                    RxMsg.clearAll();
                }
                break;
            case PRTCL_STATE_RECEIVE_DATA:
                RxBuf[RxBufPos] = data_byte;
                RxBufPos++;

                /* Stop Prtcl timeout timer. */
                stopRxPrtclTimer();

                /* Receive data complete. */
                if (RxBufPos >= RxMsg.getLen()) {
                    RxMsg.setData(RxBuf);
                    DEBUG_PRINT("All data received.");

                    PrtclReadState = PRTCL_STATE_FIND_HD;

                    if (RxMsg.checkECD() == true) {
                        DEBUG_PRINT("ECD is correct.");

                        /* This message is ping acknowledge with data */
                        if ((RxMsg.getFlag(PRTCL_FC_FLAG_PIN) == true) &&
                                (RxMsg.getFlag(PRTCL_FC_FLAG_ACK) == true)) {
                            DEBUG_PRINT("This message is ping acknowledge with data message.");
                            pingAckHandler(RxMsg);
                        }

                    } else {
                        DEBUG_PRINT("ECD is incorrect.");
                        dataAckHandler();
                    }
                    RxMsg.clearAll();
                }

                break;
            default:
                break;
        }
    }

    public void dataInput(byte[] data)
    {
        int i = 0;

        for (i=0; i<data.length; i++) {
            dataInput(data[i]);
        }
    }



    /************************************************************
     * Private handler method.
     ************************************************************/
    private void pingAckHandler(RR2FPrtcl msg)
    {
        /* Process restart timer here. */
        restartPingTimeoutTimer();
        if (ConnectionStatus != true) {
            ConnectionStatus = true;
            rr2fConnected();
        }

        if (msg.getLen() > 0) {
            DEBUG_PRINT("Data received : ");
            for (int i=0; i<msg.getLen(); i++) {
                DEBUG_PRINT("%c", msg.getData()[i]);
            }

            RxDataBuf = appendByteArray(RxDataBuf, msg.getData());

            if (msg.getFlag(RR2FPrtcl.PRTCL_FC_FLAG_DR) == false) {
                rr2fDataReceive(RxDataBuf);
                RxDataBuf = new byte[0];
                stopRxDataTimer();
                //onPingNeeded();                               // When has data remain Wait for next Ping !!!
                DEBUG_PRINT("There are no data remaining.");
            } else {
                restartRxDataTimer();
                //onPingNeeded();                               //  When has data remain Wait for next Ping !!!
                DEBUG_PRINT("There are data remaining.");
            }
        }
    }

    private void sendDataInQueue()
    {
        if (TxQueue.size() > 0) {
            /* Refill TxBuf from TxQueue. */
            TxBuf = TxQueue.get(0);

            TxBufHead = 0;
            TxBufTail = TxBuf.length;

            TxState = TX_STATE_WRITING;
            DEBUG_PRINT("Refill TxBuf from TxQueue.");

            dataAckHandler();
        }
    }

    private void onTxFinish(int status)
    {
        DEBUG_PRINT("Write finished.");
        rr2fWriteFinish(status);

        /* Remove first element on TxQueue. */
        TxQueue.remove(0);

        if (TxQueue.size() > 0) {
            sendDataInQueue();
        }
    }

    private void dataAckHandler()
    {
        byte[] piece_data;
        int piece_data_len;
        RR2FPrtcl msg;

        if (TxState != TX_STATE_WRITING) {
            return;
        }

        if (TxBuf.length > 0) {
            msg = new RR2FPrtcl();
            restartTxDataTimer();

            if ((TxBufTail - TxBufHead) > TxPieceSize) {
                DEBUG_PRINT("Remaining data size is more than ");
                piece_data_len = TxPieceSize;
                piece_data = new byte[piece_data_len];

                System.arraycopy(TxBuf, TxBufHead, piece_data, 0, piece_data_len);
                TxBufHead += piece_data_len;

                msg.setFlag(RR2FPrtcl.PRTCL_FC_FLAG_DR);
            } else {
                piece_data_len = TxBufTail - TxBufHead;
                piece_data = new byte[piece_data_len];

                System.arraycopy(TxBuf, TxBufHead, piece_data, 0, piece_data_len);

                TxBuf = new byte[0];
                TxBufHead = 0;
                TxBufTail = 0;
            }

            msg.setLen(piece_data_len);
            msg.setData(piece_data);
            msg.reCalculateECD();
            rr2fOutput(msg.getByteArray());
        } else {
            stopTxDataTimer();
            TxState = TX_STATE_IDLE;
            onTxFinish(WRITE_OK);
        }
    }

    /************************************************************
     * Private Timer handler.
     ************************************************************/
    private void onRxPrtclTimeout()
    {
        PrtclReadState = PRTCL_STATE_FIND_HD;
        RxMsg.clearAll();
        DEBUG_PRINT("PRTCL receiving timeout.");
    }

    private void onRxDataTimeout()
    {
        RxDataBuf = new byte[0];
        DEBUG_PRINT("Multi message receiving timeout.");
    }

    private void onTxDataTimeout()
    {
        TxBuf = new byte[0];
        TxBufHead = 0;
        TxBufTail = 0;
        TxState = TX_STATE_IDLE;
        onTxFinish(WRITE_ERROR);
    }

    private void onPingNeeded()
    {
        RR2FPrtcl msg = new RR2FPrtcl();
        msg.setFC(RR2FPrtcl.PRTCL_FC_FLAG_PIN);
        msg.reCalculateECD();

        rr2fOutput(msg.getByteArray());
        restartPingTimer();
    }

    private void onPingTimeout()
    {
        ConnectionStatus = false;
        rr2fDisconnected();
    }

    /************************************************************
     * Private Timer Task handle method.
     ************************************************************/

    private void relifeRxPrtclTimeoutTimerTask ()
    {
        RxPrtclTimeoutTimerTask = new TimerTask() {
            @Override
            public void run() {
                onRxPrtclTimeout();
            }
        };
    }

    private void relifeRxDataTimeoutTimerTask ()
    {
        RxDataTimeoutTimerTask = new TimerTask() {
            @Override
            public void run() {
                onRxDataTimeout();
            }
        };
    }

    private void relifeTxDataTimeoutTimerTask ()
    {
        TxDataTimeoutTimerTask = new TimerTask() {
            @Override
            public void run() {
                onTxDataTimeout();
            }
        };
    }

    private void relifePingTimerTask ()
    {
        PingTimerTask = new TimerTask() {
            @Override
            public void run() {
                onPingNeeded();
            }
        };
    }

    private void relifePingTimeoutTimerTask ()
    {
        PingTimeoutTimerTask = new TimerTask() {
            @Override
            public void run() {
                onPingTimeout();
            }
        };
    }

    /************************************************************
     * Private Timer management method.
     ************************************************************/

    /********************RxPrtclTimer********************/
    private void startRxPrtclTimer()
    {
        RxPrtclTimeoutTimer = new Timer();
        relifeRxPrtclTimeoutTimerTask();
        RxPrtclTimeoutTimer.schedule(RxPrtclTimeoutTimerTask, RX_PRTCL_TIMEOUT);
    }

    private void stopRxPrtclTimer()
    {
        if (RxPrtclTimeoutTimer != null) {
            RxPrtclTimeoutTimer.cancel();
        }
    }

    private void restartRxPrtclTimer()
    {
        stopRxPrtclTimer();
        startRxPrtclTimer();
    }

    /********************RxDataTimer********************/
    private void startRxDataTimer()
    {
        RxDataTimeoutTimer = new Timer();
        relifeRxDataTimeoutTimerTask();
        RxDataTimeoutTimer.schedule(RxDataTimeoutTimerTask, RX_DATA_TIMEOUT);
    }

    private void stopRxDataTimer()
    {
        if (RxDataTimeoutTimer != null) {
            RxDataTimeoutTimer.cancel();
        }
    }

    private void restartRxDataTimer()
    {
        stopRxDataTimer();
        startRxDataTimer();
    }

    /********************TxDataTimer********************/
    private void startTxDataTimer()
    {
        TxDataTimeoutTimer = new Timer();
        relifeTxDataTimeoutTimerTask();
        TxDataTimeoutTimer.schedule(TxDataTimeoutTimerTask, TX_DATA_TIMEOUT);
    }

    private void stopTxDataTimer()
    {
        if (TxDataTimeoutTimer != null) {
            TxDataTimeoutTimer.cancel();
        }
    }

    private void restartTxDataTimer()
    {
        stopTxDataTimer();
        startTxDataTimer();
    }

    /********************PingTimer********************/
    private void startPingTimer()
    {
        PingTimer = new Timer();
        relifePingTimerTask();
        PingTimer.schedule(PingTimerTask, PING_TIMER_TIME);
    }

    private void stopPingTimer()
    {
        if (PingTimer != null) {
            PingTimer.cancel();
        }
    }

    private void restartPingTimer()
    {
        stopPingTimer();
        startPingTimer();
    }

    /********************PingTimeoutTimer********************/
    private void startPingTimeoutTimer()
    {
        PingTimeoutTimer = new Timer();
        relifePingTimeoutTimerTask();
        PingTimeoutTimer.schedule(PingTimeoutTimerTask, PING_TIMEOUT_TIME);
    }

    private void stopPingTimeoutTimer()
    {
        if (PingTimeoutTimer != null) {
            PingTimeoutTimer.cancel();
        }
    }

    private void restartPingTimeoutTimer()
    {
        stopPingTimeoutTimer();
        startPingTimeoutTimer();
    }

    /************************************************************
     * Private interface handle method.
     ************************************************************/


    /********************High-Level Interface********************/

    private void rr2fDataReceive(byte[] data)
    {
        synchronized(HighLevelCallback) {
            HighLevelCallback.rr2fDataReceive(data);
        }
    }
    private void rr2fWriteFinish(int status)
    {
        synchronized(HighLevelCallback) {
            HighLevelCallback.rr2fWriteFinish(status);
        }
    }
    private void rr2fConnected()
    {
        synchronized(HighLevelCallback) {
            HighLevelCallback.rr2fConnected();
        }
    }
    private void rr2fDisconnected()
    {
        synchronized(HighLevelCallback) {
            HighLevelCallback.rr2fDisconnected();
        }
    }

    /********************Low-Level Interface********************/
    private void rr2fOutput(byte[] data)
    {
        synchronized (LowLevelCallback) {
            LowLevelCallback.rr2fOutput(data);
        }
    }

    /************************************************************
     * Private helping method.
     ************************************************************/
    private byte[] appendByteArray(byte[] src_data, byte[] append_data)
    {
        byte[] rtn_data = new byte[src_data.length + append_data.length];

        /* Copy src_data to rtn_data. */
        System.arraycopy(src_data, 0, rtn_data, 0, src_data.length);

        /* Copy append_data to rtn_data. */
        System.arraycopy(append_data, 0, rtn_data, src_data.length, append_data.length);

        return rtn_data;
    }

    private byte MASK(byte reg, byte mask)
    {
        return (byte)(reg & mask);
    }

    private byte PRTCL_HEAD_GET_ID(byte reg)
    {
        return MASK(reg, PRTCL_HEAD_ID_MASK);
    }

    private byte PRTCL_HEAD_GET_ECD(byte reg)
    {
        return MASK(reg, PRTCL_HEAD_ECD_MASK);
    }

    private byte PRTCL_FC_GET_FLAG(byte reg, byte flag)
    {
        return MASK(reg, flag);
    }

    private byte PRTCL_GET_LEN(byte reg)
    {
        return MASK(reg, PRTCL_LEN_MASK);
    }

    /************************************************************
     * TEST
     ************************************************************/
    public void TEST()
    {
        byte value = MASK((byte)0xff, (byte)0x0f);
        DEBUG_PRINT("MASK : %d", value);
    }
}
