package com.hendyirawan.smartroad.web;

import com.google.common.base.Preconditions;
import com.hendyirawan.smartroad.core.*;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.core.util.string.JavaScriptUtils;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.DynamicImageResource;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soluvas.web.site.Interaction;
import org.soluvas.web.site.SeoBookmarkableMapper;
import org.soluvas.web.site.widget.DateTimeLabel2;
import org.soluvas.web.site.widget.MeasureLabel;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.wicketstuff.annotation.mount.MountPath;
import org.wicketstuff.gmap.GMap;
import org.wicketstuff.gmap.api.GLatLng;
import org.wicketstuff.gmap.api.GMarker;
import org.wicketstuff.gmap.api.GMarkerOptions;

import javax.inject.Inject;
import javax.measure.unit.SI;
import java.util.Iterator;

/**
 * Created by ceefour on 28/12/14.
 */
@MountPath("/${localePrefId}/surveys/${surveyId}")
public class SurveyShowPage extends PubLayout {

    private static final Logger log = LoggerFactory.getLogger(SurveyShowPage.class);

    @Inject
    private CameraRepository cameraRepo;
    @Inject
    private SurveyRepository surveyRepo;
    @Inject
    private Environment env;
    @Inject
    private RoadAnalyzer roadAnalyzer;

    private IModel<Survey> model;
    private IModel<byte[]> augmentedBytes = new Model<>();
    private IModel<byte[]> blurredBytes = new Model<>();
    private IModel<byte[]> edgesBytes = new Model<>();

