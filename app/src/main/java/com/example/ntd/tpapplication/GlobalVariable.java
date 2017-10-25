package com.example.ntd.tpapplication;

/**
 * Created by ntd on 21/10/2015.
 */
public class GlobalVariable
{
    final public static int CONFIG_FILE_VEHICLE_NUMNER_INDEX = 1;
    final public static int CONFIG_FILE_DISTANCE_LONG_INDEX = 2;
    final public static int CONFIG_FILE_DISTANCE_MEDIUM_INDEX = 3;
    final public static int CONFIG_FILE_DISTANCE_SHORT_INDEX = 4;
    final public static int CONFIG_FILE_KEY_INDEX = 5;
    final public static int CONFIG_FILE_ACCELERATION_INDEX = 6;
    final public static int CONFIG_FILE_PASSWORD_INDEX = 7;
    final public static int CONFIG_FILE_RTB_INDEX = 8;

    final public static int DOOR_LOCK_DRIVER = 0;
    final public static int DOOR_LOCK_PASSENGER = 1;
    final public static int DOOR_LOCK_SIDE = 2;
    final public static int DOOR_LOCK_CAB = 3;
    final public static int DOOR_LOCK_VAULT = 4;
    final public static int DOOR_LOCK_REAR = 5;
    final public static int HATCH_LOCK = 6;

    final public static int COLOR_RED = 0xFFFF0000;
    final public static int COLOR_GREEN = 0xFF00FF00;

    final public static String LOCK_TEXT = "Locked";
    final public static String UNLOCK_TEXT = "Unlocked";

    final public static String RTB_CONFIG_ACTIVITY_EXTRA_PASSIN_NAME = "RTB_CONFIG_ACTIVITY_EXTRA_PASSIN";
    final public static String RTB_CONFIG_ACTIVITY_EXTRA_RETURN_NAME = "RTB_CONFIG_ACTIVITY_EXTRA_RETURN";


    /* Activity request code. */
    final public static int RTB_CONFIG_ACTIVITY_REQUEST_CODE = 100000;
    final public static int CONFIGURE_PAGE_ACTIVITY_REQUEST_CODE = 100001;

    final public static int CONNECTION_SUCCESS = 0;
    final public static int CONNECTION_FAIL_NO_DEVICE = -1;
    final public static int CONNECTION_FAIL_UNKNOWN = -100;


    public static int connectionType;
    public static boolean runProgramFirst=false;
    public static boolean ReadFileConfig=false;

    public static boolean SetFist=false;


    public static boolean isUnPlug=false;
    public static String IV="0000000000000000";
    public static InterfaceFT311 FTDIGlob;

    public static String  VehicleNo ="00000";

    public static String  Distance_s="0" ;
    public static String  Distance_m ="0";
    public static String  Distance_l ="0";
    public static String  KeyNumber ="0" ;
    public static String  Acceleration_l ="0";
    public static String  PassworkConfig ="1234" ;
    public static String  RTBInfo = "0,0,0,0,0,0,0,0,0,0,0,0";

//public static String IV="00000000";
}
