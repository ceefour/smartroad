package com.hendyirawan.smartroad;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.opencv.core.Core;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.social.twitter.api.Tweet;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import javax.inject.Inject;

//@EnableAutoConfiguration
@SpringBootApplication(exclude = {
    LiquibaseAutoConfiguration.class//, // java.lang.IllegalStateException: Cannot find changelog location: class path resource [db/changelog/db.changelog-master.yaml] (please add changelog or check your Liquibase configuration)
    //HibernateJpaAutoConfiguration.class
    }
)
@Profile("twittercollector")
//@ComponentScan(excludeFilters=@ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=CommandLineRunner.class))
public class TwitterCollectorApplication implements CommandLineRunner {

    private static Logger log = LoggerFactory.getLogger(TwitterCollectorApplication.class);

    static {
        log.info("Loading OpenCV: {} from {}", Core.NATIVE_LIBRARY_NAME, System.getProperty("java.library.path"));
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    @Inject
    private Environment env;

    public static void main(String[] args) {
        new SpringApplicationBuilder(TwitterCollectorApplication.class)
                .profiles("twittercollector")
                .web(false)
                .run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        final ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(env.getRequiredProperty("spring.social.twitter.app-id"))
                .setOAuthConsumerSecret(env.getRequiredProperty("spring.social.twitter.app-secret"))
                .setOAuthAccessToken(env.getRequiredProperty("spring.social.twitter.access-token"))
                .setOAuthAccessTokenSecret(env.getRequiredProperty("spring.social.twitter.access-token-secret"));
        final TwitterFactory tf = new TwitterFactory(cb.build());
        final Twitter twitter = tf.getInstance();
        final QueryResult results = twitter.search(new Query("jalan rusak").count(100));
        for (Status tweet : results.getTweets()) {
            log.info("@{}: {} * {} * {} * {}", tweet.getUser().getScreenName(), tweet.getText(),
                    tweet.getPlace() != null ? tweet.getPlace().getFullName() : "N/A",
                    tweet.getGeoLocation() != null ? tweet.getGeoLocation().getLatitude() + "," + tweet.getGeoLocation().getLongitude() : "N/A",
                    new DateTime(tweet.getCreatedAt(), DateTimeZone.forOffsetMillis(tweet.getUser().getUtcOffset() * 1000)));
        }
    }
}
