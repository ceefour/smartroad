package com.hendyirawan.smartroad;

import com.google.common.collect.Iterables;
import com.hendyirawan.smartroad.core.RoadTweet;
import com.hendyirawan.smartroad.core.RoadTweetRepository;
import com.hendyirawan.smartroad.core.SocialTopic;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.pdfbox.io.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.opencv.core.Core;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soluvas.commons.config.CommonsWebConfig;
import org.soluvas.json.JsonUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.actuate.autoconfigure.CrshAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import twitter4j.*;

import javax.inject.Inject;
import javax.measure.unit.SI;
import java.util.Locale;

//@EnableAutoConfiguration
@SpringBootApplication(exclude = {
    LiquibaseAutoConfiguration.class, // java.lang.IllegalStateException: Cannot find changelog location: class path resource [db/changelog/db.changelog-master.yaml] (please add changelog or check your Liquibase configuration)
    CrshAutoConfiguration.class
    //HibernateJpaAutoConfiguration.class
    }
)
@Profile("twittercollector")
@Import(CommonsWebConfig.class)
//@ComponentScan(excludeFilters=@ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=CommandLineRunner.class))
public class TwitterCollectorApp implements CommandLineRunner {

    private static Logger log = LoggerFactory.getLogger(TwitterCollectorApp.class);
    public static final double INDONESIA_CENTER_LAT = -2.7;
    public static final double INDONESIA_CENTER_LON = 117.0;

