package com.hendyirawan.smartroad;

import com.google.common.collect.ImmutableSet;
import com.hendyirawan.smartroad.core.RoadTweetRepository;
import com.hendyirawan.smartroad.core.TwitterFollowed;
import com.hendyirawan.smartroad.core.TwitterFollowedRepository;
import com.hendyirawan.smartroad.twitter.TwitterConfig;
import org.hibernate.exception.ConstraintViolationException;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import twitter4j.Twitter;
import twitter4j.User;

import javax.inject.Inject;
import java.util.Set;

//@EnableAutoConfiguration
@SpringBootApplication(exclude = {
    LiquibaseAutoConfiguration.class, // java.lang.IllegalStateException: Cannot find changelog location: class path resource [db/changelog/db.changelog-master.yaml] (please add changelog or check your Liquibase configuration)
    CrshAutoConfiguration.class
    //HibernateJpaAutoConfiguration.class
    }
)
@Profile("twitterautofollow")
//@ComponentScan(excludeFilters=@ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=CommandLineRunner.class))
public class TwitterAutoFollowApp implements CommandLineRunner {

    private static Logger log = LoggerFactory.getLogger(TwitterAutoFollowApp.class);

    static {
        log.info("Loading OpenCV: {} from {}", Core.NATIVE_LIBRARY_NAME, System.getProperty("java.library.path"));
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    @Inject
    private Environment env;
    @Inject
    private TwitterFollowedRepository twitterFollowedRepo;
    @Inject
    private RoadTweetRepository roadTweetRepo;
    @Inject
    private Twitter twitter;
    @Inject
    private TwitterConfig twitterConfig;

    public static void main(String[] args) {
        new SpringApplicationBuilder(TwitterAutoFollowApp.class)
                .profiles("twitter", "twitterautofollow")
                .web(false)
                .run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        // get all our current followers first
        final Set<String> followeds = twitterFollowedRepo.findAllFolloweds(twitterConfig.getAppScreenName().toLowerCase());
        log.info("{} has followed {} users: {}", twitterConfig.getAppScreenName(), followeds.size(),
                followeds.stream().limit(10).toArray());

        // find relevant twitter users that we haven't followed
//        final Set<String> relevantScreenNames = roadTweetRepo.findAllDistinctScreenNames();
//        log.info("Got {} relevant screen names (un-excluded): {}", relevantScreenNames.size(),
//                relevantScreenNames.stream().limit(10).toArray());
        final Set<String> relevantScreenNames = roadTweetRepo.findAllDistinctScreenNamesExcluding(followeds);
        log.info("Got {} relevant screen names (excluded): {}", relevantScreenNames.size(),
                relevantScreenNames.stream().limit(10).toArray());

        // Following, with delay...
        log.info("{} will follow {} new relevant users", twitterConfig.getAppScreenName(), relevantScreenNames.size());
        for (final String relevantScreenName : relevantScreenNames) {
            log.info("{} following {} ...", twitterConfig.getAppScreenName(), relevantScreenName);
            final User followed = twitter.friendsFollowers().createFriendship(relevantScreenName);
            log.info("{} has followed {}: {}", twitterConfig.getAppScreenName(), relevantScreenName,
                    followed);
            final TwitterFollowed twitterFollowed = new TwitterFollowed();
            twitterFollowed.setFollowerScreenNameLower(twitterConfig.getAppScreenName().toLowerCase());
            twitterFollowed.setFollowedScreenNameLower(followed.getScreenName().toLowerCase());
            try {
                log.info("Saving {}'s friend: {}", twitterConfig.getAppScreenName(), followed.getScreenName());
                twitterFollowedRepo.save(twitterFollowed);
            } catch (DataIntegrityViolationException | ConstraintViolationException e) {
                log.info("Skipping {}'s already existing friend {} due to {}", twitterConfig.getAppScreenName(), followed.getScreenName(), e);
            }
            log.info("Waiting 1 minute before next follow...");
            Thread.sleep(60000);
        }

        log.info("Auto-follow for {} done", twitterConfig.getAppScreenName());
    }
}
