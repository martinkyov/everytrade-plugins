package io.everytrade.server.plugin.impl.everytrade.parser.exchange.binance.v3;

import com.univocity.parsers.common.DataValidationException;
import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import io.everytrade.server.model.Currency;
import io.everytrade.server.model.CurrencyPair;
import io.everytrade.server.plugin.api.parser.ParsingProblem;
import io.everytrade.server.plugin.impl.everytrade.parser.exception.DataIgnoredException;
import io.everytrade.server.plugin.impl.everytrade.parser.exchange.ExchangeBean;
import io.everytrade.server.plugin.impl.everytrade.parser.exchange.IExchangeSpecificParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.everytrade.server.plugin.api.parser.ParsingProblemType.PARSED_ROW_IGNORED;
import static io.everytrade.server.plugin.api.parser.ParsingProblemType.ROW_PARSING_FAILED;

public class BinanceExchangeSpecificParserV3 implements IExchangeSpecificParser {
    private final String delimiter;
    private List<ParsingProblem> parsingProblems = List.of();
    // Currency CODES (Currency.code(), e.g. "1INCH"), NOT enum names (Currency.name(), e.g. "_1INCH"): tickers that
    // start with a digit are not legal Java identifiers, so their enum name is prefixed with '_' and would never
    // match an exchange ticker. Resolution therefore always goes through code()/Currency.fromCode().
    private static final Set<String> CURRENCY_CODES = new HashSet<>();
    // Binance's settlement assets, most-used first. Consulted ONLY to break a tie between two otherwise valid splits
    // of a symbol whose amount columns carry no ticker; never a filter, so a pair quoted in anything else still
    // resolves through the longest-base rule below.
    private static final List<String> QUOTE_PREFERENCE = List.of(
        "USDT", "FDUSD", "USDC", "BUSD", "TUSD", "BTC", "ETH", "BNB", "EUR", "TRY", "BRL", "GBP", "AUD", "JPY",
        "RUB", "UAH", "ZAR", "NGN", "PLN", "RON", "ARS", "CZK", "DAI", "XRP", "DOGE", "TRX", "SOL", "DOT",
        "IDRT", "BIDR", "BVND", "VAI", "PAX", "UST", "AEUR", "COP", "MXN");

    public BinanceExchangeSpecificParserV3(String delimiter) {
        this.delimiter = delimiter;
    }

    static {
        for (Currency currency : Currency.values()) {
            CURRENCY_CODES.add(currency.code());
        }
    }

