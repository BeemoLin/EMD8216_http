import java.io.OutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class IRControlService {
	
	final boolean debug = true;

	private final int SERVERPORT = 502;
	private String SERVER_IP = "127.0.0.1";
	
	public IRControlService(String IP) {
		SERVER_IP = IP;
		ipPing(SERVER_IP);
	}
	
	private void ipPing(String ip) {
		String ipAddress = ip;
				
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
		
		System.out.println("IR Connect : " + reachable);
	}

	private byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];

		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}

		return data;
	}

	void sendCommand(String irCommand) {
		Socket socket;
		try {
			socket = new Socket(SERVER_IP, SERVERPORT);
			OutputStream out = socket.getOutputStream();
			
			byte[] bytes = hexStringToByteArray(irCommand);
			
			out.write(bytes);
			out.flush();
			out.close();

			socket.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
