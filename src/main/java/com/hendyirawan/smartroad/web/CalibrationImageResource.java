package com.hendyirawan.smartroad.web;

import com.google.common.collect.ImmutableList;
import com.hendyirawan.smartroad.core.Camera;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.DynamicImageResource;
import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ceefour on 6/3/15.
 */
public class CalibrationImageResource extends DynamicImageResource {

    private static final Logger log = LoggerFactory.getLogger(CalibrationImageResource.class);
    private final IModel<Camera> model;

    public CalibrationImageResource(IModel<Camera> model) {
        super();
        this.model = model;
    }

    @Override
    protected byte[] getImageData(Attributes attributes) {
        final Camera camera = model.getObject();
        if (camera.getCalibrationImage() == null) {
            log.info("Camera '{}' has no calibration image, returning empty projection", camera.getId());
            return new byte[]{};
        }
        final Mat calibrationMat = Highgui.imdecode(new MatOfByte(camera.getCalibrationImage()),
                Highgui.CV_LOAD_IMAGE_COLOR);
        Imgproc.resize(calibrationMat, calibrationMat, new Size(320, 240), 0, 0, Imgproc.INTER_AREA);

        final Double vanishU = camera.getVanishU();
        final Double vanishV = camera.getVanishV();
        final Double leftU = camera.getLeftU();
        final Double rightU = camera.getRightU();
        if (vanishU != null && vanishV != null) {
            // draw horizon line
            final int vanishY = Math.round((float) (vanishV * calibrationMat.height()));
            final Point vanishPoint = new Point(vanishU * calibrationMat.width(), vanishV * calibrationMat.height());
            Core.circle(calibrationMat, vanishPoint, 4, new Scalar(255, 0, 0), 2);
//        Core.line(blurred, new Point(0, vanishY), new Point(blurred.width(), vanishY),
//                new Scalar(200, 0, 0), 2);
            if (leftU != null && rightU != null) {
                final Point leftPoint = new Point(leftU * calibrationMat.width(), calibrationMat.height());
                final Point rightPoint = new Point(rightU * calibrationMat.width(), calibrationMat.height());
                Core.polylines(calibrationMat, ImmutableList.of(new MatOfPoint(vanishPoint, leftPoint, rightPoint, vanishPoint)), false,
                        new Scalar(255, 0, 0), 2);
            }
        }

        final MatOfByte calibrationBytesMat = new MatOfByte();
        Highgui.imencode(".jpg", calibrationMat, calibrationBytesMat);
        return calibrationBytesMat.toArray();
    }
}
