package com.hendyirawan.betterroads;

import org.opencv.core.Core;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BetterRoadsApplication implements CommandLineRunner {

    private static Logger log = LoggerFactory.getLogger(BetterRoadsApplication.class);

    static {
        log.info("Loading OpenCV: {}", Core.NATIVE_LIBRARY_NAME);
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        SpringApplication.run(BetterRoadsApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

    }
}
