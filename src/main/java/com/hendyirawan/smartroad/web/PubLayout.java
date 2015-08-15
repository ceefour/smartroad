package com.hendyirawan.smartroad.web;

import de.agilecoders.wicket.core.markup.html.bootstrap.html.MetaTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.soluvas.web.bootstrap.FontAwesome;
import org.soluvas.web.bootstrap.GrowlBehavior;
import org.soluvas.web.site.SeoBookmarkableMapper;
import org.springframework.core.env.Environment;

import javax.inject.Inject;

/**
 * Created by ceefour on 27/12/14.
 */
public abstract class PubLayout extends WebPage {

    public abstract IModel<String> getTitleModel();

    public abstract IModel<String> getMetaDescriptionModel();

    protected String localePrefId;

    @Inject
    protected Environment env;

    public PubLayout(PageParameters parameters) {
        super(parameters);
        localePrefId = parameters.get(SeoBookmarkableMapper.LOCALE_PREF_ID_PARAMETER)
                .toString(SeoBookmarkableMapper.DEFAULT_LOCALE_PREF_ID);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        add(new GrowlBehavior());
        add(new Label("title", getTitleModel()));
        add(new MetaTag("metaDescription", new Model<>("description"), getMetaDescriptionModel()));
        add(new BookmarkablePageLink<>("detectorLink", DetectorPage.class,
                new PageParameters().set(SeoBookmarkableMapper.LOCALE_PREF_ID_PARAMETER, localePrefId)));
        add(new BookmarkablePageLink<>("roadsLink", RoadListPage.class,
                new PageParameters().set(SeoBookmarkableMapper.LOCALE_PREF_ID_PARAMETER, localePrefId)));
        add(new BookmarkablePageLink<>("camerasLink", CameraListPage.class,
                new PageParameters().set(SeoBookmarkableMapper.LOCALE_PREF_ID_PARAMETER, localePrefId)));

        add(new BookmarkablePageLink<>("tweetListLink", TweetListPage.class,
                new PageParameters().set(SeoBookmarkableMapper.LOCALE_PREF_ID_PARAMETER, localePrefId)));
        add(new BookmarkablePageLink<>("tweetMapLink", TweetMapPage.class,
                new PageParameters().set(SeoBookmarkableMapper.LOCALE_PREF_ID_PARAMETER, localePrefId)));
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(FontAwesome.asHeaderItem());
    }

}
