package io.everytrade.server.plugin.impl.everytrade.parser.exchange.srajtofle;

import io.everytrade.server.model.SupportedExchange;
import io.everytrade.server.plugin.api.parser.FeeRebateImportedTransactionBean;
import io.everytrade.server.plugin.api.parser.ImportedTransactionBean;
import io.everytrade.server.plugin.api.parser.TransactionCluster;
import io.everytrade.server.plugin.impl.everytrade.parser.EverytradeCsvMultiParser;
import io.everytrade.server.plugin.impl.everytrade.parser.exchange.bean.ParserTestUtils;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static io.everytrade.server.model.Currency.BTC;
import static io.everytrade.server.model.Currency.CZK;
import static io.everytrade.server.model.Currency.EUR;
import static io.everytrade.server.model.TransactionType.AIRDROP;
import static io.everytrade.server.model.TransactionType.BUY;
import static io.everytrade.server.model.TransactionType.DEPOSIT;
import static io.everytrade.server.model.TransactionType.EARNING;
import static io.everytrade.server.model.TransactionType.FEE;
import static io.everytrade.server.model.TransactionType.FORK;
import static io.everytrade.server.model.TransactionType.REWARD;
import static io.everytrade.server.model.TransactionType.SELL;
import static io.everytrade.server.model.TransactionType.STAKE;
import static io.everytrade.server.model.TransactionType.STAKING_REWARD;
import static io.everytrade.server.model.TransactionType.UNSTAKE;
import static io.everytrade.server.model.TransactionType.WITHDRAWAL;
import static io.everytrade.server.plugin.impl.everytrade.parser.exchange.ExchangeBean.FEE_UID_PART;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Srajtofle format is the WhaleBooks export without UNIT_PRICE and VOLUME_QUOTE. It ships in
 * two widths - the full 13-column one and the 9-column one that also drops the trailing metadata
 * columns (ETD-2200) - and both are served by the same bean.
 *
 * @see SrajtofleBeanV1
 */
class SrajtofleBeanV1Test {

    private static final String HEADER =
        "UID;DATE;SYMBOL;ACTION;QUANTITY;FEE;FEE_CURRENCY;ADDRESS_FROM;ADDRESS_TO;NOTE;LABELS;PARTNER;REFERENCE\n";

    private static final String HEADER_COMMA_SEPARATED =
        "UID,DATE,SYMBOL,ACTION,QUANTITY,FEE,FEE_CURRENCY,ADDRESS_FROM,ADDRESS_TO,NOTE,LABELS,PARTNER,REFERENCE\n";

    /**
     * The 9-column variant: no NOTE, LABELS, PARTNER or REFERENCE column at all.
     */
    private static final String HEADER_9_COLUMNS =
        "UID;DATE;SYMBOL;ACTION;QUANTITY;FEE;FEE_CURRENCY;ADDRESS_FROM;ADDRESS_TO\n";

    private static final String HEADER_9_COLUMNS_COMMA_SEPARATED =
        "UID,DATE,SYMBOL,ACTION,QUANTITY,FEE,FEE_CURRENCY,ADDRESS_FROM,ADDRESS_TO\n";

    private static final String WHALEBOOKS_HEADER =
        "UID;DATE;SYMBOL;ACTION;QUANTITY;UNIT_PRICE;VOLUME_QUOTE;FEE;FEE_CURRENCY;ADDRESS_FROM;ADDRESS_TO;NOTE;LABELS;"
            + "PARTNER;REFERENCE";

    /** Synthetic, deliberately checksum-invalid addresses - never a real one from a customer file. */
    private static final String ADDRESS_FROM = "bc1qsenderaddresssenderaddresssenderaddr0";
    private static final String ADDRESS_TO = "bc1qreceiveraddressreceiveraddressreceiv0";

    // ---------------------------------------------------------------------------------------------
    // header resolution
    // ---------------------------------------------------------------------------------------------

    @Test
    void srajtofleHeaderResolvesToSrajtofle() {
        assertEquals(
            SupportedExchange.SRAJTOFLE,
            EverytradeCsvMultiParser.DESCRIPTOR.getSupportedExchange(HEADER.strip())
        );
    }

    @Test
    void srajtofleCommaSeparatedHeaderResolvesToSrajtofle() {
        assertEquals(
            SupportedExchange.SRAJTOFLE,
            EverytradeCsvMultiParser.DESCRIPTOR.getSupportedExchange(HEADER_COMMA_SEPARATED.strip())
        );
    }

