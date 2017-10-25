package com.example.ntd.tpapplication;

/**
 * Created by BURIN_SAPSIRI on 9/28/2017.
 */

public class DoorLockInfo {

    final private int INFO_LEN = 7;

    /* <DRV_LCK>,<PSG_LCK>,<SIDE_LCK>,<CAB_LCK>,<VLT_LCK>,<REAR_LCK>,<HATCH> */

    final private int DRV_DOOR_LOCK_INDEX = 0;
    final private int PSG_DOOR_LOCK_INDEX = 1;
    final private int SIDE_DOOR_LOCK_INDEX = 2;
    final private int CAB_DOOR_LOCK_INDEX = 3;
    final private int VAULT_DOOR_LOCK_INDEX = 4;
    final private int REAR_DOOR_LOCK_INDEX = 5;
    final private int HATCH_LOCK_INDEX = 6;

    final private String STATUS_LOCK = "1";
    final private String STATUS_UNLOCK = "0";
    final private String SPLITER = ",";

    private boolean[] door_lock_status = new boolean[INFO_LEN];

    static private int MIN(int x, int y)
    {
        if (x < y) {
            return x;
        } else {
            return y;
        }
    }

    public DoorLockInfo()
    {

    }

    public DoorLockInfo(String str)
    {
        fromString(str);
    }

    /******************** Driver door ********************/
    public boolean getDriverDoorLocked()
    {
        return door_lock_status[DRV_DOOR_LOCK_INDEX];
    }

    public void setDriverDoorLocked(boolean locked)
    {
        door_lock_status[DRV_DOOR_LOCK_INDEX] = locked;
    }
    /*****************************************************/

    /******************** Passenger door ********************/
    public boolean getPassengerDoorLocked()
    {
        return door_lock_status[PSG_DOOR_LOCK_INDEX];
    }

    public void setPasssengerDoorLocked(boolean locked)
    {
        door_lock_status[PSG_DOOR_LOCK_INDEX] = locked;
    }
    /********************************************************/

    /******************** Side door ********************/
    public boolean getSideDoorLocked()
    {
        return door_lock_status[SIDE_DOOR_LOCK_INDEX];
    }

    public void setSideDoorLocked(boolean locked)
    {
        door_lock_status[SIDE_DOOR_LOCK_INDEX] = locked;
    }
    /***************************************************/

    /******************** Cabin door ********************/
    public boolean getCabinDoorLocked()
    {
        return door_lock_status[CAB_DOOR_LOCK_INDEX];
    }

    public void setCabinDoorLocked(boolean locked)
    {
        door_lock_status[CAB_DOOR_LOCK_INDEX] = locked;
    }
    /****************************************************/

    /******************** Vault door ********************/
    public boolean getVaultDoorLocked()
    {
        return door_lock_status[VAULT_DOOR_LOCK_INDEX];
    }

    public void setVaultDoorLocked(boolean locked)
    {
        door_lock_status[VAULT_DOOR_LOCK_INDEX] = locked;
    }
    /****************************************************/

    /******************** Rear door ********************/
    public boolean getRearDoorLocked()
    {
        return door_lock_status[REAR_DOOR_LOCK_INDEX];
    }

    public void setRearDoorLocked(boolean locked)
    {
        door_lock_status[REAR_DOOR_LOCK_INDEX] = locked;
    }

    public boolean getHatchLocked() {
        return door_lock_status[HATCH_LOCK_INDEX];
    }

    public void setHatchLocked(boolean locked)
    {
        door_lock_status[HATCH_LOCK_INDEX] = locked;
    }

    /***************************************************/



    public boolean fromString(String status_str)
    {
        String[] str_arr = status_str.split(SPLITER);
        int length = MIN(str_arr.length, INFO_LEN);

        for (int i=0; i<length; i++) {
            if (str_arr[i].equals(STATUS_LOCK)) {
                door_lock_status[i] = true;
            } else {
                door_lock_status[i] = false;
            }
        }

        return true;
    }

    public String toString()
    {
        boolean[] statuses = new boolean[INFO_LEN];

        String ret = "";

        for (int i=0; i<INFO_LEN; i++) {
            if (door_lock_status[i]) {
                ret += STATUS_LOCK;
            } else {
                ret += STATUS_UNLOCK;
            }

            if (i<(INFO_LEN-1)) {
                ret += SPLITER;
            }
        }

        return ret;
    }
}
