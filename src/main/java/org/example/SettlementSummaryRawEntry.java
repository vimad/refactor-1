package org.example;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

@Data
public class SettlementSummaryRawEntry {

    @CsvBindByName(column = "Scheme")
    private String scheme;
    @CsvBindByName(column = "Transaction Type")
    private String transactionType;
    @CsvBindByName(column = "Count")
    private int count;
    @CsvBindByName(column = "Source Currency")
    private String sourceCurrency;
    @CsvBindByName(column = "Source Amount")
    private double sourceAmount;
    @CsvBindByName(column = "Settlement Currency")
    private String settlementCurrency;
    @CsvBindByName(column = "Settlement Amount")
    private double settlementAmount;
    @CsvBindByName(column = "Fees")
    private double fees;
    @CsvBindByName(column = "Net Settlement")
    private double netSettlement;
    @CsvBindByName(column = "Report Date")
    private String reportDate;
    @CsvBindByName(column = "Settlement Date")
    private String settlementDate;
}