    @Override
    public List<? extends ExchangeBean> parse(File inputFile) {
        parsingProblems = new ArrayList<>();
        final List<BinanceBeanV3> binanceBeans = new ArrayList<>();

        try (Reader reader = new FileReader(inputFile, StandardCharsets.UTF_8)) {
            final CsvParserSettings csvParserSettings = new CsvParserSettings();
            csvParserSettings.getFormat().setDelimiter(delimiter);
            csvParserSettings.setHeaderExtractionEnabled(false);
            CsvParser parser = new CsvParser(csvParserSettings);
            List<Record> allRecords = parser.parseAllRecords(reader);
            // remove row with header
            if(!allRecords.isEmpty()) {
                allRecords.remove(0);
            }
            for (final Record record : allRecords) {
                String[] values = record.getValues();
                values = correctLinesWithCommaBetweenQuotes(values);
                BinanceBeanV3 binanceBean = parseExchangeBean(values);
                if (binanceBean != null) {
                    binanceBeans.add(binanceBean);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return binanceBeans;
    }

    /**
     * e.g. "2020-05-29 11:13:27,ADABTC,BUY,0.0000067200,""3,813.0000000000ADA"",0.02562336BTC,3.8130000000ADA"
     * @param values
     * @return
     */
    private static String[] correctLinesWithCommaBetweenQuotes(String[] values) {
        try {
            if (values.length > 7) {
                String[] newValues = new String[7];
                int j = 0;
                for (int i = 0; i < values.length; i++) {
                    if (values[i].contains("\"\"")) {
                        newValues[i] = (values[i] + values[i + 1]).replace(",", "").replace("\"", "");
                        i++;
                    } else {
                        newValues[j] = values[i];
                    }
                    j++;
                }
                return newValues;
            } else if (values.length == 1) {
                String text = "";
                var val = values[0];
                var parts = val.split("\"");
                for (int i = 0; i < parts.length; ++i) {
                    if ((i + 1) % 2 == 0) {
                        parts[i] = parts[i].replace(",", "");
                    }
                    text += parts[i];
                }
                values = text.split(",");
            }
        } catch (Exception ignored) {
        }
        return values;
    }

    @Override
    public List<ParsingProblem> getParsingProblems() {
        return parsingProblems;
    }

    private BinanceBeanV3 parseExchangeBean(String[] vals) {
        String row = String.join(",", vals);
        try {
            // Resolve the pair from the AMOUNT columns first (vals[4] "0.01964BTC", vals[5] "348.9898376BUSD"): the
            // ticker glued to each number names its own side of the trade, so it is per-row evidence that cannot be
            // ambiguous. The "Pair" column alone can be, and silently: base+quote concatenations collide, e.g.
            // "BTCBUSD" is both BTC+BUSD and BTCB+USD (394 of the ~1.03M concatenations over the current Currency
            // enum do this). Until ETS-5078 the pair string was looked up in a map built from
            // CurrencyPair.getTradeablePairs(), where a collision simply kept whichever entry the HashSet iteration
            // inserted last - and since Currency is an enum whose hashCode() is the identity hash, that order depends
            // on JVM allocation history. Measured on this fixture: the same "BTCBUSD" resolved to BTCB/USD or to
            // BTC/BUSD in the same build depending only on what had been class-loaded first, so one server restart
            // could import the same file as a different asset.
            //
            // Longest-code-suffix matching is what makes the amount columns safe for digit-leading tickers too:
            // "5.11INCH" ends with both "INCH" and "1INCH", and the longer one is the real ticker (5.1 of 1INCH).
            Currency baseCurrency = extractCurrencyFromEndOrNull(vals[4]);
            Currency quoteCurrency = extractCurrencyFromEndOrNull(vals[5]);
            CurrencyPair currencyPair;
            if (baseCurrency != null && quoteCurrency != null) {
                currencyPair = new CurrencyPair(baseCurrency, quoteCurrency);
            } else {
                // The "Quantity"/"Amount" variant of this export carries bare numbers, so the symbol is all there is.
                currencyPair = resolvePairFromSymbol(vals[1]);
                if (currencyPair == null) {
                    throw new DataValidationException("Could not extract base or quote currency from values");
                }
            }
            // The fee can be paid in a third asset (typically BNB), so it is not part of the pair.
            Currency feeCurrency = extractCurrencyFromEndOrNull(vals[6]);

            return new BinanceBeanV3(
                vals[0], // date
                vals[1], // pair
                vals[2], // type
                vals[4], // filled amount with currency
                vals[5], // total amount with currency
                vals[6], // fee
                feeCurrency,
                currencyPair
            );
        } catch (DataIgnoredException e) {
            parsingProblems.add(
                new ParsingProblem(row, e.getMessage(), PARSED_ROW_IGNORED)
            );
        } catch (Exception e) {
            parsingProblems.add(
                new ParsingProblem(row, e.getMessage(), ROW_PARSING_FAILED)
            );
        }
        return null;
    }

    /**
     * Splits a concatenated Binance symbol such as {@code "1INCHUSDT"} into its two {@link Currency#code()} halves.
     *
     * <p>Used only when the amount columns carry bare numbers and the symbol is therefore the sole evidence. Every
     * split point is tried, so a digit-leading base (1INCH, 1000SATS) is found exactly like any other; no precomputed
     * map of concatenations is involved, because such a map cannot represent an ambiguous key and silently keeps one
     * arbitrary winner.
     *
     * <p>When more than one split is valid, the QUOTE decides, and it is picked from {@link #QUOTE_PREFERENCE} -
     * Binance's settlement assets, most-used first. That is how the exchange actually builds a symbol: the quote
     * comes from a short list while the base is the long tail. "Longest quote wins" looks like the same rule and is
     * not: it reads ETHWBTC as ETH/WBTC (really ETHW/BTC), NEXOUSD as NEX/OUSD (really NEXO/USD) and LUNAEUR as
     * LUN/AEUR (really LUNA/EUR), because a longer ticker happens to end the symbol.
     *
     * <p>If no candidate is quoted in a settlement asset the LONGEST BASE wins, with the base code as the final
     * alphabetical tie-break - arbitrary, but fixed, which is the whole point: the answer may never again depend on
     * hash iteration order. A row whose amount columns name their own currencies never reaches any of this.
     *
     * @return the resolved pair, or {@code null} when no split yields two known currency codes
     */
    private static CurrencyPair resolvePairFromSymbol(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return null;
        }
        String upper = symbol.trim().toUpperCase();
        String bestBase = null;
        String bestQuote = null;
        int bestQuoteRank = Integer.MAX_VALUE;
        for (int split = 1; split < upper.length(); split++) {
            String base = upper.substring(0, split);
            String quote = upper.substring(split);
            if (!CURRENCY_CODES.contains(base) || !CURRENCY_CODES.contains(quote)) {
                continue;
            }
            int rank = QUOTE_PREFERENCE.indexOf(quote);
            if (rank < 0) {
                rank = Integer.MAX_VALUE;
            }
            if (bestBase == null || betterThan(rank, base, bestQuoteRank, bestBase)) {
                bestBase = base;
                bestQuote = quote;
                bestQuoteRank = rank;
            }
        }
        return bestBase == null ? null : new CurrencyPair(Currency.fromCode(bestBase), Currency.fromCode(bestQuote));
    }

    /** A settlement-asset quote beats any other; among equals the longer base wins, then the alphabetically first. */
    private static boolean betterThan(int rank, String base, int bestRank, String bestBase) {
        if (rank != bestRank) {
            return rank < bestRank;
        }
        if (base.length() != bestBase.length()) {
            return base.length() > bestBase.length();
        }
        return base.compareTo(bestBase) < 0;
    }

    /**
     * Resolves the currency a value like "0.00010548BNB" ends with by the longest matching {@link Currency#code()}
     * suffix (longest wins so overlapping codes disambiguate). Returns {@code null} when nothing matches, which the
     * caller treats as "this column names no currency" - an unknown fee coin, or a bare number.
     *
     * <p>A code made only of digits is never matched here. There is exactly one ({@code 00}), and it would turn the
     * trailing zeros of any bare amount into a currency: "0.02000000" ends with "00", so a Binance export whose
     * quantity column carries no ticker would resolve to 00/00 instead of falling through to the symbol. A ticker
     * glued to a number is only recognisable when it has at least one letter, so that is the requirement.
     */
    private static Currency extractCurrencyFromEndOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String upper = value.toUpperCase();
        Currency best = null;
        for (Currency currency : Currency.values()) {
            String code = currency.code();
            if (hasNoLetter(code)) {
                continue;
            }
            if (upper.endsWith(code) && (best == null || code.length() > best.code().length())) {
                best = currency;
            }
        }
        return best;
    }

    private static boolean hasNoLetter(String code) {
        for (int i = 0; i < code.length(); i++) {
            if (Character.isLetter(code.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
