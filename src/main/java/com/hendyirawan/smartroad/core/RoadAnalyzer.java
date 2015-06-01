package com.hendyirawan.smartroad.core;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ceefour on 5/13/15.
 */
@Service
public class RoadAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(RoadAnalyzer.class);

    /**
     * Position (where 0.0 is left and 1.0 is right) of vanishing point of road
     * (it's usually inside image) at the horizon.
     */
    private final double vanishU = 0.13;
    private final double vanishV = 0.32;
    /**
     * Position (where 0.0 is left and 1.0 is right) of left edge of road (may be off-image) at the bottom edge of image.
     */
    private final double leftU = -0.05; // V is always 1.0 (bottommost)
    /**
     * Position (where 0.0 is left and 1.0 is right) of right edge of road (may be off-image) at the bottom edge of image.
     */
    private final double rightU = 1.7; // V is always 1.0 (bottommost)
    private final int stdWidth = 640;
    private final int stdHeight = 480;

    public RoadAnalysis analyze(Mat img) {
        log.info("Analyzing road condition from {}×{}/{} image",
                img.width(), img.height(), img.channels());

        final RoadAnalysis roadAnalysis = new RoadAnalysis();
        roadAnalysis.original = img;
        final Mat resized = new Mat();
        Imgproc.resize(img, resized, new Size(stdWidth, stdHeight));

//        final File inFile = new File("sample/pothole1.jpg");
//        final Mat img = Highgui.imread(inFile.getPath());
        final Mat blurred = new Mat(img.size(), img.type());

        Imgproc.blur(resized, blurred, new Size(5, 5));
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

        // draw horizon line
        final int vanishY = Math.round((float) (vanishV * blurred.height()));
        final Point vanishPoint = new Point(vanishU * blurred.width(), vanishV * blurred.height());
//        Core.line(blurred, new Point(0, vanishY), new Point(blurred.width(), vanishY),
//                new Scalar(200, 0, 0), 2);
        final Point leftPoint = new Point(leftU * blurred.width(), blurred.height());
        final Point rightPoint = new Point(rightU * blurred.width(), blurred.height());
        Core.circle(blurred, vanishPoint, 4, new Scalar(255, 0, 0), 2);
        Core.polylines(blurred, ImmutableList.of(new MatOfPoint(vanishPoint, leftPoint, rightPoint, vanishPoint)), false,
                new Scalar(255, 0, 0), 2);
        final MatOfPoint2f roadContour = new MatOfPoint2f(vanishPoint, leftPoint, rightPoint, vanishPoint);

        // http://stackoverflow.com/questions/10262600/how-to-detect-region-of-large-of-white-pixels-using-opencv
        final ArrayList<MatOfPoint> contours = new ArrayList<>();
        final Mat hierarchy = new Mat();
        Imgproc.findContours(detectedEdges, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE,
                new Point(0, 0));
        log.info("{} Contours: {}", contours.size(), contours);
        log.info("{} Hierarchy: {}", hierarchy.size(), hierarchy);
        for (int i = 0; i < contours.size(); i++) {
            final MatOfPoint contour = contours.get(i);
            final double contourArea = Imgproc.contourArea(contour);
            if (contourArea >= 5.0 && contour.toList().size() >= 5) { // fitEllipse must have >= 5 points
                final boolean belowHorizon = contour.toList().stream().allMatch(it ->
                        //it.y >= vanishY
                        Imgproc.pointPolygonTest(roadContour, it, true) >= 1
                );
                final RotatedRect ellipse = Imgproc.fitEllipse(new MatOfPoint2f(contour.toArray()));
                final double ellipseArea = ellipse.size.area();
                final boolean ellipseBelowHorizon = ellipse.boundingRect().tl().y >= vanishY;
                if (belowHorizon && ellipseBelowHorizon) {
                    if (contourArea >= 20.0 && ellipseArea >= 4000) {
//                        Point sump = new Point(0, 0);
//                        for (Point p : contour.toList()) {
//                            sump.x += p.x;
//                            sump.y += p.y;
//                        }
//                        final Point centerPoint = new Point(sump.x / contour.rows(), sump.y / contour.rows());
                        // http://stackoverflow.com/a/18945856
                        final Point centerPoint = getCenterPoint(contour);
                        final MatOfPoint fixedContour;
                        if (contour.isContinuous()) {
                            fixedContour = contour;
                        } else {
                            fixedContour = new MatOfPoint(contour);
                            fixedContour.reshape(contour.cols(), contour.rows() + 1);
                            fixedContour.put(fixedContour.rows(), 1, fixedContour.get(fixedContour.rows() - 1, 1));
                        }
                        log.info("Big {} Contour #{} {} area {} center {}: {}",
                                belowHorizon ? "BELOW HORIZON" : "above horizon", i, contour.size(), contourArea,
                                centerPoint,
                                contour.toList().stream().limit(10).toArray());
//                        Imgproc.drawContours(blurred, contours, i, new Scalar(140, 80, 255), // http://www.color-hex.com/color/ff69b4
//                                2);
                        Core.polylines(blurred, ImmutableList.of(fixedContour), false, new Scalar(100, 60, 255), 2);
                        Core.circle(blurred, centerPoint, 4, new Scalar(255, 0, 0), 2);
                        log.info("Ellipse area {} {} size {}", ellipseArea, ellipse, ellipse.size);
                        Core.ellipse(blurred, ellipse, new Scalar(0, 255, 0), 2);
                    } else {
                        // Draw small contours without any ellipse
                        log.debug("Small {} Contour #{} {} area {}: {}", belowHorizon ? "BELOW HORIZON" : "above horizon",
                                i, contour.size(), contourArea,
                                contour.toList().stream().limit(10).toArray());
                        // If possible, differentiate between start-middle-end of contour lines
                        if (contour.rows() >= 4) {
                            Core.polylines(blurred, ImmutableList.of(new MatOfPoint(contour.rowRange(0, 2))),
                                    false, new Scalar(0, 255, 255), 1);
                            Core.polylines(blurred, ImmutableList.of(new MatOfPoint(contour.rowRange(2, contour.rows() - 2))),
                                    false, new Scalar(0, 0, 255), 1);
                            Core.polylines(blurred, ImmutableList.of(new MatOfPoint(contour.rowRange(contour.rows() - 2, contour.rows()))),
                                    false, new Scalar(255, 0, 0), 1);
                        } else {
                            Core.polylines(blurred, ImmutableList.of(contour),
                                    false, new Scalar(0, 255, 255), 1);
                        }
//                        Imgproc.drawContours(blurred, contours, i, new Scalar(0, 0, 255), // http://www.color-hex.com/color/ff69b4
//                                1);
                    }
                } else {
                    log.trace("{} Contour #{} {} area {}: {}", belowHorizon ? "BELOW HORIZON" : "above horizon", i, contour.size(), contourArea,
                            contour.toList().stream().limit(10).toArray());
                }
            }
        }

        // CLUSTERING PROBLEM: We want to detect closely located clusters of small contours
        final MatOfPoint2f contourCenters = new MatOfPoint2f(
            contours.stream().map(it -> getCenterPoint(it))
//                    .filter(it -> it.y >= vanishY)
                    .filter(it -> Imgproc.pointPolygonTest(roadContour, it, true) >= 1)
                    .toArray(Point[]::new)
        );
        log.info("Contour centers: {}", contourCenters);
        final MatOfInt bestLabels = new MatOfInt();
        final Mat clusterCenters = new Mat();
        final int clusterCount = Math.min(20, contourCenters.rows());
        final int attempts = 5;
        Core.kmeans(contourCenters, clusterCount, bestLabels,
                new TermCriteria(TermCriteria.MAX_ITER | TermCriteria.EPS, 10000, 3.0), attempts,
                Core.KMEANS_PP_CENTERS, clusterCenters);
        log.info("Best labels: {} {}", bestLabels, bestLabels.dump());
        log.info("Cluster centers: {} {}", clusterCenters, clusterCenters.dump());
        final ArrayListMultimap<Integer, Point> clusters = ArrayListMultimap.create();
        for (int i = 0; i < contourCenters.rows(); i++) {
            int[] cell = new int[] {0};
            bestLabels.get(i, 0, cell);
            final int clusterIdx = cell[0];
            final Point point = new Point(contourCenters.get(i, 0));
            clusters.put(clusterIdx, point);
        }
        // draw each cluster
        for (int cl = 0; cl < clusterCount; cl++) {
            Core.polylines(blurred, ImmutableList.of(new MatOfPoint(clusters.get(cl).toArray(new Point[] {}))),
                    false, new Scalar(255, 0, 255), 1);
        }

        log.info("Blurred: {}×{}/{} {}", blurred.width(), blurred.height(), blurred.channels(), blurred);
        roadAnalysis.blurred = blurred;
        return roadAnalysis;
    }

    protected Point getCenterPoint(MatOfPoint contour) {
        Point centerPoint;
        final Moments moments = Imgproc.moments(contour);
        if (moments.get_m00() != 0) {
            centerPoint = new Point(
                    moments.get_m10() / moments.get_m00(),
                    moments.get_m01() / moments.get_m00());
        } else {
            centerPoint = new Point(contour.get(0, 0));
            log.trace("Cannot find center point for {} {}, using first point: {}",
                    contour, contour.toList(), centerPoint);
        }
        return centerPoint;
    }

}
