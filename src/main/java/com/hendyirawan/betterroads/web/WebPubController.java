package com.hendyirawan.betterroads.web;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soluvas.commons.SlugUtils;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

@Controller
class WebPubController {

    private static final Logger log = LoggerFactory.getLogger(WebPubController.class);

    @Inject
    private RoadRepository roadRepo;

    @RequestMapping(value = "favicon.ico", method = RequestMethod.GET,
            produces = "image/vnd.microsoft.icon")
    @ResponseBody
    public ResponseEntity favicon() throws IOException {
        final byte[] body = IOUtils.toByteArray(WebPubController.class.getResourceAsStream("/com/gigastic/core/gigastic-128.ico"));
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