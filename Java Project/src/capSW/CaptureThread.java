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
	private BufferedImage finalImage;
	
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
		
		finalImage = new BufferedImage(widthSW.get(), heightSW.get(), BufferedImage.TYPE_INT_RGB);
	}
	
	// NEEDS SYNCHRONISED
	public void setOutputRes(int w, int h) {
		widthSW.set(w);
		heightSW.set(h);
	}
	
	@Override
	public void run() {
		while (true) {			
			BufferedImage screen = robot.createScreenCapture(capRect); // get single frame
			
			// Resize to Stormworks Monitor resolution
			Image scaled = screen.getScaledInstance(widthSW.get(), heightSW.get(), Image.SCALE_FAST);
			finalImage.getGraphics().drawImage(scaled, 0, 0, null);
			
			main.updateFrame(finalImage); // send frame to main
			
			try {Thread.sleep(250);} catch (InterruptedException e) {e.printStackTrace();} // wait before getting new frame
		}
	}
}