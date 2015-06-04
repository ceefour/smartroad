package com.hendyirawan.smartroad.web;

import com.google.common.collect.ImmutableList;
import com.hendyirawan.smartroad.core.Road;
import com.hendyirawan.smartroad.core.RoadRepository;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soluvas.web.site.SeoBookmarkableMapper;
import org.soluvas.web.site.widget.LineMapColumn;
import org.soluvas.web.site.widget.LinkColumn;
import org.soluvas.web.site.widget.MeasureColumn;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.wicketstuff.annotation.mount.MountPath;

import javax.inject.Inject;
import javax.measure.unit.SI;
import java.util.Iterator;

/**
 * Created by ceefour on 28/12/14.
 */
@MountPath("/${localePrefId}/roads")
public class RoadListPage extends PubLayout {

    private static final Logger log = LoggerFactory.getLogger(RoadListPage.class);

    @Inject
    private RoadRepository roadRepo;
    @Inject
    private Environment env;

    private IModel<Road> model;

    public RoadListPage(PageParameters parameters) {
        super(parameters);
        add(new BookmarkablePageLink<>("addLink", RoadModifyPage.class,
                new PageParameters().set(SeoBookmarkableMapper.LOCALE_PREF_ID_PARAMETER, parameters.get(SeoBookmarkableMapper.LOCALE_PREF_ID_PARAMETER).toString())));
        final RoadDataProvider roadDp = new RoadDataProvider();
        final AjaxFallbackDefaultDataTable<Road, String> table = new AjaxFallbackDefaultDataTable<>("table", ImmutableList.of(
                new PropertyColumn<>(new Model<>("ID"), "id", "id"),
                new LinkColumn<>(new Model<>("Name"), "name", "name",
                        RoadShowPage.class,
                        new PageParameters().set(SeoBookmarkableMapper.LOCALE_PREF_ID_PARAMETER, localePrefId),
                        "roadId", "id"),
                new PropertyColumn<>(new Model<>("Pavement"), "pavement", "pavement"),
                new PropertyColumn<>(new Model<>("Description"), "description"),
//                new MeasureColumn<>(new Model<>("Length"), SI.METER, "length"), // // TODO: incompatible class :( see https://github.com/opengeospatial/geoapi/issues/8
                new MeasureColumn<>(new Model<>("Width"), SI.METER, "width"),
                new LineMapColumn<>(new Model<>("Map"), "startLat", "startLon", "finishLat", "finishLon")
            ), roadDp, roadDp.itemsPerPage);
        add(table);
        //add(new BootstrapPagingNavigator("navigator", roadDp));
    }

    @Override
    public IModel<String> getTitleModel() {
        return new Model<>("Roads");
    }

    @Override
    public IModel<String> getMetaDescriptionModel() {
        return new Model<>("Roads");
    }

    private class RoadDataProvider extends SortableDataProvider<Road, String> {
        protected int itemsPerPage = 10;

        @Override
        public Iterator<? extends Road> iterator(long first, long count) {
            final Sort sort;
            if (getSort() != null) {
                sort = new Sort(getSort().isAscending() ? Sort.Direction.ASC : Sort.Direction.DESC,
                        getSort().getProperty());
            } else {
                sort = new Sort("name");
            }
            final Page<Road> roads = roadRepo.findAll(
                    new PageRequest((int) (first / itemsPerPage), itemsPerPage, sort));
            return roads.iterator();
        }

        @Override
        public long size() {
            return roadRepo.count();
        }

        @Override
        public IModel<Road> model(Road object) {
            return new Model<>(object);
        }
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
