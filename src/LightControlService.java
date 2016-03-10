import java.io.IOException;

import com.tcwa.control.JacEmxClass;
import com.tcwa.control.parameterClass;

public class LightControlService {

	final boolean debug = false;

	boolean send_flag = true;

	private JacEmxClass emd8216 = new JacEmxClass();
	private parameterClass parameter = new parameterClass();

	final int NO_ERROR = 0;
	private int CardID = 2;
	private int status = 0;
	private final int[] password = { 1, 2, 3, 4, 5, 6, 7, 8 };
	private boolean[] portStatus = new boolean[16];
	private boolean[] portData = new boolean[16];
	
	short[] old_config = new short[2], old_state = new short[2];
	byte frist_flag = 0;

	int[] point_on = new int[16];
	int[] point_off = new int[16];
	int circuit_error, password_error, alert_off;
	boolean[] played = new boolean[16];

	// 窗簾point 11~17
	int window_point[] = { 11, 16 };

	// 客廳燈
	int living_room_light = 1;
	int living_room_control = 9;

	public boolean[] portData_status = new boolean[16];
	
	private JacEmxClass getEmd8216() {
		return emd8216;
	}

	private parameterClass getParameterClass() {
		return parameter;
	}
	
	public String serviceStart(short[] ip) {
		
		if(ipPing(ip)) {
			connect(ip);
			return "connect success";
		}
		
		return "connect fail";
	}
	
	private boolean ipPing(short[] ip) {
		String ipAddress = ip[0] + "." + ip[1] + "." + ip[2] + "." + ip[3];
				
		Process p1;
		int returnVal;
		boolean reachable = false;
		try {
			p1 = java.lang.Runtime.getRuntime().exec("ping -c 2 -t 1 " + ipAddress);
			returnVal = p1.waitFor();
			reachable = (returnVal==0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return reachable;
	}

	private void connect(short[] ip) {
		status = initial(ip);
		if (status == 0) {
			System.out.println(password);
			status = unlock(password);
			byte port = 0;
			ReadPortConfig(port);
			port = 1;
			ReadPortConfig(port);
			// port_set();

			frist_flag = 1;
			// create thread run the thing.

			Thread thread = new Thread(data_loop);//创建线程
	        thread.start();//启动线程
		}
	}


	private short bit_change_u8(int bite, short data, byte state) {
		short short_data;
		short short_state = (short) ((data >> bite) & 1);
		if (debug)
			System.out.println("short_state = " + short_state);
		if (short_state == state) {
			if (debug)
				System.out.println("0.short_state == state");
		} else if ((short_state != state) && state == 0) {
			short_data = (short) ~(short) (1 << bite);
			if (debug)
				System.out.println("1.short_state = " + short_state);
			data = (short) (short_data & data);
		} else if ((short_state != state) && state == 1) {
			short_data = (short) (1 << bite);
			if (debug)
				System.out.println("2.short_state = " + short_state);
			data = (short) (short_data + data);
		}
		if (debug)
			System.out.println("data = " + Integer.toBinaryString(data));
		return data;
	}

	// 變更 io 點的狀態
	boolean changePointStatus(int point_int, byte on_off) {
		byte point = (byte) point_int;
		byte port, state;

		state = (byte) on_off;
		port = (byte) (point < 8 ? 0 : 1);

		short buff = bit_change_u8(point - port * 8, getParameterClass().PortData()[port], state);
		// System.out.println("JacEmxClass: click from LightControl CardID=" +
		// CardID);
		
		getEmd8216().EMD8216_port_set(CardID, port, buff);
		if (debug)
			System.out.println("changePointStatus: EMD8216_port_set = " + status);

		if (status == NO_ERROR) {
			getParameterClass().PortData()[port] = buff;
			return true;
		}

		return false;
	}

	// �u�@�W��r1���u�@���e
	private Runnable data_loop = new Runnable() {
		synchronized public void run() {
			while (getParameterClass().isInitial()) {
	
				try {
					// beemolin
					byte port = 0;
					//ReadPortConfig(port);
					ReadPortData(port);
					port = 1;
					//ReadPortConfig(port);
					ReadPortData(port);
					
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		
		}
	};

	// ok
	private void ReadPortConfig(byte port) {
		status = getEmd8216().EMD8216_port_config_read(CardID, port, getParameterClass());
		if (debug)
			System.out.println("EMD8216_port_config_read = " + status);
		if (debug)
			System.out.println("Port " + port + " Config = " + getParameterClass().config);

		if (NO_ERROR == status) {
			getParameterClass().PortConfig()[port] = getParameterClass().config;
			if ((old_config[port] != getParameterClass().PortConfig()[port]) || frist_flag == 0) {
				old_config[port] = getParameterClass().PortConfig()[port];
				// parameterClass.Data = emd8216.Data;
				short point = 0;
				int buffer = 0;
				for (point = 0; point < 8; point++) {
					buffer = getParameterClass().config >> point;
					if (1 == (buffer & 1)) {
						portStatus[point + port * 8] = false;
					} else {
						portStatus[point + port * 8] = true;
					}
				}
			}
		}
	}

	private void ReadPortData(byte port) {
		status = getEmd8216().EMD8216_port_read(CardID, port,
				getParameterClass());
		if (debug)
			System.out.println("EMD8216_port_read = " + status);
		if (debug)
			System.out.println("Port " + port + " = " + this.getParameterClass().data);
		if (NO_ERROR == status) {
			getParameterClass().PortData()[port] = this.getParameterClass().data;
			if (debug) System.out.println("parameter.data = " + this.getParameterClass().data);
			// if((old_state[port] != parameterClass.PortData[port]) ||
			// frist_flag == 0 )
			if (true) {
				old_state[port] = getParameterClass().PortData()[port];
				short point = 0;
				int buffer = 0;
				for (point = 0; point < 8; point++) {
					buffer = getParameterClass().data >> point;

					if (1 == (buffer & 1)) {
						// 保全開啟 偵測全部do點
						// Port_ToggleButton[point+port*8].setChecked(true);
						portData[point + port * 8] = true;
						
						// for httpserver control
						portData_status[point + port * 8] = true;
						
						// 找到相對port的Button
						int btnKey = point + port * 8 + 1;
						if (debug) System.out.println("point" + btnKey + " = " + portData_status[point + port * 8]);
					} else {
						// Port_ToggleButton[point+port*8].setChecked(false);
						portData[point + port * 8] = false;
						
						// for httpserver control
						portData_status[point + port * 8] = false;
						
						// 找到相對port的Button
						int btnKey = point + port * 8 + 1;
						if (debug) System.out.println("point" + btnKey + " = " + portData_status[point + port * 8]);
					}
				}
			}
		}
	}

	int unlock(int[] password) {
		status = this.getEmd8216().EMD8216_security_unlock(CardID, password);
		if (debug)
			System.out.println("EMD8216_security_unlock = " + NO_ERROR + "===" + status);
		if (NO_ERROR == status) {
			this.getParameterClass().password(password);
		}
		return status;
	}

	int initial(short[] ip) {
		for (int sock = 100; sock < 110; sock++) {
			status = this.getEmd8216().EMD8216_initial(CardID, ip, sock, 6936, 1000,
					this.getParameterClass());
			if (debug)
				System.out.println("EMD8216_initial status = " + status);
			if (debug)
				System.out.println("CardType = " + this.getParameterClass().cardType);
			if (status == NO_ERROR) {
				this.getParameterClass().ip(ip);
				this.getParameterClass().isInitial(true);
				sock = 110;
			}
		}
		return status;
	}


}
