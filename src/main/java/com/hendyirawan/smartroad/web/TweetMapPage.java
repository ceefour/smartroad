package com.hendyirawan.smartroad.web;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableList;
import com.hendyirawan.smartroad.TwitterCollectorApp;
import com.hendyirawan.smartroad.core.RoadTweet;
import com.hendyirawan.smartroad.core.RoadTweetRepository;
import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.BootstrapPagingNavigator;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.MathUtils;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soluvas.web.site.widget.DateColumn;
import org.soluvas.web.site.widget.MarkerMapColumn;
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

import javax.inject.Inject;
import javax.measure.unit.SI;
import java.util.Iterator;

/**
 */
@MountPath("/${localePrefId}/tweets/map")
public class TweetMapPage extends PubLayout {

    private static final Logger log = LoggerFactory.getLogger(TweetMapPage.class);

    @Inject
    private RoadTweetRepository roadTweetRepo;
    @Inject
    private Environment env;

    public TweetMapPage(PageParameters parameters) {
        super(parameters);
        final RoadTweetDataProvider roadTweetDp = new RoadTweetDataProvider();
//        final AjaxFallbackDefaultDataTable<RoadTweet, String> table = new AjaxFallbackDefaultDataTable<>("table", ImmutableList.of(
//                new PropertyColumn<>(new Model<>("ID"), "id", "id"),
//                new PropertyColumn<>(new Model<>("User"), "userScreenName", "userScreenName"),
//                new PropertyColumn<>(new Model<>("Text"), "text"),
//                new MarkerMapColumn<>(new Model<>("Map"), "lat", "lon"),
//                new PropertyColumn<>(new Model<>("Place"), "placeFullName"),
//                new DateColumn<>(new Model<>("Created"), "creationTime")
//            ), roadTweetDp, roadTweetDp.itemsPerPage);
//        add(table);
//        add(new BootstrapPagingNavigator("navigator", table));

        final Form<Void> form = new Form<>("form");

        final GMap gmap = new GMap("map");
//        gmap.setPanControlEnabled(false);
//        gmap.setScaleControlEnabled(false);
//        gmap.setScrollWheelZoomEnabled(false);
//        gmap.setZoomControlEnabled(false);
//        gmap.setMapTypeControlEnabled(false);
//        gmap.setDraggingEnabled(false);
//        gmap.setDoubleClickZoomEnabled(false);
        final GLatLng centerLatLng = new GLatLng(-2.7, 117.0);
        gmap.setCenter(centerLatLng);
        gmap.setZoom(5);
        form.add(gmap);

        final ImmutableList<RoadTweet> roadTweets = ImmutableList.copyOf(roadTweetDp.iterator(0, 100));
        for (final RoadTweet roadTweet : roadTweets) {
            final Double tweetLat;
            final Double tweetLon;
            if (roadTweet.getLat() != null) {
                tweetLat = roadTweet.getLat();
                tweetLon = roadTweet.getLon();
            } else if (roadTweet.getPlaceBoundingBoxSwLat() !=  null) {
                tweetLat = StatUtils.mean(new double[] { roadTweet.getPlaceBoundingBoxSwLat(), roadTweet.getPlaceBoundingBoxNeLat() });
                tweetLon = StatUtils.mean(new double[] { roadTweet.getPlaceBoundingBoxSwLon(), roadTweet.getPlaceBoundingBoxNeLon() });
            } else {
                continue;
            }

            final GLatLng tweetLatLng = new GLatLng(tweetLat, tweetLon);
            final String markerTitle = "@" + roadTweet.getUserScreenName() + ": " +
                    CharMatcher.WHITESPACE.replaceFrom(roadTweet.getText(), " ");
            gmap.addOverlay(new GMarker(new GMarkerOptions(gmap, tweetLatLng,
                    markerTitle)));

//            form.add(new MeasureLabel("widthLabel", new Model<>(SI.METER), new PropertyModel<>(model, "width")));
//            form.add(new MeasureLabel("startEleLabel", new Model<>(SI.METER), new PropertyModel<>(model, "startEle")));
//            form.add(new ExternalLink("geoUri", String.format("geo:%f,%f", startLat, startLon)));
//            form.add(new ExternalLink("gmapsLink", String.format("http://maps.google.com/?q=%f,%f", startLat, startLon)));
        }
        add(form);
    }

    @Override
    public IModel<String> getTitleModel() {
        return new Model<>("Tweets");
    }

    @Override
    public IModel<String> getMetaDescriptionModel() {
        return new Model<>("Tweets");
    }

    private class RoadTweetDataProvider extends SortableDataProvider<RoadTweet, String> {
        protected int itemsPerPage = 10;

        @Override
        public Iterator<? extends RoadTweet> iterator(long first, long count) {
            final Sort sort;
            if (getSort() != null) {
                sort = new Sort(getSort().isAscending() ? Sort.Direction.ASC : Sort.Direction.DESC,
                        getSort().getProperty());
            } else {
                sort = new Sort("creationTime");
            }
            final Page<RoadTweet> roadTweets = roadTweetRepo.findAll(
                    new PageRequest((int) (first / itemsPerPage), itemsPerPage, sort));
            return roadTweets.iterator();
        }

        @Override
        public long size() {
            return roadTweetRepo.count();
        }

        @Override
        public IModel<RoadTweet> model(RoadTweet object) {
            return new Model<>(object);
        }
    }

}
