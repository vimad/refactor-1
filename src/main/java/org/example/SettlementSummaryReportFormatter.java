package org.example;

import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class SettlementSummaryReportFormatter {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

    @SneakyThrows
    public String getFormattedContent(String rawString, boolean monthly) {
        List<SettlementSummaryRawEntry> rawEntries = loadRawEntries(rawString, SettlementSummaryRawEntry.class);
        return convertToSettlementReportFormat(rawEntries, monthly);
    }

    private String convertToSettlementReportFormat(List<SettlementSummaryRawEntry> rawEntries, boolean monthly) throws IOException {

        StringWriter writer = new StringWriter();

        writeReportDetails(writer, rawEntries, monthly);

        // Table headers
        CSVWriter tableHeaderWriter = new CSVWriter(writer);
        tableHeaderWriter.writeNext(new String[]{"Scheme", "Count", "Source Currency", "Source Amount",
                "Settlement Currency", "Settlement Amount", "Fees", "Net Settlement"});
        tableHeaderWriter.close();

        Map<String, List<SettlementSummaryRawEntry>> schemeMap = groupedByScheme(rawEntries);

        // process each scheme
        for (Map.Entry<String, List<SettlementSummaryRawEntry>> schemeMapEntry : schemeMap.entrySet()) {
            String scheme = schemeMapEntry.getKey();
            List<SettlementSummaryRawEntry> schemeEntries = schemeMapEntry.getValue();
            String settlementCurrency = schemeEntries.get(0).getSettlementCurrency();
            // print scheme
            writer.write(scheme + "\n");
            //scheme table


            int totalCount = 0;
            double totalSettlementAmount = 0;
            double totalFees = 0;
            double totalNetSettlementAmount = 0;
            Map<String, List<SettlementSummaryRawEntry>> sourceCurrencyMap = groupedBySourceCurrency(schemeEntries);
            int sourceCurrenciesSize = sourceCurrencyMap.size();
            // write each source currency sub table
            for (Map.Entry<String, List<SettlementSummaryRawEntry>> sourceCurrencyMapEntry
                    : sourceCurrencyMap.entrySet()) {
                int subTotalCount = 0;
                double subTotalSourceAmount = 0;
                double subTotalSettlementAmount = 0;
                double subTotalFees = 0;
                double subTotalNetSettlementAmount = 0;
                String sourceCurrency = sourceCurrencyMapEntry.getKey();

                List<SettlementSummaryRawEntry> sourceCurrencyEntries = sourceCurrencyMapEntry.getValue();
                CSVWriter currencyTableWriter = new CSVWriter(writer);
                // write each transaction type entries
                for (SettlementSummaryRawEntry rawEntry : sourceCurrencyEntries) {

                    String transactionType = rawEntry.getTransactionType();
                    int count = rawEntry.getCount();
                    double sourceAmount = rawEntry.getSourceAmount();
                    double settlementAmount = rawEntry.getSettlementAmount();
                    double fees = rawEntry.getFees();
                    double netSettlementAmount = rawEntry.getNetSettlement();

                    currencyTableWriter.writeNext(
                            getLine(transactionType,
                                    count,
                                    sourceCurrency,
                                    sourceAmount,
                                    settlementCurrency,
                                    settlementAmount,
                                    fees,
                                    netSettlementAmount)

                    );
                    subTotalCount += count;
                    subTotalSourceAmount += sourceAmount;
                    subTotalSettlementAmount += settlementAmount;
                    subTotalFees += fees;
                    subTotalNetSettlementAmount += netSettlementAmount;
                }

                currencyTableWriter.writeNext(
                        // EFTPOS and Direct Debit has only AUD as source currency
                        getLine(getSubTotalHeader(sourceCurrenciesSize),
                                subTotalCount,
                                sourceCurrency,
                                subTotalSourceAmount,
                                settlementCurrency,
                                subTotalSettlementAmount,
                                subTotalFees,
                                subTotalNetSettlementAmount)

                );
                currencyTableWriter.close();
                writer.write("\n");
                totalCount += subTotalCount;
                totalSettlementAmount += subTotalSettlementAmount;
                totalFees += subTotalFees;
                totalNetSettlementAmount += subTotalNetSettlementAmount;
            }
            writeTotalEntry(writer,
                    sourceCurrenciesSize,
                    totalCount,
                    settlementCurrency,
                    totalSettlementAmount,
                    totalFees,
                    totalNetSettlementAmount
            );
        }

        // Get the combined CSV string
        return writer.toString();
    }

    private static String getSubTotalHeader(int sourceCurrenciesSize) {
        return sourceCurrenciesSize > 1 ? "Sub Total" : "Total";
    }

    private static void writeTotalEntry(StringWriter writer,
                                        int sourceCurrenciesSize,
                                        int totalCount,
                                        String settlementCurrency,
                                        double totalSettlementAmount,
                                        double totalFees,
                                        double totalNetSettlementAmount) throws IOException {
        // EFTPOS and Direct Debit has only AUD as source currency. therefore Sub Total entry will be written as Total
        if (sourceCurrenciesSize > 1) {
            CSVWriter tableWriter = new CSVWriter(writer);
            tableWriter.writeNext(new String[]{
                    "Total",
                    String.valueOf(totalCount),
                    "",
                    "",
                    settlementCurrency,
                    DECIMAL_FORMAT.format(totalSettlementAmount),
                    DECIMAL_FORMAT.format(totalFees),
                    DECIMAL_FORMAT.format(totalNetSettlementAmount)}
            );
            tableWriter.close();
            // Separate tables with a blank line
            writer.write("\n");
        }
    }

    private static String[] getLine(String schemeValue,
                                    double count,
                                    String sourceCurrency,
                                    double sourceAmount,
                                    String settlementCurrency,
                                    double settlementAmount,
                                    double fees,
                                    double netSettlementAmount) {
        return new String[]{
                schemeValue,
                String.valueOf(count),
                sourceCurrency,
                DECIMAL_FORMAT.format(sourceAmount),
                settlementCurrency,
                DECIMAL_FORMAT.format(settlementAmount),
                DECIMAL_FORMAT.format(fees),
                DECIMAL_FORMAT.format(netSettlementAmount)};
    }

    private static String[] getLine(String paymentType, int count, double amount, double fee, double netSettlement) {
        return new String[]{
                paymentType,
                String.valueOf(count),
                DECIMAL_FORMAT.format(amount),
                DECIMAL_FORMAT.format(fee),
                DECIMAL_FORMAT.format(netSettlement)};
    }

    private Map<String, List<SettlementSummaryRawEntry>> groupedByScheme(List<SettlementSummaryRawEntry> rawEntries) {
        return Optional.ofNullable(rawEntries)
                .orElse(Collections.emptyList())
                .stream()
                .collect(
                        Collectors.groupingBy(SettlementSummaryRawEntry::getScheme, HashMap::new, Collectors.toCollection(ArrayList::new))
                );
    }


    private Map<String, List<SettlementSummaryRawEntry>> groupedBySourceCurrency(List<SettlementSummaryRawEntry> entries) {
        return Optional.ofNullable(entries)
                .orElse(Collections.emptyList())
                .stream()
                .collect(
                        Collectors.groupingBy(SettlementSummaryRawEntry::getSourceCurrency, HashMap::new, Collectors.toCollection(ArrayList::new))
                );
    }

    private void writeReportDetails(StringWriter writer, List<SettlementSummaryRawEntry> rawEntries, boolean monthly) throws IOException {
        CSVWriter headerWriter = new CSVWriter(writer);

        headerWriter.writeNext(new String[]{"Report Date", getReportDate(rawEntries)});
        headerWriter.writeNext(new String[]{
                "Settlement Date", monthly ? getSettlementPeriod(rawEntries) : getSettlementDate(rawEntries)
        });

        headerWriter.close();
        writer.write("\n");
    }

    private String getReportDate(List<SettlementSummaryRawEntry> rawEntries) {
        String reportDate;
        if (CollectionUtils.isNotEmpty(rawEntries)) {
            reportDate = DateUtil.convertStringFormat(
                    rawEntries.get(0).getReportDate(),
                    "yyyyMMdd",
                    "dd/MM/yyyy");
        } else {
            reportDate = DateUtil
                    .getFormattedCurrentDate("dd/MM/yyyy");
        }

        return reportDate;
    }

    private String getSettlementPeriod(List<SettlementSummaryRawEntry> rawEntries) {
        String settlementDate;
        if (CollectionUtils.isNotEmpty(rawEntries)) {
            settlementDate = DateUtil.getMonthPeriodFormat(
                    rawEntries.get(0).getSettlementDate(),
                    "dd/MM/yyyy"
            );
        } else {
            settlementDate = DateUtil.getMonthPeriodFormat(
                    DateUtil.getFormattedPreviousDate("yyyyMM", 0, 1, 0),
                    "dd/MM/yyyy"
            );
        }
        return settlementDate;
    }

    private String getSettlementDate(List<SettlementSummaryRawEntry> rawEntries) {
        String settlementDate;
        if (CollectionUtils.isNotEmpty(rawEntries)) {
            settlementDate = DateUtil.convertStringFormat(
                    rawEntries.get(0).getSettlementDate(),
                    "yyyyMMdd",
                    "dd/MM/yyyy");
        } else {
            settlementDate = DateUtil
                    .getFormattedPreviousDate("dd/MM/yyyy", 0, 0, 1);
        }
        return settlementDate;
    }

    private static List<SettlementSummaryRawEntry> loadRawEntries(String csvString, Class<? extends SettlementSummaryRawEntry> reportClass) {
        List<SettlementSummaryRawEntry> rawEntries;
        try (StringReader reader = new StringReader(csvString)) {
            CsvToBean<SettlementSummaryRawEntry> csvToBean = new CsvToBeanBuilder<SettlementSummaryRawEntry>(reader)
                    .withType(reportClass)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            rawEntries = csvToBean.parse();

        } catch (Exception e) {
            System.out.println(e);;
            throw e;
        }
        return rawEntries;
    }

}
