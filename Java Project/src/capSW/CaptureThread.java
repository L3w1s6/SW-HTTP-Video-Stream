package capSW;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;

public class CaptureThread extends Thread {
	private ResourceSync<Integer> widthSW, heightSW;
	
	private Run main;
	private Robot robot;
	private Rectangle capRect;
	
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
	}
	
	// NEEDS SYNCHRONISED
	public void setOutputRes(int w, int h) {
		widthSW.set(w);
		heightSW.set(h);
	}
	
	@Override
	public void run() {
		BufferedImage screen = robot.createScreenCapture(capRect); // get single frame
		
		// Resize to Stormworks Monitor resolution
		Image scaled = screen.getScaledInstance(widthSW.get(), heightSW.get(), Image.SCALE_FAST);
		BufferedImage finalImage = new BufferedImage(widthSW.get(), heightSW.get(), BufferedImage.TYPE_INT_RGB);
		finalImage.getGraphics().drawImage(scaled, 0, 0, null);
		
		main.updateFrame(finalImage);
		System.out.println("Cap: updated frame");
	}
}