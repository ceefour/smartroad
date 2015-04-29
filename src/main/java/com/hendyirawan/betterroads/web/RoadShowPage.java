package com.hendyirawan.betterroads.web;

import com.hendyirawan.betterroads.core.Road;
import com.hendyirawan.betterroads.core.RoadRepository;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.wicketstuff.annotation.mount.MountPath;

import javax.inject.Inject;

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
        model = new Model<>(new Road());
        setDefaultModel(model);
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
