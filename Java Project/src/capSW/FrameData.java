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
	
	// encode int as char values to optimise char count (converting for which Lua uses UTF-8)
	private String intToCharStr(int x, int len) {
		char[] chars = new char[len];
		
		// add to char array 8-bit section (reverse order)
		int i = 0;
		do {
			chars[len - 1 - i] = (char) (x & 0xFF);
			System.out.printf("%d %d|", len - 1 - i, (x & 0xFF));
			x >>= 8;
		} while (++i < len);
		System.out.printf("'%s'\n", String.valueOf(chars));
		
		return String.valueOf(chars);
	}
	
	// overloaded for if calc length instead of setting it
	private String intToCharStr(int x) { 
		int len = (int) Math.ceil((32 - Integer.numberOfLeadingZeros(x)) / 8f); // calc number of bytes (8 bits) needed
		return intToCharStr(x, len);
	}
	
	private byte[] intToBytes(int x, int len) {
		// print error message if x out of range for number of bytes
		final int MAX = (int) Math.pow(2, 8 * len) - 1;
		if (x > MAX || x < 0) {
			System.err.printf("intToBytes error: %d outside range of 0 - %d\n", x, MAX);
		}
		
		byte[] bytes = new byte[len + 1];
		
		// add to char array 8-bit section (reverse order)
		int i = 0;
		do {
			bytes[len - 1 - i] = (byte) 0;
//			System.out.printf("%d %d|", len - 1 - i, (x & 0xFF));
			x >>= 8;
		} while (++i < len);
//		System.out.printf("'%s'\n", String.valueOf(bytes));
		
		bytes[len] = (byte) 1;
		return bytes;
	}
	
	// encode scaled img (not thread-safe)
	private void encodeScaled() {
//		StringBuilder packet = new StringBuilder();
//		int width = scaledW;
//		int height = scaledH;
//		
//		for (int y = 0; y < height; y++) {
//			for (int x = 0; x < width; x++) {
//				int colour = scaledImg.getRGB(x, y);
//				Color c = new Color(colour);
//				
//				int startX = x;
//				
//				// Find how many horizontal pixels have the same color
//				while (x + 1 < width && scaledImg.getRGB(x + 1, y) == colour) x++;
//				int runWidth = (x - startX) + 1;
//				
//				// format - XXYYWWRGB
//				
//				// pos/transform data (2 chars to ensure never not enough bits)
//				packet.append(intToCharStr(startX, 2));
//				packet.append(intToCharStr(y, 2));
//				packet.append(intToCharStr(runWidth, 2));
//				// colour data
//				packet.append(intToCharStr(c.getRGB() ^ (0xFF << 24), 3)); // convert RGB (masking out A) to 8-bit chars
//			}
//		}
//		encodedImg = packet.toString();
//		System.out.println(encodedImg.length());
//		encodedImg = intToCharStr(128, 2); // IN GAME THIS TURNS INTO LENGTH OF 2????
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
	
	public byte[] getEncodedBytes() {
		lock.lock();
		try {
			return intToBytes(256, 3);
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