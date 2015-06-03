package com.hendyirawan.smartroad.core;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by ceefour on 28/04/2015.
 */
@Repository
public interface CameraRepository extends PagingAndSortingRepository<Camera, String> {
}
