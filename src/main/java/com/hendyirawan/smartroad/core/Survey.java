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
        @Index(columnList = "camera_id")
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
    private RoadDamageKind damageKind;
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

    public RoadDamageKind getDamageKind() {
        return damageKind;
    }

    public void setDamageKind(RoadDamageKind damageKind) {
        this.damageKind = damageKind;
    }

    /**
     * If exists, pothole area, in {@link javax.measure.unit.SI#MILLIMETER}.
     * @return
     */
    public Double getPotholeArea() {
        return potholeArea;
    }

    public void setPotholeArea(Double potholeArea) {
        this.potholeArea = potholeArea;
    }
}