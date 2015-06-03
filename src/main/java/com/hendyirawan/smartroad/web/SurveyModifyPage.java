package com.hendyirawan.smartroad.web;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.hendyirawan.smartroad.core.*;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.jpeg.JpegParser;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.extensions.yui.calendar.DateTimeField;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.*;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soluvas.web.site.Interaction;
import org.soluvas.web.site.SeoBookmarkableMapper;
import org.soluvas.web.site.widget.DateTimeLabel2;
import org.springframework.core.env.Environment;
import org.wicketstuff.annotation.mount.MountPath;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by ceefour on 28/12/14.
 */
@MountPath("/${localePrefId}/surveys/modify")
public class SurveyModifyPage extends PubLayout {

    private static final Logger log = LoggerFactory.getLogger(SurveyModifyPage.class);

    @Inject
    private CameraRepository cameraRepo;
    @Inject
    private SurveyRepository surveyRepo;
    @Inject
    private Environment env;

    private IModel<Survey> model;

    public SurveyModifyPage(PageParameters parameters) {
        super(parameters);
        if (!parameters.get("surveyId").isEmpty()) {
            final long surveyId = parameters.get("surveyId").toLong();
            final Survey survey = surveyRepo.findOne(surveyId);
            model = new Model<>(survey);
        } else {
            final Survey survey = new Survey();
            final String cameraId = Preconditions.checkNotNull(parameters.get("cameraId").toString(),
                    "Camera ID must be specified");
            survey.setCamera(cameraRepo.findOne(cameraId));
            model = new Model<>(survey);
        }
        setDefaultModel(model);
        add(new Label("heading", getTitleModel()));
        add(new BookmarkablePageLink<>("cameraLink", CameraShowPage.class,
                new PageParameters().set(SeoBookmarkableMapper.LOCALE_PREF_ID_PARAMETER, localePrefId)
                        .set("cameraId", model.getObject().getCamera().getId())));

        final Form<Survey> form = new Form<>("form", model);
        form.setOutputMarkupId(true);
        form.add(new Label("idLabel", new PropertyModel<>(model, "id")));

        final Image photoImg = new Image("photoImg", new SurveyPhotoImageResource(model, 320, 240));
        photoImg.setOutputMarkupId(true);
        form.add(photoImg);

//        final DateTimeLabel2 surveyTimeLabel = new DateTimeLabel2("surveyTimeLabel", new PropertyModel<>(model, "surveyTime"));
//        surveyTimeLabel.setOutputMarkupId(true);
//        form.add(surveyTimeLabel);
        final TextField<DateTime> surveyTimeFld = new TextField<DateTime>("surveyTimeFld", new PropertyModel<>(model, "surveyTime"), DateTime.class) {
            @Override
            protected void convertInput() {
                setConvertedInput(!Strings.isNullOrEmpty(getInput()) ? new DateTime(getInput()) : null);
            }
        };
        surveyTimeFld.setOutputMarkupId(true);
        form.add(surveyTimeFld);
        final DropDownChoice<RoadDamageKind> damageKindSelect = new DropDownChoice<>("damageKind", new PropertyModel<>(model, "damageKind"),
                ImmutableList.copyOf(RoadDamageKind.values()));
        damageKindSelect.setOutputMarkupId(true);
        form.add(damageKindSelect);
        final NumberTextField<Double> potholeAreaFld = new NumberTextField<>("potholeAreaFld", new PropertyModel<>(model, "potholeArea"), Double.class)
                .setMinimum(0d);
        potholeAreaFld.setOutputMarkupId(true);
        form.add(potholeAreaFld);

        final ListModel<FileUpload> photoFilesModel = new ListModel<>(new ArrayList<>());
        final FileUploadField photoFilesFld = new FileUploadField("photoFiles", photoFilesModel);
        form.add(photoFilesFld);
        form.add(new IndicatingAjaxButton("uploadBtn") {
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                 super.onSubmit(target, form);
                 @Nullable
                 final FileUpload file = photoFilesModel.getObject().iterator().next();
                 final Metadata metadata = new Metadata();
                 final ContentHandler handler = new DefaultHandler();
                 final Parser parser = new JpegParser();
                 final ParseContext context = new ParseContext();
                 //final String mimeType = tika.detect(fileName);
                 try {
                     parser.parse(file.getInputStream(), handler, metadata, context);
                     @Nullable
                     final Date originalDate = metadata.getDate(Metadata.ORIGINAL_DATE);
                     if (originalDate != null) {
                         // TODO: shouldn't hardcode time zone
                         final DateTime surveyTime = new DateTime(originalDate, DateTimeZone.forID("Asia/Jakarta"));
                         model.getObject().setSurveyTime(surveyTime);
                     } else {
                         model.getObject().setSurveyTime(new DateTime());
                     }

                     model.getObject().setPhotoType(file.getContentType());
                     model.getObject().setPhotoWidth(metadata.getInt(Metadata.IMAGE_WIDTH));
                     model.getObject().setPhotoHeight(metadata.getInt(Metadata.IMAGE_LENGTH));
                     model.getObject().setPhoto(file.getBytes());
                     target.add(photoImg, surveyTimeFld, damageKindSelect, potholeAreaFld);
                 } catch (Exception e) {
                     Throwables.propagate(e);
                 }
             }
         });

        form.add(new IndicatingAjaxButton("saveBtn") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onSubmit(target, form);
                Survey survey = model.getObject();
                if (survey.getCreationTime() == null) {
                    survey.setCreationTime(new DateTime());
                }
                if (survey.getSurveyTime() == null) {
                    survey.setSurveyTime(new DateTime());
                }
                survey.setModificationTime(new DateTime());

                survey = surveyRepo.save(survey);
                Interaction.MODIFIED.info("Survey '%s' saved.", survey.getSurveyTime());
                setResponsePage(SurveyShowPage.class,
                        new PageParameters().set(SeoBookmarkableMapper.LOCALE_PREF_ID_PARAMETER, localePrefId)
                                .set("surveyId", survey.getId()));
            }
        });
        add(form);
    }

    @Override
    public IModel<String> getTitleModel() {
        return new AbstractReadOnlyModel<String>() {
            @Override
            public String getObject() {
                if (model.getObject().getCreationTime() != null) {
                    return "Edit Survey " + model.getObject().getSurveyTime();
                } else {
                    return "Add Survey";
                }
            }
        };
    }

    @Override
    public IModel<String> getMetaDescriptionModel() {
        return new Model<>("Add/Edit Survey");
    }

}
