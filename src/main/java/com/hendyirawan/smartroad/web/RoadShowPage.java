package com.hendyirawan.smartroad.web;

import com.hendyirawan.smartroad.core.Road;
import com.hendyirawan.smartroad.core.RoadRepository;
import com.opencsv.CSVWriter;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soluvas.web.site.Interaction;
import org.soluvas.web.site.SeoBookmarkableMapper;
import org.soluvas.web.site.widget.MeasureLabel;
import org.springframework.core.env.Environment;
import org.wicketstuff.annotation.mount.MountPath;
import org.wicketstuff.gmap.GMap;
import org.wicketstuff.gmap.api.GLatLng;
import org.wicketstuff.gmap.api.GPolyline;

import javax.inject.Inject;
import javax.measure.unit.SI;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by ceefour on 28/12/14.
 */
@MountPath("/${localePrefId}/roads/${roadId}")
public class RoadShowPage extends PubLayout {

    private static final Logger log = LoggerFactory.getLogger(RoadShowPage.class);

    @Inject
    private RoadRepository roadRepo;
    @Inject
    private Environment env;

    private IModel<Road> model;

    public RoadShowPage(PageParameters parameters) {
        super(parameters);
        final long roadId = parameters.get("roadId").toLong();
        final Road road = roadRepo.findOne(roadId);
        model = new Model<>(road);
        setDefaultModel(model);

        final Form<Road> form = new Form<>("form", model);
        form.add(new Label("heading", new PropertyModel<>(model, "name")));
        form.add(new BookmarkablePageLink<>("editLink", RoadModifyPage.class,
                new PageParameters().set(SeoBookmarkableMapper.LOCALE_PREF_ID_PARAMETER, localePrefId)
                        .set("roadId", roadId)));
        form.add(new DownloadLink("exportExcel", new AbstractReadOnlyModel<File>() {
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

/*
  val localePrefId = params.get("localePrefId").toString
  val pageable = new PageRequest(params.get("page").toInt(0), params.get("size").toInt(20))
//  val placeSlugs = mapAsJavaMap(places.asScala
//    .map { it: Place => it.getId() -> SlugUtils.generateSegment(it.getName()) }.toMap)
//  log.debug("placeSlugs={}", placeSlugs)
  val placeDp = new SortableDataProvider[Place, String] {
    override def iterator(first: Long, count: Long): util.Iterator[_ <: Place] = placeRepo.findAll(pageable).iterator()

    override def model(obj: Place): IModel[Place] = new Model[Place](obj)

    override def size(): Long = placeRepo.count()
  }
  val placesDv = new StatelessDataView[Place]("places", placeDp, pageable.getPageSize) {
    override def populateItem(item: Item[Place]): Unit = {
      val placeSlug = SlugUtils.generateSegment(item.getModelObject.getName)
      item.add(new ExternalLink("link", s"/$localePrefId/$placeSlug", item.getModelObject.getName))
    }
  }
  placesDv.setCurrentPage(pageable.getPageNumber)
  add(placesDv)
  add(new StatelessBootstrapPagingNavigator("navigator", placesDv, null))
  private val googleBrowserApiKey = env.getRequiredProperty("googleBrowserApiKey")
  val mapUri = UriComponentsBuilder.fromUriString("https://www.google.com/maps/embed/v1/place")
    .queryParam("q", "Bandung, Jawa Barat, Indonesia")
    .queryParam("key", googleBrowserApiKey).build()
  add(new WebMarkupContainer("map")
    .add(new AttributeModifier("src", mapUri)))

  // Bandung, West Java, Indonesia: Travel Guide to Paris van Java/City of Flowers | Gigastic
  override def getTitleModel: IModel[String] = new Model("Bandung, Jawa Barat, Indonesia: Informasi Tempat Wisata ke Paris van Java/Kota Kembang | Gigastic")
  // Travel guide to Bandung (Paris van Java / City of Flowers), West Java, Indonesia, featuring up-to-date information on attractions, hotels, restaurants, nightlife, travel tips and more. Free and reliable advice written by Gigastic travelers from around the globe.
  override def getMetaDescriptionModel: IModel[String] = new Model("Informasi daftar tempat wisata di Bandung, Jawa Barat Indonesia (Paris van Java/Kota Kembang). Pilihan wajib dikunjungi di musim liburan. Mulai dari wisata alam, wisata kuliner, wisata budaya, dan wisata unik di Bandung.")
*/

}