    /**
     * ETD-2200: the same export also exists without the four trailing metadata columns. Before the
     * 9-column template was registered this header matched nothing and the upload died with
     * UnknownHeaderException.
     */
    @Test
    void nineColumnHeaderResolvesToSrajtofle() {
        assertEquals(
            SupportedExchange.SRAJTOFLE,
            EverytradeCsvMultiParser.DESCRIPTOR.getSupportedExchange(HEADER_9_COLUMNS.strip())
        );
    }

    @Test
    void nineColumnCommaSeparatedHeaderResolvesToSrajtofle() {
        assertEquals(
            SupportedExchange.SRAJTOFLE,
            EverytradeCsvMultiParser.DESCRIPTOR.getSupportedExchange(HEADER_9_COLUMNS_COMMA_SEPARATED.strip())
        );
    }

    /**
     * The Srajtofle template is a strict subset of the WhaleBooks one and CsvHeader matching is
     * subset-based, so a WhaleBooks file matches both. The tie must be resolved by column count,
     * otherwise adding Srajtofle would silently hijack every WhaleBooks import.
     */
    @Test
    void whalebooksHeaderStillResolvesToWhalebooks() {
        assertEquals(
            SupportedExchange.EVERYTRADE,
            EverytradeCsvMultiParser.DESCRIPTOR.getSupportedExchange(WHALEBOOKS_HEADER)
        );
    }

    /**
     * A WhaleBooks row must keep its unit price -- proves the WhaleBooks bean, not the Srajtofle
     * one, actually parsed the file.
     */
    @Test
    void whalebooksRowKeepsUnitPriceAfterSrajtofleWasAdded() {
        final String row = "1;01.11.2024 00:00:00;BTC/EUR;BUY;5;20000;100000;;;;;;;\n";
        final var cluster = ParserTestUtils.getTransactionCluster(WHALEBOOKS_HEADER + "\n" + row);

        assertEquals(0, new BigDecimal("20000").compareTo(cluster.getMain().getUnitPrice()));
    }

    // ---------------------------------------------------------------------------------------------
    // trades -- imported price-less
    // ---------------------------------------------------------------------------------------------

    @Test
    void buyIsImportedWithoutUnitPrice() {
        final String row = "1;01.11.2024 00:00:00;BTC/EUR;BUY;5;;;;;;;;\n";
        final var actual = ParserTestUtils.getTransactionClusters(HEADER + row);

        ParserTestUtils.checkEqual(
            new TransactionCluster(
                new ImportedTransactionBean("1", Instant.parse("2024-11-01T00:00:00Z"), BTC, EUR, BUY,
                    new BigDecimal("5"), null, null, null, null),
                List.of()
            ),
            actual.get(0)
        );
        assertNull(actual.get(0).getMain().getUnitPrice(), "no UNIT_PRICE column in this format");
    }

    @Test
    void sellIsImportedWithoutUnitPrice() {
        final String row = "2;04.06.2025 00:00:00;BTC/CZK;SELL;0,20;;;;;;;;\n";
        final var cluster = ParserTestUtils.getTransactionCluster(HEADER + row);

        assertEquals(SELL, cluster.getMain().getAction());
        assertEquals(BTC, cluster.getMain().getBase());
        assertEquals(CZK, cluster.getMain().getQuote());
        assertEquals(0, new BigDecimal("0.20").compareTo(cluster.getMain().getVolume()));
        assertNull(cluster.getMain().getUnitPrice());
    }

    /**
     * The WhaleBooks exporter writes numbers with a decimal comma, so the re-import must accept it.
     */
    @Test
    void decimalCommaQuantityIsParsed() {
        final String row = "3;01.06.2025 00:00:00;BTC/CZK;BUY;1,5;;;;;;;;\n";
        final var cluster = ParserTestUtils.getTransactionCluster(HEADER + row);

        assertEquals(0, new BigDecimal("1.5").compareTo(cluster.getMain().getVolume()));
    }

    // ---------------------------------------------------------------------------------------------
    // fee sub-transaction
    // ---------------------------------------------------------------------------------------------

    @Test
    void tradeFeeInQuoteCurrencyBecomesRelatedTransaction() {
        final String row = "4;05.06.2025 00:00:00;BTC/CZK;SELL;0,01;200;CZK;;;;;;\n";
        final var cluster = ParserTestUtils.getTransactionCluster(HEADER + row);

        assertEquals(1, cluster.getRelated().size());
        final var fee = (FeeRebateImportedTransactionBean) cluster.getRelated().get(0);
        assertEquals(FEE, fee.getAction());
        assertEquals(CZK, fee.getFeeRebateCurrency());
        assertEquals(0, new BigDecimal("200").compareTo(fee.getVolume()));
        assertTrue(fee.getUid().startsWith("4"), "related fee uid is derived from the main uid");
    }

