package com.hendyirawan.betterroads.web;

import com.hendyirawan.betterroads.core.RoadRepository;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.io.IOException;
import java.time.Duration;

@Controller
class WebPubController {

    private static final Logger log = LoggerFactory.getLogger(WebPubController.class);

    @Inject
    private RoadRepository roadRepo;

    @RequestMapping(value = "favicon.ico", method = RequestMethod.GET,
            produces = "image/png")
//            produces = "image/vnd.microsoft.icon")
    @ResponseBody
    public ResponseEntity favicon() throws IOException {
        final byte[] body = IOUtils.toByteArray(WebPubController.class.getResourceAsStream("/com/hendyirawan/betterroads/core/icon-128.png"));
        final HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("max-age=" + Duration.ofDays(30).getSeconds());
        return ResponseEntity.ok().headers(headers).body(body);
    }

    @RequestMapping(value = "robots.txt", method = RequestMethod.GET,
            produces = "text/plain")
    @ResponseBody
    public ResponseEntity<String> robotsTxt() {
        final String body = "User-agent: *\n" +
                "Disallow:\n";
        final HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("max-age=" + Duration.ofDays(1).getSeconds());
        return ResponseEntity.ok().headers(headers).body(body);
    }

    @RequestMapping("/")
    public String home() {
        return "home";
    }

}