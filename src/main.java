import static spark.Spark.*;
import static spark.Spark.get;
import static spark.Spark.post;

import spark.Request;
import spark.Response;
import spark.ResponseTransformer;
import spark.Route;
import spark.examples.transformer.MyMessage;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.tcwa.control.Config;

import java.time.ZonedDateTime;

public class main {

	static LightControlService lCService_1;
	static LightControlService lCService_2;
	static LightControlService lCService_3;
	static LightControlService lCService_4;
	
	static IRControlService IRService_1;
	static IRControlService IRService_2;
	static IRControlService IRService_3;
	static IRControlService IRService_4;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Config config = setting_read("~/sites/info.json");
		//Config config = setting_read("/Users/Beemo/Sites/emd8216/info.json");
		
		httpServer();
		System.out.println("Start web service");
		
		String model_ip_1 = config.getModel_ip_1();
		if(!model_ip_1.equals("")) {
			switch(config.getModel_type_1()) {
			case "tcec-88":
				lCService_1 = new LightControlService();
				lCService_1.serviceStart(ip_parse(model_ip_1));
				icServer_1(config.getModel_name_1());
			break;
			case "tcec-s":
				lCService_1 = new LightControlService();
				lCService_1.serviceStart(ip_parse(model_ip_1));
				icServer_1(config.getModel_name_1());
			break;
			case "ir":
				IRService_1 = new IRControlService(model_ip_1);
				irServer_1(config.getModel_name_1());
			break;
			}
		}
		
		String model_ip_2 = config.getModel_ip_2();
		if(!model_ip_2.equals("")) {
			switch(config.getModel_type_2()) {
			case "tcec-88":
				lCService_2 = new LightControlService();
				lCService_2.serviceStart(ip_parse(model_ip_2));
				icServer_2(config.getModel_name_2());
			break;
			case "tcec-s":
				lCService_2 = new LightControlService();
				lCService_2.serviceStart(ip_parse(model_ip_2));
				icServer_2(config.getModel_name_2());
			break;
			case "ir":
				IRService_2 = new IRControlService(model_ip_2);
				irServer_2(config.getModel_name_2());
			break;
			}
		}
		
		String model_ip_3 = config.getModel_ip_3();
		if(!model_ip_3.equals("")) {
			switch(config.getModel_type_3()) {
			case "tcec-88":
				lCService_3 = new LightControlService();
				lCService_3.serviceStart(ip_parse(model_ip_3));
				icServer_3(config.getModel_name_3());
			break;
			case "tcec-s":
				lCService_3 = new LightControlService();
				lCService_3.serviceStart(ip_parse(model_ip_3));
				icServer_3(config.getModel_name_3());
			break;
			case "ir":
				IRService_3 = new IRControlService(model_ip_3);
				irServer_3(config.getModel_name_3());
			break;
			}
		}
		
