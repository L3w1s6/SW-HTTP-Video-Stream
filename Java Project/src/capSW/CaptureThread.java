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
	private FrameData frameData;
	private ResourceSync<Boolean> running;
	
	public CaptureThread(FrameData frameData) {
		this.frameData = frameData;
		
		running = new ResourceSync<>(true);
		
		this.frameData.updateResolution(288, 160);
	}
	
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
	            frameData.updateImg(screen);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		org.bytedeco.javacv.FFmpegLogCallback.set(); // enable better FFmpeg logs
		String filter = "ddagrab=framerate=2:draw_mouse=0,hwdownload,format=bgra"; // different filters separated by "," | different settings of same filter separated by ":"
		
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