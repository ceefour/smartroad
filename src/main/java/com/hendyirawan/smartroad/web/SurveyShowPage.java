package com.hendyirawan.smartroad.web;

import com.google.common.base.Preconditions;
import com.hendyirawan.smartroad.core.*;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.core.util.string.JavaScriptUtils;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
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

    private IModel<Survey> model;

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

//        form.add(new DateTimeLabel2("surveyTime", new PropertyModel<>(model, "surveyTime")));
        form.add(new Label("damageKind", new PropertyModel<>(model, "damageKind")));
        form.add(new MeasureLabel("potholeArea",
                new Model<>(SI.MILLIMETER.times(SI.MILLIMETER)),
                new PropertyModel<>(model, "potholeArea")));

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
