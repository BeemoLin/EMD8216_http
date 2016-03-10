import static spark.Spark.*;

public class HttpServer {
	public static void main(String[] args) {
		port(5678);
        get("/hello", (req, res) -> "Hello World");
        
        get("/news/:section", (request, response) -> {
            response.type("text/xml");
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><news>" + request.params("section") + "</news>";
        });
    }

}
