package com.hendyirawan.smartroad.core;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by ceefour on 6/4/15.
 */
public interface SurveyRepository extends PagingAndSortingRepository<Survey, Long> {

    Page<Survey> findAllByCameraId(String cameraId, Pageable pageable);
    long countByCameraId(String cameraId);

}
