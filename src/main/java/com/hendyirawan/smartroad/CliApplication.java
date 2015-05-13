package com.hendyirawan.smartroad;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.actuate.autoconfigure.CrshAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Profile;

import java.io.File;

@SpringBootApplication(
    exclude = {//HibernateJpaAutoConfiguration.class,
            CrshAutoConfiguration.class,
            WebMvcAutoConfiguration.class}
)
@Profile("cli")
public class CliApplication implements CommandLineRunner {

    private static Logger log = LoggerFactory.getLogger(CliApplication.class);

    static {
        log.info("Loading OpenCV: {} from {}", Core.NATIVE_LIBRARY_NAME, System.getProperty("java.library.path"));
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(CliApplication.class)
                .profiles("cli")
                .web(false)
                .run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        final File inFile = new File("sample/pothole1.jpg");
        final Mat img = Highgui.imread(inFile.getPath());
        final Mat blurred = new Mat(img.size(), img.type());

        Imgproc.blur(img, blurred, new Size(5, 5));
        final File blurFile = new File(System.getProperty("java.io.tmpdir"), "pothole1_blur.jpg");
        Highgui.imwrite(blurFile.getPath(), blurred);
        log.info("Blurred written to {}", blurFile);

        double lowThreshold = 3;
        double ratio = 3;
        int kernelSize = 3;
        final Mat detectedEdges = new Mat();
        Imgproc.Canny(blurred, detectedEdges, lowThreshold, lowThreshold * ratio, kernelSize, false);

        final File edgesFile = new File(System.getProperty("java.io.tmpdir"), "pothole1_edges.jpg");
        Highgui.imwrite(edgesFile.getPath(), detectedEdges);
        log.info("Detected edges written to {}", detectedEdges);
    }
}
