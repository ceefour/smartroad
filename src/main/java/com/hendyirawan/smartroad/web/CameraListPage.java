package com.hendyirawan.smartroad.web;

import com.google.common.collect.ImmutableList;
import com.hendyirawan.smartroad.core.Camera;
import com.hendyirawan.smartroad.core.CameraRepository;
import com.hendyirawan.smartroad.web.CameraModifyPage;
import com.hendyirawan.smartroad.web.CameraShowPage;
import com.hendyirawan.smartroad.web.PubLayout;
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
import org.soluvas.web.site.widget.DateColumn;
import org.soluvas.web.site.widget.LinkColumn;
import org.soluvas.web.site.widget.MarkerMapColumn;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.wicketstuff.annotation.mount.MountPath;

import javax.inject.Inject;
import java.util.Iterator;

/**
 * Created by ceefour on 28/12/14.
 */
@MountPath("/${localePrefId}/cameras")
public class CameraListPage extends PubLayout {

    private static final Logger log = LoggerFactory.getLogger(CameraListPage.class);

    @Inject
    private CameraRepository cameraRepo;
    @Inject
    private Environment env;

    private IModel<Camera> model;

    public CameraListPage(PageParameters parameters) {
        super(parameters);
        add(new BookmarkablePageLink<>("addLink", CameraModifyPage.class,
                new PageParameters().set(SeoBookmarkableMapper.LOCALE_PREF_ID_PARAMETER, localePrefId)));
        final CameraDataProvider cameraDp = new CameraDataProvider();
        final AjaxFallbackDefaultDataTable<Camera, String> table = new AjaxFallbackDefaultDataTable<>("table", ImmutableList.of(
                new PropertyColumn<>(new Model<>("ID"), "id", "id"),
                new LinkColumn<>(new Model<>("Name"), "name", "name",
                        CameraShowPage.class,
                        new PageParameters().set(SeoBookmarkableMapper.LOCALE_PREF_ID_PARAMETER, localePrefId),
                        "cameraId", "id"),
                new PropertyColumn<>(new Model<>("Description"), "description"),
                new LinkColumn<>(new Model<>("Road"), "road.name", RoadShowPage.class,
                        new PageParameters().set(SeoBookmarkableMapper.LOCALE_PREF_ID_PARAMETER, localePrefId),
                        "roadId", "road.id"),
//                new MeasureColumn<>(new Model<>("Length"), SI.METER, "length"), // // TODO: incompatible class :( see https://github.com/opengeospatial/geoapi/issues/8
//                new MeasureColumn<>(new Model<>("Width"), SI.METER, "width"),
                new MarkerMapColumn<>(new Model<>("Map"), "lat", "lon"),
                new DateColumn<>(new Model<>("Surveyed"), "surveyTime"),
                new PropertyColumn<>(new Model<>("Damage"), "damageKind", "damageKind"),
                new PropertyColumn<>(new Model<>("Level"), "damageLevel", "damageLevel")
            ), cameraDp, cameraDp.itemsPerPage);
        add(table);
        //add(new BootstrapPagingNavigator("navigator", cameraDp));
    }

    @Override
    public IModel<String> getTitleModel() {
        return new Model<>("Cameras");
    }

    @Override
    public IModel<String> getMetaDescriptionModel() {
        return new Model<>("Cameras");
    }

    private class CameraDataProvider extends SortableDataProvider<Camera, String> {
        protected int itemsPerPage = 10;

        @Override
        public Iterator<? extends Camera> iterator(long first, long count) {
            final Sort sort;
            if (getSort() != null) {
                sort = new Sort(getSort().isAscending() ? Sort.Direction.ASC : Sort.Direction.DESC,
                        getSort().getProperty());
            } else {
                sort = new Sort("name");
            }
            final Page<Camera> cameras = cameraRepo.findAll(
                    new PageRequest((int) (first / itemsPerPage), itemsPerPage, sort));
            return cameras.iterator();
        }

        @Override
        public long size() {
            return cameraRepo.count();
        }

        @Override
        public IModel<Camera> model(Camera object) {
            return new Model<>(object);
        }
    }

}
