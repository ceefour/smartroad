package com.hendyirawan.smartroad;

import org.opencv.core.Core;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Profile;

//@EnableAutoConfiguration
@SpringBootApplication(exclude =
    LiquibaseAutoConfiguration.class // java.lang.IllegalStateException: Cannot find changelog location: class path resource [db/changelog/db.changelog-master.yaml] (please add changelog or check your Liquibase configuration)
)
@Profile("daemon")
//@ComponentScan(excludeFilters=@ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=CommandLineRunner.class))
public class DaemonApp implements CommandLineRunner {

    private static Logger log = LoggerFactory.getLogger(DaemonApp.class);

    static {
        log.info("Loading OpenCV: {} from {}", Core.NATIVE_LIBRARY_NAME, System.getProperty("java.library.path"));
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(DaemonApp.class)
                .profiles("daemon")
                .run(args);
    }

    @Override
    public void run(String... args) throws Exception {
    }
}
