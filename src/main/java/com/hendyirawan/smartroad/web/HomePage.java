package com.hendyirawan.smartroad.web;

import org.apache.wicket.model.*;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Created by ceefour on 27/12/14.
 */
public class HomePage extends PubLayout {

  public HomePage(PageParameters parameters) {
    super(parameters);
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
