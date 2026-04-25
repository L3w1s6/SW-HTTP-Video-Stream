package capSW;

import org.bytedeco.ffmpeg.global.avfilter;
import org.bytedeco.ffmpeg.global.avformat;
import org.bytedeco.ffmpeg.global.avdevice;
import org.bytedeco.ffmpeg.avfilter.AVFilter;
import org.bytedeco.ffmpeg.avformat.AVInputFormat;

public class CheckDDAGrab {
	public static void main(String[] args) {
		// Initialize the device and filter registries
        avdevice.avdevice_register_all();

        System.out.println("--- Checking DDAGRAB Implementation ---");

        // 1. Check if it's registered as an Input Format (Device)
        AVInputFormat format = avformat.av_find_input_format("ddagrab");
        if (format != null) {
            System.out.println("[X] Found as Input Format: " + format.name().getString());
        } else {
            System.out.println("[ ] NOT an Input Format (This is why setFormat('ddagrab') crashes)");
        }

        // 2. Check if it's registered as a Filter
        AVFilter filter = avfilter.avfilter_get_by_name("ddagrab");
        if (filter != null) {
            System.out.println("[X] Found as Video Filter: " + filter.name().getString());
            System.out.println("    Description: " + filter.description().getString());
        } else {
            System.out.println("[ ] NOT found as a Filter.");
        }
	}
}