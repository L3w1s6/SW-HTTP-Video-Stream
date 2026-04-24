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
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

public class Run {
	private ResourceSync<String> frameEncoded = new ResourceSync<>();
	private final int PORT;
	private int outW, outH; // shouldn't need thread-safe?
	
	public Run() {
		PORT = 8080;
		outW = 64;
		outH = 64;
	}

	private static HashMap<String, String> parseQuery(String query) {
		HashMap<String, String> params = new HashMap<>();
		// return if query is empty
		if (query == null || query.isBlank()) {
			return params;
		}
		
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
	
	// returns true if difference between new resolution & current presolution
	private boolean outResDiff(int w, int h) {
		return w != outW || h != outH;
	}
	
	// Thread-safe update current encoded frame with new encoded frame
	public void updateFrame(String encoded) {
		frameEncoded.set(encoded);
	}

	public static void main(String[] args) {
		Run main = new Run();
		
		/*
		 * Screen capture (downscaled)
		 */
		CaptureThread capThread = new CaptureThread(main);
		capThread.start();
		
		/*
		 * HTTP server
		 */
		HttpServer server = null;
		try {
			
			server = HttpServer.create(new InetSocketAddress(main.PORT), 0);
			System.out.println("Server started on port " + main.PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		server.createContext("/stream", exchange -> {
			// SEND the compressed frame back as response
			String encoded = main.frameEncoded.get();
			exchange.sendResponseHeaders(200, encoded.length());
			OutputStream os = exchange.getResponseBody();
			os.write(encoded.getBytes());
			exchange.close();
		});
		server.createContext("/data", exchange -> {
			// RECEIVE data from Stormworks
			String query = exchange.getRequestURI().getQuery();
			if (query != null) {// check query exists
				HashMap<String, String> params = parseQuery(query);
				System.out.println(params);
				
				if (params.containsKey("w") && params.containsKey("h")) {
					int w = Integer.parseInt(params.get("w")), h = Integer.parseInt(params.get("h"));
					if (main.outResDiff(w, h)) {
						capThread.setOutputRes(w, h);
					}
				}
			}
			
			// always return & close GET
			exchange.sendResponseHeaders(200, -1);
			exchange.close();
		});
		server.start();
	}
}