    @Test
    void tradeFeeInBaseCurrencyBecomesRelatedTransaction() {
        final String row = "5;01.06.2025 00:00:00;BTC/CZK;BUY;1;0,123;BTC;;;;;;\n";
        final var cluster = ParserTestUtils.getTransactionCluster(HEADER + row);

        assertEquals(1, cluster.getRelated().size());
        final var fee = (FeeRebateImportedTransactionBean) cluster.getRelated().get(0);
        assertEquals(BTC, fee.getFeeRebateCurrency());
        assertEquals(0, new BigDecimal("0.123").compareTo(fee.getVolume()));
    }

    @Test
    void feeWithoutCurrencyIsReportedAsFailedFee() {
        final String row = "6;01.06.2025 00:00:00;BTC/CZK;BUY;1;0,123;;;;;;;\n";
        final var cluster = ParserTestUtils.getTransactionCluster(HEADER + row);

        assertEquals(0, cluster.getRelated().size());
        assertEquals(1, cluster.getFailedFeeTransactionCount());
        assertEquals("Fee currency is null. ", cluster.getFailedFeeReason());
    }

    @Test
    void standaloneFeeRow() {
        final String row = "7;01.06.2025 00:00:00;BTC;FEE;0,001;;;;;standalone fee;;;\n";
        final var cluster = ParserTestUtils.getTransactionCluster(HEADER + row);

        final var fee = (FeeRebateImportedTransactionBean) cluster.getMain();
        assertEquals(FEE, fee.getAction());
        assertEquals(BTC, fee.getBase());
        assertEquals(0, new BigDecimal("0.001").compareTo(fee.getVolume()));
        assertEquals("standalone fee", fee.getNote());
    }

    // ---------------------------------------------------------------------------------------------
    // price-less types -- unchanged behaviour compared to the WhaleBooks parser
    // ---------------------------------------------------------------------------------------------

    @Test
    void depositUsesAddressFrom() {
        final String row = "8;02.01.2025 00:00:00;BTC;DEPOSIT;0,9999;;;addr-from;;;;;\n";
        final var cluster = ParserTestUtils.getTransactionCluster(HEADER + row);

        assertEquals(DEPOSIT, cluster.getMain().getAction());
        assertEquals(BTC, cluster.getMain().getBase());
        assertEquals(0, new BigDecimal("0.9999").compareTo(cluster.getMain().getVolume()));
        assertEquals("addr-from", cluster.getMain().getAddress());
    }

    @Test
    void withdrawalUsesAddressTo() {
        final String row = "9;12.06.2025 00:00:00;BTC;WITHDRAWAL;0,088;;;;addr-to;;;;\n";
        final var cluster = ParserTestUtils.getTransactionCluster(HEADER + row);

        assertEquals(WITHDRAWAL, cluster.getMain().getAction());
        assertEquals("addr-to", cluster.getMain().getAddress());
    }

    @Test
    void stakeAndUnstake() {
        final String rows =
            "10;29.07.2025 00:00:00;BTC;STAKE;0,5;;;;;;;;\n"
            + "11;01.10.2025 00:00:00;BTC;UNSTAKE;0,1;;;;;;;;\n";
        final var clusters = ParserTestUtils.getTransactionClusters(HEADER + rows);

        assertEquals(2, clusters.size());
        assertEquals(STAKE, clusters.get(0).getMain().getAction());
        assertEquals(UNSTAKE, clusters.get(1).getMain().getAction());
        assertNull(clusters.get(0).getMain().getUnitPrice());
    }

