import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class irService {

	private boolean mRunning;
	
	final boolean debug = true;

	boolean send_flag = true;

	private static Socket socket;

	private static final int SERVERPORT = 502;
	private static String SERVER_IP = "192.168.0.100";

	final int NO_ERROR = 0;
	int CardID;
	int status = 0;
	String[] ip = new String[4];
	final int[] password = { 1, 2, 3, 4, 5, 6, 7, 8 };
	boolean[] portStatus = new boolean[16];
	boolean[] portData = new boolean[16];
	short[] old_config = new short[2], old_state = new short[2];
	byte frist_flag = 0;


	int[] point_on = new int[16];
	int[] point_off = new int[16];
	int circuit_error, password_error, alert_off;
	boolean[] played = new boolean[16];

	ClientThread ir_task;
	Thread ir_thread;
	
	public irService(String ip) {

		mRunning = false;
		
		SERVER_IP = ip;

		ir_task = new ClientThread();
		ir_thread = new Thread(ir_task);
		ir_thread.start();
	}
	
	public void send() {
		try {
			String str = "";
			// 切換 IR output 1 or 2
			//String irOutput = "003"; // IRoutput 1+2

			
			str = "01020000000B0110044F00020400010003";

			byte[] bytes = hexStringToByteArray(str);

			sendBytes(bytes, 0, bytes.length);
			System.out.println("send data:" + byteArrayToHexString(bytes));

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];

		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}

		return data;
	}

	public static String byteArrayToHexString(byte[] b) {
		int len = b.length;
		String data = new String();

		for (int i = 0; i < len; i++) {
			data += Integer.toHexString((b[i] >> 4) & 0xf);
			data += Integer.toHexString(b[i] & 0xf);
		}
		return data;
	}

	public static void sendBytes(byte[] myByteArray, int start, int len) throws IOException {
		if (len < 0)
			throw new IllegalArgumentException("Negative length not allowed");
		if (start < 0 || start >= myByteArray.length)
			throw new IndexOutOfBoundsException("Out of bounds: " + start);
		// Other checks if needed.

		// May be better to save the streams in the support class;
		// just like the socket variable.
		OutputStream out = socket.getOutputStream();
		DataOutputStream dos = new DataOutputStream(out);

		dos.writeInt(len);
		if (len > 0) {
			dos.write(myByteArray, start, len);
		}
	}
	
	public static void sendCommand(String irCommand) {
		try {
			//byte[] bytes = hexStringToByteArray(irCommand);
			byte[] bytes = (irCommand + "\r\n").getBytes(StandardCharsets.UTF_8);
			for(byte b: bytes) {
				System.out.print(","+ Integer.toHexString(b));
			}
			sendBytes(bytes, 0, bytes.length);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public class ClientThread implements Runnable {
		private boolean isRunning = true;
		
		private BufferedReader input;

		@Override
		public void run() {
			while(isRunning) 
	        {
				try {
					InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
	
					socket = new Socket(serverAddr, SERVERPORT);
	
					input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	
					String line;
					while ((line = input.readLine()) != null) {
						System.out.println("receive data:" + line);
					}
	
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
	        }	
		}
		
		public void stopThread() 
	    {
	        this.isRunning = false;
	    }
	}

}