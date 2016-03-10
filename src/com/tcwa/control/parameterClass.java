package com.tcwa.control;

public class parameterClass {
	public static byte cardType;
	public static short version[] = new short[2];
	public static short config;
	public static short data;
	public static short polarity;
	public static byte state;
	public static byte counter[] = new byte[8];
	public static byte lock_status;
	public static int WDT_time;
	public static byte WDT_state[] = new byte[2];
	public static byte WDT_enable;
	public static int standalone_function_number;
	public static byte standalone_enable;
	public static byte standalone_power_on_enable;

	private short[] PortData = new short[2];
	private boolean isChange;
	private boolean isInitial;
	private int[] password = new int[8];
	private short PortConfig[] = new short[2];
	private short[] ip = new short[4];
	private byte ioState[] = new byte[8];

	// get set PortData
	public short[] PortData() {
		return this.PortData;
	}

	public void PortData(short[] portData) {
		this.PortData = portData;
	}

	// get set isChange
	public boolean isChange() {
		return this.isChange;
	}

	public void isChange(boolean isChange) {
		this.isChange = isChange;
	}

	// get set isInitial
	public boolean isInitial() {
		return this.isInitial;
	}

	public void isInitial(boolean isInitial) {
		this.isInitial = isInitial;
	}

	// get set password
	public int[] password() {
		return this.password;
	}

	public void password(int[] password) {
		this.password = password;
	}

	// get set PortConfig
	public short[] PortConfig() {
		return this.PortConfig;
	}

	public void PortConfig(short[] PortConfig) {
		this.PortConfig = PortConfig;
	}

	// get set ip
	public short[] ip() {
		return this.ip;
	}

	public void ip(short[] ip) {
		this.ip = ip;
	}

	// get set xxx
	public byte[] ioState() {
		return this.ioState;
	}

	public void ioState(byte[] ioState) {
		this.ioState = ioState;
	}
}
