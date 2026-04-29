package capSW;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameGrabber.Exception;

import javafx.animation.AnimationTimer;
import javafx.embed.swing.SwingFXUtils;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

public class CaptureThread extends Thread {
//	private ResourceSync<Integer> widthSW, heightSW;
	
	private Run main;
	private FrameData frameData;
	private ResourceSync<Boolean> running;
//	private ResourceSync<BufferedImage> img;
//	private String imgEncoded;
	
	public CaptureThread(Run main, FrameData frameData) {
		this.main = main;
		this.frameData = frameData;
		
		running = new ResourceSync<>(true);
		
		this.frameData.updateResolution(288, 160);
//		widthSW = new ResourceSync<>(288);
//		heightSW = new ResourceSync<>(160);
		
//		img = new ResourceSync<>(new BufferedImage(widthSW.get(), heightSW.get(), BufferedImage.TYPE_INT_RGB));
//		imgEncoded = "";
	}
	
	// Thread-safe change width/height vars
//	public void setOutputRes(int w, int h) {
//		widthSW.set(w);
//		heightSW.set(h);
//	}
	
//	private void encode() {
//		StringBuilder packet = new StringBuilder();
//		int width = img.get().getWidth();
//		int height = img.get().getHeight();
//		
//		for (int y = 0; y < height; y++) {
//			for (int x = 0; x < width; x++) {
//				int color = img.get().getRGB(x, y);
//				int startX = x;
//				
//				// Find how many horizontal pixels have the same color
//				while (x + 1 < width && img.get().getRGB(x + 1, y) == color) {x++;}
//				int runWidth = (x - startX) + 1;
//				
//				// Format: R,G,B,X,Y,W| (Quantized to 0-255)
//				Color c = new Color(color);
//				packet.append(String.format("%d,%d,%d,%d,%d,%d|", 
//				c.getRed(), c.getGreen(), c.getBlue(), startX, y, runWidth));
//			}
//		}
//		imgEncoded = packet.toString();
//	}
	
//	public BufferedImage getImage() {
//		return img.get();
//	}
	
	public void startCapture() {
		running.set(true);
	}
	
	public void stopCapture() {
		running.set(false);
	}
	
	public void capture(FFmpegFrameGrabber grabber, Java2DFrameConverter converter) {
		try {
			Frame frame;
			frame = grabber.grab();
			if (frame != null) {
				BufferedImage screen = converter.getBufferedImage(frame); // get frame as BufferedImage
	            
	            // Resize image to SW resolution
//	            Image scaled = screen.getScaledInstance(widthSW.get(), heightSW.get(), Image.SCALE_FAST);
//	            img.set(new BufferedImage(widthSW.get(), heightSW.get(), BufferedImage.TYPE_INT_RGB));
//	            img.get().getGraphics().drawImage(scaled, 0, 0, null);
//				
//				encode(); //convert image buffer to string
//				
//				main.setFrame(img.get());
//				main.updateFrame(imgEncoded); // send frame to main
				
				frameData.updateImg(screen);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		org.bytedeco.javacv.FFmpegLogCallback.set(); // enable better FFmpeg logs
		String filter = "ddagrab=framerate=1:draw_mouse=0,hwdownload,format=bgra"; // different filters separated by "," | different settings of same filter separated by ":"
		
		// run inside try-with-resources to prevent resource leaks
		try (
				FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(filter); // Desktop Duplication mode (grabs frame directly from GPU memory buffer just before sent to monitor)
				Java2DFrameConverter converter = new Java2DFrameConverter();
			) {
			grabber.setFormat("lavfi"); // tells FFmpeg to treat input as filter
			grabber.start();
			
			// loop run capture at specific time (if running active) 
			while (true) {
				if (running.get()) {
					capture(grabber, converter);
				}
			}
		} catch (org.bytedeco.javacv.FrameGrabber.Exception e) {
			e.printStackTrace();
		}
	}
}