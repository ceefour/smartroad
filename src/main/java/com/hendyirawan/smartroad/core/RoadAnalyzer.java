package com.hendyirawan.smartroad.core;

import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by ceefour on 5/13/15.
 */
@Service
public class RoadAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(RoadAnalyzer.class);

    public RoadAnalysis analyze(Mat img) {
        log.info("Analyzing road condition from {}×{}/{} image",
                img.width(), img.height(), img.channels());

        final RoadAnalysis roadAnalysis = new RoadAnalysis();
        roadAnalysis.original = img;

//        final File inFile = new File("sample/pothole1.jpg");
//        final Mat img = Highgui.imread(inFile.getPath());
        final Mat blurred = new Mat(img.size(), img.type());

        Imgproc.blur(img, blurred, new Size(5, 5));
//        final File blurFile = new File(System.getProperty("java.io.tmpdir"), "pothole1_blur.jpg");
//        Highgui.imwrite(blurFile.getPath(), blurred);
//        log.info("Blurred written to {}", blurFile);

        double lowThreshold = 50;
        double ratio = 3;
        int kernelSize = 3;
        final Mat detectedEdges = new Mat();
        Imgproc.Canny(blurred.clone(), detectedEdges, lowThreshold, lowThreshold * ratio, kernelSize, false);

//        final File edgesFile = new File(System.getProperty("java.io.tmpdir"), "pothole1_edges.jpg");
//        Highgui.imwrite(edgesFile.getPath(), detectedEdges);
//        log.info("Detected edges written to {}", detectedEdges);
        roadAnalysis.edges = detectedEdges;

        // http://stackoverflow.com/questions/10262600/how-to-detect-region-of-large-of-white-pixels-using-opencv
        final ArrayList<MatOfPoint> contours = new ArrayList<>();
        final Mat hierarchy = new Mat();
        Imgproc.findContours(detectedEdges, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE,
                new Point(0, 0));
        log.info("{} Contours: {}", contours.size(), contours);
        log.info("{} Hierarchy: {}", hierarchy.size(), hierarchy);
        for (int i = 0; i < contours.size(); i++) {
            final MatOfPoint contour = contours.get(0);
            final double contourArea = Imgproc.contourArea(contour);
            if (contourArea >= 1.0) {
                log.info("Contour #{} {} area {}", i, contour, contourArea);
                //if (contourArea >= 200) {
                    Imgproc.drawContours(blurred, contours, i, new Scalar(180, 105, 255), // http://www.color-hex.com/color/ff69b4
                        2);
                //}
            }
        }

        log.info("Blurred: {}×{}/{} {}", blurred.width(), blurred.height(), blurred.channels(), blurred);
        roadAnalysis.blurred = blurred;
        return roadAnalysis;
    }

}
