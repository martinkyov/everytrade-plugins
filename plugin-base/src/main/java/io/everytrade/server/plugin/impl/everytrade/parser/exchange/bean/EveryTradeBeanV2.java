package io.everytrade.server.plugin.impl.everytrade.parser.exchange.bean;

import com.univocity.parsers.annotations.Format;
import com.univocity.parsers.annotations.Headers;
import com.univocity.parsers.annotations.Parsed;
import io.everytrade.server.model.Currency;
import io.everytrade.server.model.TransactionType;
import io.everytrade.server.plugin.api.parser.ImportedTransactionBean;
import io.everytrade.server.plugin.api.parser.TransactionCluster;
import io.everytrade.server.plugin.impl.everytrade.parser.exchange.ExchangeBean;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

@Headers(sequence = {"UID", "DATE", "SYMBOL", "ACTION", "QUANTY", "VOLUME", "FEE"}, extract = true)
public class EveryTradeBeanV2 extends ExchangeBean {
    private String uid;
    private Instant date;
    private Currency symbolBase;
    private Currency symbolQuote;
    private TransactionType action;
    private BigDecimal quantity;
    private BigDecimal volume;
    private BigDecimal fee;

    @Parsed(field = "UID")
    public void setUid(String uid) {
        this.uid = uid;
    }

    @Parsed(field = "DATE")
    @Format(formats = {"dd.MM.yy HH:mm:ss", "yyyy-MM-dd HH:mm:ss"}, options = {"locale=US", "timezone=UTC"})
    public void setDate(Date date) {
        this.date = date.toInstant();
    }

    @Parsed(field = "SYMBOL")
    public void setSymbol(String symbol) {
        String[] symbolParts = symbol.split("/");
        symbolBase = Currency.valueOf(symbolParts[0]);
        symbolQuote = Currency.valueOf(symbolParts[1]);
    }

    @Parsed(field = "ACTION")
    public void setAction(String action) {
        this.action = detectTransactionType(action);
    }

    @Parsed(field = "QUANTY", defaultNullRead = "0")
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    @Parsed(field = "VOLUME", defaultNullRead = "0")
    public void setVolume(BigDecimal volume) {
        this.volume = volume;
    }

    @Parsed(field = "FEE", defaultNullRead = "0")
    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    @Override
    public TransactionCluster toTransactionCluster() {
        //TODO: mcharvat - implement
        return null;
//        validateCurrencyPair(symbolBase, symbolQuote);
//
//        return new ImportedTransactionBean(
//            uid,            //uuid
//            date,           //executed
//            symbolBase,     //base
//            symbolQuote,    //quote
//            action,         //action
//            quantity,       //base quantity
//            evalUnitPrice(volume, quantity),  //unit price
//            fee             //fee quote
//        );
    }
}
