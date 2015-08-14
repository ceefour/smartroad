package com.hendyirawan.smartroad.web;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableList;
import com.hendyirawan.smartroad.TwitterCollectorApp;
import com.hendyirawan.smartroad.core.RoadTweet;
import com.hendyirawan.smartroad.core.RoadTweetRepository;
import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.BootstrapPagingNavigator;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.MathUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.json.JSONObject;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.UrlResourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soluvas.jpa.jpa.Geolocation;
import org.soluvas.web.site.widget.DateColumn;
import org.soluvas.web.site.widget.DateTimeLabel2;
import org.soluvas.web.site.widget.MarkerMapColumn;
import org.soluvas.web.site.widget.MeasureLabel;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.wicketstuff.annotation.mount.MountPath;
import org.wicketstuff.gmap.GMap;
import org.wicketstuff.gmap.api.*;
import org.wicketstuff.gmap.event.ClickListener;

import javax.inject.Inject;
import javax.measure.unit.SI;
import java.util.Iterator;
import java.util.List;

/**
 */
@MountPath("/${localePrefId}/tweets/map")
public class TweetMapPage extends PubLayout {

    private static final Logger log = LoggerFactory.getLogger(TweetMapPage.class);

    public static final GIcon ICON_BROKEN = new GIcon("/road_sign/broken.png", new GSize(32, 32), new GPoint(15, 15));

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

        final Model<RoadTweet> selectedTweetModel = new Model<>();

        final WebMarkupContainer sideDiv = new WebMarkupContainer("sideDiv");
        sideDiv.setOutputMarkupId(true);
        final WebMarkupContainer tweetDiv = new WebMarkupContainer("tweetDiv", selectedTweetModel) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(getDefaultModelObject() != null);
            }
        };
        tweetDiv.add(new Label("userScreenName", new PropertyModel<>(selectedTweetModel, "userScreenName")));
        tweetDiv.add(new Label("text", new PropertyModel<>(selectedTweetModel, "text")));
        final Image tweetImg = new Image("img", (ResourceReference) null) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                if (selectedTweetModel.getObject() != null && selectedTweetModel.getObject().getImagePathForApp() != null) {
                    setImageResourceReference(new UrlResourceReference(Url.parse(selectedTweetModel.getObject().getImagePathForApp())));
                    setVisible(true);
                } else {
                    setVisible(false);
                }
            }
        };
        tweetDiv.add(tweetImg);
        tweetDiv.add(new Label("placeFullName", new PropertyModel<>(selectedTweetModel, "placeFullName")));
        tweetDiv.add(new DateTimeLabel2("creationTime", new PropertyModel<>(selectedTweetModel, "creationTime")));
        sideDiv.add(tweetDiv);
        form.add(sideDiv);

        final List<RoadTweet> roadTweets = roadTweetRepo.findAllWithLocation(
                new PageRequest(0, 100, Sort.Direction.DESC, "fetchTime")).getContent();
        log.info("Got {} road tweets from repo", roadTweets.size());
        for (final RoadTweet roadTweet : roadTweets) {
            if (roadTweet.isRetweet()) {
                continue;
            }

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
            String markerTitleEscaped = JSONObject.quote(
                    "@" + roadTweet.getUserScreenName() + ": " + roadTweet.getText());
            markerTitleEscaped = markerTitleEscaped.substring(1, markerTitleEscaped.length() - 1);

            final GMarker marker;
            if (roadTweet.getMediaContentType() != null) {
                log.info("has icon: {}", roadTweet.getPlaceFullName());
                final GIcon mediaIcon = new GIcon(roadTweet.getImagePathForApp(),
                        null,//new GSize(roadTweet.getMediaWidth(), roadTweet.getMediaHeight()),
                        null, null, new GSize(48, 48));
                marker = new GMarker(new GMarkerOptions(gmap, tweetLatLng,
                        markerTitleEscaped, mediaIcon));
                gmap.addOverlay(marker);
            } else {
                marker = new GMarker(new GMarkerOptions(gmap, tweetLatLng,
                        markerTitleEscaped, ICON_BROKEN));
                gmap.addOverlay(marker);
            }
            marker.addListener(GEvent.click, new GEventHandler() {
                @Override
                public void onEvent(AjaxRequestTarget target) {
                    log.info("Clicked {} @{}: {}", roadTweet.getId(), roadTweet.getUserScreenName(), roadTweet.getText());
                    selectedTweetModel.setObject(roadTweet);
                    target.add(sideDiv);
//                    if (latLng != null) {
////					info("Clicked " + latLng);
//                        gmap.removeAllOverlays();
//                        final Place place = placeModel.getObject();
//                        if (place.getGeo() == null) {
//                            place.setGeo(new Geolocation());
//                        }
//                        place.getGeo().setLatitude(latLng.getLat());
//                        place.getGeo().setLongitude(latLng.getLng());
//                        gmap.addOverlay(new GMarker(new GMarkerOptions(gmap, latLng)));
//                        target.add(latLngDiv);
//                    }
                }
            });

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