    static {
        log.info("Loading OpenCV: {} from {}", Core.NATIVE_LIBRARY_NAME, System.getProperty("java.library.path"));
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    @Inject
    private Environment env;
    @Inject
    private Twitter twitter;
    @Inject
    private RoadTweetRepository roadTweetRepo;
    @Inject
    private CloseableHttpClient httpClient;

    public static void main(String[] args) {
        new SpringApplicationBuilder(TwitterCollectorApp.class)
                .profiles("twitter", "twittercollector")
                .web(false)
                .run(args);
    }

    /**
     * Keywords:
     *
     * <ul>
     *     <li>jalan rusak</li>
     *     <li>jalan lubang</li>
     *     <li>jalan berlubang</li>
     *     <li>jalan lobang</li>
     *     <li>jalan berlobang</li>
     * </ul>
     *
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        LocalDate until = new LocalDate(DateTimeZone.forID("Asia/Jakarta")).minusDays(6);
        Long since = 0l; // oldest
        while (true) {
            final DateTime fetchTime = new DateTime();
//            log.info("Collecting tweets since ID {} ...", since);
            log.info("Collecting tweets since {} until {} ...", since, until);
            Query query = new Query("jalan rusak OR jalan lubang OR jalan berlubang").count(100)
                    //.geoCode(new GeoLocation(INDONESIA_CENTER_LAT, INDONESIA_CENTER_LON), 2000, SI.KILOMETRE.toString()) // Indonesia. TODO: use JSR36 in the future
                    .sinceId(since)
                    .until(until.toString())
                    .resultType(Query.ResultType.recent);
            final QueryResult results = twitter.search(query);
            log.info("Got {} tweets this time", results.getTweets().size());
            for (Status tweet : results.getTweets()) {
                if (since == null || tweet.getId() > since) {
                    since = tweet.getId();
                }

                final DateTimeZone timeZone = DateTimeZone.forOffsetMillis(tweet.getUser().getUtcOffset() * 1000);
                final DateTime creationTime = new DateTime(tweet.getCreatedAt(), timeZone);
                log.info("@{} {} {}: {} * {} * {} * {}", tweet.getUser().getScreenName(), creationTime, tweet.getId(), tweet.getText(),
                        tweet.getPlace() != null ? tweet.getPlace().getFullName() : "N/A",
                        tweet.getGeoLocation() != null ? tweet.getGeoLocation().getLatitude() + "," + tweet.getGeoLocation().getLongitude() : "N/A",
                        creationTime);

                final RoadTweet roadTweet = new RoadTweet();
                roadTweet.setId(tweet.getId());
                roadTweet.setTopic(SocialTopic.ROAD_DAMAGE);
                roadTweet.setTimeZone(timeZone);
                roadTweet.setCreationTime(creationTime);
                roadTweet.setFetchTime(fetchTime);
                roadTweet.setText(tweet.getText());
                roadTweet.setLang(Locale.forLanguageTag(tweet.getLang()));
                roadTweet.setRetweet(tweet.isRetweet());

                roadTweet.setUserId(tweet.getUser().getId());
                roadTweet.setUserScreenName(tweet.getUser().getScreenName());
                roadTweet.setUserName(tweet.getUser().getName());
                roadTweet.setUserLocation(tweet.getUser().getLocation());
                roadTweet.setUserLang(Locale.forLanguageTag(tweet.getUser().getLang()));
                if (tweet.getPlace() != null) {
                    roadTweet.setPlaceId(tweet.getPlace().getId());
                    roadTweet.setPlaceType(tweet.getPlace().getPlaceType());
                    roadTweet.setPlaceName(tweet.getPlace().getName());
                    roadTweet.setPlaceFullName(tweet.getPlace().getFullName());
                    roadTweet.setPlaceStreetAddress(tweet.getPlace().getStreetAddress());
                    roadTweet.setPlaceCountry(tweet.getPlace().getCountry());
                    roadTweet.setPlaceCountryCode(tweet.getPlace().getCountryCode());
                    roadTweet.setPlaceUri(tweet.getPlace().getURL());
                    roadTweet.setPlaceBoundingBoxType(tweet.getPlace().getBoundingBoxType());
                    roadTweet.setPlaceBoundingBoxSwLat(tweet.getPlace().getBoundingBoxCoordinates()[0][1].getLatitude());
                    roadTweet.setPlaceBoundingBoxSwLon(tweet.getPlace().getBoundingBoxCoordinates()[0][1].getLongitude());
                    roadTweet.setPlaceBoundingBoxNeLat(tweet.getPlace().getBoundingBoxCoordinates()[0][3].getLatitude());
                    roadTweet.setPlaceBoundingBoxNeLon(tweet.getPlace().getBoundingBoxCoordinates()[0][3].getLongitude());
                }
                if (tweet.getGeoLocation() != null) {
                    roadTweet.setLat(tweet.getGeoLocation().getLatitude());
                    roadTweet.setLon(tweet.getGeoLocation().getLongitude());
                }

                if (tweet.getMediaEntities() != null && tweet.getMediaEntities().length >= 1) {
                    final MediaEntity media = tweet.getMediaEntities()[0];
                    roadTweet.setMediaId(media.getId());
                    roadTweet.setMediaType(media.getType());
                    roadTweet.setMediaNormalUri(media.getURL());
                    roadTweet.setMediaDisplayUri(media.getDisplayURL());
                    roadTweet.setMediaExpandedUri(media.getExpandedURL());
                    roadTweet.setMediaUriHttp(media.getMediaURL());
                    roadTweet.setMediaUriHttps(media.getMediaURLHttps());
                    roadTweet.setMediaText(media.getText());
                    roadTweet.setMediaSizes(JsonUtils.asJson(media.getSizes()));

                    final MediaEntity.Size origMediaSize = Iterables.getLast(media.getSizes().entrySet()).getValue();
                    roadTweet.setMediaWidth(origMediaSize.getWidth());
                    roadTweet.setMediaHeight(origMediaSize.getHeight());
                    roadTweet.setMediaExtension(FilenameUtils.getExtension(media.getMediaURL()));

                    if ("photo".equals(roadTweet.getMediaType())) {
                        final HttpGet httpGet = new HttpGet(roadTweet.getMediaUriHttp());
                        try (final CloseableHttpResponse resp = httpClient.execute(httpGet)) {
                            roadTweet.setMediaContentType(resp.getEntity().getContentType().getValue());
                            roadTweet.setMediaContent(IOUtils.toByteArray(resp.getEntity().getContent()));
                            //roadTweet.setMediaContentLength((int) resp.getEntity().getContentLength()); // NOT RELIABLE, sometimes returns -1
                            roadTweet.setMediaContentLength(roadTweet.getMediaContent().length);
                        }
                    }
                }
                roadTweetRepo.save(roadTweet);
            }

            final LocalDate today = new LocalDate(DateTimeZone.forID("Asia/Jakarta"));
            if (until.isBefore(today.plusDays(1))) {
                until = until.plusDays(1);
            }

            log.info("Waiting before next collect (since ID {}, until {}), press Ctrl+C to exit...", since, until);
            Thread.sleep(20000);
        }
    }
}
