package com.hendyirawan.smartroad.core;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by ceefour on 28/04/2015.
 */
@Repository
public interface CameraRepository extends PagingAndSortingRepository<Camera, String> {

    public Page<Camera> findAllByRoadId(long roadId, Pageable pageable);
}