		String model_ip_4 = config.getModel_ip_4();
		if(!model_ip_4.equals("")) {
			switch(config.getModel_type_4()) {
			case "tcec-88":
				lCService_4 = new LightControlService();
				lCService_4.serviceStart(ip_parse(model_ip_4));
				icServer_4(config.getModel_name_4());
			break;
			case "tcec-s":
				lCService_4 = new LightControlService();
				lCService_4.serviceStart(ip_parse(model_ip_4));
				icServer_4(config.getModel_name_4());
			break;
			case "ir":
				IRService_4 = new IRControlService(model_ip_4);
				irServer_4(config.getModel_name_4());
			break;
			}
		}
	}
	
	private static short[] ip_parse(String strIP) {
		short[] ip = {0, 0, 0, 0};
		ip[0] = Short.parseShort(strIP.split("\\.")[0]);
		ip[1] = Short.parseShort(strIP.split("\\.")[1]);
		ip[2] = Short.parseShort(strIP.split("\\.")[2]);
		ip[3] = Short.parseShort(strIP.split("\\.")[3]);
		return ip;
	}

	private static Config setting_read(String filename) {
		JsonReader reader = null;
		
		try {
			reader = new JsonReader(new FileReader(filename));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Gson gson = new Gson();
		
		return gson.fromJson(reader, Config.class);
	}
	
	private static void httpServer() {
		port(8080);
        get("/hello", (req, res) -> "true");
        
	}
	
	private static void icServer_1(String name) {
		// 172.18.0.33
        get("/" + name + "/:point", (request, response) -> {
        	int point = Integer.parseInt(request.params("point"));
        	boolean output = lCService_1.portData_status[(int)point];
        	
        	System.out.println("emd2816_status : point = " + point + " output = " + output);
        	return output;
        });
        
        get("/" + name + "/:point/:status", (request, response) -> {
        	int point = Integer.parseInt(request.params("point"));
        	int status = Integer.parseInt(request.params("status"));
        	
        	lCService_1.changePointStatus(point, (byte) status);
        	
        	System.out.println("emd2816_set : point = " + point + " input = " + status);
        	return "done";
        });
	}
	
	private static void icServer_2(String name) {
		// 172.18.0.33
        get("/" + name + "/:point", (request, response) -> {
        	int point = Integer.parseInt(request.params("point"));
        	boolean output = lCService_2.portData_status[(int)point];
        	
        	System.out.println("emd2816_status : point = " + point + " output = " + output);
        	return output;
        });
        
        get("/" + name + "/:point/:status", (request, response) -> {
        	int point = Integer.parseInt(request.params("point"));
        	int status = Integer.parseInt(request.params("status"));
        	
        	lCService_2.changePointStatus(point, (byte) status);
        	
        	System.out.println("emd2816_set : point = " + point + " input = " + status);
        	return "done";
        });
	}
	
	private static void icServer_3(String name) {
		// 172.18.0.33
        get("/" + name + "/:point", (request, response) -> {
        	int point = Integer.parseInt(request.params("point"));
        	boolean output = lCService_3.portData_status[(int)point];
        	
        	System.out.println("emd2816_status : point = " + point + " output = " + output);
        	return output;
        });
        
        get("/" + name + "/:point/:status", (request, response) -> {
        	int point = Integer.parseInt(request.params("point"));
        	int status = Integer.parseInt(request.params("status"));
        	
        	lCService_3.changePointStatus(point, (byte) status);
        	
        	System.out.println("emd2816_set : point = " + point + " input = " + status);
        	return "done";
        });
	}
	
	private static void icServer_4(String name) {
		// 172.18.0.33
        get("/" + name + "/:point", (request, response) -> {
        	int point = Integer.parseInt(request.params("point"));
        	boolean output = lCService_4.portData_status[(int)point];
        	
        	System.out.println("emd2816_status : point = " + point + " output = " + output);
        	return output;
        });
        
        get("/" + name + "/:point/:status", (request, response) -> {
        	int point = Integer.parseInt(request.params("point"));
        	int status = Integer.parseInt(request.params("status"));
        	
        	lCService_4.changePointStatus(point, (byte) status);
        	
        	System.out.println("emd2816_set : point = " + point + " input = " + status);
        	return "done";
        });
	}
        
	private static void irServer_1(String name) {
        // 172.18.0.34
        get("/" + name, (req, res) -> "/ir/:point(4byte)/:output(1or 2 or 3)");
        get("/" + name + "/:point/:output", (request, response) -> {
        	String point = request.params("point");
        	int a = Integer.parseInt(point);
        	String r = Integer.toHexString(a);
        	String hex = String.format("%0$4s", r).replace(' ', '0');
        	
        	String output = request.params("output");
        	
        	String str = "01020000000B0110044F000204" + hex + "000" + output;
        	
        	IRService_1.sendCommand(str);
        	
			System.out.println("send ir command : point = " + hex + " output = " + output);
	    	return ZonedDateTime.now();
        });
	}
	
	private static void irServer_2(String name) {
        // 172.18.0.34
        get("/" + name, (req, res) -> "/ir/:point(4byte)/:output(1or 2 or 3)");
        get("/" + name + "/:point/:output", (request, response) -> {
        	String point = request.params("point");
        	int a = Integer.parseInt(point);
        	String r = Integer.toHexString(a);
        	String hex = String.format("%0$4s", r).replace(' ', '0');
        	
        	String output = request.params("output");
        	
        	String str = "01020000000B0110044F000204" + hex + "000" + output;
        	
        	IRService_2.sendCommand(str);
        	
			System.out.println("send ir command : point = " + hex + " output = " + output);
	    	return ZonedDateTime.now();
        });
	}
	
	private static void irServer_3(String name) {
        // 172.18.0.34
        get("/" + name, (req, res) -> "/ir/:point(4byte)/:output(1or 2 or 3)");
        get("/" + name + "/:point/:output", (request, response) -> {
        	String point = request.params("point");
        	int a = Integer.parseInt(point);
        	String r = Integer.toHexString(a);
        	String hex = String.format("%0$4s", r).replace(' ', '0');
        	
        	String output = request.params("output");
        	
        	String str = "01020000000B0110044F000204" + hex + "000" + output;
        	
        	IRService_3.sendCommand(str);
        	
			System.out.println("send ir command : point = " + hex + " output = " + output);
	    	return ZonedDateTime.now();
        });
	}
	
	private static void irServer_4(String name) {
        // 172.18.0.34
        get("/" + name, (req, res) -> "/ir/:point(4byte)/:output(1or 2 or 3)");
        get("/" + name + "/:point/:output", (request, response) -> {
        	String point = request.params("point");
        	int a = Integer.parseInt(point);
        	String r = Integer.toHexString(a);
        	String hex = String.format("%0$4s", r).replace(' ', '0');
        	
        	String output = request.params("output");
        	
        	String str = "01020000000B0110044F000204" + hex + "000" + output;
        	
        	IRService_4.sendCommand(str);
        	
			System.out.println("send ir command : point = " + hex + " output = " + output);
	    	return ZonedDateTime.now();
        });
	}
}