    @Test
    void zeroCostGainTypes() {
        final String rows =
            "12;01.06.2025 00:00:01;BTC;STAKE REWARD;1,4;;;;;;;;\n"
            + "13;01.06.2025 00:00:02;BTC;AIRDROP;1,3;;;;;;;;\n"
            + "14;01.06.2025 00:00:03;BTC;EARN;1,2;;;;;;;;\n"
            + "15;01.06.2025 00:00:04;BTC;FORK;1,1;;;;;;;;\n"
            + "16;07.08.2025 00:00:00;BTC;REWARD;1,7;;;;;;;;\n";
        final var clusters = ParserTestUtils.getTransactionClusters(HEADER + rows);

        assertEquals(5, clusters.size());
        assertEquals(STAKING_REWARD, clusters.get(0).getMain().getAction(), "\"STAKE REWARD\" alias");
        assertEquals(AIRDROP, clusters.get(1).getMain().getAction());
        assertEquals(EARNING, clusters.get(2).getMain().getAction(), "\"EARN\" alias");
        assertEquals(FORK, clusters.get(3).getMain().getAction());
        assertEquals(REWARD, clusters.get(4).getMain().getAction());
        clusters.forEach(c -> assertNull(c.getMain().getUnitPrice()));
        clusters.forEach(c -> assertEquals(BTC, c.getMain().getQuote(), "quote falls back to base"));
    }

    /**
     * There is no price column in this format, but a zero-cost-gain row can still name its valuation
     * currency through SYMBOL -- that must not collapse to the base (ETD-2182).
     */
    @Test
    void zeroCostGainKeepsQuoteCurrencyWhenSymbolIsAPair() {
        final String row = "20;07.08.2025 00:00:00;BTC/CZK;REWARD;1,7;;;;;;;;\n";
        final var cluster = ParserTestUtils.getTransactionCluster(HEADER + row);

        assertEquals(REWARD, cluster.getMain().getAction());
        assertEquals(BTC, cluster.getMain().getBase());
        assertEquals(CZK, cluster.getMain().getQuote(), "quote currency must survive");
        assertNull(cluster.getMain().getUnitPrice(), "this format carries no price at all");
    }

    /**
     * STAKE / UNSTAKE are transfers, not valued acquisitions -- they stay base/base even with a pair.
     */
    @Test
    void stakeKeepsBaseAsQuoteEvenWithAPairSymbol() {
        final String row = "21;29.07.2025 00:00:00;BTC/CZK;STAKE;0,5;;;;;;;;\n";
        final var cluster = ParserTestUtils.getTransactionCluster(HEADER + row);

        assertEquals(STAKE, cluster.getMain().getAction());
        assertEquals(BTC, cluster.getMain().getQuote());
    }

    // ---------------------------------------------------------------------------------------------
    // pass-through columns
    // ---------------------------------------------------------------------------------------------

    @Test
    void notePartnerReferenceAndLabelsArePassedThrough() {
        final String row = "17;01.06.2025 00:00:00;BTC/CZK;BUY;1;;;from;to;a note;label1,label2;ACME;ref-42\n";
        final var cluster = ParserTestUtils.getTransactionCluster(HEADER + row);
        final var tx = cluster.getMain();

        assertEquals("a note", tx.getNote());
        assertEquals("label1,label2", tx.getLabels());
        assertEquals("ACME", tx.getPartner());
        assertEquals("ref-42", tx.getReference());
        assertEquals("from", tx.getAddress(), "ADDRESS_FROM wins for a BUY");
    }

    // ---------------------------------------------------------------------------------------------
    // rejected rows
    // ---------------------------------------------------------------------------------------------

    @Test
    void rowWithoutQuantityAndWithoutFeeIsRejected() {
        final String row = "18;01.06.2025 00:00:00;BTC/CZK;BUY;;;;;;;;;\n";
        final var result = ParserTestUtils.getParseResult(HEADER + row);

        assertEquals(0, result.getTransactionClusters().size());
        assertEquals(1, result.getParsingProblems().size());
    }

    @Test
    void negativeQuantityIsRejected() {
        final String row = "19;01.06.2025 00:00:00;BTC/CZK;BUY;-1;;;;;;;;\n";
        final var result = ParserTestUtils.getParseResult(HEADER + row);

        assertEquals(0, result.getTransactionClusters().size());
        assertEquals(1, result.getParsingProblems().size());
    }

    // ---------------------------------------------------------------------------------------------
    // 9-column variant (ETD-2200) -- no NOTE / LABELS / PARTNER / REFERENCE column
    // ---------------------------------------------------------------------------------------------

