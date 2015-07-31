package com.hendyirawan.smartroad.web;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.hendyirawan.smartroad.core.Camera;
import com.hendyirawan.smartroad.core.CameraRepository;
import com.hendyirawan.smartroad.core.Road;
import com.hendyirawan.smartroad.core.RoadRepository;
import com.opencsv.CSVWriter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.util.string.JavaScriptUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.*;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soluvas.web.site.Interaction;
import org.soluvas.web.site.SeoBookmarkableMapper;
import org.soluvas.web.site.widget.MeasureLabel;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.wicketstuff.annotation.mount.MountPath;
import org.wicketstuff.gmap.GMap;
import org.wicketstuff.gmap.api.GLatLng;
import org.wicketstuff.gmap.api.GPolyline;

import javax.inject.Inject;
import javax.measure.unit.SI;
import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.Objects;

/**
 * Created by ceefour on 28/12/14.
 */
@MountPath("/${localePrefId}/roads/${roadId}")
public class RoadShowPage extends PubLayout {

    private static final Logger log = LoggerFactory.getLogger(RoadShowPage.class);

    @Inject
    private RoadRepository roadRepo;
    @Inject
    private CameraRepository cameraRepo;
    @Inject
    private Environment env;

    private IModel<Road> model;

    public RoadShowPage(PageParameters parameters) {
        super(parameters);
        final long roadId = parameters.get("roadId").toLong();
        final Road road = Preconditions.checkNotNull(roadRepo.findOne(roadId),
                "Cannot find road '%s'", roadId);
        model = new Model<>(road);
        setDefaultModel(model);

        final Form<Road> form = new Form<>("form", model);
        form.add(new Label("heading", new PropertyModel<>(model, "name")));
        form.add(new Label("pavement", new PropertyModel<>(model, "pavement")));
        form.add(new BookmarkablePageLink<>("editLink", RoadModifyPage.class,
                new PageParameters().set(SeoBookmarkableMapper.LOCALE_PREF_ID_PARAMETER, localePrefId)
                        .set("roadId", roadId)));

        form.add(new DownloadLink("exportExcel", new AbstractReadOnlyModel<File>() {
            @Override
            public File getObject() {
                try {
                    final File outFile = File.createTempFile("road", ".xlsx");
                    final URL inFile = RoadShowPage.class.getResource("/com/hendyirawan/smartroad/road-survey-template.xlsx");
                    log.info("Reading from '{}'", inFile);
                    try (final InputStream stream = inFile.openStream()) {
                        //final POIFSFileSystem fs = new POIFSFileSystem(stream);
                        try (final XSSFWorkbook workbook = new XSSFWorkbook(stream)) {
                            final XSSFSheet surveySheet = workbook.getSheetAt(0);
//                            final XSSFCell titleCell = surveySheet.getRow(0).getCell(0);
//                            log.info("Title is: {}", titleCell.getStringCellValue());
                            final Road road = model.getObject();
                            surveySheet.getRow(2).getCell(9).setCellValue(road.getName());
                            surveySheet.getRow(5).getCell(9).setCellValue(Objects.toString(road.getPavement()));
                            final DateTimeFormatter formatter = DateTimeFormat.forStyle("MS");
                            surveySheet.getRow(6).getCell(15).setCellValue(new DateTime(DateTimeZone.forID("Asia/Jakarta")).toString(formatter));

//                            final List<Camera> cameras = ImmutableList.copyOf(cameraRepo.findAll(new Sort("id")));
                            final List<Camera> cameras = cameraRepo.findAllByRoadId(roadId, new PageRequest(0, 1000, Sort.Direction.ASC, "id")).getContent();
                            if (!cameras.isEmpty()) {
                                surveySheet.getRow(5).getCell(3).setCellValue(cameras.get(0).getDescription());
                                surveySheet.getRow(6).getCell(3).setCellValue(cameras.get(cameras.size() - 1).getDescription());
                            }
                            int sumPotholeCount = 0;
                            double sumPotholeArea = 0d;

                            for (int cameraIdx = 0; cameraIdx < 9; cameraIdx++) {
                                final int row = 11 + (cameraIdx * 5);
                                for (int cell = 0; cell <= 19; cell++) {
                                    surveySheet.getRow(row).getCell(cell).setCellValue("");
                                    surveySheet.getRow(row + 1).getCell(cell).setCellValue("");
                                    surveySheet.getRow(row + 2).getCell(cell).setCellValue("");
                                    surveySheet.getRow(row + 3).getCell(cell).setCellValue("");
                                    surveySheet.getRow(row + 4).getCell(cell).setCellValue("");
                                }
                            }

                            for (int cameraIdx = 0; cameraIdx < cameras.size(); cameraIdx++) {
                                final int row = 11 + (cameraIdx * 5);
                                final Camera camera = cameras.get(cameraIdx);
                                if (camera.getPotholeCount() != null) {
                                    sumPotholeCount += camera.getPotholeCount();
                                }
                                if (camera.getPotholeArea() != null) {
                                    sumPotholeArea += camera.getPotholeArea();
                                }
                                surveySheet.getRow(row).getCell(0).setCellValue(camera.getDescription());
                                for (int cell = 1; cell <= 19; cell++) {
                                    switch (cell) {
                                        case 2:
                                            if (camera.getPotholeCount() != null) {
                                                surveySheet.getRow(row).getCell(cell).setCellValue(camera.getPotholeCount());
                                            }
                                            break;
                                        case 3:
                                            if (camera.getPotholeArea() != null) {
                                                surveySheet.getRow(row).getCell(cell).setCellValue(camera.getPotholeArea());
                                            }
                                            break;
                                        case 6:
                                            if (camera.getDamageKind() != null) {
                                                surveySheet.getRow(row).getCell(cell).setCellValue(camera.getDamageKind().toString());
                                            }
                                            break;
                                        case 7:
                                            if (camera.getPotholeLength() != null) {
                                                surveySheet.getRow(row).getCell(cell).setCellValue(camera.getPotholeLength());
                                            }
                                            break;
                                        case 8:
                                            if (camera.getPotholeWidth() != null) {
                                                surveySheet.getRow(row).getCell(cell).setCellValue(camera.getPotholeWidth());
                                            }
                                            break;
                                        case 17:
                                            if (camera.getPotholeDepth() != null) {
                                                surveySheet.getRow(row).getCell(cell).setCellValue(camera.getPotholeDepth());
                                            }
                                            break;
                                        default:
                                    }
                                }
                            }
                            surveySheet.getRow(69).getCell(1).setCellValue(sumPotholeCount);
                            surveySheet.getRow(70).getCell(1).setCellValue(sumPotholeArea);

//                            final File outFile = new File(System.getProperty("user.home"), "tmp/road-survey-test.xlsx");
                            log.info("Writing to '{}'", outFile);
                            workbook.write(new FileOutputStream(outFile));
                            return outFile;
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Error exporting Excel: " + e, e);
                }
            }
        }));

        form.add(new DownloadLink("exportCsv", new AbstractReadOnlyModel<File>() {
            @Override
            public File getObject() {
                try {
                    final File file = File.createTempFile("road", ".csv");
                    try (final FileWriter fileWriter = new FileWriter(file)) {
                        try (final CSVWriter csv = new CSVWriter(fileWriter)) {
                            /*
<strong style="color: red">Kerusakan: POTHOLES (BERLOBANG)</strong>

<p>Penurunan berbentuk cekungan dari permukaan perkerasan sampai seluruh lapisan hotmix sampai ke base coursenya,umumnya mempunyai sisi yg tajam dan vertikal dekat sisi dari lobang, lobang biasa terjadi pada jalan yg
mempunyai hotmix yg tipis 25 sampai 50 mm dan jarang terjadi pada jalan hot mix yg tebal 100 mm.</p>
<p><strong>Masalah yg timbul:</strong> roughness, infiltrasi air pada perkerasan</p>
<p><strong>Penyebab yg mungkin:</strong> umumnya, lobang merupakan hasil dari retak buaya, lalu berlanjut akibat lalu lintas terlepasnya bagian retak menjadi lobang.</p>

<p><strong>Perbaikan:</strong> dengan penambalan.</p>

                             */
                            csv.writeNext(new String[]{
                                    "roadId", "name", "lat", "lon", "ele", "damage",
                                    "damageDescription",
                                    "problems",
                                    "causes",
                                    "repair",
                                    "damageLevel",
                                    "damageLength", "damageWidth", "damageDepth", "damageVolume"});
                            csv.writeNext(new String[]{
                                    "1", "Jl. Ir. H. Juanda", "-6.3453469", "169.234873", "450",
                                    "POTHOLES (BERLOBANG)",
                                    "Penurunan berbentuk cekungan dari permukaan perkerasan sampai seluruh lapisan hotmix sampai ke base coursenya,umumnya mempunyai sisi yg tajam dan vertikal dekat sisi dari lobang, lobang biasa terjadi pada jalan yg\n" +
                                            "mempunyai hotmix yg tipis 25 sampai 50 mm dan jarang terjadi pada jalan hot mix yg tebal 100 mm.",
                                    "roughness, infiltrasi air pada perkerasan",
                                    "umumnya, lobang merupakan hasil dari retak buaya, lalu berlanjut akibat lalu lintas terlepasnya bagian retak menjadi lobang.",
                                    "dengan penambalan",
                                    "MEDIUM",
                                    "7.89 cm", "5.22 cm", "3.4 cm", "25.3 cm^3"
                            });
                        }
                    }
                    return file;
                } catch (IOException e) {
                    throw new RuntimeException("Error", e);
                }
            }
        }));
        form.add(new AjaxButton("trashBtn") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onSubmit(target, form);
                final Road road = model.getObject();
                roadRepo.delete(roadId);
                Interaction.DELETED.info("Road '%s' deleted.", road.getName());
                setResponsePage(RoadListPage.class,
                        new PageParameters().set(SeoBookmarkableMapper.LOCALE_PREF_ID_PARAMETER,
                                getPageParameters().get(SeoBookmarkableMapper.LOCALE_PREF_ID_PARAMETER).toString()));
            }

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                super.updateAjaxAttributes(attributes);
                attributes.getAjaxCallListeners().add(new AjaxCallListener() {
                    @Override
                    public CharSequence getPrecondition(Component component) {
                        final Road road = model.getObject();
                        return "return confirm(\"Delete road '" + JavaScriptUtils.escapeQuotes(road.getName()) + "?\");";
                    }
                });
            }
        });

