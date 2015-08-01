package com.hendyirawan.smartroad;

import com.hendyirawan.smartroad.core.RoadTweet;
import com.hendyirawan.smartroad.core.RoadTweetRepository;
import com.hendyirawan.smartroad.core.SocialTopic;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.opencv.core.Core;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.actuate.autoconfigure.CrshAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.Transactional;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import javax.inject.Inject;
import java.util.Locale;

//@EnableAutoConfiguration
@SpringBootApplication(exclude = {
    LiquibaseAutoConfiguration.class, // java.lang.IllegalStateException: Cannot find changelog location: class path resource [db/changelog/db.changelog-master.yaml] (please add changelog or check your Liquibase configuration)
    CrshAutoConfiguration.class
    //HibernateJpaAutoConfiguration.class
    }
)
@Profile("twittercollector")
//@ComponentScan(excludeFilters=@ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=CommandLineRunner.class))
public class TwitterCollectorApp implements CommandLineRunner {

    private static Logger log = LoggerFactory.getLogger(TwitterCollectorApp.class);

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
        final DateTime fetchTime = new DateTime();
        final QueryResult results = twitter.search(
                new Query("jalan rusak").count(100)
                        .geoCode(new GeoLocation(-2.67, 119.56), 1500, "km")); // Indonesia. TODO: use JSR36 in the future
        for (Status tweet : results.getTweets()) {
            final DateTimeZone timeZone = DateTimeZone.forOffsetMillis(tweet.getUser().getUtcOffset() * 1000);
            final DateTime creationTime = new DateTime(tweet.getCreatedAt(), timeZone);
            log.info("@{}: {} * {} * {} * {}", tweet.getUser().getScreenName(), tweet.getText(),
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
            roadTweetRepo.save(roadTweet);
        }
    }
}
