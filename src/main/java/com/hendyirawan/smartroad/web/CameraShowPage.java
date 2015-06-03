package com.hendyirawan.smartroad.web;

import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.hendyirawan.smartroad.core.Camera;
import com.hendyirawan.smartroad.core.CameraRepository;
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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soluvas.web.site.Interaction;
import org.soluvas.web.site.SeoBookmarkableMapper;
import org.soluvas.web.site.widget.MeasureLabel;
import org.springframework.core.env.Environment;
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
import javax.measure.unit.SI;
import java.io.IOException;

/**
 * Created by ceefour on 28/12/14.
 */
@MountPath("/${localePrefId}/cameras/${cameraId}")
public class CameraShowPage extends PubLayout {

    private static final Logger log = LoggerFactory.getLogger(CameraShowPage.class);

    @Inject
    private CameraRepository cameraRepo;
    @Inject
    private Environment env;

    private IModel<Camera> model;

    public CameraShowPage(PageParameters parameters) {
        super(parameters);
        final String cameraId = parameters.get("cameraId").toString();
        final Camera camera = cameraRepo.findOne(cameraId);
        model = new Model<>(camera);
        setDefaultModel(model);

        final Form<Camera> form = new Form<>("form", model);
        form.add(new Label("heading", new PropertyModel<>(model, "name")));
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
