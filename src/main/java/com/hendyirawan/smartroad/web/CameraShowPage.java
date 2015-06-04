package com.hendyirawan.smartroad.web;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.hendyirawan.smartroad.core.*;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.jpeg.JpegParser;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.core.util.string.JavaScriptUtils;
import org.apache.wicket.datetime.StyleDateConverter;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.joda.time.base.AbstractInstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soluvas.web.site.Interaction;
import org.soluvas.web.site.SeoBookmarkableMapper;
import org.soluvas.web.site.widget.DateTimeLabel;
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
import org.wicketstuff.gmap.api.GPolyline;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.measure.quantity.Area;
import javax.measure.unit.ProductUnit;
import javax.measure.unit.SI;
import java.io.IOException;
import java.util.Iterator;

/**
 * Created by ceefour on 28/12/14.
 */
@MountPath("/${localePrefId}/cameras/${cameraId}")
public class CameraShowPage extends PubLayout {

    private static final Logger log = LoggerFactory.getLogger(CameraShowPage.class);

    @Inject
    private CameraRepository cameraRepo;
    @Inject
    private SurveyRepository surveyRepo;
    @Inject
    private Environment env;

    private IModel<Camera> model;

    private class SurveyDataProvider extends SortableDataProvider<Survey, String> {
        protected int itemsPerPage = 10;

        @Override
        public Iterator<? extends Survey> iterator(long first, long count) {
            final Sort sort;
            if (getSort() != null) {
                sort = new Sort(getSort().isAscending() ? Sort.Direction.ASC : Sort.Direction.DESC,
                        getSort().getProperty());
            } else {
                sort = new Sort(Sort.Direction.DESC, "surveyTime");
            }
            final Page<Survey> surveys = surveyRepo.findAllByCameraId(model.getObject().getId(),
                    new PageRequest((int) (first / itemsPerPage), itemsPerPage, sort));
            return surveys.iterator();
        }

        @Override
        public long size() {
            return surveyRepo.countByCameraId(model.getObject().getId());
        }

        @Override
        public IModel<Survey> model(Survey object) {
            return new Model<>(object);
        }
    }

