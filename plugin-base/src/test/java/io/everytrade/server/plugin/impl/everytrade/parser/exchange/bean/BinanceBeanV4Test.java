package io.everytrade.server.plugin.impl.everytrade.parser.exchange.bean;

import io.everytrade.server.model.Currency;
import io.everytrade.server.model.TransactionType;
import io.everytrade.server.plugin.api.parser.FeeRebateImportedTransactionBean;
import io.everytrade.server.plugin.api.parser.ImportedTransactionBean;
import io.everytrade.server.plugin.api.parser.ParseResult;
import io.everytrade.server.plugin.api.parser.TransactionCluster;
import io.everytrade.server.test.TestUtils;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static io.everytrade.server.model.Currency.ADA;
import static io.everytrade.server.model.Currency.BNB;
import static io.everytrade.server.model.Currency.BTC;
import static io.everytrade.server.model.Currency.CZK;
import static io.everytrade.server.model.Currency.DOGE;
import static io.everytrade.server.model.Currency.ETH;
import static io.everytrade.server.model.Currency.EUR;
import static io.everytrade.server.model.Currency.RUNE;
import static io.everytrade.server.model.Currency.SOL;
import static io.everytrade.server.model.Currency.USDC;
import static io.everytrade.server.model.Currency.USDT;
import static io.everytrade.server.model.Currency.UST;
import static io.everytrade.server.model.TransactionType.BUY;
import static io.everytrade.server.model.TransactionType.DEPOSIT;
import static io.everytrade.server.model.TransactionType.EARNING;
import static io.everytrade.server.model.TransactionType.REBATE;
import static io.everytrade.server.model.TransactionType.SELL;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BinanceBeanV4Test {
    public static final String HEADER_CORRECT = "\uFEFFUser_ID,UTC_Time,Account,Operation,Coin,Change,Remark\n";

   @Test
   void testBuyRelated() {
       String row0 = "63676019,2021-01-05 02:37:59,Spot,Transaction Related,EUR,-40.02168000,\n";
       String row1 = "63676019,2021-01-05 02:37:59,Spot,Buy,USDT,16.14000000,\n";
       String row2 = "63676019,2021-01-05 02:37:59,Spot,Buy,USDT,32.38000000,\n";
       String row3 = "63676019,2021-01-05 02:37:59,Spot,Transaction Related,EUR,-19.94904000,\n";
       String row4 = "63676019,2021-01-05 02:37:59,Spot,Buy,USDT,124.61000000,\n";
       String row5 = "63676019,2021-01-05 02:37:59,Spot,Transaction Related,EUR,-154.01796000,\n";
       String row6 = "63676019,2021-01-05 02:38:00,Spot,Buy,USDT,109.98000000,\n";
       String row7 = "63676019,2021-01-05 02:38:00,Spot,Buy,USDT,12.04000000,\n";
       String row8 = "63676019,2021-01-05 02:38:00,Spot,Buy,USDT,12.03000000,\n";
       String row9 = "63676019,2021-01-05 02:38:00,Spot,Transaction Related,EUR,-24.69528000,\n";
       String row10 = "63676019,2021-01-05 02:38:00,Spot,Transaction Related,EUR,-14.86908000,\n";
       String row11 = "63676019,2021-01-05 02:38:00,Spot,Transaction Related,EUR,-135.93528000,\n";
       String row12 = "63676019,2021-01-05 02:38:00,Spot,Buy,USDT,19.98000000,\n";
       String row13 = "63676019,2021-01-05 02:38:00,Spot,Transaction Related,EUR,-14.88144000,\n";
       String row14 = "63676019,2021-01-05 02:38:00,Spot,Fee,EUR,-10.0,\n";
       String row15 = "63676019,2021-01-05 02:38:00,Spot,Fee,EUR,-10.0,\n";

       final String join =
           row0 + row1 + row2 + row3 + row4 + row5 + row6 + row7 + row8 + row9 + row10 + row11 + row12 + row13 + row14 + row15;

       final TransactionCluster actual = ParserTestUtils.getTransactionCluster(HEADER_CORRECT + join);

       final TransactionCluster expected = new TransactionCluster(
           new ImportedTransactionBean(
               null,
               Instant.parse("2021-01-05T02:38:00Z"),
               USDT,
               EUR,
               BUY,
               new BigDecimal("327.1600000000"),
               new BigDecimal("1.2360000000")
           ),
           List.of(
               new FeeRebateImportedTransactionBean(
                   null,
                   Instant.parse("2021-01-05T02:38:00Z"),
                   EUR,
                   EUR,
                   TransactionType.FEE,
                   new BigDecimal("20.0"),
                   EUR
               )
           ));
       TestUtils.testTxs( expected.getRelated().get(0),actual.getRelated().get(0));
       TestUtils.testTxs( expected.getMain(),actual.getMain());
   }

   @Test
   void testSellRelated() {
       String row0 = "63676019,2021-01-05 02:37:59,Spot,Transaction Related,EUR,40.02168000,\n";
       String row1 = "63676019,2021-01-05 02:37:59,Spot,Sell,USDT,-16.14000000,\n";
       String row2 = "63676019,2021-01-05 02:37:59,Spot,Sell,USDT,-32.38000000,\n";
       String row3 = "63676019,2021-01-05 02:37:59,Spot,Transaction Related,EUR,19.94904000,\n";
       String row4 = "63676019,2021-01-05 02:37:59,Spot,Sell,USDT,-124.61000000,\n";
       String row5 = "63676019,2021-01-05 02:37:59,Spot,Transaction Related,EUR,154.01796000,\n";
       String row6 = "63676019,2021-01-05 02:38:00,Spot,Sell,USDT,-109.98000000,\n";
       String row7 = "63676019,2021-01-05 02:38:00,Spot,Sell,USDT,-12.04000000,\n";
       String row8 = "63676019,2021-01-05 02:38:00,Spot,Sell,USDT,-12.03000000,\n";
       String row9 = "63676019,2021-01-05 02:38:00,Spot,Transaction Related,EUR,24.69528000,\n";
       String row10 = "63676019,2021-01-05 02:38:00,Spot,Transaction Related,EUR,14.86908000,\n";
       String row11 = "63676019,2021-01-05 02:38:00,Spot,Transaction Related,EUR,135.93528000,\n";
       String row12 = "63676019,2021-01-05 02:38:00,Spot,Sell,USDT,-19.98000000,\n";
       String row13 = "63676019,2021-01-05 02:38:00,Spot,Transaction Related,EUR,14.88144000,\n";
       String row14 = "63676019,2021-01-05 02:38:00,Spot,Fee,EUR,-10.0,\n";
       String row15 = "63676019,2021-01-05 02:38:00,Spot,Fee,EUR,-10.0,\n";

       final String join =
           row0 + row1 + row2 + row3 + row4 + row5 + row6 + row7 + row8 + row9 + row10 + row11 + row12 + row13 + row14 + row15;

       final TransactionCluster actual = ParserTestUtils.getTransactionCluster(HEADER_CORRECT + join);

       final TransactionCluster expected = new TransactionCluster(
           new ImportedTransactionBean(
               null,
               Instant.parse("2021-01-05T02:38:00Z"),
               USDT,
               EUR,
               SELL,
               new BigDecimal("327.1600000000"),
               new BigDecimal("1.2360000000")
           ),
           List.of(
               new FeeRebateImportedTransactionBean(
                   null,
                   Instant.parse("2021-01-05T02:38:00Z"),
                   EUR,
                   EUR,
                   TransactionType.FEE,
                   new BigDecimal("20.0"),
                   EUR
               )
           ));
       TestUtils.testTxs( expected.getRelated().get(0),actual.getRelated().get(0));
       TestUtils.testTxs( expected.getMain(),actual.getMain());
   }




    @Test
    void testConvertSell() {
        final String row0 = "41438313,2022-01-01 10:56:29,Spot,Sell,ETH,76.65970000,\"\"\n";
        final String row1 = "41438313,2022-01-01 10:56:29,Spot,Fee,BNB,-0.00011168,\"\"\n";
        final String row2 = "41438313,2022-01-01 10:56:29,Spot,Sell,BTC,-13.00000000,\"\"\n";
        final String join = row0 + row1 + row2;

        final TransactionCluster actual = ParserTestUtils.getTransactionCluster(HEADER_CORRECT + join);

        final TransactionCluster expected = new TransactionCluster(
            new ImportedTransactionBean(
                null,
                Instant.parse("2022-01-01T10:56:29Z"),
                ETH,
                BTC,
                BUY,
                new BigDecimal("76.6597000000"),
                new BigDecimal("0.1695806271")
            ),
            List.of(
                new FeeRebateImportedTransactionBean(
                    null,
                    Instant.parse("2022-01-01T10:56:29Z"),
                    BNB,
                    BNB,
                    TransactionType.FEE,
                    new BigDecimal("0.00011168"),
                    BNB
                )
            )
        );
        TestUtils.testTxs( expected.getRelated().get(0),actual.getRelated().get(0));
        TestUtils.testTxs( expected.getMain(),actual.getMain());
    }

    @Test
    void testConvertSellFiatInBase() {
        final String row0 = "41438313,2022-01-01 10:56:29,Spot,Sell,EUR,11106.42,\"\"\n";
        final String row1 = "41438313,2022-01-01 10:56:29,Spot,Fee,EUR,-11.10642,\"\"\n";
        final String row2 = "41438313,2022-01-01 10:56:29,Spot,Sell,USDT,-9820,\"\"\n";
        final String join = row0 + row1 + row2;

        final TransactionCluster actual = ParserTestUtils.getTransactionCluster(HEADER_CORRECT + join);

        final TransactionCluster expected = new TransactionCluster(
            new ImportedTransactionBean(
                null,
                Instant.parse("2022-01-01T10:56:29Z"),
                Currency.USDT,
                EUR,
                SELL,
                new BigDecimal("9820.0000000000"),
                new BigDecimal("1.1310000000")
            ),
            List.of(
                new FeeRebateImportedTransactionBean(
                    null,
                    Instant.parse("2022-01-01T10:56:29Z"),
                    EUR,
                    EUR,
                    TransactionType.FEE,
                    new BigDecimal("11.10642"),
                    EUR
                )
            )
        );
        TestUtils.testTxs( expected.getRelated().get(0),actual.getRelated().get(0));
        TestUtils.testTxs( expected.getMain(),actual.getMain());
    }

    @Test
    void testCardCashback() {
        final String row0 = "\"38065325,2022-02-26 05:48:48,Card,Card Cashback,BNB,0.00066906,\"\"\"\"\"\n";
        final String row1 = "\"38065325,2022-02-26 05:48:48,Card,Card Cashback,BNB,0.00006234,\"\"\"\"\"\n";
        final String join = row0 + row1;

        final List<TransactionCluster> actual = ParserTestUtils.getTransactionClusters(HEADER_CORRECT + join);

        final TransactionCluster expected1 = new TransactionCluster(
            new ImportedTransactionBean(
                null,
                Instant.parse("2022-02-26T05:48:48Z"),
                BNB,
                BNB,
                REBATE,
                new BigDecimal("0.00066906"),
                null,
                "CARD CASHBACK",
                null
            ),
            List.of()
        );
        final TransactionCluster expected2 = new TransactionCluster(
            new ImportedTransactionBean(
                null,
                Instant.parse("2022-02-26T05:48:48Z"),
                BNB,
                BNB,
                REBATE,
                new BigDecimal("0.00006234"),
                null,
                "CARD CASHBACK",
                null
            ),
            List.of()
        );

        TestUtils.testTxs( expected1.getMain(),actual.get(0).getMain());
        TestUtils.testTxs( expected2.getMain(),actual.get(1).getMain());
    }

    @Test
    void testCommissionRebate() {
        final String row0 = "70366274,2022-08-10 13:25:58,Spot,Commission Rebate,SOL,0.00182000,\"\"\n";
        final String row1 = "70366274,2022-08-10 13:25:58,Spot,Commission Rebate,SOL,0.00220000,\"\"\n";
        final String join = row0 + row1;

        final List<TransactionCluster> actual = ParserTestUtils.getTransactionClusters(HEADER_CORRECT + join);

        final TransactionCluster expected1 = new TransactionCluster(
            new ImportedTransactionBean(
                null,
                Instant.parse("2022-08-10T13:25:58Z"),
                SOL,
                SOL,
                REBATE,
                new BigDecimal("0.00182000"),
                null,
                "COMMISSION REBATE",
                null
            ),
            List.of()
        );


        final TransactionCluster expected2 = new TransactionCluster(
            new ImportedTransactionBean(
                null,
                Instant.parse("2022-08-10T13:25:58Z"),
                SOL,
                SOL,
                REBATE,
                new BigDecimal("0.00220000"),
                null,
                "COMMISSION REBATE",
                null
            ),
            List.of()
        );
        TestUtils.testTxs( expected1.getMain(),actual.get(0).getMain());
        TestUtils.testTxs( expected2.getMain(),actual.get(1).getMain());
    }

    @Test
    void testConvertBuy() {
        final String row0 = "41438313,2022-01-01 10:58:15,Spot,Buy,BTC,-5896.73580000,\"\"\n";
        final String row1 = "41438313,2022-01-01 10:58:15,Spot,Fee,BNB,-0.00859660,\"\"\n";
        final String row2 = "41438313,2022-01-01 10:58:15,Spot,Buy,ETH,4477.40000000,\"\"\n";
        final String join = row0 + row1 + row2;

        final TransactionCluster actual = ParserTestUtils.getTransactionCluster(HEADER_CORRECT + join);

        final TransactionCluster expected = new TransactionCluster(
            new ImportedTransactionBean(
                null,
                Instant.parse("2022-01-01T10:58:15Z"),
                ETH,
                BTC,
                BUY,
                new BigDecimal("4477.4000000000"),
                new BigDecimal("1.3170000000")
            ),
            List.of(
                new FeeRebateImportedTransactionBean(
                    null,
                    Instant.parse("2022-01-01T10:58:15Z"),
                    BNB,
                    BNB,
                    TransactionType.FEE,
                    new BigDecimal("0.00859660"),
                    BNB
                )
            )
        );
        TestUtils.testTxs( expected.getRelated().get(0),actual.getRelated().get(0));
        TestUtils.testTxs( expected.getMain(),actual.getMain());
    }

    @Test
    void testLargeOtcTradingBuy() {
        final String row0 = "42138160,2022-01-12 18:00:24,Spot,Large OTC trading,DOGE,481.58400000,\"\"\n";
        final String row1 = "42138160,2022-01-12 18:00:24,Spot,Large OTC trading,USDT,-3000.00000000,\"\"\n";
        final String join = row0 + row1;

        final TransactionCluster actual = ParserTestUtils.getTransactionCluster(HEADER_CORRECT + join);

        final TransactionCluster expected = new TransactionCluster(
            new ImportedTransactionBean(
                null,
                Instant.parse("2022-01-12T18:00:24Z"),
                DOGE,
                USDT,
                BUY,
                new BigDecimal("481.5840000000"),
                new BigDecimal("6.2294428386"),
                "LARGE OTC TRADING",
                null
            ),
            List.of()
        );
        TestUtils.testTxs( expected.getMain(),actual.getMain());
    }

    @Test
    void testLargeOtcTradingBuy1() {
        final String row0 = "70366274,2022-08-08 12:22:18,Spot,Large OTC Trading,USDC,7524.81470366,\"\"\n";
        final String row1 = "70366274,2022-08-08 12:22:18,Spot,Large OTC Trading,USDT,-7523.83706360,\"\"\n";
        final String join = row0 + row1;

        final TransactionCluster actual = ParserTestUtils.getTransactionCluster(HEADER_CORRECT + join);

        final TransactionCluster expected = new TransactionCluster(
            new ImportedTransactionBean(
                null,
                Instant.parse("2022-08-08T12:22:18Z"),
                USDC,
                USDT,
                BUY,
                new BigDecimal("7524.8147036600"),
                new BigDecimal("0.9998700779"),
                "LARGE OTC TRADING",
                null
            ),
            List.of()
        );
        TestUtils.testTxs( expected.getMain(),actual.getMain());
    }

    @Test
    void testLargeOtcTradingSell() {
        final String row0 = "42138160,2022-01-12 18:00:24,Spot,Large OTC trading,USDT,-3000.00000000,\"\"\n";
        final String row1 = "42138160,2022-01-12 18:00:24,Spot,Large OTC trading,DOGE,481.58400000,\"\"\n";
        final String join = row0 + row1;

        final TransactionCluster actual = ParserTestUtils.getTransactionCluster(HEADER_CORRECT + join);

        final TransactionCluster expected = new TransactionCluster(
            new ImportedTransactionBean(
                null,
                Instant.parse("2022-01-12T18:00:24Z"),
                DOGE,
                USDT,
                BUY,
                new BigDecimal("481.5840000000"),
                new BigDecimal("6.2294428386"),
                "LARGE OTC TRADING",
                null
            ),
            List.of()
        );
        TestUtils.testTxs( expected.getMain(),actual.getMain());
    }

    @Test
    void testLargeOtcTradingConvert() {
        final String row0 = "70366274,2022-04-06 23:15:18,Spot,Large OTC Trading,UST,-1557.71867805,\"\"\n";
        final String row1 = "70366274,2022-04-06 23:15:18,Spot,Large OTC Trading,RUNE,14513.31689430,\"\"\n";
        final String join = row0 + row1;

        final TransactionCluster actual = ParserTestUtils.getTransactionCluster(HEADER_CORRECT + join);

        final TransactionCluster expected = new TransactionCluster(
            new ImportedTransactionBean(
                null,
                Instant.parse("2022-04-06T23:15:18Z"),
                RUNE,
                UST,
                BUY,
                new BigDecimal("14513.3168943000"),
                new BigDecimal("0.1073303015"),
                "LARGE OTC TRADING",
                null
            ),
            List.of()
        );
        TestUtils.testTxs( expected.getMain(),actual.getMain());
    }

    @Test
    void testSmallAssetsExchangeBuy() {
        final String row0 = "40360729,2020-11-24 15:44:58,Spot,Small assets exchange BNB,EUR,-0.00111400,\"\"\n";
        final String row1 = "40360729,2020-11-24 15:44:58,Spot,Small assets exchange BNB,BNB,0.00003908,\"\"\n";
        final String row2 = "40360729,2020-11-24 15:44:58,Spot,Small assets exchange BNB,USDT,-0.00350306,\"\"\n";
        final String row3 = "40360729,2020-11-24 15:44:58,Spot,Small assets exchange BNB,BNB,0.00010405,\"\"\n";
        final String join = row0 + row1 + row2 + row3;

        final List<TransactionCluster> actual = ParserTestUtils.getTransactionClusters(HEADER_CORRECT + join);

        final TransactionCluster expected1 = new TransactionCluster(
            new ImportedTransactionBean(
                null,
                Instant.parse("2020-11-24T15:44:58Z"),
                BNB,
                EUR,
                BUY,
                new BigDecimal("0.0000390800"),
                new BigDecimal("28.5056294780"),
                "SMALL ASSETS EXCHANGE BNB",
                null
            ),
            List.of()
        );

        final TransactionCluster expected2 = new TransactionCluster(
            new ImportedTransactionBean(
                null,
                Instant.parse("2020-11-24T15:44:58Z"),
                BNB,
                USDT,
                BUY,
                new BigDecimal("0.0001040500"),
                new BigDecimal("33.6670831331"),
                "SMALL ASSETS EXCHANGE BNB",
                null
                ),
            List.of()
        );
        TestUtils.testTxs( expected1.getMain(),actual.get(0).getMain());
        TestUtils.testTxs( expected2.getMain(),actual.get(1).getMain());
    }

    @Test
    void testSmallAssetsExchangeBuyWrongInputs() {
        final String row0 = "40360729,2020-11-24 15:44:58,Spot,Small assets exchange BNB,EUR,-0.00111400,\"\"\n";
        final String row1 = "40360729,2020-11-24 15:44:58,Spot,Small assets exchange BNB,BNB,0.00003908,\"\"\n";
        final String row2 = "40360729,2020-11-24 15:44:58,Spot,Small assets exchange BNB,USDT,-0.00350306,\"\"\n";
        final String join = row0 + row1 + row2;

        final ParseResult actual = ParserTestUtils.getParseResult(HEADER_CORRECT + join);

        assertEquals( 3,actual.getParsingProblems().size());
        assertEquals( "PARSED_ROW_IGNORED", actual.getParsingProblems().get(0).getParsingProblemType().name());
    }

    @Test
    void testOperationTypeSimpleEarnFlexibleInterest() {
        final String row0 = "40360729,2020-11-28 00:59:18,Earn,Simple Earn Flexible Interest,BTC,3.8E-7,\"\"\n";
        final String row1 = "40360729,2020-11-28 00:59:18,Earn,Simple Earn Flexible Interest,ETH,0.00011628,\"\"\n";
        final String row2 = "40360729,2020-11-29 00:59:53,Earn,Simple Earn Flexible Interest,BTC,3.8E-7,\"\"\n";
        final String join = row0 + row1 + row2;

        final List<TransactionCluster> actual = ParserTestUtils.getTransactionClusters(HEADER_CORRECT + join);

        final TransactionCluster expected0 = new TransactionCluster(
            new ImportedTransactionBean(
                null,
                Instant.parse("2020-11-28T00:59:18Z"),
                BTC,
                BTC,
                EARNING,
                new BigDecimal("0.00000038"),
                null,
                "SIMPLE EARN FLEXIBLE INTEREST",
                null,
                null
            ),
            List.of()
        );

        final TransactionCluster expected1 = new TransactionCluster(
            new ImportedTransactionBean(
                null,
                Instant.parse("2020-11-28T00:59:18Z"),
                ETH,
                ETH,
                EARNING,
                new BigDecimal("0.00011628"),
                null,
                "SIMPLE EARN FLEXIBLE INTEREST",
                null,
                null
            ),
            List.of()
        );

        final TransactionCluster expected2 = new TransactionCluster(
            new ImportedTransactionBean(
                null,
                Instant.parse("2020-11-29T00:59:53Z"),
                BTC,
                BTC,
                EARNING,
                new BigDecimal("0.00000038"),
                null,
                "SIMPLE EARN FLEXIBLE INTEREST",
                null,
                null
            ),
            List.of()
        );

        TestUtils.testTxs( expected0.getMain(),actual.get(1).getMain());
        TestUtils.testTxs( expected1.getMain(),actual.get(0).getMain());
        TestUtils.testTxs( expected2.getMain(),actual.get(2).getMain());
    }

    @Test
    void testBinanceConvert() {
        final String row0 = "132491447,2021-06-10 09:52:01,Spot,Binance Convert,ADA,-10.00000000,\"\"\n";
        final String row1 = "132491447,2021-06-10 09:52:01,Spot,Binance Convert,USDT,15.67270000,\"\"\n";
        final String row2 = "132491447,2021-06-10 09:54:06,Spot,Binance Convert,ADA,-10.00000000,\"\"\n";
        final String row3 = "132491447,2021-06-10 09:54:06,Spot,Binance Convert,USDT,15.71860000,\"\"\n";
        final String row4 = "132491447,2021-06-10 09:54:26,Spot,Binance Convert,EUR,25.81453252,\"\"\n";
        final String row5 = "132491447,2021-06-10 09:54:26,Spot,Binance Convert,USDT,-31.40596026,\"\"\n";

        final String join = row0 + row1 + row2 + row3 + row4 + row5;

        final List<TransactionCluster> actual = ParserTestUtils.getTransactionClusters(HEADER_CORRECT + join);

        final TransactionCluster expected0 = new TransactionCluster(
            new ImportedTransactionBean(
                null,
                Instant.parse("2021-06-10T09:52:01Z"),
                USDT,
                ADA,
                BUY,
                new BigDecimal("15.6727000000"),
                new BigDecimal("0.6380521544"),
                "BINANCE CONVERT",
                null,
                null
            ),
            List.of()
        );

        final TransactionCluster expected1 = new TransactionCluster(
            new ImportedTransactionBean(
                null,
                Instant.parse("2021-06-10T09:54:06Z"),
                USDT,
                ADA,
                BUY,
                new BigDecimal("15.7186000000"),
                new BigDecimal("0.6361889736"),
                "BINANCE CONVERT",
                null,
                null
            ),
            List.of()
        );

        final TransactionCluster expected2 = new TransactionCluster(
            new ImportedTransactionBean(
                null,
                Instant.parse("2021-06-10T09:54:26Z"),
                USDT,
                EUR,
                SELL,
                new BigDecimal("31.4059602600"),
                new BigDecimal("0.8219628474"),
                "BINANCE CONVERT",
                null,
                null
            ),
            List.of()
        );
        TestUtils.testTxs( expected0.getMain(),actual.get(0).getMain());
        TestUtils.testTxs( expected1.getMain(),actual.get(2).getMain());
        TestUtils.testTxs( expected2.getMain(),actual.get(1).getMain());
    }

    @Test
    void testTransactionRelated() {
        final String row0 = "155380140,2021-05-12 18:39:47,Spot,Transaction Related,BTC,0.00083700,\"\"\n";
        final String row1 = "155380140,2021-05-12 18:39:47,Spot,Deposit,CZK,981.00000000,\"\"\n";
        final String row2 = "155380140,2021-05-12 18:39:47,Spot,Transaction Related,CZK,-981.00000000,\"\"\n";
        final String join = row0 + row1 + row2;

        final List<TransactionCluster> actual = ParserTestUtils.getTransactionClusters(HEADER_CORRECT + join);

        final TransactionCluster expected0 = new TransactionCluster(
            new ImportedTransactionBean(
                null,
                Instant.parse("2021-05-12T18:39:47Z"),
                BTC,
                CZK,
                BUY,
                new BigDecimal("0.0008370000"),
                new BigDecimal("1172043.0107526882"),
                "TRANSACTION RELATED",
                null,
                null
            ),
            List.of()
        );

        final TransactionCluster expected1 = new TransactionCluster(
            new ImportedTransactionBean(
                null,
                Instant.parse("2021-05-12T18:39:47Z"),
                CZK,
                CZK,
                DEPOSIT,
                new BigDecimal("981.00000000"),
                null,
                null,
                null,
                null
            ),
            List.of()
        );

        TestUtils.testTxs( expected0.getMain(),actual.get(0).getMain());
        TestUtils.testTxs( expected1.getMain(),actual.get(1).getMain());
    }

}