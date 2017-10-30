package com.example.ntd.tpapplication;

/**
 * Created by BURIN_SAPSIRI on 9/28/2017.
 */

public class RTBConfigureInfo {

    final private int INFO_LEN = 14;

    /* <DRV>,<DRV_LCK>,<PSG>,<PSG_LCK>,<SIDE>,<SIDE_LCK>,<CAB>,<CAB_LCK>,<VLT>,<VLT_LCK>,<REAR>,<REAR_LCK>,<ALMNET>,<HATCH> */

    final private int DRV_DOOR_INDEX = 0;
    final private int DRV_DOOR_LOCK_INDEX = 1;
    final private int PSG_DOOR_INDEX = 2;
    final private int PSG_DOOR_LOCK_INDEX = 3;
    final private int SIDE_DOOR_INDEX = 4;
    final private int SIDE_DOOR_LOCK_INDEX = 5;
    final private int CAB_DOOR_INDEX = 6;
    final private int CAB_DOOR_LOCK_INDEX = 7;
    final private int VAULT_DOOR_INDEX = 8;
    final private int VAULT_DOOR_LOCK_INDEX = 9;
    final private int REAR_DOOR_INDEX = 10;
    final private int REAR_DOOR_LOCK_INDEX = 11;
    final private int ALARM_NET_INDEX = 12;
    final private int HATCH_INDEX = 13;

    final private String STATUS_ENABLE = "1";
    final private String STATUS_DISABLE = "0";
    final private String SPLITER = ",";

    private boolean[] door_status = new boolean[INFO_LEN];

    public RTBConfigureInfo() {
        for (int i=0; i<INFO_LEN; i++) {
            door_status[i] = true;
        }
    }

    public RTBConfigureInfo(String parse_str) {
        fromString(parse_str);
    }

    /******************** Driver door ********************/
    public boolean getDriverDoorEnabled()
    {
        return door_status[DRV_DOOR_INDEX];
    }

    public void setDriverDoorEnabled(boolean status)
    {
        door_status[DRV_DOOR_INDEX] = status;
    }

    public boolean getDriverDoorLockEnabled()
    {
        return door_status[DRV_DOOR_LOCK_INDEX];
    }

    public void setDriverDoorLockEnabled(boolean status)
    {
        door_status[DRV_DOOR_LOCK_INDEX] = status;
    }
    /*****************************************************/

    /******************** Passenger door ********************/
    public boolean getPassengerDoorEnabled()
    {
        return door_status[PSG_DOOR_INDEX];
    }

    public void setPassengerDoorEnabled(boolean status)
    {
        door_status[PSG_DOOR_INDEX] = status;
    }

    public boolean getPassengerDoorLockEnabled()
    {
        return door_status[PSG_DOOR_LOCK_INDEX];
    }

    public void setPassengerDoorLockEnabled(boolean status)
    {
        door_status[PSG_DOOR_LOCK_INDEX] = status;
    }
    /********************************************************/

    /******************** Side door ********************/
    public boolean getSideDoorEnabled()
    {
        return door_status[SIDE_DOOR_INDEX];
    }

    public void setSideDoorEnabled(boolean status)
    {
        door_status[SIDE_DOOR_INDEX] = status;
    }

    public boolean getSideDoorLockEnabled()
    {
        return door_status[SIDE_DOOR_LOCK_INDEX];
    }

    public void setSideDoorLockEnabled(boolean status)
    {
        door_status[SIDE_DOOR_LOCK_INDEX] = status;
    }
    /***************************************************/

    /******************** Cabin door ********************/
    public boolean getCabinDoorEnabled()
    {
        return door_status[CAB_DOOR_INDEX];
    }

    public void setCabinDoorEnabled(boolean status)
    {
        door_status[CAB_DOOR_INDEX] = status;
    }

    public boolean getCabinDoorLockEnabled()
    {
        return door_status[CAB_DOOR_LOCK_INDEX];
    }

    public void setCabinDoorLockEnabled(boolean status)
    {
        door_status[CAB_DOOR_LOCK_INDEX] = status;
    }
    /****************************************************/

    /******************** Vault door ********************/
    public boolean getVaultDoorEnabled()
    {
        return door_status[VAULT_DOOR_INDEX];
    }

    public void setVaultDoorEnabled(boolean status)
    {
        door_status[VAULT_DOOR_INDEX] = status;
    }

    public boolean getVaultDoorLockEnabled()
    {
        return door_status[VAULT_DOOR_LOCK_INDEX];
    }

    public void setVaultDoorLockEnabled(boolean status)
    {
        door_status[VAULT_DOOR_LOCK_INDEX] = status;
    }
    /****************************************************/

    /******************** Rear door ********************/
    public boolean getRearDoorEnabled()
    {
        return door_status[REAR_DOOR_INDEX];
    }

    public void setRearDoorEnabled(boolean status)
    {
        door_status[REAR_DOOR_INDEX] = status;
    }

    public boolean getRearDoorLockEnabled()
    {
        return door_status[REAR_DOOR_LOCK_INDEX];
    }

    public void setRearDoorLockEnabled(boolean status)
    {
        door_status[REAR_DOOR_LOCK_INDEX] = status;
    }
    /***************************************************/

    /******************** Alarm net ********************/
    public boolean getAlarmNetEnabled()
    {
        return door_status[ALARM_NET_INDEX];
    }

    public void setAlarmNetEnabled(boolean enabled)
    {
        door_status[ALARM_NET_INDEX] = enabled;
    }
    /***************************************************/

    /******************** Hatch ********************/
    public boolean getHatchEnabled()
    {
        return door_status[HATCH_INDEX];
    }

    public void setHatchEnabled(boolean enabled)
    {
        door_status[HATCH_INDEX] = enabled;
    }
    /***********************************************/

    public boolean fromString(String status_str)
    {
        String[] str_arr = status_str.split(SPLITER);

        if (str_arr.length != INFO_LEN) {
            return false;
        }

        for (int i=0; i<INFO_LEN; i++) {
            if (str_arr[i].equals(STATUS_ENABLE)) {
                door_status[i] = true;
            } else {
                door_status[i] = false;
            }
        }

        return true;
    }

    public String toString()
    {
        boolean[] statuses = new boolean[INFO_LEN];

        String ret = "";

        for (int i=0; i<INFO_LEN; i++) {
            if (door_status[i]) {
                ret += STATUS_ENABLE;
            } else {
                ret += STATUS_DISABLE;
            }

            if (i<(INFO_LEN-1)) {
                ret += SPLITER;
            }
        }

        return ret;
    }
}
