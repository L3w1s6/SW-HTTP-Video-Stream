package capSW;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;

public class CaptureThread extends Thread {
	private ResourceSync<Integer> widthSW, heightSW;
	
	private Run main;
	private Robot robot;
	private Rectangle capRect;
	private BufferedImage img;
	private String imgEncoded;
	
	public CaptureThread(Run main) {
		this.main = main;
		
		robot = null;
		try {
			robot = new Robot();
			System.out.println("Cap: Robot initialised");
		} catch (AWTException e) {
			e.printStackTrace();
		}
		capRect = new Rectangle(0, 0, 1920, 1080); // my screen size
		
		widthSW = new ResourceSync<>(288);
		heightSW = new ResourceSync<>(160);
		
		img = new BufferedImage(widthSW.get(), heightSW.get(), BufferedImage.TYPE_INT_RGB);
		imgEncoded = "";
	}
	
	// Thread-safe change width/height vars
	public void setOutputRes(int w, int h) {
		widthSW.set(w);
		heightSW.set(h);
	}
	
	private void encode() {
		StringBuilder packet = new StringBuilder();
		int width = img.getWidth();
		int height = img.getHeight();
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int color = img.getRGB(x, y);
				int startX = x;
				
				// Find how many horizontal pixels have the same color
				while (x + 1 < width && img.getRGB(x + 1, y) == color) {x++;}
				int runWidth = (x - startX) + 1;
				
				// Format: R,G,B,X,Y,W| (Quantized to 0-255)
				Color c = new Color(color);
				packet.append(String.format("%d,%d,%d,%d,%d,%d|", 
				c.getRed(), c.getGreen(), c.getBlue(), startX, y, runWidth));
			}
		}
		imgEncoded = packet.toString();
	}
	
	@Override
	public void run() {
		while (true) {			
			BufferedImage screen = robot.createScreenCapture(capRect); // get single frame
			
			// Resize to Stormworks Monitor resolution
			Image scaled = screen.getScaledInstance(widthSW.get(), heightSW.get(), Image.SCALE_FAST);
			img.getGraphics().drawImage(scaled, 0, 0, null);
			
			encode(); //convert image buffer to string
			main.updateFrame(imgEncoded); // send frame to main
			
			try {Thread.sleep(250);} catch (InterruptedException e) {e.printStackTrace();} // wait before getting new frame
		}
	}
}