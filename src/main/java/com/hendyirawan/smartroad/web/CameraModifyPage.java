package com.hendyirawan.smartroad.web;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.hendyirawan.smartroad.core.Camera;
import com.hendyirawan.smartroad.core.CameraRepository;
import com.hendyirawan.smartroad.core.Road;
import com.hendyirawan.smartroad.core.RoadRepository;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.jpeg.JpegParser;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.*;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.validator.PatternValidator;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soluvas.web.site.Interaction;
import org.soluvas.web.site.OnChangeThrottledBehavior;
import org.soluvas.web.site.SeoBookmarkableMapper;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.wicketstuff.annotation.mount.MountPath;
import org.wicketstuff.gmap.GMap;
import org.wicketstuff.gmap.api.*;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ceefour on 28/12/14.
 */
@MountPath("/${localePrefId}/cameras/modify")
public class CameraModifyPage extends PubLayout {

    private static final Logger log = LoggerFactory.getLogger(CameraModifyPage.class);

    @Inject
    private RoadRepository roadRepo;
    @Inject
    private CameraRepository cameraRepo;
    @Inject
    private Environment env;

    private IModel<Camera> model;

    public static final Pattern GPS_PATTERN = Pattern.compile("(\\d+)\\/(\\d+), (\\d+)\\/(\\d+), (\\d+)\\/(\\d+)");
    public static final Pattern GPS_PATTERN2 = Pattern.compile("([-]?[0-9.]+)° ([0-9.]+)' ([0-9.]+)\"");

    public static double gpsStrToDecimal(String str, String ref) {
//                        exif:GPSLatitude: 6/1, 54/1, 5361/100 == 6deg 54' 53.610"
//                        exif:GPSLatitudeRef: S
//                        exif:GPSLongitude: 107/1, 35/1, 5377/100
//                        exif:GPSLongitudeRef: E
        Preconditions.checkNotNull(str, "GPS string must be provided");
        Preconditions.checkNotNull(ref, "GPS ref must be provided");
        final Matcher matcher = GPS_PATTERN.matcher(str);
        Preconditions.checkArgument(matcher.matches(), "Invalid GPS string '%s'", str);
        final double degrees = Double.parseDouble(matcher.group(1)) / Double.parseDouble(matcher.group(2));
        final double minutes = Double.parseDouble(matcher.group(3)) / Double.parseDouble(matcher.group(4));
        final double seconds = Double.parseDouble(matcher.group(5)) / Double.parseDouble(matcher.group(6));
        final double flip = "W".equalsIgnoreCase(ref) || "S".equalsIgnoreCase(ref) ? -1 : 1;
        final double num = flip * (degrees + (minutes / 60d) + (seconds / 3600d));
        return num;
    }

    public static double gpsStrToDecimal2(String str) {
//                        exif:GPSLatitude: 6/1, 54/1, 5361/100 == 6deg 54' 53.610"
//                        exif:GPSLatitudeRef: S
//                        exif:GPSLongitude: 107/1, 35/1, 5377/100
//                        exif:GPSLongitudeRef: E
        Preconditions.checkNotNull(str, "GPS string must be provided");
        final Matcher matcher = GPS_PATTERN2.matcher(str);
        Preconditions.checkArgument(matcher.matches(), "Invalid GPS string '%s'", str);
        final double degrees = Double.parseDouble(matcher.group(1));
        final double minutes = Double.parseDouble(matcher.group(2));
        final double seconds = Double.parseDouble(matcher.group(3));
        final double num;
        if (degrees > 0) {
            num = degrees + (minutes / 60d) + (seconds / 3600d);
        } else {
            num = degrees - (minutes / 60d) - (seconds / 3600d);
        }
        return num;
    }

