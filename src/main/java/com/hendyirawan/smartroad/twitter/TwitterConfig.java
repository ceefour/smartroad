package com.hendyirawan.smartroad.twitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import javax.inject.Inject;

/**
 * Created by ceefour on 01/08/2015.
 */
@Configuration
@Profile("twitter")
public class TwitterConfig {

    private static Logger log = LoggerFactory.getLogger(TwitterConfig.class);

    @Inject
    private Environment env;

    public Long getAppUserId() {
        return env.getRequiredProperty("spring.social.twitter.user-id", Long.class);
    }

    public String getAppScreenName() {
        return env.getRequiredProperty("spring.social.twitter.screen-name");
    }

    @Bean
    public Twitter twitter() {
        final ConfigurationBuilder cb = new ConfigurationBuilder();
        final String appId = env.getRequiredProperty("spring.social.twitter.app-id");
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(appId)
                .setOAuthConsumerSecret(env.getRequiredProperty("spring.social.twitter.app-secret"))
                .setOAuthAccessToken(env.getRequiredProperty("spring.social.twitter.access-token"))
                .setOAuthAccessTokenSecret(env.getRequiredProperty("spring.social.twitter.access-token-secret"));
        final TwitterFactory tf = new TwitterFactory(cb.build());
        final Twitter twitter = tf.getInstance();
        return twitter;
    }

}
