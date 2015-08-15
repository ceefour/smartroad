package com.hendyirawan.smartroad.web;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.springframework.core.env.Environment;

import javax.inject.Inject;

/**
 * Created by ceefour on 27/12/14.
 */
public class HomePage extends PubLayout {

    public HomePage(PageParameters parameters) {
        super(parameters);
        final Boolean twitterWidgetsEnabled = env.getProperty("spring.social.twitter.widgets", Boolean.class, false);
        add(new WebMarkupContainer("twitterEmbed").setVisible(twitterWidgetsEnabled));
    }

    @Override
    public IModel<String> getTitleModel() {
        return new Model<>("Better Roads: Memudahkan, hemat waktu dan biaya untuk survey jalan.");
    }

    @Override
    public IModel<String> getMetaDescriptionModel() {
        return new Model<>("Memudahkan, hemat waktu dan biaya untuk survey jalan.");
    }
}
