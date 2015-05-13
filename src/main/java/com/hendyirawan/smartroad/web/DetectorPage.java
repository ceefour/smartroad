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
import org.apache.wicket.request.resource.IResource;
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

    private IModel<byte[]> resultBytes = new Model<>();

    @Inject
    private RoadAnalyzer roadAnalyzer;

    public DetectorPage(PageParameters parameters) {
        super(parameters);
        final ListModel<FileUpload> cameraFilesModel = new ListModel<>();

        final Image resultImg = new Image("resultImg", new DynamicImageResource() {
            @Override
            protected byte[] getImageData(Attributes attributes) {
                return resultBytes.getObject();
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
                final Mat cameraMat = Highgui.imdecode(new MatOfByte(cameraFile.getBytes()), 0);
                final RoadAnalysis analysis = roadAnalyzer.analyze(cameraMat);
                final MatOfByte edgesBytes = new MatOfByte();
                Highgui.imencode(".jpg", analysis.edges, edgesBytes);
                resultBytes.setObject(edgesBytes.toArray());
                log.info("Edges is {} bytes", resultBytes.getObject().length);
                Interaction.INFO.info("Edges is %s bytes", resultBytes.getObject().length);
                target.add(resultImg);
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
