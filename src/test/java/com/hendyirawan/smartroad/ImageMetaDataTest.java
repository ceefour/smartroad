// inspired by http://blog.jeroenreijn.com/2010/04/metadata-extraction-with-apache-tika.html
package com.hendyirawan.smartroad;

import com.google.common.base.Preconditions;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.jpeg.JpegParser;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.*;

public class ImageMetaDataTest {
    private static final Logger log = LoggerFactory.getLogger(ImageMetaDataTest.class);
    private static final File fileName = new File("sample/P_20150523_130340_HDR_good.jpg");
    private Tika tika;

    @Before
    public void setUp() throws IOException {
        tika = new Tika();
        log.info("Opening '{}'", fileName);
    }

    @Test
    public void testImageMetadataCameraModel() throws IOException, SAXException, TikaException {
        final Metadata metadata = new Metadata();
        final ContentHandler handler = new DefaultHandler();
        final Parser parser = new JpegParser();
        final ParseContext context = new ParseContext();
        final String mimeType = tika.detect(fileName);
//        final String mimeType;
//        try (InputStream stream = Preconditions.checkNotNull(IOUtils.toBufferedInputStream(FileUtils.openInputStream(fileName)),
//                "Cannot open file '%s'", fileName)) {
//            mimeType = tika.detect(fileName);
//            metadata.set(Metadata.CONTENT_TYPE, mimeType);
//        }
        try (InputStream stream = Preconditions.checkNotNull(IOUtils.toBufferedInputStream(FileUtils.openInputStream(fileName)),
                "Cannot open file '%s'", fileName)) {
            parser.parse(stream, handler, metadata, context);
        }
        log.info("Model: {}", metadata.get("Model"));
        for (final String name : metadata.names()) {
            log.info("{} = {}", name, metadata.get(name));
        }
        Assert.assertThat("The EXIF Model should be filled",
                metadata.get("Model"), Matchers.not(Matchers.isEmptyOrNullString()));
    }

}
