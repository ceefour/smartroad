package com.hendyirawan.smartroad.web;

import com.google.common.collect.ImmutableList;
import com.hendyirawan.smartroad.core.Road;
import com.hendyirawan.smartroad.core.RoadDamageKind;
import com.hendyirawan.smartroad.core.RoadPavement;
import com.hendyirawan.smartroad.core.RoadRepository;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soluvas.web.site.Interaction;
import org.soluvas.web.site.SeoBookmarkableMapper;
import org.springframework.core.env.Environment;
import org.wicketstuff.annotation.mount.MountPath;
import org.wicketstuff.gmap.GMap;
import org.wicketstuff.gmap.api.*;

import javax.inject.Inject;
import java.util.Optional;

/**
 * Created by ceefour on 28/12/14.
 */
@MountPath("/${localePrefId}/roads/modify")
public class RoadModifyPage extends PubLayout {

    private static final Logger log = LoggerFactory.getLogger(RoadModifyPage.class);

    @Inject
    private RoadRepository roadRepo;
    @Inject
    private Environment env;

    private IModel<Road> model;

    public RoadModifyPage(PageParameters parameters) {
        super(parameters);
        if (!parameters.get("roadId").isEmpty()) {
            final long roadId = parameters.get("roadId").toLong();
            final Road road = roadRepo.findOne(roadId);
            model = new Model<>(road);
        } else {
            final Road road = new Road();
            road.setCreationTime(new DateTime());
            model = new Model<>(road);
        }
        setDefaultModel(model);
        add(new Label("heading", getTitleModel()));
        final Form<Road> form = new Form<>("form", model);
        form.setOutputMarkupId(true);
        form.add(new Label("idFld", new PropertyModel<>(model, "id")));
        form.add(new TextField<>("nameFld", new PropertyModel<>(model, "name")).setRequired(true));
        final DropDownChoice<RoadPavement> pavementSelect = new DropDownChoice<>("pavementSelect", new PropertyModel<>(model, "pavement"),
                ImmutableList.copyOf(RoadPavement.values()));
        pavementSelect.setRequired(true);
        pavementSelect.setOutputMarkupId(true);
        form.add(pavementSelect);
        form.add(new TextField<>("descriptionFld", new PropertyModel<>(model, "description")));
        final NumberTextField<Double> widthFld = new NumberTextField<>("widthFld", new PropertyModel<>(model, "width"), Double.class);
        widthFld.setRequired(true);
        widthFld.setMinimum(0d);
        widthFld.setMaximum(20d);
        widthFld.setStep(1d);
        form.add(widthFld);

        final WebMarkupContainer startDiv = new WebMarkupContainer("startDiv");
        startDiv.setOutputMarkupId(true);
        startDiv.add(new NumberTextField<>("startLatFld", new PropertyModel<>(model, "startLat"), Double.class)
                .setMinimum(-90d).setMaximum(90d).setStep(0.000000000000001)); // not ideal, but this is W3C spec!
        startDiv.add(new NumberTextField<>("startLonFld", new PropertyModel<>(model, "startLon"), Double.class)
                .setMinimum(-180d).setMaximum(180d).setStep(0.000000000000001)); // not ideal, but this is W3C spec!
        startDiv.add(new NumberTextField<>("startEleFld", new PropertyModel<>(model, "startEle"), Double.class)
                .setMinimum(-1000d).setMaximum(10000d).setStep(1d));
        form.add(startDiv);
        final WebMarkupContainer finishDiv = new WebMarkupContainer("finishDiv");
        finishDiv.setOutputMarkupId(true);
        finishDiv.add(new NumberTextField<>("finishLatFld", new PropertyModel<>(model, "finishLat"), Double.class)
                .setMinimum(-90d).setMaximum(90d).setStep(0.000000000000001)); // not ideal, but this is W3C spec!
        finishDiv.add(new NumberTextField<>("finishLonFld", new PropertyModel<>(model, "finishLon"), Double.class)
                .setMinimum(-180d).setMaximum(180d).setStep(0.000000000000001)); // not ideal, but this is W3C spec!
        finishDiv.add(new NumberTextField<>("finishEleFld", new PropertyModel<>(model, "finishEle"), Double.class)
                .setMinimum(-1000d).setMaximum(10000d).setStep(1d));
        form.add(finishDiv);

        final GMap gmap = new GMap("map");
        gmap.setPanControlEnabled(true);
        gmap.setScaleControlEnabled(true);
        gmap.setScrollWheelZoomEnabled(true);
        gmap.setDoubleClickZoomEnabled(false);
//        Geolocation geo = Optional.ofNullable(placeModel.getObject().getGeo()).or(new Geolocation());
//        // Toronto: 43.7000° N, 79.4000
        // Bandung: geo:-6.916667,107.6
        final GLatLng startLatLng = new GLatLng(
                Optional.ofNullable(model.getObject().getStartLat()).orElse(-6.916667),
                Optional.ofNullable(model.getObject().getStartLon()).orElse(107.6));
        final GLatLng finishLatLng = new GLatLng(
                Optional.ofNullable(model.getObject().getFinishLat()).orElse(-6.916667),
                Optional.ofNullable(model.getObject().getFinishLon()).orElse(107.62)); // a bit east
        gmap.setCenter(startLatLng);

        // http://www.benjaminkeen.com/google-maps-coloured-markers/
        final GIcon markerA = new GIcon("/map_markers/blue_MarkerA.png");
        final GIcon markerB = new GIcon("/map_markers/blue_MarkerB.png");

        final GMarker startMarker = new GMarker(new GMarkerOptions(gmap, startLatLng, "Start", markerA, null).draggable(true));
        startMarker.addListener(GEvent.dragend, new GEventHandler() {
            @Override
            public void onEvent(AjaxRequestTarget target) {
                final GLatLng latLng = startMarker.getLatLng();
                model.getObject().setStartLat(latLng.getLat());
                model.getObject().setStartLon(latLng.getLng());
                target.add(startDiv);
            }
        });
        gmap.addOverlay(startMarker);
        final GMarker finishMarker = new GMarker(new GMarkerOptions(gmap, finishLatLng, "Finish", markerB, null).draggable(true));
        finishMarker.addListener(GEvent.dragend, new GEventHandler() {
            @Override
            public void onEvent(AjaxRequestTarget target) {
                final GLatLng latLng = finishMarker.getLatLng();
                model.getObject().setFinishLat(latLng.getLat());
                model.getObject().setFinishLon(latLng.getLng());
                target.add(finishDiv);
            }
        });
        gmap.addOverlay(finishMarker);
//        startMap.add(new DblClickListener() {
//            @Override
//            protected void onDblClick(AjaxRequestTarget target, GLatLng latLng) {
//                if (latLng != null) {
////					info("Clicked " + latLng);
//                    startMap.removeAllOverlays();
//                    model.getObject().setStartLat(latLng.getLat());
//                    model.getObject().setStartLon(latLng.getLng());
//                    startMap.addOverlay(new GMarker(new GMarkerOptions(startMap, latLng)));
//                    target.add(startDiv);
//                }
//            }
//        });
        form.add(gmap);

        form.add(new IndicatingAjaxButton("saveBtn") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onSubmit(target, form);
                Road road = model.getObject();
                if (road.getCreationTime() == null) {
                    road.setCreationTime(new DateTime());
                }
                road.setModificationTime(new DateTime());

                if (road.getStartLat() != null && road.getStartLon() != null &&
                        road.getFinishLat() != null && road.getFinishLon() != null) {
                    road.setCenterLat((road.getStartLat() + road.getFinishLat()) / 2d);
                    road.setCenterLon((road.getStartLon() + road.getFinishLon()) / 2d);
                    // TODO: incompatible class :( see https://github.com/opengeospatial/geoapi/issues/8
//                    final GeodeticCalculator calc = new GeodeticCalculator();
//                    calc.setStartingGeographicPoint(road.getStartLon(), road.getStartLat());
//                    calc.setDestinationGeographicPoint(road.getFinishLon(), road.getFinishLat());
//                    road.setLength(calc.getOrthodromicDistance());
                }
                if (road.getStartEle() != null && road.getFinishEle() != null) {
                    road.setCenterEle((road.getStartEle() + road.getFinishEle()) / 2d);
                }

                road = roadRepo.save(road);
                Interaction.MODIFIED.info("Road '%s' saved.", road.getName());
                setResponsePage(RoadShowPage.class,
                        new PageParameters().set(SeoBookmarkableMapper.LOCALE_PREF_ID_PARAMETER, localePrefId)
                            .set("roadId", road.getId()));
            }
        });
        add(form);
    }

    @Override
    public IModel<String> getTitleModel() {
        return new AbstractReadOnlyModel<String>() {
            @Override
            public String getObject() {
                if (model.getObject().getName() != null) {
                    return "Edit Road " + model.getObject().getName();
                } else {
                    return "Add Road";
                }
            }
        };
    }

    @Override
    public IModel<String> getMetaDescriptionModel() {
        return new PropertyModel<>(getDefaultModel(), "description");
    }

}
