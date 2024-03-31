package org.example;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SettlementSummaryReportFormatterTest {

    SettlementSummaryReportFormatter settlementSummaryReportFormatter = new SettlementSummaryReportFormatter();

    @Test
    void processDailySettlementContent_givenDataExists_shouldReturnValid() throws IOException {
        String file = "src/test/resources/settlement_summary/daily_au_000";
        String csvString = new String(Files.readAllBytes(Paths.get(file)));

        String expectedFile = "src/test/resources/settlement_summary/daily_summary_expected_string.csv";
        String expectedCsvString = new String(Files.readAllBytes(Paths.get(expectedFile)));

        String result = settlementSummaryReportFormatter.getFormattedContent(csvString, false);

        assertAll(
                () -> assertNotNull(result, "should not be null"),
                () -> assertEquals(expectedCsvString, result, "should be equal")
        );
    }

    @Test
    void processDailySettlementContent_givenEmpty_shouldReturnValid() throws IOException {
        String file = "src/test/resources/settlement_summary/daily_au_000_empty";
        String csvString = new String(Files.readAllBytes(Paths.get(file)));

        String result = settlementSummaryReportFormatter.getFormattedContent(csvString, false);

        assertNotNull(result);
    }

    @Test
    void processMonthlySettlementContent_givenDataExists_shouldReturnValid() throws IOException {
        String file = "src/test/resources/settlement_summary/monthly_au_000";
        String csvString = new String(Files.readAllBytes(Paths.get(file)));

        String expectedFile = "src/test/resources/settlement_summary/monthly_summary_expected_string.csv";
        String expectedCsvString = new String(Files.readAllBytes(Paths.get(expectedFile)));

        String result = settlementSummaryReportFormatter.getFormattedContent(csvString, true);

        assertAll(
                () -> assertNotNull(result, "should not be null"),
                () -> assertEquals(expectedCsvString, result, "should be equal")
        );
    }

    @Test
    void processMonthlySettlementContent_givenEmpty_shouldReturnValid() throws IOException {
        String file = "src/test/resources/settlement_summary/monthly_au_000_empty";
        String csvString = new String(Files.readAllBytes(Paths.get(file)));

        String result = settlementSummaryReportFormatter.getFormattedContent(csvString, true);

        assertNotNull(result);
    }
}