package capSW;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.HashMap;

import com.sun.net.httpserver.HttpServer;

import javafx.application.Application;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

public class Run {
	private final int PORT = 8080;
	
	// parse list of parameters into HashMap
	private static HashMap<String, String> parseQuery(String query) {
		HashMap<String, String> params = new HashMap<>();
		// return if query is empty
		if (query == null || query.isBlank()) {
			return params;
		}
		
		// split all params into key,value
		for (String pair : query.split("&")) {
			int i = pair.indexOf("=");
			// Use URLDecoder in case there are special characters
			String key;
			try {
				key = URLDecoder.decode(pair.substring(0, i), "UTF-8");
				String value = URLDecoder.decode(pair.substring(i + 1), "UTF-8");
				params.put(key, value);
			} catch (UnsupportedEncodingException e) {// missing "=" or invalid encoding
				System.out.print("Error parsing query: ");
				e.printStackTrace();
				continue;
			}
		}
		return params;
	}

	public static void main(String[] args) {
		Run main = new Run();
		FrameData frameData = new FrameData();
		
		/*
		 * Screen capture
		 */
		CaptureThread capThread = new CaptureThread(frameData);
		capThread.start();
		
		/*
		 * HTTP server
		 */
		HttpServer server = null;
		try {
			
			server = HttpServer.create(new InetSocketAddress(main.PORT), 0);
			server.createContext("/stream", exchange -> {
				// receive data from Stormworks
				String query = exchange.getRequestURI().getQuery();
				if (query != null) {// check query exists
					HashMap<String, String> params = parseQuery(query);
					
					if (params.containsKey("w") && params.containsKey("h")) {
						int w = Integer.parseInt(params.get("w")), h = Integer.parseInt(params.get("h"));
						frameData.updateResolution(w, h);
					}
				}
				
				// send encoded frame as response
				String encoded = frameData.getEncoded();
				exchange.sendResponseHeaders(200, encoded.length());
				OutputStream os = exchange.getResponseBody();
				os.write(encoded.getBytes());
				exchange.close();
			});
			server.start();
			System.out.println("Server started on port " + main.PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/*
		 * UI
		 */
	}
}