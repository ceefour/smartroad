package com.hendyirawan.smartroad;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Created by ceefour on 6/4/15.
 */
public class ExcelTest {

    private static final Logger log = LoggerFactory.getLogger(ExcelTest.class);

    @Test
    public void openFile() throws IOException {
        final File inFile = new File(System.getProperty("user.home"), "git/smartroad/sample/road-survey-template.xlsx");
        log.info("Reading from '{}'", inFile);
        try (final FileInputStream stream = new FileInputStream(inFile)) {
            //final POIFSFileSystem fs = new POIFSFileSystem(stream);
            try (final XSSFWorkbook workbook = new XSSFWorkbook(stream)) {
                final XSSFSheet surveySheet = workbook.getSheetAt(0);
                final XSSFCell titleCell = surveySheet.getRow(0).getCell(0);
                log.info("Title is: {}", titleCell.getStringCellValue());
                surveySheet.getRow(11).getCell(2).setCellValue(12345);
                final File outFile = new File(System.getProperty("user.home"), "tmp/road-survey-test.xlsx");
                log.info("Writing to '{}'", outFile);
                workbook.write(new FileOutputStream(outFile));
            }
        }
    }
}
