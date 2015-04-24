package com.hendyirawan.betterroads;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
public class BetterRoadsApplication implements CommandLineRunner {

    private static Logger log = LoggerFactory.getLogger(BetterRoadsApplication.class);

    static {
        log.info("Loading OpenCV: {}", Core.NATIVE_LIBRARY_NAME);
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        SpringApplication.run(BetterRoadsApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        final File inFile = new File("sample/pothole1.jpg");
        final Mat img = Highgui.imread(inFile.getPath());
        final Mat blurred = new Mat(img.size(), img.type());

        Imgproc.blur(img, blurred, new Size(5, 5));
        final File blurFile = new File(System.getProperty("java.io.tmpdir"), "pothole1_blur.jpg");
        Highgui.imwrite(blurFile.getPath(), blurred);
        log.info("Blurred written to {}", blurFile);

        double lowThreshold = 3;
        double ratio = 3;
        int kernelSize = 3;
        final Mat detectedEdges = new Mat();
        Imgproc.Canny(blurred, detectedEdges, lowThreshold, lowThreshold * ratio, kernelSize, false);

        final File edgesFile = new File(System.getProperty("java.io.tmpdir"), "pothole1_edges.jpg");
        Highgui.imwrite(edgesFile.getPath(), detectedEdges);
        log.info("Detected edges written to {}", detectedEdges);
    }
}
