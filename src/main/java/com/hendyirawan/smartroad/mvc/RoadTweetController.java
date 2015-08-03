package com.hendyirawan.smartroad.mvc;

import com.google.common.base.Preconditions;
import com.hendyirawan.smartroad.core.RoadTweet;
import com.hendyirawan.smartroad.core.RoadTweetRepository;
import org.joda.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.time.Period;

/**
 * Created by ceefour on 03/08/2015.
 */
@RestController
public class RoadTweetController {

    @Inject
    private RoadTweetRepository roadTweetRepo;

    @RequestMapping(method = RequestMethod.GET, value = "tweets/{id}/media")
    public ResponseEntity<byte[]> getRoadTweetMedia(@PathVariable("id") long id) {
        final RoadTweet roadTweet = Preconditions.checkNotNull(roadTweetRepo.findOne(id),
                "Cannot find tweet %s", id);
        final HttpHeaders headers = new HttpHeaders();
        headers.setLastModified(roadTweet.getCreationTime().getMillis());
        headers.setExpires(System.currentTimeMillis() + Duration.standardDays(365).getMillis());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(roadTweet.getMediaContentType()))
                .contentLength(roadTweet.getMediaContentLength())
                .headers(headers)
                .body(roadTweet.getMediaContent());
    }
}
