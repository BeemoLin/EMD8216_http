package com.tcwa.control;

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.lang.Thread;

class receiveClass {
	public boolean debug = false;
	public byte[] data_all = new byte[4096];
	public byte success_flag; // Flag �]0:Send command Failed;
								// 0x63:Send command successfully�^
	public byte command; // command�]EMD8216 UDP COMMAND LIST�^

	public void Run() {
		success_flag = data_all[32];
		command = data_all[33];

		int i;
		for (i = 0; i < 50; i++) {
			if (debug == true)
				System.out.println("data_all[" + i + "] = " + data_all[i]);
		}
		if (debug == true)
			System.out.println("receiveData.Run");
	}
}

class sendClass {
	public boolean debug = false;
	public byte[] data_all = new byte[48]; // card_name={��E��, ��M��, ��D��,
											// ��8��, ��2��, ��1��, ��6�� };

	public String card_name;
	public String password = "00000000"; // password,8 words
	public byte command; // command�]EMD8216 UDP COMMAND LIST�^

	public void run() {
		byte[] Card = new byte[7];
		try {
			Card = card_name.getBytes("US-ASCII");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for (int i = 0; i < card_name.length(); i++) {
			data_all[i] = Card[i];
		}
		byte[] Password = new byte[8];
		try {
			Password = password.getBytes("US-ASCII");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for (int i = 0; i < password.length(); i++) {
			data_all[7 + i] = Password[i];
		}
		data_all[15] = command;

		int i;
		for (i = 0; i < 20; i++) {
			if (debug == true)
				System.out.println("data_all[" + i + "] = " + data_all[i]);
		}
		if (debug == true)
			System.out.println("sendClass.Run");
	}

}

public class JacEmxClass {
	int Status;
	public boolean debug = false;
	Thread mThread;

	private receiveClass receiveData = new receiveClass();
	private sendClass sendData = new sendClass();
	private commandClass command = new commandClass();
	private standaloneData[] standalone_data = new standaloneData[32];

	String DEST_IP;
	int DEST_PORT;
	int TimeOut, endTimeOut;

	public void initial(String IP, String CardName, String Password, int Remote_port, int timeOut) {
		DEST_IP = IP;
		DEST_PORT = Remote_port;
		TimeOut = timeOut;
		sendData.card_name = CardName;
		sendData.password = Password;
		
		// Create thread
        
		/*Thread thread = new Thread(r2);//创建线程
        thread.start();//启动线程
        
		mThread = new HandlerThread("initial");
		mThread.start();
		mThreadHandler = new Handler(mThread.getLooper());
		*/
	}

	public int send(int command) {
		sendData.command = (byte) command;
		long StartTime = System.currentTimeMillis();
		
		//mThreadHandler.post(r2);
		// Create thread
        Thread thread = new Thread(r2);//创建线程
        thread.start();//启动线程
        
		do {
			try {
				if (debug == true)
					System.out.println("send wait!!");
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} while (send_flag);
		if (debug == true)
			System.out.println("send ok!!");
		if (debug == true)
			System.out.println("used time : " + (System.currentTimeMillis() - StartTime));
		return Status;
	}

	boolean send_flag = true;

	DatagramSocket[] socket = new DatagramSocket[2000];
	DatagramPacket[] sendPacket = new DatagramPacket[2000];
	DatagramPacket[] receivePacket = new DatagramPacket[2000];
	int CardID;
	private Runnable r2 = new Runnable() {
		synchronized public void run() {
			sendData.run();
			send_flag = true;
			try {
				if (socket[CardID] == null) {
					if (debug == true)
						System.out.println("null");
					socket[CardID] = new DatagramSocket(null);
					socket[CardID].setReuseAddress(true);
					socket[CardID].bind(new InetSocketAddress(DEST_PORT));
					socket[CardID].setBroadcast(false);
				}
				InetAddress address;
				do {
					address = InetAddress.getByName(DEST_IP);
					// send data back to the client
					sendPacket[CardID] = new DatagramPacket(sendData.data_all, sendData.data_all.length, address,
							DEST_PORT);
					socket[CardID].setBroadcast(false);
					socket[CardID].send(sendPacket[CardID]);
					//System.out.println("JacEmxClass:CardID" + CardID + " send to " + address + ":" + DEST_PORT);
					// receive data from the client
					receivePacket[CardID] = new DatagramPacket(receiveData.data_all, receiveData.data_all.length);
					socket[CardID].setSoTimeout(TimeOut);
					socket[CardID].receive(receivePacket[CardID]);
					receiveData.Run();
					//System.out.println("JacEmxClass:CardID" + CardID + " receive from "
					//		+ receivePacket[CardID].getAddress() + ":" + receivePacket[CardID].getPort());
					if (debug == true)
						System.out.println("receiveData.success_flag = " + receiveData.success_flag);
					if (debug == true)
						System.out.println("receiveData.command = " + receiveData.command);
					Status = receiveData.success_flag;

					socket[CardID].close();
					socket[CardID] = null;
					send_flag = false;
				} while (send_flag);
			} catch (Exception e) {
				send_flag = false;
				Status = errorCodeClass.TIME_OUT_ERROR;
				if (debug == true)
					System.out.println(e);
			}
			if (debug == true)
				System.out.println("End thread");
		}
	};

	public final byte COMMAND_SUCCESSFUL = 99;

	// --- Initial / Close
	public int EMD8216_initial(int cardID, short[] IP, int Host_Port, int Remote_port, int TimeOut_ms,
			parameterClass CardType) {
		System.out.println("JacEmxClass: CardID:" + cardID + "IP:" + IP[3] + "Host_Port:" + Host_Port + "Remote_port:"
				+ Remote_port);
		CardID = cardID;
		DEST_IP = Short.toString(IP[0]) + "." + Short.toString(IP[1]) + "." + Short.toString(IP[2]) + "."
				+ Short.toString(IP[3]);
		DEST_PORT = Remote_port;
		TimeOut = TimeOut_ms;
		sendData.card_name = "EMD8216";
		// Create thread
		//mThread = new HandlerThread("EMD8216");
		//mThread.start();
		//mThreadHandler = new Handler(mThread.getLooper());
		Status = send(command.GET_CARD_TYPE);
		if (Status == errorCodeClass.TIME_OUT_ERROR) {
			return Status;
		}
		parameterClass.cardType = receiveData.data_all[33];
		if (receiveData.data_all[32] == COMMAND_SUCCESSFUL)
			Status = errorCodeClass.JSDRV_NO_ERROR;
		if (debug == true)
			System.out.println("Status = " + Status);
		return Status;
	}

	public int EMD8216_firmware_version_read(int cardID, parameterClass Version) {
		CardID = cardID;
		Status = send(command.GET_FIRMWARE_VERSION);
		if (receiveData.data_all[32] == COMMAND_SUCCESSFUL)
			Status = errorCodeClass.JSDRV_NO_ERROR;
		parameterClass.version[0] = receiveData.data_all[0];
		parameterClass.version[1] = receiveData.data_all[1];
		return Status;
	}

	public int EMD8216_close(int cardID) {
		return errorCodeClass.JSDRV_NO_ERROR;
	}

	// --- Input / Output

	public int EMD8216_port_polarity_set(int cardID, byte port, short polarity) {
		CardID = cardID;
		sendData.data_all[0] = port;
		sendData.data_all[1] = (byte) polarity;
		Status = send(command.SET_POLARITY);
		if (receiveData.data_all[32] == COMMAND_SUCCESSFUL)
			Status = errorCodeClass.JSDRV_NO_ERROR;
		return Status;
	}

	public int EMD8216_port_polarity_read(int cardID, byte port, parameterClass polarity) {
		CardID = cardID;
		sendData.data_all[16] = port;
		Status = send(command.READ_POLARITY);
		if (receiveData.data_all[32] == COMMAND_SUCCESSFUL)
			Status = errorCodeClass.JSDRV_NO_ERROR;
		if (sendData.data_all[15] != receiveData.data_all[33])
			return errorCodeClass.COMMAND_ERROR;
		if (sendData.data_all[16] != receiveData.data_all[0])
			return errorCodeClass.PORT_ERROR;
		parameterClass.polarity = (short) receiveData.data_all[1];
		return Status;
	}

	public int EMD8216_port_set(int cardID, byte port, short data) {
		System.out.println("JacEmxClass: click from LightControl CardID=" + CardID);
		CardID = cardID;
		sendData.data_all[16] = port;
		sendData.data_all[17] = (byte) data;
		Status = send(command.SET_PORT);
		if (receiveData.data_all[32] == COMMAND_SUCCESSFUL)
			Status = errorCodeClass.JSDRV_NO_ERROR;
		if (sendData.data_all[15] != receiveData.data_all[33])
			return errorCodeClass.COMMAND_ERROR;
		return Status;
	}

	public int EMD8216_port_read(int cardID, byte port, parameterClass data) {
		CardID = cardID;
		sendData.data_all[16] = port;
		Status = send(command.READ_PORT);
		if (receiveData.data_all[32] == COMMAND_SUCCESSFUL)
			Status = errorCodeClass.JSDRV_NO_ERROR;
		if (sendData.data_all[15] != receiveData.data_all[33])
			return errorCodeClass.COMMAND_ERROR;
		if (sendData.data_all[16] != receiveData.data_all[0])
			return errorCodeClass.PORT_ERROR;
		parameterClass.data = (short) receiveData.data_all[1];
		return Status;
	}

	public int EMD8216_port_config_set(int cardID, byte port, short config) {
		CardID = cardID;
		sendData.data_all[16] = port;
		sendData.data_all[17] = (byte) config;
		Status = send(command.SET_PORT_CONFIG);
		if (receiveData.data_all[32] == COMMAND_SUCCESSFUL)
			Status = errorCodeClass.JSDRV_NO_ERROR;
		if (sendData.data_all[15] != receiveData.data_all[33])
			return errorCodeClass.COMMAND_ERROR;
		return Status;
	}

	public int EMD8216_port_config_read(int cardID, byte port, parameterClass config) {
		CardID = cardID;
		sendData.data_all[16] = port;
		Status = send(command.READ_PORT_CONFIG);
		if (receiveData.data_all[32] == COMMAND_SUCCESSFUL)
			Status = errorCodeClass.JSDRV_NO_ERROR;
		if (sendData.data_all[15] != receiveData.data_all[33])
			return errorCodeClass.COMMAND_ERROR;
		if (sendData.data_all[16] != receiveData.data_all[0])
			return errorCodeClass.PORT_ERROR;
		parameterClass.config = (short) receiveData.data_all[1];
		return Status;
	}

	public int EMD8216_point_polarity_set(int cardID, byte port, byte point, byte data) {
		CardID = cardID;
		sendData.data_all[16] = port;
		sendData.data_all[17] = (byte) point;
		sendData.data_all[18] = (byte) data;
		Status = send(command.SET_POINT_POLARITY);
		if (receiveData.data_all[32] == COMMAND_SUCCESSFUL)
			Status = errorCodeClass.JSDRV_NO_ERROR;
		if (sendData.data_all[15] != receiveData.data_all[33])
			return errorCodeClass.COMMAND_ERROR;
		return Status;
	}

	public int EMD8216_point_polarity_read(int cardID, byte port, byte point, parameterClass state) {
		CardID = cardID;
		sendData.data_all[16] = port;
		sendData.data_all[17] = point;
		Status = send(command.READ_POINT_POLARITY);
		if (receiveData.data_all[32] == COMMAND_SUCCESSFUL)
			Status = errorCodeClass.JSDRV_NO_ERROR;
		if (sendData.data_all[15] != receiveData.data_all[33])
			return errorCodeClass.COMMAND_ERROR;
		if (sendData.data_all[16] != receiveData.data_all[0])
			return errorCodeClass.PORT_ERROR;
		if (sendData.data_all[17] != receiveData.data_all[1])
			return errorCodeClass.IN_POINT_ERROR;
		parameterClass.state = receiveData.data_all[2];
		return Status;
	}

	public int EMD8216_point_set(int cardID, byte port, byte point, byte state) {
		CardID = cardID;
		sendData.data_all[16] = port;
		sendData.data_all[17] = (byte) point;
		sendData.data_all[18] = (byte) state;
		Status = send(command.SET_POINT);
		if (receiveData.data_all[32] == COMMAND_SUCCESSFUL)
			Status = errorCodeClass.JSDRV_NO_ERROR;
		if (sendData.data_all[15] != receiveData.data_all[33])
			return errorCodeClass.COMMAND_ERROR;
		return Status;
	}

	public int EMD8216_point_read(int cardID, byte port, byte point, parameterClass state) {
		CardID = cardID;
		sendData.data_all[16] = port;
		sendData.data_all[17] = point;
		Status = send(command.READ_POINT);
		if (receiveData.data_all[32] == COMMAND_SUCCESSFUL)
			Status = errorCodeClass.JSDRV_NO_ERROR;
		if (sendData.data_all[15] != receiveData.data_all[33])
			return errorCodeClass.COMMAND_ERROR;
		if (sendData.data_all[16] != receiveData.data_all[0])
			return errorCodeClass.PORT_ERROR;
		if (sendData.data_all[17] != receiveData.data_all[1])
			return errorCodeClass.IN_POINT_ERROR;
		parameterClass.state = receiveData.data_all[2];
		return Status;
	}

	public int EMD8216_point_config_set(int cardID, byte port, byte point, byte config) {
		CardID = cardID;
		sendData.data_all[16] = port;
		sendData.data_all[17] = (byte) point;
		sendData.data_all[18] = (byte) config;
		Status = send(command.SET_POINT_CONFIG);
		if (receiveData.data_all[32] == COMMAND_SUCCESSFUL)
			Status = errorCodeClass.JSDRV_NO_ERROR;
		if (sendData.data_all[15] != receiveData.data_all[33])
			return errorCodeClass.COMMAND_ERROR;
		return Status;
	}

	public int EMD8216_point_config_read(int cardID, byte port, byte point, parameterClass config) {
		CardID = cardID;
		sendData.data_all[16] = port;
		sendData.data_all[17] = point;
		Status = send(command.READ_POINT_CONFIG);
		if (receiveData.data_all[32] == COMMAND_SUCCESSFUL)
			Status = errorCodeClass.JSDRV_NO_ERROR;
		if (sendData.data_all[15] != receiveData.data_all[33])
			return errorCodeClass.COMMAND_ERROR;
		if (sendData.data_all[16] != receiveData.data_all[0])
			return errorCodeClass.PORT_ERROR;
		if (sendData.data_all[17] != receiveData.data_all[1])
			return errorCodeClass.IN_POINT_ERROR;
		parameterClass.config = receiveData.data_all[2];
		return Status;
	}

	// EMD8216_polarity_read(u32 CardID,u8 port,u8 *data);
	// EMD8216_polarity_set(u32 CardID,u8 port,u8 data);
	// EMD8216_port_read(u32 CardID,u8 port,u8 *data);
	// EMD8216_port_set(u32 CardID,u8 port,u8 data);
	// EMD8216_port_config_read(u32 CardID ,u8 port, u8 *config);
	// EMD8216_port_config_set(u32 CardID ,u8 port, u8 config);

	// EMD8216_point_polarity_read(u32 CardID,u8 port,u8 point,u8 *data);
	// EMD8216_point_polarity_set(u32 CardID,u8 port,u8 point,u8 data);
	// EMD8216_point_read(u32 CardID,u8 port,u8 point,u8 *state);
	// EMD8216_point_set(u32 CardID, u8 port, u8 point, u8 state);
	// EMD8216_point_config_read(u32 CardID, u8 port, u8 point, u8 *config);
	// EMD8216_point_config_set(u32 CardID, u8 port, u8 point, u8 config);

	// --- Counter
	public int EMD8216_counter_mask_set(int cardID, byte port, short channel) {
		CardID = cardID;
		sendData.data_all[16] = port;
		sendData.data_all[17] = (byte) channel;
		Status = send(command.SET_COUNTER_MASK);
		if (receiveData.data_all[32] == COMMAND_SUCCESSFUL)
			Status = errorCodeClass.JSDRV_NO_ERROR;
		if (sendData.data_all[15] != receiveData.data_all[33])
			return errorCodeClass.COMMAND_ERROR;
		return Status;
	}

	public int EMD8216_counter_enable(int cardID) {
		CardID = cardID;
		Status = send(command.ENABLE_COUNTER_MODE);
		if (receiveData.data_all[32] == COMMAND_SUCCESSFUL)
			Status = errorCodeClass.JSDRV_NO_ERROR;
		if (sendData.data_all[15] != receiveData.data_all[33])
			return errorCodeClass.COMMAND_ERROR;
		return Status;
	}

	public int EMD8216_counter_disable(int cardID) {
		CardID = cardID;
		Status = send(command.DISABLE_COUNTER_MODE);
		if (receiveData.data_all[32] == COMMAND_SUCCESSFUL)
			Status = errorCodeClass.JSDRV_NO_ERROR;
		if (sendData.data_all[15] != receiveData.data_all[33])
			return errorCodeClass.COMMAND_ERROR;
		return Status;
	}

	public int EMD8216_counter_read(int cardID, byte port, parameterClass counter) {
		CardID = cardID;
		sendData.data_all[16] = port;
		Status = send(command.READ_COUNTER);
		if (receiveData.data_all[32] == COMMAND_SUCCESSFUL)
			Status = errorCodeClass.JSDRV_NO_ERROR;
		if (sendData.data_all[15] != receiveData.data_all[33])
			return errorCodeClass.COMMAND_ERROR;
		if (sendData.data_all[16] != receiveData.data_all[0])
			return errorCodeClass.PORT_ERROR;
		for (int index = 0; index < 8; index++)
			parameterClass.counter[index] = receiveData.data_all[index];
		return Status;
	}

	public int EMD8216_counter_clear(int cardID, byte port, short channel) {
		CardID = cardID;
		sendData.data_all[16] = port;
		sendData.data_all[17] = (byte) channel;
		Status = send(command.CLEAR_COUNTER);
		if (receiveData.data_all[32] == COMMAND_SUCCESSFUL)
			Status = errorCodeClass.JSDRV_NO_ERROR;
		if (sendData.data_all[15] != receiveData.data_all[33])
			return errorCodeClass.COMMAND_ERROR;
		return Status;
	}

	// EMD8216_counter_mask_set(u32 CardID,u8 port,u8 channel);
	// EMD8216_counter_enable(u32 CardID);
	// EMD8216_counter_disable(u32 CardID);
	// EMD8216_counter_read(u32 CardID,u8 port,u32 counter[8]);
	// EMD8216_counter_clear(u32 CardID,u8 port,u8 channel);

	// --- software key
	public int EMD8216_security_unlock(int cardID, int[] password) {
		CardID = cardID;
		sendData.password = Integer.toString(password[0]) + Integer.toString(password[1])
				+ Integer.toString(password[2]) + Integer.toString(password[3]) + Integer.toString(password[4])
				+ Integer.toString(password[5]) + Integer.toString(password[6]) + Integer.toString(password[7]);
		Status = send(command.READ_POINT);
		if (debug == true)
			System.out.println("sendData.password = " + sendData.password);
		if (receiveData.data_all[32] == COMMAND_SUCCESSFUL)
			Status = errorCodeClass.JSDRV_NO_ERROR;
		if (sendData.data_all[15] != receiveData.data_all[33])
			return errorCodeClass.COMMAND_ERROR;
		return Status;
	}

	public int EMD8216_security_status_read(int cardID, parameterClass lock_status) {
		return 0;
	}

	public int EMD8216_password_change(int cardID, int[] Oldpassword, int[] password) {
		CardID = cardID;
		sendData.password = Integer.toString(Oldpassword[0]) + Integer.toString(Oldpassword[1])
				+ Integer.toString(Oldpassword[2]) + Integer.toString(Oldpassword[3]) + Integer.toString(Oldpassword[4])
				+ Integer.toString(Oldpassword[5]) + Integer.toString(Oldpassword[6])
				+ Integer.toString(Oldpassword[7]);
		for (int index = 0; index < 8; index++)
			sendData.data_all[16 + index] = (byte) password[index];
		Status = send(command.CHANGE_PASSWORD);
		if (receiveData.data_all[32] == COMMAND_SUCCESSFUL)
			Status = errorCodeClass.JSDRV_NO_ERROR;
		if (sendData.data_all[15] != receiveData.data_all[33])
			return errorCodeClass.COMMAND_ERROR;
		return Status;
	}

	public int EMD8216_password_set_default(int cardID) {
		CardID = cardID;
		Status = send(command.RESTORE_PASSWORD);
		if (receiveData.data_all[32] == COMMAND_SUCCESSFUL)
			Status = errorCodeClass.JSDRV_NO_ERROR;
		if (sendData.data_all[15] != receiveData.data_all[33])
			return errorCodeClass.COMMAND_ERROR;
		return Status;
	}

	// --- software key
	// EMD8216_security_unlock(u32 CardID,u8 password[8]);
	// EMD8216_security_status_read(u32 CardID,u8 *lock_status);
	// EMD8216_password_change(u32 CardID,u8 Oldpassword[8],u8 Password[8]);
	// EMD8216_password_set_default(u32 CardID);

	// --- Miscellaneous
	public int EMD8216_change_socket_port(int cardID, int Remote_port) {
		CardID = cardID;
		sendData.data_all[16] = (byte) (Remote_port & 255);
		sendData.data_all[17] = (byte) (Remote_port >> 8);
		Status = send(command.CHANGE_SOCKETPORT);
		if (receiveData.data_all[32] == COMMAND_SUCCESSFUL)
			Status = errorCodeClass.JSDRV_NO_ERROR;
		if (sendData.data_all[15] != receiveData.data_all[33])
			return errorCodeClass.COMMAND_ERROR;
		return Status;
	}

	public int EMD8216_change_IP(int cardID, byte[] IP) {
		CardID = cardID;
		sendData.data_all[16] = IP[0];
		sendData.data_all[17] = IP[1];
		sendData.data_all[18] = IP[2];
		sendData.data_all[19] = IP[3];
		Status = send(command.CHANGE_IP);
		if (receiveData.data_all[32] == COMMAND_SUCCESSFUL)
			Status = errorCodeClass.JSDRV_NO_ERROR;
		if (sendData.data_all[15] != receiveData.data_all[33])
			return errorCodeClass.COMMAND_ERROR;
		return Status;
	}

	public int EMD8216_reboot(int cardID) {
		CardID = cardID;
		Status = send(command.REBOOT);
		if (receiveData.data_all[32] == COMMAND_SUCCESSFUL)
			Status = errorCodeClass.JSDRV_NO_ERROR;
		if (sendData.data_all[15] != receiveData.data_all[33])
			return errorCodeClass.COMMAND_ERROR;
		return Status;
	}

	// --- WDT
	public int EMD8216_WDT_enable(int cardID) {
		CardID = cardID;
		Status = send(command.ENABLE_WDT);
		if (receiveData.data_all[32] == COMMAND_SUCCESSFUL)
			Status = errorCodeClass.JSDRV_NO_ERROR;
		if (sendData.data_all[15] != receiveData.data_all[33])
			return errorCodeClass.COMMAND_ERROR;
		return Status;
	}

	public int EMD8216_WDT_disable(int cardID) {
		CardID = cardID;
		Status = send(command.DISABLE_WDT);
		if (receiveData.data_all[32] == COMMAND_SUCCESSFUL)
			Status = errorCodeClass.JSDRV_NO_ERROR;
		if (sendData.data_all[15] != receiveData.data_all[33])
			return errorCodeClass.COMMAND_ERROR;
		return Status;
	}

	public int EMD8216_WDT_set(int cardID, int time, byte[] state) {
		CardID = cardID;
		sendData.data_all[16] = (byte) (time & 255);
		sendData.data_all[17] = (byte) (time >> 8);
		sendData.data_all[18] = state[0];
		sendData.data_all[19] = state[1];
		Status = send(command.SET_WDT);
		if (receiveData.data_all[32] == COMMAND_SUCCESSFUL)
			Status = errorCodeClass.JSDRV_NO_ERROR;
		if (sendData.data_all[15] != receiveData.data_all[33])
			return errorCodeClass.COMMAND_ERROR;
		return Status;
	}

	public int EMD8216_WDT_read(int cardID, parameterClass WDT_time, parameterClass WDT_state,
			parameterClass WDT_enable) {
		CardID = cardID;
		Status = send(command.READ_WDT);
		if (receiveData.data_all[32] == COMMAND_SUCCESSFUL)
			Status = errorCodeClass.JSDRV_NO_ERROR;
		if (sendData.data_all[15] != receiveData.data_all[33])
			return errorCodeClass.COMMAND_ERROR;
		int time = 0, time_L = 0, time_H = 0;
		if (receiveData.data_all[1] < 0) {
			time_H = receiveData.data_all[1] + 256;
			time_H = ((time_H) << 8);
		} else {
			time_H = receiveData.data_all[1];
			time_H = ((time_H) << 8);
		}
		if (receiveData.data_all[0] < 0) {
			time_L = receiveData.data_all[0] + 256;
		} else {
			time_L = receiveData.data_all[0];
		}
		System.out.println("time_L = " + time_L);
		time = time_H + time_L;
		System.out.println("time = " + time);
		parameterClass.WDT_time = time;
		parameterClass.WDT_state[0] = receiveData.data_all[2];
		parameterClass.WDT_state[1] = receiveData.data_all[3];
		parameterClass.WDT_enable = receiveData.data_all[4];

		for (int i = 0; i < 5; i++) {
			if (debug == true)
				System.out.println("receiveData.data_all[" + i + "] = " + receiveData.data_all[i]);
		}
		return Status;
	}

	// --- WDT
	// EMD8216_WDT_enable(u32 CardID);
	// EMD8216_WDT_disable(u32 CardID);
	// EMD8216_WDT_set(u32 CardID,u16 time,u8 state[2]);
	// EMD8216_WDT_read(u32 CardID,u16 *time,u8 state[2],u8 *enable);

	// --- standalone

	public int EMD8216_standalone_enable(int cardID) {
		CardID = cardID;
		Status = send(command.ENABLE_STANDALONE);
		if (receiveData.data_all[32] == COMMAND_SUCCESSFUL)
			Status = errorCodeClass.JSDRV_NO_ERROR;
		if (sendData.data_all[15] != receiveData.data_all[33])
			return errorCodeClass.COMMAND_ERROR;
		return Status;
	}

	public int EMD8216_standalone_disable(int cardID) {
		CardID = cardID;
		Status = send(command.DISABLE_STANDALONE);
		if (receiveData.data_all[32] == COMMAND_SUCCESSFUL)
			Status = errorCodeClass.JSDRV_NO_ERROR;
		if (sendData.data_all[15] != receiveData.data_all[33])
			return errorCodeClass.COMMAND_ERROR;
		return Status;
	}

	public int EMD8216_standalone_config_set(int cardID, byte standalone_number, standaloneData[] data,
			byte standalone_state) {
		CardID = cardID;
		for (int index = 0; index < standalone_number; index += 2) {
			sendData.data_all[16 + 0] = (byte) index;
			sendData.data_all[16 + 1] = standalone_number;
			sendData.data_all[16 + 2] = data[index].timer_mode;
			sendData.data_all[16 + 3] = data[index + 1].timer_mode;
			sendData.data_all[16 + 4] = (byte) (data[index].timer_value & 255);
			sendData.data_all[16 + 5] = (byte) (data[index].timer_value >> 8);
			sendData.data_all[16 + 6] = (byte) (data[index + 1].timer_value & 255);
			sendData.data_all[16 + 7] = (byte) (data[index + 1].timer_value >> 8);
			sendData.data_all[16 + 8] = (byte) (data[index].in_point_bit & 255);
			sendData.data_all[16 + 9] = (byte) (data[index].in_point_bit >> 8);
			sendData.data_all[16 + 10] = (byte) (data[index + 1].in_point_bit & 255);
			sendData.data_all[16 + 11] = (byte) (data[index + 1].in_point_bit >> 8);
			sendData.data_all[16 + 12] = (byte) (data[index].in_state_bit & 255);
			sendData.data_all[16 + 13] = (byte) (data[index].in_state_bit >> 8);
			sendData.data_all[16 + 14] = (byte) (data[index + 1].in_state_bit & 255);
			sendData.data_all[16 + 15] = (byte) (data[index + 1].in_state_bit >> 8);
			sendData.data_all[16 + 16] = (byte) (data[index].out_point_bit & 255);
			sendData.data_all[16 + 17] = (byte) (data[index].out_point_bit >> 8);
			sendData.data_all[16 + 18] = (byte) (data[index + 1].out_point_bit & 255);
			sendData.data_all[16 + 19] = (byte) (data[index + 1].out_point_bit >> 8);
			sendData.data_all[16 + 20] = data[index].out_mode;
			sendData.data_all[16 + 21] = data[index + 1].out_mode;
			sendData.data_all[16 + 22] = standalone_state;
			Status = send(command.SET_STANDALONE_CONFIG);
			if (receiveData.data_all[32] == COMMAND_SUCCESSFUL)
				Status = errorCodeClass.JSDRV_NO_ERROR;
			if (sendData.data_all[15] != receiveData.data_all[33])
				return errorCodeClass.COMMAND_ERROR;
		}
		return Status;
	}

	/*
	 * 
	 * for ic struct onec two data. typedef struct_StandaloneData { u8
	 * function_index; // start function index //bit 0 u8 function_number; // be
	 * used max function number //bit 1 u8 timer_mode[2]; // set timer mode
	 * //bit 2~3 u16 timer_value[2]; // set time constant //bit 4~7 u16
	 * input_point[2]; // choose input IO_00 ~ IO_17 //bit 8~11 u16
	 * input_state[2]; // set input state //bit 12~15 u16 output_point[2]; //
	 * choose output IO_00 ~ IO_17 //bit 16~19 u8 out_mode[2]; // set out mode
	 * //bit 20~21 u8 standalone_state; // 1: standalone Enable; 0: standalone
	 * Disable //bit 22 }StandaloneData
	 * 
	 */
	public int EMD8216_standalone_config_read(int cardID, parameterClass standalone_function_number,
			standaloneData[] data, parameterClass standalone_enable, parameterClass standalone_power_on_enable) {
		CardID = cardID;

		Status = send(command.READ_STANDALONE_CONFIG);
		if (receiveData.data_all[32] == COMMAND_SUCCESSFUL)
			Status = errorCodeClass.JSDRV_NO_ERROR;
		if (sendData.data_all[15] != receiveData.data_all[33])
			return errorCodeClass.COMMAND_ERROR;
		parameterClass.standalone_function_number = receiveData.data_all[1];
		parameterClass.standalone_enable = (byte) (receiveData.data_all[22] & 1);
		parameterClass.standalone_power_on_enable = receiveData.data_all[23];
		for (int index = 0; index < parameterClass.standalone_function_number; index += 2) {
			sendData.data_all[16 + 0] = (byte) index;
			Status = send(command.READ_STANDALONE_CONFIG);
			if (receiveData.data_all[32] == COMMAND_SUCCESSFUL)
				Status = errorCodeClass.JSDRV_NO_ERROR;
			if (sendData.data_all[15] != receiveData.data_all[33])
				return errorCodeClass.COMMAND_ERROR;
			parameterClass.standalone_function_number = receiveData.data_all[1];
			data[index].timer_mode = receiveData.data_all[2];
			data[index + 1].timer_mode = receiveData.data_all[3];
			data[index].timer_value = TwoByteToInt(receiveData.data_all[4], receiveData.data_all[5]);
			data[index + 1].timer_value = TwoByteToInt(receiveData.data_all[6], receiveData.data_all[7]);
			data[index].in_point_bit = TwoByteToInt(receiveData.data_all[8], receiveData.data_all[9]);
			data[index + 1].in_point_bit = TwoByteToInt(receiveData.data_all[10], receiveData.data_all[11]);
			data[index].in_state_bit = TwoByteToInt(receiveData.data_all[12], receiveData.data_all[13]);
			data[index + 1].in_state_bit = TwoByteToInt(receiveData.data_all[14], receiveData.data_all[15]);
			data[index].out_point_bit = TwoByteToInt(receiveData.data_all[16], receiveData.data_all[17]);
			data[index + 1].out_point_bit = TwoByteToInt(receiveData.data_all[18], receiveData.data_all[19]);
			data[index].out_mode = receiveData.data_all[20];
			data[index + 1].out_mode = receiveData.data_all[21];
		}
		return Status;
	}

	int TwoByteToInt(byte byte_L, byte byte_H) {
		int data_L = 0, data_H = 0;
		if (byte_H < 0) {
			data_H = byte_H + 256;
			data_H = ((data_H) << 8);
		} else {
			data_H = byte_H;
			data_H = ((data_H) << 8);
		}
		if (byte_L < 0) {
			data_L = byte_L + 256;
		} else {
			data_L = byte_L;
		}
		return data_H + data_L;
	}
	// --- standalone
	// EMD8216_standalone_enable(u32 CardID);
	// EMD8216_standalone_disable(u32 CardID);
	// EMD8216_standalone_config_set(u32 CardID,u8
	// function_number,_StandaloneData data[32],u8 standalone_state);
	// EMD8216_standalone_config_read(u32 CardID,u8
	// *function_number,_StandaloneData data[32],u8 *enable,u8
	// *power_on_enable);

}

class standaloneData {
	public int in_point_bit;
	public int in_state_bit;
	public int timer_value;
	public int out_point_bit;
	public byte timer_mode;
	public byte out_mode;
}

class errorCodeClass {
	public final static int JSDRV_NO_ERROR = 0;
	public final static int INITIAL_SOCKET_ERROR = 1;
	public final static int IP_ADDRESS_ERROR = 2;
	public final static int UNLOCK_ERROR = 3;
	public final static int LOCK_COUNTER_ERROR = 4;
	public final static int SET_SECURITY_ERROR = 5;
	public final static int DEVICE_RW_ERROR = 100;
	public final static int NO_CARD = 101;
	public final static int DUPLICATE_ID = 102;
	public final static int ID_ERROR = 300;
	public final static int PORT_ERROR = 301;
	public final static int IN_POINT_ERROR = 302;
	public final static int OUT_POINT_ERROR = 303;
	public final static int PARAMETERS_ERROR = 305;
	public final static int CHANGE_SOCKET_ERROR = 306;
	public final static int UNLOCK_SECURITY_ERROR = 307;
	public final static int PASSWORD_ERROR = 308;
	public final static int REBOOT_ERROR = 309;
	public final static int TIME_OUT_ERROR = 310;
	public final static int CREAT_SOCKET_ERROR = 311;
	public final static int CHANGE_IP_ERROR = 312;
	public final static int MASK_ERROR = 313;
	public final static int COUNTER_ENABLE_ERROR = 314;
	public final static int COUNTER_DISABLE_ERROR = 315;
	public final static int COUNTER_READ_ERROR = 316;
	public final static int COUNTER_CLEAR_ERROR = 317;
	public final static int TIME_ERROR = 318;
	public final static int CARD_VERSION_ERROR = 320;
	public final static int STANDALONE_ENABLE_ERROR = 321;
	public final static int STANDALONE_DISABLE_ERROR = 322;
	public final static int STANDALONE_CONFIG_ERROR = 323;
	public final static int COMMAND_ERROR = 323;
}

class commandClass {
	public final byte GET_CARD_TYPE = (byte) Integer.parseInt("01", 16);
	public final byte REBOOT = (byte) Integer.parseInt("02", 16);
	public final byte CHANGE_SOCKETPORT = (byte) Integer.parseInt("03", 16);
	public final byte CHANGE_PASSWORD = (byte) Integer.parseInt("04", 16);
	public final byte RESTORE_PASSWORD = (byte) Integer.parseInt("05", 16);
	public final byte CHANGE_IP = (byte) Integer.parseInt("06", 16);
	public final byte GET_FIRMWARE_VERSION = (byte) Integer.parseInt("07", 16);
	public final byte WRITE_MAC = (byte) Integer.parseInt("FA", 16);
	public final byte SET_COUNTER_MASK = (byte) Integer.parseInt("20", 16);
	public final byte ENABLE_COUNTER_MODE = (byte) Integer.parseInt("21", 16);
	public final byte DISABLE_COUNTER_MODE = (byte) Integer.parseInt("22", 16);
	public final byte READ_COUNTER = (byte) Integer.parseInt("23", 16);
	public final byte CLEAR_COUNTER = (byte) Integer.parseInt("24", 16);
	public final byte SET_PORT_CONFIG = (byte) Integer.parseInt("30", 16);
	public final byte READ_PORT_CONFIG = (byte) Integer.parseInt("31", 16);
	public final byte SET_PORT = (byte) Integer.parseInt("32", 16);
	public final byte READ_PORT = (byte) Integer.parseInt("33", 16);
	public final byte SET_POLARITY = (byte) Integer.parseInt("34", 16);
	public final byte READ_POLARITY = (byte) Integer.parseInt("35", 16);
	public final byte SET_POINT_CONFIG = (byte) Integer.parseInt("36", 16);
	public final byte READ_POINT_CONFIG = (byte) Integer.parseInt("37", 16);
	public final byte SET_POINT = (byte) Integer.parseInt("38", 16);
	public final byte READ_POINT = (byte) Integer.parseInt("39", 16);
	public final byte SET_POINT_POLARITY = (byte) Integer.parseInt("3A", 16);
	public final byte READ_POINT_POLARITY = (byte) Integer.parseInt("3B", 16);
	public final byte ENABLE_STANDALONE = (byte) Integer.parseInt("50", 16);
	public final byte DISABLE_STANDALONE = (byte) Integer.parseInt("51", 16);
	public final byte SET_STANDALONE_CONFIG = (byte) Integer.parseInt("52", 16);
	public final byte READ_STANDALONE_CONFIG = (byte) Integer.parseInt("53", 16);
	public final byte ENABLE_WDT = (byte) Integer.parseInt("60", 16);
	public final byte DISABLE_WDT = (byte) Integer.parseInt("61", 16);
	public final byte SET_WDT = (byte) Integer.parseInt("62", 16);
	public final byte READ_WDT = (byte) Integer.parseInt("63", 16);
}
