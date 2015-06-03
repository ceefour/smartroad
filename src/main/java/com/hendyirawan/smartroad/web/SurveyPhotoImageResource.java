package com.hendyirawan.smartroad.web;

import com.google.common.collect.ImmutableList;
import com.hendyirawan.smartroad.core.Camera;
import com.hendyirawan.smartroad.core.Survey;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.DynamicImageResource;
import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ceefour on 6/4/15.
 */
public class SurveyPhotoImageResource extends DynamicImageResource {
    private static final Logger log = LoggerFactory.getLogger(SurveyPhotoImageResource.class);
    private IModel<Survey> model;
    private int width;
    private int height;

    public SurveyPhotoImageResource(IModel<Survey> model, int width, int height) {
        this.model = model;
        this.width = width;
        this.height = height;
    }

    @Override
    protected byte[] getImageData(Attributes attributes) {
        final Survey survey = model.getObject();
        final Camera camera = survey.getCamera();
        if (survey.getPhoto() == null) {
            log.info("Survey '{}' has no photo, returning empty photo", survey.getSurveyTime());
            return new byte[]{};
        }
        final Mat mat = Highgui.imdecode(new MatOfByte(survey.getPhoto()),
                Highgui.CV_LOAD_IMAGE_COLOR);
        Imgproc.resize(mat, mat, new Size(width, height), 0, 0, Imgproc.INTER_AREA);

        final Double vanishU = camera.getVanishU();
        final Double vanishV = camera.getVanishV();
        final Double leftU = camera.getLeftU();
        final Double rightU = camera.getRightU();
        if (vanishU != null && vanishV != null) {
            // draw horizon line
            final int vanishY = Math.round((float) (vanishV * mat.height()));
            final Point vanishPoint = new Point(vanishU * mat.width(), vanishV * mat.height());
            Core.circle(mat, vanishPoint, 4, new Scalar(255, 0, 0), 2);
//        Core.line(blurred, new Point(0, vanishY), new Point(blurred.width(), vanishY),
//                new Scalar(200, 0, 0), 2);
            if (leftU != null && rightU != null) {
                final Point leftPoint = new Point(leftU * mat.width(), mat.height());
                final Point rightPoint = new Point(rightU * mat.width(), mat.height());
                Core.polylines(mat, ImmutableList.of(new MatOfPoint(vanishPoint, leftPoint, rightPoint, vanishPoint)), false,
                        new Scalar(255, 0, 0), 2);
            }
        }

        final MatOfByte bytesMat = new MatOfByte();
        Highgui.imencode(".jpg", mat, bytesMat);
        return bytesMat.toArray();
    }
}
