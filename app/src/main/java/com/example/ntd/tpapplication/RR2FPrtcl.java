package com.example.ntd.tpapplication;


public class RR2FPrtcl {
	
	/************************************************************
	 * Public constant definition.
	 ************************************************************/
	static final public byte PRTCL_FC_FLAG_ACK = (byte)0x20;
	static final public byte PRTCL_FC_FLAG_PIN = (byte)0x40;
	static final public byte PRTCL_FC_FLAG_DR = (byte)0x80;
	/************************************************************
	 * Private constant definition.
	 ************************************************************/
	static final private int TX_STATE_IDLE = 0;
	static final private int TX_STATE_WRITING = 1;
	
	static final private int PRTCL_STATE_FIND_HD = 0;
	static final private int PRTCL_STATE_FIND_FC = 1;
	static final private int PRTCL_STATE_RECEIVE_DATA = 2;
	
	static final private int MAXIMUM_DATA_LEN = 128;
	static final private int MAXIMUM_DATA_LEN_PER_MESSAGE = 31;
	
	static final private int PRTCL_OVERHEAD = 2;
	static final private byte PRTCL_ID = (byte)0x60;
	static final private byte PRTCL_HEAD_ID_MASK = (byte)0xf0;
	static final private byte PRTCL_HEAD_ECD_MASK = (byte)0x0f;
	static final private byte PRTCL_FC_MASK = (byte)0xe0;
	static final private byte PRTCL_LEN_MASK = (byte)0x1f;
	
	/************************************************************
	 * Private variable.
	 ************************************************************/
	private byte 	hd;
	private byte 	fc;
	private byte[]	data;
	
	/************************************************************
	 * Public method.
	 ************************************************************/
	public RR2FPrtcl()
	{
		clearAll();
	}
	
	public void setFlag(byte _flag)
	{
		fc |= _flag;
	}
	
	public void clearFlag(byte _flag)
	{
		fc &= ~_flag;
	}
	
	public boolean getFlag(byte _flag)
	{
		if (MASK(fc, _flag) != 0) {
			return true;
		} else {
			return false;
		}
	}
	
	public void setFC(byte _fc)
	{
		fc = _fc;
	}
	
	public byte getFC()
	{
		return fc;
	}
	
	public void setLen(int _len)
	{
		fc = (byte)(MASK(fc, PRTCL_FC_MASK) | 
				    MASK((byte)_len, PRTCL_LEN_MASK));
	}
	
	public int getLen()
	{
		return (int)PRTCL_GET_LEN(fc);
	}
	
	public void setData(byte[] _data)
	{
		data = _data;
	}
	
	public byte[] getData()
	{
		return data;
	}
	
	public void setECD(byte _ecd)
	{
		hd = (byte)(MASK(hd, PRTCL_HEAD_ID_MASK) | MASK(_ecd, PRTCL_HEAD_ECD_MASK));
	}
	
	public byte getECD()
	{
		return MASK(hd, PRTCL_HEAD_ECD_MASK);
	}
	
	public boolean checkECD()
	{
		byte[] combined = new byte[data.length + 1];
		byte ecd = 0;
		
		combined[0] = fc;
		System.arraycopy(data, 0, combined, 1, data.length);
		
		ecd = calculateECD(combined);
		
		if (MASK(ecd, PRTCL_HEAD_ECD_MASK) == MASK(hd, PRTCL_HEAD_ECD_MASK)) {
			return true;
		} else {
			return false;
		}
	}
	
	public void reCalculateECD()
	{
		byte[] combined = new byte[data.length + 1];
		byte ecd = 0;
		
		combined[0] = fc;
		System.arraycopy(data, 0, combined, 1, data.length);
		
		ecd = calculateECD(combined);
		
		hd = (byte)(MASK(hd, PRTCL_HEAD_ID_MASK) | MASK(ecd, PRTCL_HEAD_ECD_MASK));
	}
	
	public byte[] getByteArray()
	{
		byte[] combined = new byte[data.length + PRTCL_OVERHEAD];
		
		combined[0] = hd;
		combined[1] = fc;
		System.arraycopy(data, 0, combined, PRTCL_OVERHEAD, data.length);
		
		return combined;
	}
	
	public void clearAll()
	{
		hd = PRTCL_ID;
		fc = 0;
		data = new byte[0];
	}
	
	/************************************************************
	 * Private method.
	 ************************************************************/
	byte calculateECDByte(byte _data)
	{
	    return (byte)((_data >> 4) ^ (_data & 0x0f));
	}

	byte calculateECD(byte[] _data)
	{
	    byte xor_data = 0;
	    int i = 0;

	    xor_data = calculateECDByte(_data[0]);

	    for (i=1; i<_data.length; i++) {
	        xor_data ^= calculateECDByte(_data[i]);
	    }

	    return xor_data;
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
}
