package capSW;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

public class Run {
	public static String encodeOptimised(BufferedImage img) {
	    StringBuilder packet = new StringBuilder();
	    int width = img.getWidth();
	    int height = img.getHeight();

	    for (int y = 0; y < height; y++) {
	        for (int x = 0; x < width; x++) {
	            int color = img.getRGB(x, y);
	            int startX = x;

	            // Find how many horizontal pixels have the same color
	            while (x + 1 < width && img.getRGB(x + 1, y) == color) {
	                x++;
	            }
	            int runWidth = (x - startX) + 1;

	            // Format: R,G,B,X,Y,W| (Quantized to 0-255)
	            Color c = new Color(color);
	            packet.append(String.format("%d,%d,%d,%d,%d,%d|", 
	                c.getRed(), c.getGreen(), c.getBlue(), startX, y, runWidth));
	        }
	    }
	    return packet.toString();
	}
	
	public static void main(String[] args) {
		/*
		 * Screen capture (downscaled)
		 */
		Robot robot = null;
		try {
			robot = new Robot();
			System.out.println("Robot initialised");
		} catch (AWTException e) {
			e.printStackTrace();
		}
		Rectangle captureRect = new Rectangle(0, 0, 1920, 1080); // Your screen
		BufferedImage screen = robot.createScreenCapture(captureRect);

		// Resize to Stormworks Monitor resolution (e.g., 64x64)
		Image scaled = screen.getScaledInstance(128, 128, Image.SCALE_FAST);
		BufferedImage finalImage = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
		finalImage.getGraphics().drawImage(scaled, 0, 0, null);
		
		/*
		 * HTTP server
		 */
		HttpServer server = null;
		try {
			final int PORT = 8080;
			server = HttpServer.create(new InetSocketAddress(PORT), 0);
			System.out.println("Server started on port " + PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		server.createContext("/stream", exchange -> {
			// SEND the compressed frame back as response
		    String hexFrame = encodeOptimised(finalImage);
		    exchange.sendResponseHeaders(200, hexFrame.length());
		    OutputStream os = exchange.getResponseBody();
		    os.write(hexFrame.getBytes());
		    os.close();
		});
		server.createContext("/data", exchange -> {
			// RECEIVE data from Stormworks
		    InputStream inStream = exchange.getRequestBody();
		    String luaData = new String(inStream.readAllBytes()); 
//		    System.out.println("Data from Stormworks: " + luaData + " | " + exchange.getRequestHeaders());
		    System.out.println(exchange.getRequestURI());
		});
		server.start();
	}

}