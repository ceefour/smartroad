package com.hendyirawan.smartroad.core;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import twitter4j.GeoLocation;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Locale;

/**
 * We make this a flat structure to make it portable.
 * Created by ceefour on 01/08/2015.
 */
@Entity
@Table(schema = "smartroad", indexes = {
        @Index(columnList = "fetchTime"),
        @Index(columnList = "creationTime"),
        @Index(columnList = "topic"),
        @Index(columnList = "userId"),
        @Index(columnList = "userScreenName"),
})
public class RoadTweet implements Serializable {

    @Id
    private Long id;
    @Column(columnDefinition = "timestamp with time zone")
    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime fetchTime;
    @Column(columnDefinition = "timestamp with time zone")
    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime creationTime;
    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTimeZoneAsString")
    private DateTimeZone timeZone;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SocialTopic topic;
    private String userScreenName;
    private Long userId;
    private String userName;
    private String userLocation;
    @Column(columnDefinition = "varchar(255)")
    @Type(type="org.soluvas.jpa.PersistentLocale")
    private Locale userLang;
    private String text;
    @Column(columnDefinition = "varchar(255)")
    @Type(type="org.soluvas.jpa.PersistentLocale")
    private Locale lang;
    @Column(nullable = false)
    private boolean retweet;
    private String placeId;
    private String placeName;
    private String placeFullName;
    private String placeType;
    private String placeUri;
    private String placeCountry;
    private String placeCountryCode;
    private String placeStreetAddress;
    private Double placeBoundingBoxSwLat;
    private Double placeBoundingBoxSwLon;
    private Double placeBoundingBoxNeLat;
    private Double placeBoundingBoxNeLon;
    private String placeBoundingBoxType;
    private Double lat;
    private Double lon;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DateTime getFetchTime() {
        return fetchTime;
    }

    public void setFetchTime(DateTime fetchTime) {
        this.fetchTime = fetchTime;
    }

    public DateTime getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(DateTime creationTime) {
        this.creationTime = creationTime;
    }

    public DateTimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(DateTimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public SocialTopic getTopic() {
        return topic;
    }

    public void setTopic(SocialTopic topic) {
        this.topic = topic;
    }

    public String getUserScreenName() {
        return userScreenName;
    }

    public void setUserScreenName(String userScreenName) {
        this.userScreenName = userScreenName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(String userLocation) {
        this.userLocation = userLocation;
    }

    public Locale getUserLang() {
        return userLang;
    }

    public void setUserLang(Locale userLang) {
        this.userLang = userLang;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Locale getLang() {
        return lang;
    }

    public void setLang(Locale lang) {
        this.lang = lang;
    }

    public boolean isRetweet() {
        return retweet;
    }

    public void setRetweet(boolean retweet) {
        this.retweet = retweet;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getPlaceFullName() {
        return placeFullName;
    }

    public void setPlaceFullName(String placeFullName) {
        this.placeFullName = placeFullName;
    }

    public String getPlaceType() {
        return placeType;
    }

    public void setPlaceType(String placeType) {
        this.placeType = placeType;
    }

    public String getPlaceUri() {
        return placeUri;
    }

    public void setPlaceUri(String placeUri) {
        this.placeUri = placeUri;
    }

    public String getPlaceCountry() {
        return placeCountry;
    }

    public void setPlaceCountry(String placeCountry) {
        this.placeCountry = placeCountry;
    }

    public String getPlaceCountryCode() {
        return placeCountryCode;
    }

    public void setPlaceCountryCode(String placeCountryCode) {
        this.placeCountryCode = placeCountryCode;
    }

    public String getPlaceStreetAddress() {
        return placeStreetAddress;
    }

    public void setPlaceStreetAddress(String placeStreetAddress) {
        this.placeStreetAddress = placeStreetAddress;
    }

    public Double getPlaceBoundingBoxSwLat() {
        return placeBoundingBoxSwLat;
    }

    public void setPlaceBoundingBoxSwLat(Double placeBoundingBoxSwLat) {
        this.placeBoundingBoxSwLat = placeBoundingBoxSwLat;
    }

    public Double getPlaceBoundingBoxSwLon() {
        return placeBoundingBoxSwLon;
    }

    public void setPlaceBoundingBoxSwLon(Double placeBoundingBoxSwLon) {
        this.placeBoundingBoxSwLon = placeBoundingBoxSwLon;
    }

    public Double getPlaceBoundingBoxNeLat() {
        return placeBoundingBoxNeLat;
    }

    public void setPlaceBoundingBoxNeLat(Double placeBoundingBoxNeLat) {
        this.placeBoundingBoxNeLat = placeBoundingBoxNeLat;
    }

    public Double getPlaceBoundingBoxNeLon() {
        return placeBoundingBoxNeLon;
    }

    public void setPlaceBoundingBoxNeLon(Double placeBoundingBoxNeLon) {
        this.placeBoundingBoxNeLon = placeBoundingBoxNeLon;
    }

    public String getPlaceBoundingBoxType() {
        return placeBoundingBoxType;
    }

    public void setPlaceBoundingBoxType(String placeBoundingBoxType) {
        this.placeBoundingBoxType = placeBoundingBoxType;
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
}
