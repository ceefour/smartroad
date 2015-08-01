package com.hendyirawan.smartroad.core;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.ws.rs.QueryParam;
import java.util.List;
import java.util.Set;

/**
 * Created by ceefour on 28/04/2015.
 */
@Repository
public interface TwitterFollowedRepository extends PagingAndSortingRepository<TwitterFollowed, Long> {

    @Query("SELECT followedScreenNameLower FROM TwitterFollowed WHERE followerScreenNameLower = :followerScreenNameLower ORDER BY followedScreenNameLower")
    Set<String> findAllFolloweds(@Param("followerScreenNameLower") String followerScreenNameLower);

    int countByFollowerScreenNameLower(String followerScreenNameLower);
}
