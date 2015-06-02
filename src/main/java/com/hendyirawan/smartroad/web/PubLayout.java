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

/**
 * Created by ceefour on 27/12/14.
 */
public abstract class PubLayout extends WebPage {

    public abstract IModel<String> getTitleModel();

    public abstract IModel<String> getMetaDescriptionModel();

    protected String localePrefId;

    public PubLayout(PageParameters parameters) {
        super(parameters);
        localePrefId = parameters.get(SeoBookmarkableMapper.LOCALE_PREF_ID_PARAMETER).toString();
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        add(new GrowlBehavior());
        add(new Label("title", getTitleModel()));
        add(new MetaTag("metaDescription", new Model<>("description"), getMetaDescriptionModel()));
        add(new BookmarkablePageLink<>("detectorLink", DetectorPage.class,
                new PageParameters().set(SeoBookmarkableMapper.LOCALE_PREF_ID_PARAMETER, SeoBookmarkableMapper.DEFAULT_LOCALE_PREF_ID)));
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(FontAwesome.asHeaderItem());
    }

}