    public CameraModifyPage(PageParameters parameters) {
        super(parameters);
        if (!parameters.get("cameraId").isEmpty()) {
            final String cameraId = parameters.get("cameraId").toString();
            final Camera camera = cameraRepo.findOne(cameraId);
            model = new Model<>(camera);
        } else {
            final Camera camera = new Camera();
            camera.setCreationTime(new DateTime());
            model = new Model<>(camera);
        }
        setDefaultModel(model);
        add(new Label("heading", getTitleModel()));
        final Form<Camera> form = new Form<>("form", model);
        form.setOutputMarkupId(true);

        final LoadableDetachableModel<List<Road>> roadsModel = new LoadableDetachableModel<List<Road>>() {
            @Override
            protected List<Road> load() {
                return roadRepo.findAll(new PageRequest(0, 1000, Sort.Direction.ASC, "name")).getContent();
            }
        };
        final DropDownChoice<Road> roadSelect = new DropDownChoice<>("roadSelect", new PropertyModel<>(model, "road"),
                roadsModel, new ChoiceRenderer<Road>() {
            @Override
            public Object getDisplayValue(Road object) {
                return object.getName();
            }

            @Override
            public String getIdValue(Road object, int index) {
                return object.getId().toString();
            }
        });
        roadSelect.setRequired(true);
        form.add(roadSelect);

        final TextField<String> idFld = new TextField<>("idFld", new PropertyModel<>(model, "id"));
        idFld.add(new PatternValidator(Pattern.compile("[a-z][a-z0-9]*")));
        idFld.setRequired(true);
        idFld.setEnabled(model.getObject().getId() == null);
        form.add(idFld);
        form.add(new TextField<>("nameFld", new PropertyModel<>(model, "name")).setRequired(true));
        form.add(new TextField<>("descriptionFld", new PropertyModel<>(model, "description")));

        final WebMarkupContainer locDiv = new WebMarkupContainer("locDiv");
        locDiv.setOutputMarkupId(true);
        locDiv.add(new NumberTextField<>("latFld", new PropertyModel<>(model, "lat"), Double.class)
                .setMinimum(-90d).setMaximum(90d).setStep(0.000000000000001)); // not ideal, but this is W3C spec!
        locDiv.add(new NumberTextField<>("lonFld", new PropertyModel<>(model, "lon"), Double.class)
                .setMinimum(-180d).setMaximum(180d).setStep(0.000000000000001));
        locDiv.add(new NumberTextField<>("eleFld", new PropertyModel<>(model, "ele"), Double.class)
                .setMinimum(-1000d).setMaximum(10000d).setStep(1d));
        form.add(locDiv);

        final ListModel<FileUpload> calibrationModel = new ListModel<>(new ArrayList<>());

        final WebMarkupContainer projectionDiv = new WebMarkupContainer("projectionDiv");
        projectionDiv.setOutputMarkupId(true);
        final Image calibrationImg = new Image("calibrationImg", new CalibrationImageResource(model));
        calibrationImg.setOutputMarkupId(true);
        projectionDiv.add(calibrationImg);
        final NumberTextField<Double> vanishUFld = new NumberTextField<>("vanishUFld", new PropertyModel<>(model, "vanishU"), Double.class)
                .setMinimum(0d).setMaximum(1d).setStep(0.02);
        vanishUFld.add(new OnChangeThrottledBehavior() {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(calibrationImg);
            }
        });
        projectionDiv.add(vanishUFld);
        final NumberTextField<Double> vanishVFld = new NumberTextField<>("vanishVFld", new PropertyModel<>(model, "vanishV"), Double.class)
                .setMinimum(0d).setMaximum(1d).setStep(0.02);
        vanishVFld.add(new OnChangeThrottledBehavior() {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(calibrationImg);
            }
        });
        projectionDiv.add(vanishVFld);
        final NumberTextField<Double> leftUFld = new NumberTextField<>("leftUFld", new PropertyModel<>(model, "leftU"), Double.class)
                .setMinimum(-3d).setMaximum(4d).setStep(0.02);
        leftUFld.add(new OnChangeThrottledBehavior() {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(calibrationImg);
            }
        });
        projectionDiv.add(leftUFld);
        final NumberTextField<Double> rightUFld = new NumberTextField<>("rightUFld", new PropertyModel<>(model, "rightU"), Double.class)
                .setMinimum(-3d).setMaximum(4d).setStep(0.02);
        rightUFld.add(new OnChangeThrottledBehavior() {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(calibrationImg);
            }
        });
        projectionDiv.add(rightUFld);
        form.add(projectionDiv);

        final GMap gmap = new GMap("map");
        gmap.setOutputMarkupId(true);
        gmap.setPanControlEnabled(true);
        gmap.setScaleControlEnabled(true);
        gmap.setScrollWheelZoomEnabled(true);
        gmap.setDoubleClickZoomEnabled(false);
