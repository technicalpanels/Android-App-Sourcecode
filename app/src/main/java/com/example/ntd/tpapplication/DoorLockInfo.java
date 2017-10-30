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

    final public static int UNLOCKED = 0;
    final public static int LOCKED = 1;
    final public static int INVALID = 2;

    private int[] door_lock_status = new int[INFO_LEN];

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
    public int getDriverDoorLocked()
    {
        return door_lock_status[DRV_DOOR_LOCK_INDEX];
    }

    public void setDriverDoorLocked(int locked)
    {
        door_lock_status[DRV_DOOR_LOCK_INDEX] = locked;
    }
    /*****************************************************/

    /******************** Passenger door ********************/
    public int getPassengerDoorLocked()
    {
        return door_lock_status[PSG_DOOR_LOCK_INDEX];
    }

    public void setPasssengerDoorLocked(int locked)
    {
        door_lock_status[PSG_DOOR_LOCK_INDEX] = locked;
    }
    /********************************************************/

    /******************** Side door ********************/
    public int getSideDoorLocked()
    {
        return door_lock_status[SIDE_DOOR_LOCK_INDEX];
    }

    public void setSideDoorLocked(int locked)
    {
        door_lock_status[SIDE_DOOR_LOCK_INDEX] = locked;
    }
    /***************************************************/

    /******************** Cabin door ********************/
    public int getCabinDoorLocked()
    {
        return door_lock_status[CAB_DOOR_LOCK_INDEX];
    }

    public void setCabinDoorLocked(int locked)
    {
        door_lock_status[CAB_DOOR_LOCK_INDEX] = locked;
    }
    /****************************************************/

    /******************** Vault door ********************/
    public int getVaultDoorLocked()
    {
        return door_lock_status[VAULT_DOOR_LOCK_INDEX];
    }

    public void setVaultDoorLocked(int locked)
    {
        door_lock_status[VAULT_DOOR_LOCK_INDEX] = locked;
    }
    /****************************************************/

    /******************** Rear door ********************/
    public int getRearDoorLocked()
    {
        return door_lock_status[REAR_DOOR_LOCK_INDEX];
    }

    public void setRearDoorLocked(int locked)
    {
        door_lock_status[REAR_DOOR_LOCK_INDEX] = locked;
    }

    public int getHatchLocked() {
        return door_lock_status[HATCH_LOCK_INDEX];
    }

    public void setHatchLocked(int locked)
    {
        door_lock_status[HATCH_LOCK_INDEX] = locked;
    }

    /***************************************************/



    public boolean fromString(String status_str)
    {
        String[] str_arr = status_str.split(SPLITER);

        for (int i=0; i<INFO_LEN; i++) {
            if (i < str_arr.length) {
                if (str_arr[i].equals(STATUS_LOCK)) {
                    door_lock_status[i] = LOCKED;
                } else if (str_arr[i].equals(STATUS_UNLOCK)) {
                    door_lock_status[i] = UNLOCKED;
                } else {
                    door_lock_status[i] = INVALID;
                }
            } else {
                door_lock_status[i] = INVALID;
            }
        }

        return true;
    }

    public String toString()
    {
        String ret = "";

        for (int i=0; i<INFO_LEN; i++) {
            if (door_lock_status[i] == LOCKED) {
                ret += STATUS_LOCK;
            } else  if (door_lock_status[i] == UNLOCKED) {
                ret += STATUS_UNLOCK;
            } else {
                ret += STATUS_LOCK;
            }

            if (i<(INFO_LEN-1)) {
                ret += SPLITER;
            }
        }

        return ret;
    }
}
