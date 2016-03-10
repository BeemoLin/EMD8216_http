import static spark.Spark.*;

import java.io.IOException;
import java.time.ZonedDateTime;

public class main {

	static LightControlService lCService;
	static LightControlService lCService_s;
	static IRControlService IRService;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		httpServer();
		System.out.println("Start web service");
		
		// emd8216 config
		lCService = new LightControlService();
		short[] ip = {172, 18, 0, 33};
		String lCService_status = lCService.serviceStart(ip);
		System.out.println("EMD8216 connect :" + lCService_status);
		
		// emd8216 config
		lCService_s = new LightControlService();
		short[] ip_s = {172, 18, 0, 35};
		String lCService_s_status = lCService_s.serviceStart(ip_s);
		System.out.println("EMD8216_s connect :" + lCService_s_status);
		
		IRService = new IRControlService("172.18.0.34");

	}
	
	private static void emd8216_config() {
		// emd8216 config
		lCService = new LightControlService();
		short[] ip = {172, 18, 0, 33};
		String lCService_status = lCService.serviceStart(ip);
		System.out.println("EMD8216 connect :" + lCService_status);
	}

	private static void httpServer() {
		port(8080);
        get("/hello", (req, res) -> "Hello World");
        
        // 172.18.0.33
        get("/33/:point", (request, response) -> {
        	int point = Integer.parseInt(request.params("point"));
        	boolean output = lCService.portData_status[(int)point];
        	
        	System.out.println("emd2816_status : point = " + point + " output = " + output);
        	return output;
        });
        
        get("/33/:point/:status", (request, response) -> {
        	int point = Integer.parseInt(request.params("point"));
        	int status = Integer.parseInt(request.params("status"));
        	
        	lCService.changePointStatus(point, (byte) status);
        	
        	System.out.println("emd2816_set : point = " + point + " input = " + status);
        	return "done";
        });
        
        // 172.18.0.35
        get("/35/:point", (request, response) -> {
        	int point = Integer.parseInt(request.params("point"));
        	boolean output = lCService_s.portData_status[(int)point];
        	
        	System.out.println("emd2816_status : point = " + point + " output = " + output);
        	return output;
        });
        
        get("/35/:point/:status", (request, response) -> {
        	int point = Integer.parseInt(request.params("point"));
        	int status = Integer.parseInt(request.params("status"));
        	
        	lCService_s.changePointStatus(point, (byte) status);
        	
        	System.out.println("emd2816_set : point = " + point + " input = " + status);
        	return "done";
        });
        
        // 172.18.0.34
        get("/ir", (req, res) -> "/ir/:point(4byte)/:output(1or 2 or 3)");
        get("/ir/:point/:output", (request, response) -> {
        	String point = request.params("point");
        	String output = request.params("output");
        	
        	String str = "01020000000B0110044F000204" + point + "000" + output;
        	IRService.sendCommand(str);
        	
			System.out.println("send ir command : point = " + point + " output = " + output);
	    	return ZonedDateTime.now();
        });
	}
}
