package io.everytrade.server.plugin.impl.everytrade.parser.exchange.srajtofle;

import com.univocity.parsers.annotations.Parsed;
import com.univocity.parsers.common.DataValidationException;
import io.everytrade.server.model.Currency;
import io.everytrade.server.model.CurrencyPair;
import io.everytrade.server.model.TransactionType;
import io.everytrade.server.plugin.api.parser.FeeRebateImportedTransactionBean;
import io.everytrade.server.plugin.api.parser.ImportedTransactionBean;
import io.everytrade.server.plugin.api.parser.TransactionCluster;
import io.everytrade.server.plugin.impl.everytrade.parser.EverytradeCSVParserValidator;
import io.everytrade.server.plugin.impl.everytrade.parser.exchange.ExchangeBean;
import io.everytrade.server.plugin.impl.everytrade.parser.utils.MultiFormatDateParser;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static io.everytrade.server.model.TransactionType.DEPOSIT;
import static io.everytrade.server.model.TransactionType.FEE;
import static io.everytrade.server.model.TransactionType.REBATE;
import static io.everytrade.server.plugin.impl.everytrade.parser.ParserUtils.nullOrZero;
import static java.math.BigDecimal.ZERO;
import static lombok.AccessLevel.PRIVATE;

/**
 * Srajtofle CSV export. The format is the WhaleBooks transaction export minus the
 * {@code UNIT_PRICE} and {@code VOLUME_QUOTE} columns, because Srajtofle keeps no price or
 * quote-volume bookkeeping:
 *
 * <pre>
 * UID;DATE;SYMBOL;ACTION;QUANTITY;FEE;FEE_CURRENCY;ADDRESS_FROM;ADDRESS_TO;NOTE;LABELS;PARTNER;REFERENCE
 * </pre>
 *
 * <p>Consequences of the two missing columns:
 * <ul>
 *   <li>Priceless transaction types (DEPOSIT, WITHDRAWAL, FEE, REBATE, STAKE, UNSTAKE,
 *       STAKING_REWARD, REWARD, EARNING, FORK, AIRDROP) behave exactly as in the WhaleBooks
 *       parser -- they carry no price there either.</li>
 *   <li>Trades (BUY, SELL, INCOMING_PAYMENT, OUTGOING_PAYMENT) are imported with a
 *       {@code null} unit price. The row is accepted rather than rejected so the transaction
 *       history stays complete; the host then values the trade from the rate server the same
 *       way it does for a WhaleBooks row with an empty UNIT_PRICE.</li>
 * </ul>
 *
 * @see io.everytrade.server.plugin.impl.everytrade.parser.exchange.everytrade.EveryTradeBeanV3_2
 */
@FieldDefaults(level = PRIVATE)
@ToString
public class SrajtofleBeanV1 extends ExchangeBean {

    String uid;
    Instant date;
    Currency symbolBase;
    Currency symbolQuote;
    BigDecimal quantity;
    BigDecimal fee = ZERO;
    Currency feeCurrency;
    TransactionType action;
    String note;
    String labels;
    String addressFrom;
    String addressTo;
    String partner;
    String reference;

    @Parsed(field = "UID")
    public void setUid(String value) {
        uid = value;
    }

    @Parsed(field = "DATE")
    public void setDate(String value) {
        date = MultiFormatDateParser.parse(value);
    }

    @Parsed(field = "SYMBOL")
    public void setSymbol(String value) {
        try {
            CurrencyPair symbolParts = EverytradeCSVParserValidator.parseSymbol(value);
            symbolBase = symbolParts.getBase();
            symbolQuote = symbolParts.getQuote();
        } catch (Exception ex) {
            try {
                symbolBase = Currency.fromCode(value);
            } catch (Exception e) {
                throw new DataValidationException("Cannot find transaction currency");
            }
        }
    }

    @Parsed(field = "ACTION")
    public void setAction(String value) {
        if (value == null) {
            action = TransactionType.UNKNOWN;
            return;
        }

        String normalized = value.trim().toUpperCase()
            .replaceAll("[\\s-]+", "_");

        normalized = switch (normalized) {
            case "STAKE_REWARD" -> "STAKING_REWARD";
            case "EARN" -> "EARNING";
            default -> normalized;
        };

        action = ExchangeBean.detectTransactionType(normalized);
    }

    @Parsed(field = "QUANTITY")
    public void setQuantity(String value) {
        quantity = EverytradeCSVParserValidator.parserNumber(value);
    }

    @Parsed(field = "FEE", defaultNullRead = "0")
    public void setFee(String value) {
        fee = EverytradeCSVParserValidator.parserNumber(value);
    }

    @Parsed(field = "FEE_CURRENCY")
    public void setFeeCurrency(String value) {
        feeCurrency = value == null ? null : Currency.fromCode(EverytradeCSVParserValidator.correctCurrency(value));
    }

    @Parsed(field = "ADDRESS_FROM")
    public void setAddressFrom(String value) {
        addressFrom = value;
    }

    @Parsed(field = "ADDRESS_TO")
    public void setAddressTo(String value) {
        addressTo = value;
    }

    @Parsed(field = "NOTE")
    public void setNote(String value) {
        note = value;
    }

    @Parsed(field = "LABELS")
    public void setLabels(String value) {
        labels = value;
    }

    @Parsed(field = "PARTNER")
    public void setPartner(String value) {
        partner = value;
    }

    @Parsed(field = "REFERENCE")
    public void setReference(String value) {
        reference = value;
    }

