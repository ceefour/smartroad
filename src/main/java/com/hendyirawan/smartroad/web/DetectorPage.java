package com.hendyirawan.smartroad.web;

import com.hendyirawan.smartroad.core.RoadAnalysis;
import com.hendyirawan.smartroad.core.RoadAnalyzer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.DynamicImageResource;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soluvas.web.site.Interaction;
import org.wicketstuff.annotation.mount.MountPath;

import javax.inject.Inject;

/**
 * Created by ceefour on 5/13/15.
 */
@MountPath("/${localePrefId}/detector")
public class DetectorPage extends PubLayout {

    private static final Logger log = LoggerFactory.getLogger(DetectorPage.class);

    private IModel<byte[]> blurredBytes = new Model<>();
    private IModel<byte[]> edgesBytes = new Model<>();
    private IModel<byte[]> augmentedBytes = new Model<>();

    @Inject
    private RoadAnalyzer roadAnalyzer;

    public DetectorPage(PageParameters parameters) {
        super(parameters);
        final ListModel<FileUpload> cameraFilesModel = new ListModel<>();

        final Image augmentedImg = new Image("augmentedImg", new DynamicImageResource() {
            @Override
            protected byte[] getImageData(Attributes attributes) {
                return augmentedBytes.getObject();
            }
        });
        augmentedImg.setOutputMarkupId(true);
        add(augmentedImg);
        final Image blurredImg = new Image("blurredImg", new DynamicImageResource() {
            @Override
            protected byte[] getImageData(Attributes attributes) {
                return blurredBytes.getObject();
            }
        });
        blurredImg.setOutputMarkupId(true);
        add(blurredImg);
        final Image resultImg = new Image("resultImg", new DynamicImageResource() {
            @Override
            protected byte[] getImageData(Attributes attributes) {
                return edgesBytes.getObject();
            }
        });
        resultImg.setOutputMarkupId(true);
        add(resultImg);

        final Form<Void> form = new Form<>("form");
        form.add(new FileUploadField("cameraFiles", cameraFilesModel));
        form.add(new IndicatingAjaxButton("detectBtn") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onSubmit(target, form);
                final FileUpload cameraFile = cameraFilesModel.getObject().stream().findFirst().get();
                final Mat cameraMat = Highgui.imdecode(new MatOfByte(cameraFile.getBytes()), Highgui.CV_LOAD_IMAGE_COLOR);
                final RoadAnalysis analysis = roadAnalyzer.analyze(cameraMat, 0.13, 0.32, -0.05, 1.7, 3.5);

                final MatOfByte blurredBytesMat = new MatOfByte();
                Highgui.imencode(".jpg", analysis.blurred, blurredBytesMat);
                blurredBytes.setObject(blurredBytesMat.toArray());

                final MatOfByte edgesBytesMat = new MatOfByte();
                Highgui.imencode(".jpg", analysis.edges, edgesBytesMat);
                edgesBytes.setObject(edgesBytesMat.toArray());
                log.info("Edges is {} bytes", DetectorPage.this.edgesBytes.getObject().length);
                Interaction.INFO.info("Edges is %s bytes", DetectorPage.this.edgesBytes.getObject().length);

                final MatOfByte augmentedBytesMat = new MatOfByte();
                Highgui.imencode(".jpg", analysis.augmented, augmentedBytesMat);
                augmentedBytes.setObject(augmentedBytesMat.toArray());

                target.add(augmentedImg, blurredImg, resultImg);
            }
        });
        add(form);
    }

    @Override
    public IModel<String> getTitleModel() {
        return new Model<>("Road Damage Detector");
    }

    @Override
    public IModel<String> getMetaDescriptionModel() {
        return new Model<>("Road Damage Detector");
    }
}
