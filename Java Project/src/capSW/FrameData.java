package capSW;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;

public class FrameData {
	private ResourceSync<Integer> scaledW = new ResourceSync<>(64);
	private ResourceSync<Integer> scaledH = new ResourceSync<>(64);
	private ResourceSync<BufferedImage> img = new ResourceSync<>(new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB));
	private ResourceSync<BufferedImage> scaledImg = new ResourceSync<>(new BufferedImage(scaledW.get(), scaledH.get(), BufferedImage.TYPE_INT_RGB));
	private ResourceSync<String> encodedImg = new ResourceSync<>("");
	
	// encode scaled img
	public void encodeScaled() {
		StringBuilder packet = new StringBuilder();
		int width = scaledW.get();
		int height = scaledH.get();
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int color = scaledImg.get().getRGB(x, y);
				int startX = x;
				
				// Find how many horizontal pixels have the same color
				while (x + 1 < width && scaledImg.get().getRGB(x + 1, y) == color) {x++;}
				int runWidth = (x - startX) + 1;
				
				// Format: R,G,B,X,Y,W| (Quantized to 0-255)
				Color c = new Color(color);
				packet.append(String.format("%d,%d,%d,%d,%d,%d|", 
				c.getRed(), c.getGreen(), c.getBlue(), startX, y, runWidth));
			}
		}
		encodedImg.set(packet.toString());
	}
	
	// scale img to scaledImg
	public void scale() {
		// get shared vars
		int toW = scaledW.get(), toH = scaledH.get();
		
		// scale img & store in scaledImg
		Image scaled = img.get().getScaledInstance(toW, toH, Image.SCALE_FAST);
        scaledImg.set(new BufferedImage(toW, toH, BufferedImage.TYPE_INT_RGB));
        scaledImg.get().getGraphics().drawImage(scaled, 0, 0, null);
	}
	
	// update/calc all states
	public void updateImg(BufferedImage img) {
		this.img.set(img);
		scale();
		encodeScaled();
	}
	
	public BufferedImage getImg() {
		return img.get();
	}
	
	public BufferedImage getScaledImg() {
		return scaledImg.get();
	}
	
	public String getEncoded() {
		return encodedImg.get();
	}
	
	// set new scaled resolution & recalc if necessary
	public void updateResolution(int w, int h) {
		if (w != scaledW.get() || h != scaledH.get()) {
			scaledW.set(w);
			scaledH.set(h);
		}
	}
}