package com.hendyirawan.smartroad.core;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * Created by ceefour on 28/04/2015.
 */
@Repository
public interface RoadTweetRepository extends PagingAndSortingRepository<RoadTweet, Long> {

    @Query("SELECT DISTINCT(LOWER(rt.userScreenName)) FROM RoadTweet rt")
    Set<String> findAllDistinctScreenNames();

    @Query("SELECT DISTINCT(LOWER(rt.userScreenName)) FROM RoadTweet rt WHERE LOWER(rt.userScreenName) NOT IN (:exclusionsLower)")
    Set<String> findAllDistinctScreenNamesExcluding(@Param("exclusionsLower") Set<String> exclusionsLower);

    @Query("SELECT rt FROM RoadTweet rt WHERE rt.lat IS NOT NULL OR rt.placeBoundingBoxSwLat IS NOT NULL")
    Page<RoadTweet> findAllWithLocation(Pageable pageable);

}