    @Test
    void nineColumnDepositRowIsParsed() {
        final String row = "22;05.03.2025 11:22:33;BTC;DEPOSIT;0.00071350;0.00000771;BTC;"
            + ADDRESS_FROM + ";" + ADDRESS_TO + "\n";
        final var actual = ParserTestUtils.getTransactionClusters(HEADER_9_COLUMNS + row);

        assertEquals(1, actual.size());
        final var expected = new TransactionCluster(
            ImportedTransactionBean.createDepositWithdrawal(
                "22", Instant.parse("2025-03-05T11:22:33Z"), BTC, BTC, DEPOSIT,
                new BigDecimal("0.00071350"), ADDRESS_FROM, null, null, null, null
            ),
            List.of(new FeeRebateImportedTransactionBean(
                "22" + FEE_UID_PART, Instant.parse("2025-03-05T11:22:33Z"), BTC, BTC, FEE,
                new BigDecimal("0.00000771"), BTC, null, ADDRESS_FROM, null, null, null
            ))
        );
        ParserTestUtils.checkEqual(expected, actual.get(0));

        final var tx = actual.get(0).getMain();
        assertEquals(ADDRESS_FROM, tx.getAddress(), "a DEPOSIT is addressed by ADDRESS_FROM");
        assertNull(tx.getNote(), "no NOTE column in the 9-column variant");
        assertNull(tx.getLabels(), "no LABELS column in the 9-column variant");
        assertNull(tx.getPartner(), "no PARTNER column in the 9-column variant");
        assertNull(tx.getReference(), "no REFERENCE column in the 9-column variant");
        assertNull(tx.getUnitPrice(), "this format carries no price at all");
        assertNull(tx.getQuoteVolume(), "and no quote volume either");
    }

    @Test
    void nineColumnWithdrawalRowIsParsed() {
        final String row = "23;07.04.2025 08:09:10;BTC;WITHDRAWAL;0.00500000;0.00001234;BTC;"
            + ADDRESS_FROM + ";" + ADDRESS_TO + "\n";
        final var cluster = ParserTestUtils.getTransactionCluster(HEADER_9_COLUMNS + row);
        final var tx = cluster.getMain();

        assertEquals(WITHDRAWAL, tx.getAction());
        assertEquals(BTC, tx.getBase());
        assertEquals(BTC, tx.getQuote(), "a bare SYMBOL makes the quote fall back to the base");
        assertEquals(0, new BigDecimal("0.005").compareTo(tx.getVolume()));
        assertEquals(ADDRESS_TO, tx.getAddress(), "a WITHDRAWAL is addressed by ADDRESS_TO");
        assertNull(tx.getNote());
        assertNull(tx.getLabels());
        assertNull(tx.getPartner());
        assertNull(tx.getReference());
        assertNull(tx.getQuoteVolume());
    }

    @Test
    void nineColumnCommaSeparatedRowIsParsed() {
        final String row = "24,09.05.2025 13:14:15,BTC,DEPOSIT,0.00002,0.00000286,BTC,"
            + ADDRESS_FROM + "," + ADDRESS_TO + "\n";
        final var cluster = ParserTestUtils.getTransactionCluster(HEADER_9_COLUMNS_COMMA_SEPARATED + row);

        assertEquals(DEPOSIT, cluster.getMain().getAction());
        assertEquals(0, new BigDecimal("0.00002").compareTo(cluster.getMain().getVolume()));
        assertEquals(1, cluster.getRelated().size(), "the FEE column still produces its own fee leg");
    }

    /**
     * 43 rows of the reported file carry a FEE larger than their QUANTITY and 7 carry one exactly
     * equal to it. Whether that column really is this account's own cost is a question about the
     * format, not about header matching, so ETD-2200 leaves the accounting untouched and this test
     * only pins what the parser does today: the row is accepted and the fee leg is emitted at its
     * face value.
     */
    @Test
    void nineColumnWithdrawalWithFeeEqualToQuantityIsAccepted() {
        final String row = "26;07.04.2025 08:09:10;BTC;WITHDRAWAL;0.00071350;0.00071350;BTC;"
            + ADDRESS_FROM + ";" + ADDRESS_TO + "\n";
        final var result = ParserTestUtils.getParseResult(HEADER_9_COLUMNS + row);

        assertEquals(0, result.getParsingProblems().size());
        assertEquals(1, result.getTransactionClusters().size());

        final var cluster = result.getTransactionClusters().get(0);
        assertEquals(0, new BigDecimal("0.00071350").compareTo(cluster.getMain().getVolume()));
        assertEquals(1, cluster.getRelated().size());
        assertEquals(
            0,
            new BigDecimal("0.00071350").compareTo(cluster.getRelated().get(0).getVolume()),
            "the fee leg is as large as the withdrawal itself"
        );
    }

    // ---------------------------------------------------------------------------------------------
    // addresses and header resolution of neighbouring widths
    // ---------------------------------------------------------------------------------------------