        final Double startLat = new PropertyModel<Double>(model, "startLat").getObject();
        final Double startLon = new PropertyModel<Double>(model, "startLon").getObject();
        final Double finishLat = new PropertyModel<Double>(model, "finishLat").getObject();
        final Double finishLon = new PropertyModel<Double>(model, "finishLon").getObject();
        if (startLat != null && startLon != null && finishLat != null && finishLon != null) {
            final GMap gmap = new GMap("map");
            gmap.setPanControlEnabled(false);
            gmap.setScaleControlEnabled(false);
            gmap.setScrollWheelZoomEnabled(false);
            gmap.setZoomControlEnabled(false);
            gmap.setMapTypeControlEnabled(false);
            gmap.setDraggingEnabled(false);
            gmap.setDoubleClickZoomEnabled(false);
            final GLatLng startLatLng = new GLatLng(startLat, startLon);
            final GLatLng finishLatLng = new GLatLng(finishLat, finishLon);
            gmap.setCenter(startLatLng);
            gmap.setZoom(15);

            gmap.addOverlay(new GPolyline("red", 6, 0.8f, startLatLng, finishLatLng));
            form.add(gmap);

            form.add(new MeasureLabel("widthLabel", new Model<>(SI.METER), new PropertyModel<>(model, "width")));
            form.add(new MeasureLabel("startEleLabel", new Model<>(SI.METER), new PropertyModel<>(model, "startEle")));
            form.add(new ExternalLink("geoUri", String.format("geo:%f,%f", startLat, startLon)));
            form.add(new ExternalLink("gmapsLink", String.format("http://maps.google.com/?q=%f,%f", startLat, startLon)));
        } else {
            form.add(new EmptyPanel("widthLabel").setVisible(false));
            form.add(new EmptyPanel("startEleLabel").setVisible(false));
            form.add(new EmptyPanel("map").setVisible(false));
            form.add(new EmptyPanel("geoUri").setVisible(false));
            form.add(new EmptyPanel("gmapsLink").setVisible(false));
        }
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
