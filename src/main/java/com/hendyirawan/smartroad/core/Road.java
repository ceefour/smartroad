package com.hendyirawan.smartroad.core;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by ceefour on 29/04/2015.
 */
@Entity
@Table(schema = "smartroad", indexes = {
        @Index(columnList = "creationTime"),
        @Index(columnList = "modificationTime"),
        @Index(columnList = "name"),
        @Index(columnList = "pavement"),
        @Index(columnList = "width"),
        @Index(columnList = "length")
})
public class Road implements Serializable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Enumerated(EnumType.STRING)
    private RoadPavement pavement;
    @Column(columnDefinition = "text")
    private String description;
    private Double width;
    private Double length;
    private Double startLat;
    private Double startLon;
    private Double startEle;
    private Double finishLat;
    private Double finishLon;
    private Double finishEle;
    private Double centerLat;
    private Double centerLon;
    private Double centerEle;
    @Column(columnDefinition = "timestamp with time zone")
    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime creationTime;
    @Column(columnDefinition = "timestamp with time zone")
    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime modificationTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RoadPavement getPavement() {
        return pavement;
    }

    public void setPavement(RoadPavement pavement) {
        this.pavement = pavement;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getWidth() {
        return width;
    }

    public void setWidth(Double width) {
        this.width = width;
    }

    /**
     * Total length of road, in {@link javax.measure.unit.SI#METER}s.
     * This is usually calculated, i.e. by {@link com.hendyirawan.smartroad.web.RoadModifyPage}.
     * @return
     */
    public Double getLength() {
        return length;
    }

    public void setLength(Double length) {
        this.length = length;
    }

    public Double getStartLat() {
        return startLat;
    }

    public void setStartLat(Double startLat) {
        this.startLat = startLat;
    }

    /**
     * GPX format uses "lon" for shorthand.
     * @return
     */
    public Double getStartLon() {
        return startLon;
    }

    public void setStartLon(Double startLon) {
        this.startLon = startLon;
    }

    public Double getFinishLat() {
        return finishLat;
    }

    public void setFinishLat(Double finishLat) {
        this.finishLat = finishLat;
    }

    public Double getFinishLon() {
        return finishLon;
    }

    public void setFinishLon(Double finishLon) {
        this.finishLon = finishLon;
    }

    public Double getCenterLat() {
        return centerLat;
    }

    public void setCenterLat(Double centerLat) {
        this.centerLat = centerLat;
    }

    public Double getCenterLon() {
        return centerLon;
    }

    public void setCenterLon(Double centerLon) {
        this.centerLon = centerLon;
    }

    public Double getCenterEle() {
        return centerEle;
    }

    public void setCenterEle(Double centerEle) {
        this.centerEle = centerEle;
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

    /**
     * GPX format uses "ele" for shorthand.
     * @return
     */
    public Double getStartEle() {
        return startEle;
    }

    public void setStartEle(Double startEle) {
        this.startEle = startEle;
    }

    public Double getFinishEle() {
        return finishEle;
    }

    public void setFinishEle(Double finishEle) {
        this.finishEle = finishEle;
    }
}