    /**
     * Real exports leave ADDRESS_TO empty on some withdrawals - 9 of the 131 in the reported file,
     * including its very first row. Univocity turns the empty trailing field into null and there is
     * no fallback, so the main transaction ends up address-less. Pinned as today's behaviour:
     * {@code EveryTradeBeanV3_2} does exactly the same for a WhaleBooks withdrawal, so changing it
     * only here would split the two formats apart.
     */
    @Test
    void nineColumnWithdrawalWithEmptyAddressToLeavesMainAddressNull() {
        final String row = "32;11.06.2025 15:16:17;BTC;WITHDRAWAL;0.001;0.0000123;BTC;" + ADDRESS_FROM + ";\n";
        final var cluster = ParserTestUtils.getTransactionCluster(HEADER_9_COLUMNS + row);

        assertEquals(WITHDRAWAL, cluster.getMain().getAction());
        assertNull(cluster.getMain().getAddress(), "ADDRESS_TO is empty and there is no fallback");
        assertEquals(
            ADDRESS_FROM,
            cluster.getRelated().get(0).getAddress(),
            "while the fee leg falls back to ADDRESS_FROM - pinned as today's behaviour"
        );
    }

    /**
     * The 9-column template must not outrank the 13-column one for a full Srajtofle export, and the
     * pass-through columns must keep arriving.
     */
    @Test
    void thirteenColumnRowStillKeepsItsMetadataColumns() {
        final String row = "33;01.06.2025 00:00:00;BTC;DEPOSIT;1;;;from;to;a note;label1;ACME;ref-42\n";
        final var tx = ParserTestUtils.getTransactionCluster(HEADER + row).getMain();

        assertEquals("a note", tx.getNote());
        assertEquals("label1", tx.getLabels());
        assertEquals("ACME", tx.getPartner());
        assertEquals("ref-42", tx.getReference());
    }

    /**
     * ETD-2200: CsvHeader's ordered mode is a subsequence test, so an ORDERED 9-column template would
     * also match every priced WhaleBooks header - UNIT_PRICE, VOLUME_QUOTE, REBATE and
     * REBATE_CURRENCY all sit between QUANTITY and ADDRESS_FROM, and extra columns are tolerated.
     * None of these widths is registered anywhere, so each is refused with UnknownHeaderException
     * today; capturing them would hand them to SrajtofleBeanV1, which has no price setter, and the
     * trade would import price-less with zero parsing problems - a silent loss of cost basis in a tax
     * product. The 9-column template is registered unordered precisely to keep them refused.
     */
    @Test
    void trimmedButPricedWhalebooksHeadersAreStillRefused() {
        final List<String> pricedHeaders = List.of(
            "UID;DATE;SYMBOL;ACTION;QUANTITY;UNIT_PRICE;VOLUME_QUOTE;FEE;FEE_CURRENCY;ADDRESS_FROM;ADDRESS_TO",
            "UID;DATE;SYMBOL;ACTION;QUANTITY;UNIT_PRICE;FEE;FEE_CURRENCY;ADDRESS_FROM;ADDRESS_TO",
            "UID;DATE;SYMBOL;ACTION;QUANTITY;UNIT_PRICE;VOLUME_QUOTE;FEE;FEE_CURRENCY;REBATE;REBATE_CURRENCY;"
                + "ADDRESS_FROM;ADDRESS_TO",
            "UID;DATE;SYMBOL;ACTION;QUANTITY;UNIT_PRICE;FEE;FEE_CURRENCY;REBATE;REBATE_CURRENCY;ADDRESS_FROM;ADDRESS_TO"
        );

        pricedHeaders.forEach(header -> assertFalse(
            EverytradeCsvMultiParser.DESCRIPTOR.isHeaderSupported(header),
            "a header carrying a price column must not be captured by the price-less Srajtofle template: " + header
        ));
    }

    /**
     * The flip side of the test above: unordered matching means the file header has to be built from
     * the nine known names, but it does not have to carry all nine. A shorter price-less export is
     * accepted rather than refused, and nothing is lost because there is no column to lose.
     */
    @Test
    void aShorterPricelessHeaderBuiltFromTheSameNamesIsAccepted() {
        assertEquals(
            SupportedExchange.SRAJTOFLE,
            EverytradeCsvMultiParser.DESCRIPTOR.getSupportedExchange("UID;DATE;SYMBOL;ACTION;QUANTITY;FEE;FEE_CURRENCY")
        );
    }
}
