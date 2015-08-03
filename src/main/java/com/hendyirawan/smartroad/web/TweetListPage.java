package com.hendyirawan.smartroad.web;

import com.google.common.collect.ImmutableList;
import com.hendyirawan.smartroad.core.RoadTweet;
import com.hendyirawan.smartroad.core.RoadTweetRepository;
import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.BootstrapPagingNavigator;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soluvas.web.site.widget.DateColumn;
import org.soluvas.web.site.widget.UriImageColumn;
import org.soluvas.web.site.widget.MarkerMapColumn;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.wicketstuff.annotation.mount.MountPath;

import javax.inject.Inject;
import java.util.Iterator;

/**
 */
@MountPath("/${localePrefId}/tweets")
public class TweetListPage extends PubLayout {

    private static final Logger log = LoggerFactory.getLogger(TweetListPage.class);

    @Inject
    private RoadTweetRepository roadTweetRepo;
    @Inject
    private Environment env;

    public TweetListPage(PageParameters parameters) {
        super(parameters);
        final RoadTweetDataProvider roadTweetDp = new RoadTweetDataProvider();
        final AjaxFallbackDefaultDataTable<RoadTweet, String> table = new AjaxFallbackDefaultDataTable<>("table", ImmutableList.of(
                new PropertyColumn<>(new Model<>("ID"), "id", "id"),
                new PropertyColumn<>(new Model<>("User"), "userScreenName", "userScreenName"),
                new PropertyColumn<>(new Model<>("Text"), "text"),
                new MarkerMapColumn<>(new Model<>("Map"), "lat", "lon"),
                new PropertyColumn<>(new Model<>("Place"), "placeFullName"),
                new DateColumn<>(new Model<>("Created"), "creationTime"),
                new UriImageColumn<>(new Model<>("Media"), "imagePathForApp", 120)
            ), roadTweetDp, roadTweetDp.itemsPerPage);
        add(table);
        add(new BootstrapPagingNavigator("navigator", table));
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
