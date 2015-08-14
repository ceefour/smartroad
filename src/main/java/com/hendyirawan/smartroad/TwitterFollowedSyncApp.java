package com.hendyirawan.smartroad;

import com.hendyirawan.smartroad.core.*;
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
import twitter4j.*;

import javax.inject.Inject;

//@EnableAutoConfiguration
@SpringBootApplication(exclude = {
    LiquibaseAutoConfiguration.class, // java.lang.IllegalStateException: Cannot find changelog location: class path resource [db/changelog/db.changelog-master.yaml] (please add changelog or check your Liquibase configuration)
    CrshAutoConfiguration.class
    //HibernateJpaAutoConfiguration.class
    }
)
@Profile("twitterfollowedsync")
//@ComponentScan(excludeFilters=@ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=CommandLineRunner.class))
public class TwitterFollowedSyncApp implements CommandLineRunner {

    private static Logger log = LoggerFactory.getLogger(TwitterFollowedSyncApp.class);

    static {
        log.info("Loading OpenCV: {} from {}", Core.NATIVE_LIBRARY_NAME, System.getProperty("java.library.path"));
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    @Inject
    private Environment env;
    @Inject
    private TwitterFollowedRepository twitterFollowedRepo;
    @Inject
    private Twitter twitter;
    @Inject
    private TwitterConfig twitterConfig;

    public static void main(String[] args) {
        new SpringApplicationBuilder(TwitterFollowedSyncApp.class)
                .profiles("twitter", "twitterfollowedsync")
                .web(false)
                .run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        final String myScreenName = twitterConfig.getAppScreenName();
        final int ITEMS_PER_PAGE = 200;
        log.info("Getting {}'s friends, first page, {} items per page", myScreenName, ITEMS_PER_PAGE);
        PagableResponseList<User> page = twitter.getFriendsList(twitterConfig.getAppUserId(), -1, ITEMS_PER_PAGE);
        while (page != null) {
            for (User user : page) {
                final TwitterFollowed twitterFollowed = new TwitterFollowed();
                twitterFollowed.setFollowerScreenNameLower(myScreenName.toLowerCase());
                twitterFollowed.setFollowedScreenNameLower(user.getScreenName().toLowerCase());
                try {
                    log.debug("Saving {}'s friend: {} ...", myScreenName, user.getScreenName());
                    twitterFollowedRepo.save(twitterFollowed);
                    log.info("Saved {}'s friend: {}", myScreenName, user.getScreenName());
                } catch (DataIntegrityViolationException | ConstraintViolationException e) {
                    log.info("Skipping {}'s already existing friend {} due to {}",
                            myScreenName, user.getScreenName(), e.toString());
                }
            }
            if (page.hasNext()) {
                log.info("Getting {}'s friends, cursor {}, {} items per page", myScreenName, page.getNextCursor(), ITEMS_PER_PAGE);
                page = twitter.getFriendsList(twitter.getId(), page.getNextCursor(), ITEMS_PER_PAGE);
            } else {
                page = null;
            }
        }

        final int friendCount = twitterFollowedRepo.countByFollowerScreenNameLower(myScreenName.toLowerCase());
        log.info("Sync {}'s friends complete, now has {} friends", myScreenName, friendCount);
    }
}
