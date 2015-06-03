package com.hendyirawan.smartroad.core;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by ceefour on 6/3/15.
 */
@Entity
@Table(schema = "smartroad", indexes = {
        @Index(columnList = "creationTime"),
        @Index(columnList = "modificationTime"),
        @Index(columnList = "name")
})
public class Camera implements Serializable {

    @Id
    private String id;
    private DateTime creationTime;
    private DateTime modificationTime;
    private String name;
    @Column(columnDefinition = "text")
    private String description;
    private Double lat;
    private Double lon;
    private Double ele;
    private Double vanishU;
    private Double vanishV;
    private Double leftU;
    private Double rightU;
    @ManyToOne
    private Road road;
    private String calibrationImageType;
    private Integer calibrationImageWidth;
    private Integer calibrationImageHeight;
    @Basic(fetch = FetchType.LAZY)
    @Type(type="org.hibernate.type.BinaryType")
    private byte[] calibrationImage;

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public Double getEle() {
        return ele;
    }

    public void setEle(Double ele) {
        this.ele = ele;
    }

    /**
     * Position (where 0.0 is left and 1.0 is right) of vanishing point of road
     * (it's usually inside image) at the horizon.
     * @return
     */
    public Double getVanishU() {
        return vanishU;
    }

    public void setVanishU(Double vanishU) {
        this.vanishU = vanishU;
    }

    public Double getVanishV() {
        return vanishV;
    }

    public void setVanishV(Double vanishV) {
        this.vanishV = vanishV;
    }

    /**
     * Position (where 0.0 is left and 1.0 is right) of left edge of road (may be off-image) at the bottom edge of image.
     * @return
     */
    public Double getLeftU() {
        return leftU;
    }

    public void setLeftU(Double leftU) {
        this.leftU = leftU;
    }

    /**
     * Position (where 0.0 is left and 1.0 is right) of right edge of road (may be off-image) at the bottom edge of image.
     * @return
     */
    public Double getRightU() {
        return rightU;
    }

    public void setRightU(Double rightU) {
        this.rightU = rightU;
    }

    public Road getRoad() {
        return road;
    }

    public void setRoad(Road road) {
        this.road = road;
    }

    public String getCalibrationImageType() {
        return calibrationImageType;
    }

    public void setCalibrationImageType(String calibrationImageType) {
        this.calibrationImageType = calibrationImageType;
    }

    public Integer getCalibrationImageWidth() {
        return calibrationImageWidth;
    }

    public void setCalibrationImageWidth(Integer calibrationImageWidth) {
        this.calibrationImageWidth = calibrationImageWidth;
    }

    public Integer getCalibrationImageHeight() {
        return calibrationImageHeight;
    }

    public void setCalibrationImageHeight(Integer calibrationImageHeight) {
        this.calibrationImageHeight = calibrationImageHeight;
    }

    public byte[] getCalibrationImage() {
        return calibrationImage;
    }

    public void setCalibrationImage(byte[] calibrationImage) {
        this.calibrationImage = calibrationImage;
    }
}
