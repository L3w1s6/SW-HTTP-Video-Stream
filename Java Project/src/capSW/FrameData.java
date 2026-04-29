package capSW;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.concurrent.locks.ReentrantLock;

public class FrameData {
	private ReentrantLock lock = new ReentrantLock(); // concurrency lock
	
	// vars (not thread-safe)
	private int scaledW = 64;
	private int scaledH = 64;
	private BufferedImage img = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
	private BufferedImage scaledImg = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
	private String encodedImg = "";
	
	// encode scaled img (not thread-safe)
	private void encodeScaled() {
		StringBuilder packet = new StringBuilder();
		int width = scaledW;
		int height = scaledH;
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int color = scaledImg.getRGB(x, y);
				int startX = x;
				
				// Find how many horizontal pixels have the same color
				while (x + 1 < width && scaledImg.getRGB(x + 1, y) == color) {x++;}
				int runWidth = (x - startX) + 1;
				
				// Format: R,G,B,X,Y,W| (Quantized to 0-255)
				Color c = new Color(color);
				packet.append(String.format("%d,%d,%d,%d,%d,%d|", 
				c.getRed(), c.getGreen(), c.getBlue(), startX, y, runWidth));
			}
		}
		encodedImg = packet.toString();
	}
	
	// scale img to scaledImg (not thread-safe)
	private void scale() {
		// scale img & store in scaledImg
		Image scaled = img.getScaledInstance(scaledW, scaledH, Image.SCALE_FAST);
        scaledImg = new BufferedImage(scaledW, scaledH, BufferedImage.TYPE_INT_RGB);
        scaledImg.getGraphics().drawImage(scaled, 0, 0, null);
	}
	
	// update/calc all states (thread-safe)
	public void updateImg(BufferedImage img) {
		lock.lock();
		try {			
			this.img = img;
			scale();
			encodeScaled();
		} finally {
			lock.unlock();
		}
	}
	
	// get captured screen image (thread-safe)
	public BufferedImage getImg() {
		lock.lock();
		try {			
			return img;
		} finally {
			lock.unlock();
		}
	}
	
	// get output scaled image (thread-safe)
	public BufferedImage getScaledImg() {
		lock.lock();
		try {			
			return scaledImg;
		} finally {
			lock.unlock();
		}
	}
	
	// get scaled image encoded as string (thread-safe)
	public String getEncoded() {
		lock.lock();
		try {			
			return encodedImg;
		} finally {
			lock.unlock();
		}
	}
	
	// set scaled resolution for next frame (thread-safe)
	public void updateResolution(int w, int h) {
		lock.lock();
		try {			
			if (w != scaledW || h != scaledH) {
				scaledW = w;
				scaledH = h;
			}
		} finally {
			lock.unlock();
		}
	}
}