    @Override
    public TransactionCluster toTransactionCluster() {
        if (symbolBase != null && symbolQuote != null) {
            validateCurrencyPair(symbolBase, symbolQuote, action);
        }
        validatePositivity(quantity, fee);
        validateDate(date);
        validateRelatedTransactionAndMainTransaction(quantity, fee);

        switch (action) {
            case BUY, SELL, INCOMING_PAYMENT, OUTGOING_PAYMENT -> {
                return createBuySellTransactionCluster();
            }
            case DEPOSIT, WITHDRAWAL -> {
                return createDepositOrWithdrawalTxCluster();
            }
            case FEE -> {
                return new TransactionCluster(createFeeTransactionBean(true), List.of());
            }
            case REBATE -> {
                return new TransactionCluster(createRebateTransactionBean(), List.of());
            }
            case STAKE, UNSTAKE, STAKING_REWARD, REWARD, EARNING, FORK, AIRDROP -> {
                try {
                    return createOtherTransactionCluster();
                } catch (Exception e) {
                    throw new DataValidationException(String.format("Wrong transaction type data: %s", e.getMessage()));
                }
            }
            default -> throw new IllegalStateException(String.format("Unsupported transaction type %s.", action));
        }
    }

    /**
     * No UNIT_PRICE and no VOLUME_QUOTE in this format, so the trade is imported priceless.
     */
    private TransactionCluster createBuySellTransactionCluster() {
        if (quantity.compareTo(ZERO) == 0) {
            throw new DataValidationException("Quantity can not be zero.");
        }

        var tx = new ImportedTransactionBean(
            uid,
            date,
            symbolBase,
            symbolQuote,
            action,
            quantity,
            null,
            note,
            getAddress(),
            labels,
            partner,
            reference
        );

        TransactionCluster transactionCluster = new TransactionCluster(tx, getRelatedTxs());
        if (!nullOrZero(fee) && feeCurrency == null) {
            transactionCluster.setFailedFee(1, "Fee currency is null. ");
        }
        return transactionCluster;
    }

    private TransactionCluster createDepositOrWithdrawalTxCluster() {
        var tx = ImportedTransactionBean.createDepositWithdrawal(
            uid,
            date,
            symbolBase,
            symbolQuote,
            action,
            quantity,
            action == DEPOSIT ? addressFrom : addressTo,
            note,
            labels,
            partner,
            reference
        );

        return new TransactionCluster(tx, getRelatedTxs());
    }

    /**
     * A zero-cost-gain row (STAKING_REWARD / AIRDROP / EARNING / REWARD / FORK) may name its valuation
     * currency as "BASE/QUOTE" in SYMBOL. There is still no price column in this format, so only the
     * quote currency travels; the host derives the value from it. STAKE/UNSTAKE keep quote == base --
     * they are transfers between spot and staked, not valued acquisitions.
     */
    private TransactionCluster createOtherTransactionCluster() {
        boolean valued = action.isZeroCostGain() && symbolQuote != null && !symbolQuote.equals(symbolBase);

        var tx = new ImportedTransactionBean(
            uid,
            date,
            symbolBase,
            valued ? symbolQuote : symbolBase,
            action,
            quantity,
            null,
            (note != null && !note.isEmpty()) ? note : null,
            getAddress(),
            labels,
            partner,
            reference
        );
        return new TransactionCluster(tx, getRelatedTxs());
    }

    private List<ImportedTransactionBean> getRelatedTxs() {
        var related = new ArrayList<ImportedTransactionBean>();
        if (!nullOrZero(fee) && feeCurrency != null) {
            related.add(createFeeTransactionBean(false));
        }
        return related;
    }

    private FeeRebateImportedTransactionBean createFeeTransactionBean(boolean unrelated) {
        BigDecimal feeValue = fee;
        if (action == FEE && nullOrZero(feeValue) && quantity != null && quantity.compareTo(ZERO) != 0) {
            feeValue = quantity;
        }

        return new FeeRebateImportedTransactionBean(
            unrelated ? uid : uid + FEE_UID_PART,
            date,
            feeCurrency != null ? feeCurrency : symbolBase,
            feeCurrency != null ? feeCurrency : symbolBase,
            FEE,
            feeValue,
            feeCurrency != null ? feeCurrency : symbolBase,
            unrelated ? note : null,
            getAddress(),
            labels,
            partner,
            reference
        );
    }

    /**
     * A REBATE row carries its amount in QUANTITY -- this format has no dedicated REBATE column.
     */
    private FeeRebateImportedTransactionBean createRebateTransactionBean() {
        return new FeeRebateImportedTransactionBean(
            uid,
            date,
            symbolBase,
            symbolQuote != null ? symbolQuote : symbolBase,
            REBATE,
            quantity,
            symbolBase,
            note,
            getAddress(),
            labels,
            partner,
            reference
        );
    }

    private String getAddress() {
        return addressFrom != null ? addressFrom : addressTo;
    }

    private void validateRelatedTransactionAndMainTransaction(BigDecimal quantity, BigDecimal fee) {
        // main
        if (nullOrZero(quantity) && nullOrZero(fee)) {
            throw new DataValidationException("Row do not contain enough data for transaction");
        }
        // related
        if (quantity != null && ZERO.compareTo(quantity) == 0 && !nullOrZero(fee)) {
            throw new DataValidationException("Related transactions cannot be added to parent transactions with a value of zero.");
        }
    }
}