//        Geolocation geo = Optional.ofNullable(placeModel.getObject().getGeo()).or(new Geolocation());
//        // Toronto: 43.7000° N, 79.4000
        // Bandung: geo:-6.916667,107.6
        final GLatLng latlng = new GLatLng(
                Optional.ofNullable(model.getObject().getLat()).orElse(-6.916667),
                Optional.ofNullable(model.getObject().getLon()).orElse(107.6));
        gmap.setCenter(latlng);

        // http://www.benjaminkeen.com/google-maps-coloured-markers/
//        final GIcon markerA = new GIcon("/map_markers/blue_MarkerA.png");
//        final GIcon markerB = new GIcon("/map_markers/blue_MarkerB.png");

        final GMarker marker = new GMarker(new GMarkerOptions(gmap, latlng, "Camera").draggable(true));
        marker.addListener(GEvent.dragend, new GEventHandler() {
            @Override
            public void onEvent(AjaxRequestTarget target) {
                final GLatLng latLng = marker.getLatLng();
                model.getObject().setLat(latLng.getLat());
                model.getObject().setLon(latLng.getLng());
                target.add(locDiv);
            }
        });
        gmap.addOverlay(marker);
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

        final FileUploadField calibrationFld = new FileUploadField("calibrationFile", calibrationModel);
        form.add(calibrationFld);
        form.add(new IndicatingAjaxButton("calibrateBtn") {
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                 super.onSubmit(target, form);
                 @Nullable
                 final FileUpload file = Iterables.getFirst(calibrationModel.getObject(), null);
                 if (file != null) {
                     final Metadata metadata = new Metadata();
                     final ContentHandler handler = new DefaultHandler();
                     final Parser parser = new JpegParser();
                     final ParseContext context = new ParseContext();
                     //final String mimeType = tika.detect(fileName);
                     try {
                         parser.parse(file.getInputStream(), handler, metadata, context);
//                        exif:GPSLatitude: 6/1, 54/1, 5361/100 == 6deg 54' 53.610"
//                        exif:GPSLatitudeRef: S
//                        exif:GPSLongitude: 107/1, 35/1, 5377/100
//                        exif:GPSLongitudeRef: E
//                         final double lat = gpsStrToDecimal(metadata.get("exif:GPSLatitude"), metadata.get("exif:GPSLatitudeRef"));
//                         final double lon = gpsStrToDecimal(metadata.get("exif:GPSLongitude"), metadata.get("exif:GPSLongitudeRef"));
                         final double lat = gpsStrToDecimal2(metadata.get("GPS Latitude"));
                         final double lon = gpsStrToDecimal2(metadata.get("GPS Longitude"));
                         model.getObject().setLat(lat);
                         model.getObject().setLon(lon);
                         final GLatLng latlng = new GLatLng(lat, lon);
                         gmap.setCenter(latlng);
                         marker.getMarkerOptions().setLatLng(latlng);
                         target.add(locDiv, gmap);

                         model.getObject().setCalibrationImageType(file.getContentType());
                         model.getObject().setCalibrationImageWidth(metadata.getInt(Metadata.IMAGE_WIDTH));
                         model.getObject().setCalibrationImageHeight(metadata.getInt(Metadata.IMAGE_LENGTH));
                         model.getObject().setCalibrationImage(file.getBytes());
                         target.add(calibrationImg);
                     } catch (Exception e) {
                         Throwables.propagate(e);
                     }
                 }
             }
         });

        form.add(new IndicatingAjaxButton("saveBtn") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onSubmit(target, form);
                Camera camera = model.getObject();
                if (camera.getCreationTime() == null) {
                    camera.setCreationTime(new DateTime());
                }
                camera.setModificationTime(new DateTime());

                camera = cameraRepo.save(camera);
                Interaction.MODIFIED.info("Camera '%s' saved.", camera.getName());
                setResponsePage(CameraShowPage.class,
                        new PageParameters().set(SeoBookmarkableMapper.LOCALE_PREF_ID_PARAMETER, localePrefId)
                                .set("cameraId", camera.getId()));
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
                    return "Edit Camera " + model.getObject().getName();
                } else {
                    return "Add Camera";
                }
            }
        };
    }

    @Override
    public IModel<String> getMetaDescriptionModel() {
        return new PropertyModel<>(getDefaultModel(), "description");
    }

}
