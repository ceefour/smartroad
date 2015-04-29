package com.hendyirawan.betterroads.core;

import org.joda.time.DateTime;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by ceefour on 29/04/2015.
 */
@Entity
@Table(schema = "smartroad")
public class Road implements Serializable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(columnDefinition = "text")
    private String description;
    private Double width;
    private Double startLat;
    private Double startLon;
    private Double startEle;
    private Double finishLat;
    private Double finishLon;
    private Double finishEle;
    private DateTime creationTime;
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
