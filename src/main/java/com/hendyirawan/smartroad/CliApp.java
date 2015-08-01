package com.hendyirawan.smartroad;

import com.hendyirawan.smartroad.core.RoadAnalysis;
import com.hendyirawan.smartroad.core.RoadAnalyzer;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.actuate.autoconfigure.CrshAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Profile;

import javax.inject.Inject;
import java.io.File;

@SpringBootApplication(
    exclude = {//HibernateJpaAutoConfiguration.class,
            CrshAutoConfiguration.class,
            WebMvcAutoConfiguration.class}
)
@Profile("cli")
public class CliApp implements CommandLineRunner {

    private static Logger log = LoggerFactory.getLogger(CliApp.class);

    static {
        log.info("Loading OpenCV: {} from {}", Core.NATIVE_LIBRARY_NAME, System.getProperty("java.library.path"));
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(CliApp.class)
                .profiles("cli")
                .web(false)
                .run(args);
    }

    @Inject
    private RoadAnalyzer roadAnalyzer;

    @Override
    public void run(String... args) throws Exception {
        final File inFile = new File("sample/pothole1.jpg");
        final Mat img = Highgui.imread(inFile.getPath(), Highgui.CV_LOAD_IMAGE_COLOR);
        final RoadAnalysis roadAnalysis = roadAnalyzer.analyze(img, 0.13, 0.32, -0.05, 1.7, 3.5);

        final File blurFile = new File(System.getProperty("java.io.tmpdir"), "pothole1_blur.jpg");
        Highgui.imwrite(blurFile.getPath(), roadAnalysis.blurred);
        log.info("Blurred written to {}", blurFile);

        final File edgesFile = new File(System.getProperty("java.io.tmpdir"), "pothole1_edges.jpg");
        Highgui.imwrite(edgesFile.getPath(), roadAnalysis.edges);
        log.info("Detected edges written to {}", edgesFile);
    }
}