    public CameraShowPage(PageParameters parameters) {
        super(parameters);
        final String cameraId = parameters.get("cameraId").toString();
        final Camera camera = Preconditions.checkNotNull(cameraRepo.findOne(cameraId),
                "Cannot find camera '%s'", cameraId);
        model = new Model<>(camera);
        setDefaultModel(model);

        final Form<Camera> form = new Form<>("form", model);
        form.add(new Label("heading", new PropertyModel<>(model, "name")));
        if (model.getObject().getRoad() != null) {
            final Road road = model.getObject().getRoad();
            form.add(new BookmarkablePageLink<>("roadLink", RoadShowPage.class,
                    new PageParameters().set(SeoBookmarkableMapper.LOCALE_PREF_ID_PARAMETER, localePrefId)
                            .set("roadId", road.getId()))
                .setBody(new Model<>(road.getName())));
        } else {
            form.add(new EmptyPanel("roadLink").setVisible(false));
        }
        form.add(new BookmarkablePageLink<>("editLink", CameraModifyPage.class,
                new PageParameters().set(SeoBookmarkableMapper.LOCALE_PREF_ID_PARAMETER, localePrefId)
                        .set("cameraId", cameraId)));
        form.add(new AjaxButton("trashBtn") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onSubmit(target, form);
                final Camera camera = model.getObject();
                cameraRepo.delete(cameraId);
                Interaction.DELETED.info("Camera '%s' deleted.", camera.getName());
                setResponsePage(CameraListPage.class,
                        new PageParameters().set(SeoBookmarkableMapper.LOCALE_PREF_ID_PARAMETER,
                                localePrefId));
            }

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                super.updateAjaxAttributes(attributes);
                attributes.getAjaxCallListeners().add(new AjaxCallListener() {
                    @Override
                    public CharSequence getPrecondition(Component component) {
                        final Camera camera = model.getObject();
                        return "return confirm(\"Delete camera '" + JavaScriptUtils.escapeQuotes(camera.getName()) + "?\");";
                    }
                });
            }
        });

        form.add(new Image("calibrationImg", new CalibrationImageResource(model)));

        final Double lat = new PropertyModel<Double>(model, "lat").getObject();
        final Double lon = new PropertyModel<Double>(model, "lon").getObject();
        if (lat != null && lon != null) {
            final GMap gmap = new GMap("map");
            gmap.setPanControlEnabled(false);
            gmap.setScaleControlEnabled(false);
            gmap.setScrollWheelZoomEnabled(false);
            gmap.setZoomControlEnabled(false);
            gmap.setMapTypeControlEnabled(false);
            gmap.setDraggingEnabled(false);
            gmap.setDoubleClickZoomEnabled(false);
            final GLatLng latLng = new GLatLng(lat, lon);
            gmap.setCenter(latLng);
            gmap.setZoom(15);

            gmap.addOverlay(new GMarker(new GMarkerOptions(gmap, latLng)));
            form.add(gmap);

            form.add(new MeasureLabel("eleLabel", new Model<>(SI.METER), new PropertyModel<>(model, "ele")));
            form.add(new ExternalLink("geoUri", String.format("geo:%f,%f", lat, lon)));
            form.add(new ExternalLink("gmapsLink", String.format("http://maps.google.com/?q=%f,%f", lat, lon)));
        } else {
            form.add(new EmptyPanel("eleLabel").setVisible(false));
            form.add(new EmptyPanel("map").setVisible(false));
            form.add(new EmptyPanel("geoUri").setVisible(false));
            form.add(new EmptyPanel("gmapsLink").setVisible(false));
        }
        form.add(new Label("vanishULabel", new PropertyModel<>(model, "vanishU")));
        form.add(new Label("vanishVLabel", new PropertyModel<>(model, "vanishV")));
        form.add(new Label("leftULabel", new PropertyModel<>(model, "leftU")));
        form.add(new Label("rightULabel", new PropertyModel<>(model, "rightU")));

        final WebMarkupContainer infoDiv = new WebMarkupContainer("infoDiv");
        infoDiv.setOutputMarkupId(true);
        infoDiv.add(new DateTimeLabel2("surveyTime", new PropertyModel<>(model, "surveyTime")));
        infoDiv.add(new Label("damageKind", new PropertyModel<>(model, "damageKind")));
        infoDiv.add(new Label("damageLevel", new PropertyModel<>(model, "damageLevel")));
        infoDiv.add(new Label("potholeCount", new PropertyModel<>(model, "potholeCount")));
        infoDiv.add(new MeasureLabel("potholeWidth",
                new Model<>(SI.MILLIMETER),
                new PropertyModel<>(model, "potholeWidth")));
        infoDiv.add(new MeasureLabel("potholeLength",
                new Model<>(SI.MILLIMETER),
                new PropertyModel<>(model, "potholeLength")));
        infoDiv.add(new MeasureLabel("potholeDepth",
                new Model<>(SI.MILLIMETER),
                new PropertyModel<>(model, "potholeDepth")));
        infoDiv.add(new MeasureLabel("potholeArea",
                new Model<>(SI.MILLIMETER.times(SI.MILLIMETER)),
                new PropertyModel<>(model, "potholeArea")));
        form.add(infoDiv);

        final WebMarkupContainer potholeInfo = new WebMarkupContainer("potholeInfo") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(RoadDamageKind.POTHOLE.equals(model.getObject().getDamageKind()));
            }
        };
        form.add(potholeInfo);

        form.add(new BookmarkablePageLink<>("addSurveyLink", SurveyModifyPage.class,
                new PageParameters().set(SeoBookmarkableMapper.LOCALE_PREF_ID_PARAMETER, localePrefId)
                        .set("cameraId", cameraId)));
        final SurveyDataProvider surveyDp = new SurveyDataProvider();
        final DataView<Survey> surveysDv = new DataView<Survey>("surveysDv", surveyDp, surveyDp.itemsPerPage) {
            @Override
            protected void populateItem(Item<Survey> item) {
                final BookmarkablePageLink<SurveyShowPage> link1 = new BookmarkablePageLink<>("link1", SurveyShowPage.class,
                        new PageParameters().set(SeoBookmarkableMapper.LOCALE_PREF_ID_PARAMETER, localePrefId)
                                .set("surveyId", item.getModelObject().getId()));
                link1.add(new Image("photo", new SurveyPhotoImageResource(item.getModel(), 320, 240)));
                item.add(link1);
                final BookmarkablePageLink<SurveyShowPage> link2 = new BookmarkablePageLink<>("link2", SurveyShowPage.class,
                        new PageParameters().set(SeoBookmarkableMapper.LOCALE_PREF_ID_PARAMETER, localePrefId)
                                .set("surveyId", item.getModelObject().getId()));
                link2.add(new DateTimeLabel2("surveyTime", new PropertyModel<>(item.getModel(), "surveyTime")));
                item.add(link2);
                item.add(new Label("damageKind", new PropertyModel<>(item.getModel(), "damageKind")));
                item.add(new MeasureLabel("potholeArea",
                        new Model<>(SI.MILLIMETER.times(SI.MILLIMETER)),
                        new PropertyModel<>(item.getModel(), "potholeArea")));
            }
        };
        form.add(surveysDv);

        add(form);
    }

    @Override
    public IModel<String> getTitleModel() {
        return new PropertyModel<>(getDefaultModel(), "name");
    }

    @Override
    public IModel<String> getMetaDescriptionModel() {
        return new PropertyModel<>(getDefaultModel(), "description");
    }

}
