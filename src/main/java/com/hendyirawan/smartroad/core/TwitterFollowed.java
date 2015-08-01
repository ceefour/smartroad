package com.hendyirawan.smartroad.core;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by ceefour on 01/08/2015.
 */
@Entity
@Table(schema = "smartroad", indexes = {
        @Index(columnList = "followerscreennamelower"),
        @Index(columnList = "followedscreennamelower"),
}, uniqueConstraints = @UniqueConstraint(name = "uk_followerscreennamelower_followedscreennamelower",
    columnNames = {"followerscreennamelower", "followedscreennamelower"}))
public class TwitterFollowed implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String followerScreenNameLower;
    @Column(nullable = false)
    private String followedScreenNameLower;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFollowerScreenNameLower() {
        return followerScreenNameLower;
    }

    public void setFollowerScreenNameLower(String followerScreenNameLower) {
        this.followerScreenNameLower = followerScreenNameLower;
    }

    public String getFollowedScreenNameLower() {
        return followedScreenNameLower;
    }

    public void setFollowedScreenNameLower(String followedScreenNameLower) {
        this.followedScreenNameLower = followedScreenNameLower;
    }
}
