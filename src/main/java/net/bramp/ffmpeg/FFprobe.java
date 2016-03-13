package net.bramp.ffmpeg;

import java.io.IOException;
import java.io.Reader;

import javax.annotation.Nonnull;

import net.bramp.commons.lang3.math.gson.FractionAdapter;
import net.bramp.ffmpeg.io.LoggingFilterReader;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;

import org.apache.commons.lang3.math.Fraction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Wrapper around FFprobe
 * 
 * TODO ffprobe -v quiet -print_format json -show_format -show_streams mobileedge_1280x720.mp4
 * @author bramp
 *
 */
public class FFprobe {

	final static Logger LOG = LoggerFactory.getLogger(FFprobe.class);

	final Gson gson = setupGson();
	
	final String path;

	/**
	 * Function to run FFmpeg. We define it like this so we can swap it out (during testing)
	 */
	ProcessFunction runFunc = new RunProcessFunction();

	public FFprobe() {
		this.path = "ffprobe";
	}

	public FFprobe(@Nonnull String path) {
		this.path = path;
	}

	private static Gson setupGson() {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(Fraction.class, new FractionAdapter());
		return builder.create();
	}

	public String getPath() {
		return path;
	}

	public FFmpegProbeResult probe(String mediaPath) throws IOException {
		ImmutableList.Builder<String> args = new ImmutableList.Builder<String>();

		args.add(path)
			.add("-v", "quiet")
			.add("-print_format", "json")
			.add("-show_error")
			.add("-show_format")
			.add("-show_streams")

			//.add("--show_packets")
			//.add("--show_frames")

			.add(mediaPath);

		Reader reader = runFunc.run(args.build());

		if (LOG.isDebugEnabled()) {
			reader = new LoggingFilterReader(reader, LOG);
		}

		return gson.fromJson(reader, FFmpegProbeResult.class);
	}
}
