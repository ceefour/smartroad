package com.hendyirawan.smartroad.core;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;
import java.io.Serializable;

/**
 * A survey photo and (auto-analyzed) data taken using a particular {@link Camera}.
 * Created by ceefour on 6/3/15.
 */
@Entity
@Table(schema = "smartroad", indexes = {
        @Index(columnList = "creationtime"),
        @Index(columnList = "modificationtime"),
        @Index(columnList = "surveytime"),
        @Index(columnList = "camera_id"),
        @Index(columnList = "damagekind"),
        @Index(columnList = "damagelevel")
})
public class Survey implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(columnDefinition = "timestamp with time zone", nullable = false)
    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime creationTime;
    @Column(columnDefinition = "timestamp with time zone", nullable = false)
    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime modificationTime;
    @Column(columnDefinition = "timestamp with time zone", nullable = false)
    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime surveyTime;
    @ManyToOne(optional = false)
    private Camera camera;
    private String photoType;
    private Integer photoWidth;
    private Integer photoHeight;
    @Basic(fetch = FetchType.LAZY)
    @Type(type="org.hibernate.type.BinaryType")
    private byte[] photo;

    @Enumerated(EnumType.STRING)
    private RoadDamageLevel damageLevel;
    @Enumerated(EnumType.STRING)
    private RoadDamageKind damageKind;
    private Integer potholeCount;
    private Double potholeWidth;
    private Double potholeLength;
    private Double potholeDepth;
    private Double potholeArea;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DateTime getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(DateTime creationTime) {
        this.creationTime = creationTime;
    }

    public DateTime getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(DateTime modificationTime) {
        this.modificationTime = modificationTime;
    }

    public DateTime getSurveyTime() {
        return surveyTime;
    }

    public void setSurveyTime(DateTime surveyTime) {
        this.surveyTime = surveyTime;
    }

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public String getPhotoType() {
        return photoType;
    }

    public void setPhotoType(String photoType) {
        this.photoType = photoType;
    }

    public Integer getPhotoWidth() {
        return photoWidth;
    }

    public void setPhotoWidth(Integer photoWidth) {
        this.photoWidth = photoWidth;
    }

    public Integer getPhotoHeight() {
        return photoHeight;
    }

    public void setPhotoHeight(Integer photoHeight) {
        this.photoHeight = photoHeight;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    public RoadDamageLevel getDamageLevel() {
        return damageLevel;
    }

    public void setDamageLevel(RoadDamageLevel damageLevel) {
        this.damageLevel = damageLevel;
    }

    public RoadDamageKind getDamageKind() {
        return damageKind;
    }

    public void setDamageKind(RoadDamageKind damageKind) {
        this.damageKind = damageKind;
    }

    public Integer getPotholeCount() {
        return potholeCount;
    }

    public void setPotholeCount(Integer potholeCount) {
        this.potholeCount = potholeCount;
    }

    /**
     * If exists, pothole width ACROSS the road, in {@link javax.measure.unit.SI#MILLIMETER}.
     * @return
     */
    public Double getPotholeWidth() {
        return potholeWidth;
    }

    public void setPotholeWidth(Double potholeWidth) {
        this.potholeWidth = potholeWidth;
    }

    /**
     * If exists, pothole length ALONG the road, in {@link javax.measure.unit.SI#MILLIMETER}.
     * @return
     */
    public Double getPotholeLength() {
        return potholeLength;
    }

    public void setPotholeLength(Double potholeLength) {
        this.potholeLength = potholeLength;
    }

    /**
     * If exists, pothole depth, in {@link javax.measure.unit.SI#MILLIMETER}.
     * @return
     */
    public Double getPotholeDepth() {
        return potholeDepth;
    }

    public void setPotholeDepth(Double potholeDepth) {
        this.potholeDepth = potholeDepth;
    }

    /**
     * If exists, pothole area, in square {@link javax.measure.unit.SI#MILLIMETER}.
     * @return
     */
    public Double getPotholeArea() {
        return potholeArea;
    }

    public void setPotholeArea(Double potholeArea) {
        this.potholeArea = potholeArea;
    }
}
