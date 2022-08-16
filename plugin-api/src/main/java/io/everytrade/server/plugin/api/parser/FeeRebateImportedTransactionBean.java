package io.everytrade.server.plugin.api.parser;

import io.everytrade.server.model.Currency;
import io.everytrade.server.model.TransactionType;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

import static lombok.AccessLevel.PRIVATE;

@Getter
@ToString(callSuper = true)
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class FeeRebateImportedTransactionBean extends ImportedTransactionBean {

    Currency feeRebateCurrency;

    public FeeRebateImportedTransactionBean(String uid,
                                            Instant executed,
                                            Currency base,
                                            Currency quote,
                                            TransactionType action,
                                            BigDecimal feeRebate,
                                            Currency feeRebateCurrency) {
        this(uid, executed, base, quote, action, feeRebate, feeRebateCurrency, null);
    }

    public FeeRebateImportedTransactionBean(String uid,
                                            Instant executed,
                                            Currency base,
                                            Currency quote,
                                            TransactionType action,
                                            BigDecimal feeRebate,
                                            Currency feeRebateCurrency,
                                            String note) {
        super(uid, executed, base, quote, action, feeRebate, null, note, null);
        Objects.requireNonNull(this.feeRebateCurrency = feeRebateCurrency);
    }
}
