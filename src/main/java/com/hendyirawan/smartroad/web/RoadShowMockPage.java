package com.hendyirawan.smartroad.web;

import com.hendyirawan.smartroad.core.Road;
import com.hendyirawan.smartroad.core.RoadRepository;
import com.opencsv.CSVWriter;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.wicketstuff.annotation.mount.MountPath;

import javax.inject.Inject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by ceefour on 28/12/14.
 */
@MountPath("/${localePrefId}/roads_mock/${roadId}")
public class RoadShowMockPage extends PubLayout {

    private static final Logger log = LoggerFactory.getLogger(RoadShowMockPage.class);

    @Inject
    private RoadRepository roadRepo;
    @Inject
    private Environment env;

    private IModel<Road> model;

    public RoadShowMockPage(PageParameters parameters) {
        super(parameters);
        final long roadId = parameters.get("roadId").toLong();
        final Road road = new Road();
        road.setName("Jl. Ir. H. Juanda");
        model = new Model<>(road);
        setDefaultModel(model);

        add(new DownloadLink("exportExcel", new AbstractReadOnlyModel<File>() {
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
                            csv.writeNext(new String[] {
                                    "roadId", "name", "lat", "lon", "ele", "damage",
                                    "damageDescription",
                                    "problems",
                                    "causes",
                                    "repair",
                                    "damageLevel",
                                    "damageLength", "damageWidth", "damageDepth", "damageVolume"});
                            csv.writeNext(new String[] {
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