    public SurveyShowPage(PageParameters parameters) {
        super(parameters);
        final long surveyId = parameters.get("surveyId").toLong();
        final Survey survey = Preconditions.checkNotNull(surveyRepo.findOne(surveyId),
                "Cannot find survey '%s'", surveyId);
        model = new Model<>(survey);
        setDefaultModel(model);

        final Form<Survey> form = new Form<>("form", model);
        form.add(new DateTimeLabel2("heading", new PropertyModel<>(model, "surveyTime")));
        form.add(new BookmarkablePageLink<>("cameraLink", CameraShowPage.class,
                new PageParameters().set(SeoBookmarkableMapper.LOCALE_PREF_ID_PARAMETER, localePrefId)
                        .set("cameraId", survey.getCamera().getId()))
            .setBody(new Model<>(survey.getCamera().getName())));
        form.add(new BookmarkablePageLink<>("editLink", SurveyModifyPage.class,
                new PageParameters().set(SeoBookmarkableMapper.LOCALE_PREF_ID_PARAMETER, localePrefId)
                        .set("surveyId", surveyId)));

        final Image augmentedImg = new Image("augmentedImg", new DynamicImageResource() {
            @Override
            protected byte[] getImageData(Attributes attributes) {
                return augmentedBytes.getObject();
            }
        });
        augmentedImg.setOutputMarkupId(true);
        form.add(augmentedImg);
        final Image blurredImg = new Image("blurredImg", new DynamicImageResource() {
            @Override
            protected byte[] getImageData(Attributes attributes) {
                return blurredBytes.getObject();
            }
        });
        blurredImg.setOutputMarkupId(true);
        form.add(blurredImg);
        final Image resultImg = new Image("resultImg", new DynamicImageResource() {
            @Override
            protected byte[] getImageData(Attributes attributes) {
                return edgesBytes.getObject();
            }
        });
        resultImg.setOutputMarkupId(true);
        form.add(resultImg);

        final WebMarkupContainer infoDiv = new WebMarkupContainer("infoDiv");
        infoDiv.setOutputMarkupId(true);
//        form.add(new DateTimeLabel2("surveyTime", new PropertyModel<>(model, "surveyTime")));
        infoDiv.add(new Label("damageKind", new PropertyModel<>(model, "damageKind")));
        infoDiv.add(new Label("damageLevel", new PropertyModel<>(model, "damageLevel")));
        infoDiv.add(new Label("potholeCount", new PropertyModel<>(model, "potholeCount")));
        infoDiv.add(new MeasureLabel("potholeWidth",
                new Model<>(SI.MILLIMETRE),
                new PropertyModel<>(model, "potholeWidth")));
        infoDiv.add(new MeasureLabel("potholeLength",
                new Model<>(SI.MILLIMETRE),
                new PropertyModel<>(model, "potholeLength")));
        infoDiv.add(new MeasureLabel("potholeDepth",
                new Model<>(SI.MILLIMETRE),
                new PropertyModel<>(model, "potholeDepth")));
        infoDiv.add(new MeasureLabel("potholeArea",
                new Model<>(SI.MILLIMETRE.times(SI.MILLIMETRE)),
                new PropertyModel<>(model, "potholeArea")));
        form.add(infoDiv);

        final WebMarkupContainer potholeInfo = new WebMarkupContainer("potholeInfo") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(RoadDamageKind.POTHOLE.equals(model.getObject().getDamageKind()));
            }
        };
        potholeInfo.setOutputMarkupId(true);
        form.add(potholeInfo);

        form.add(new IndicatingAjaxButton("analyzeBtn") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onSubmit(target, form);
                final Survey survey = model.getObject();
                final Camera camera = survey.getCamera();
                final Road road = camera.getRoad();
                final Mat cameraMat = Highgui.imdecode(new MatOfByte(survey.getPhoto()), Highgui.CV_LOAD_IMAGE_COLOR);
                Preconditions.checkNotNull(camera.getVanishU(), "Camera '%s' must have vanishU", camera.getName());
                Preconditions.checkNotNull(camera.getVanishV(), "Camera '%s' must have vanishV", camera.getName());
                Preconditions.checkNotNull(camera.getLeftU(), "Camera '%s' must have leftU", camera.getName());
                Preconditions.checkNotNull(camera.getRightU(), "Camera '%s' must have rightU", camera.getName());
                Preconditions.checkNotNull(road.getWidth(), "Road '%s' must have width", road.getName());
                final RoadAnalysis analysis = roadAnalyzer.analyze(cameraMat,
                        camera.getVanishU(), camera.getVanishV(), camera.getLeftU(), camera.getRightU(),
                        camera.getRoad().getWidth());

                final MatOfByte augmentedBytesMat = new MatOfByte();
                Highgui.imencode(".jpg", analysis.augmented, augmentedBytesMat);
                augmentedBytes.setObject(augmentedBytesMat.toArray());

                final MatOfByte blurredBytesMat = new MatOfByte();
                Highgui.imencode(".jpg", analysis.blurred, blurredBytesMat);
                blurredBytes.setObject(blurredBytesMat.toArray());

                final MatOfByte edgesBytesMat = new MatOfByte();
                Highgui.imencode(".jpg", analysis.edges, edgesBytesMat);
                edgesBytes.setObject(edgesBytesMat.toArray());
                log.info("Edges is {} bytes", SurveyShowPage.this.edgesBytes.getObject().length);
                Interaction.INFO.info("Edges is %s bytes", SurveyShowPage.this.edgesBytes.getObject().length);

                survey.setDamageKind(analysis.damageKind);
                survey.setDamageLevel(analysis.damageLevel);
                survey.setPotholeCount(analysis.potholeCount);
                survey.setPotholeWidth(analysis.totalPotholeWidth);
                survey.setPotholeLength(analysis.totalPotholeLength);
                survey.setPotholeDepth(analysis.totalPotholeDepth);
                survey.setPotholeArea(analysis.totalPotholeArea);
                surveyRepo.save(survey);

                camera.setSurveyTime(survey.getSurveyTime());
                camera.setDamageKind(analysis.damageKind);
                camera.setDamageLevel(analysis.damageLevel);
                camera.setPotholeCount(analysis.potholeCount);
                camera.setPotholeWidth(analysis.totalPotholeWidth);
                camera.setPotholeLength(analysis.totalPotholeLength);
                camera.setPotholeDepth(analysis.totalPotholeDepth);
                camera.setPotholeArea(analysis.totalPotholeArea);
                cameraRepo.save(camera);

                target.add(augmentedImg, blurredImg, resultImg, infoDiv, potholeInfo);
            }
        });

        form.add(new AjaxButton("trashBtn") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onSubmit(target, form);
                final Survey survey = model.getObject();
                final String cameraId = survey.getCamera().getId();
                surveyRepo.delete(surveyId);
                Interaction.DELETED.info("Survey '%s' deleted.", survey.getSurveyTime());
                setResponsePage(CameraShowPage.class,
                        new PageParameters().set(SeoBookmarkableMapper.LOCALE_PREF_ID_PARAMETER, localePrefId)
                            .set("cameraId", cameraId));
            }

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                super.updateAjaxAttributes(attributes);
                attributes.getAjaxCallListeners().add(new AjaxCallListener() {
                    @Override
                    public CharSequence getPrecondition(Component component) {
                        final Survey survey = model.getObject();
                        return "return confirm(\"Delete survey '" + JavaScriptUtils.escapeQuotes(survey.getSurveyTime().toString()) + "?\");";
                    }
                });
            }
        });

        form.add(new Image("photoImg", new SurveyPhotoImageResource(model, 640, 480)));

        add(form);
    }

    @Override
    public IModel<String> getTitleModel() {
        return new AbstractReadOnlyModel<String>() {
            @Override
            public String getObject() {
                return model.getObject().getSurveyTime().toString();
            }
        };
    }

    @Override
    public IModel<String> getMetaDescriptionModel() {
        return new AbstractReadOnlyModel<String>() {
            @Override
            public String getObject() {
                return model.getObject().getSurveyTime().toString();
            }
        };
    }

}
