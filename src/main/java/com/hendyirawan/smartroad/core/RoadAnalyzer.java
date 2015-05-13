package com.hendyirawan.smartroad.core;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;

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
        roadAnalysis.blurred = blurred;
//        final File blurFile = new File(System.getProperty("java.io.tmpdir"), "pothole1_blur.jpg");
//        Highgui.imwrite(blurFile.getPath(), blurred);
//        log.info("Blurred written to {}", blurFile);

        double lowThreshold = 3;
        double ratio = 3;
        int kernelSize = 3;
        final Mat detectedEdges = new Mat();
        Imgproc.Canny(blurred, detectedEdges, lowThreshold, lowThreshold * ratio, kernelSize, false);

//        final File edgesFile = new File(System.getProperty("java.io.tmpdir"), "pothole1_edges.jpg");
//        Highgui.imwrite(edgesFile.getPath(), detectedEdges);
//        log.info("Detected edges written to {}", detectedEdges);
        roadAnalysis.edges = detectedEdges;

        return roadAnalysis;
    }

}
