package io.everytrade.server.plugin.impl.everytrade.parser.exchange;

import com.univocity.parsers.common.DataValidationException;
import io.everytrade.server.plugin.impl.everytrade.parser.exchange.binance.v4.BinanceBeanV4A;
import io.everytrade.server.plugin.impl.everytrade.parser.exchange.binance.v4.BinanceSortedGroupV4;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

public class BinanceExchangeSpecificParserV4 extends DefaultUnivocityExchangeSpecificParser implements IExchangeSpecificParser,
    IMultiExchangeSpecificParser<BinanceBeanV4A> {

    private static final long TRANSACTION_MERGE_TOLERANCE_MS = 1000;
    public BinanceExchangeSpecificParserV4(Class<? extends ExchangeBean> exchangeBean, String delimiter, boolean isRowInsideQuotes) {
        super(exchangeBean, delimiter, null, isRowInsideQuotes);
    }

    List<BinanceBeanV4A> rows;
    List<BinanceBeanV4A> unSupportedRows = new ArrayList<>();

    public List<? extends ExchangeBean> convertMultipleRowsToTransactions(List<BinanceBeanV4A> rows) {
        List<BinanceBeanV4A> result;
        rows = setRowsWithIds(rows);
        this.rows = rows;
        var groupedRowsByTime = createGroupsFromRows(rows);
        Map<Instant, List<BinanceBeanV4A>> sortedGroupsByDate = new TreeMap<>(groupedRowsByTime);
        // merging rows nearly in the same time
        var mergedGroups = mergeGroupsInTimeWithinTolerance(sortedGroupsByDate);
        // clean groups of rows from unsupported rubbish
        var cleanGroups = removeGroupsWithUnsupportedRows(mergedGroups);
        // creating transaction
        List<BinanceBeanV4A> rowsReadyForTxs = createTransactionFromGroupOfRows(cleanGroups);
        result = rowsReadyForTxs;
        unSupportedRows.stream().forEach(r -> {
            r.setRowNumber((long) r.getRowId());
        });
        result.addAll(unSupportedRows);
        return rowsReadyForTxs;
    }

    @Override
    public Map<?, List<BinanceBeanV4A>> removeGroupsWithUnsupportedRows(Map<?, List<BinanceBeanV4A>> rowGroups) {
        Map<Object, List<BinanceBeanV4A>> result = new HashMap<>();
        for (Map.Entry<?, List<BinanceBeanV4A>> entry : rowGroups.entrySet()) {
            var rowsInGroup = entry.getValue();
            List<BinanceBeanV4A> unSupportedRow = rowsInGroup.stream()
                .filter(r -> r.isUnsupportedRow() == true)
                .collect(Collectors.toList());
            var isOneOrMoreUnsupportedRows =
                !unSupportedRow.isEmpty();
            if (!isOneOrMoreUnsupportedRows) {
                result.put(entry.getKey(), entry.getValue());
            } else {
                var mess = unSupportedRow.get(0).getMessage();
                var ids = rowsInGroup.stream().map(r -> r.getRowId()).collect(Collectors.toList());
                var s = BinanceSortedGroupV4.parseIds(ids);
                setRowsAsUnsupported(rowsInGroup, "One or more rows in group " + "\"rows:" + s + "\" is unsupported;" + mess);
            }
        }
        return result;
    }

    private void setRowsAsUnsupported(List<BinanceBeanV4A> rowsInGroup, String message) {
        rowsInGroup.forEach(r -> {
            r.setMessage(message);
            r.setUnsupportedRow(true);
        });
        unSupportedRows.addAll(rowsInGroup);
    }

    private List<BinanceBeanV4A> setRowsWithIds(List<BinanceBeanV4A> rows) {
        int i = 1;
        for (BinanceBeanV4A row : rows) {
            i++;
            row.setRowId(i);
        }
        return rows;
    }

    private Map<Instant, List<BinanceBeanV4A>> mergeGroupsInTimeWithinTolerance(Map<Instant, List<BinanceBeanV4A>> groups) {
        Map<Instant, List<BinanceBeanV4A>> result = new HashMap<>();
        Instant previousKey = Instant.EPOCH;
        List<BinanceBeanV4A> previousValues = new ArrayList<>();
        for (Map.Entry<Instant, List<BinanceBeanV4A>> entry : groups.entrySet()) {
            var currentKey = entry.getKey();
            var currentValues = entry.getValue();
            if ((currentKey.minusMillis(TRANSACTION_MERGE_TOLERANCE_MS).equals(previousKey)
                || currentKey.minusMillis(TRANSACTION_MERGE_TOLERANCE_MS).isBefore(previousKey))) {
                List<BinanceBeanV4A> all = currentValues;
                all.addAll(previousValues);
                all.forEach(r -> {
                    r.setMergedWithAnotherGroup(true);
                });
                result.put(previousKey, all);
            } else {
                result.put(currentKey, currentValues);
            }
            previousKey = currentKey;
            previousValues = currentValues;
        }
        return result;
    }

    @Override
    public List<BinanceBeanV4A> createTransactionFromGroupOfRows(Map<?, List<BinanceBeanV4A>> groups) {
        List<BinanceBeanV4A> result = new ArrayList<>();
        for (Map.Entry<?, List<BinanceBeanV4A>> entry : groups.entrySet()) {
            var sortedGroup = new BinanceSortedGroupV4();
            sortedGroup.setTime(entry.getKey());
            var rows = entry.getValue();
            try {
                sortedGroup.sortGroup(rows);
                result.addAll(sortedGroup.createdTransactions);
            } catch (DataValidationException e) {
                var eMess = e.getMessage();
                var ids = rows.stream().map(r -> r.getRowId()).collect(Collectors.toList());
                var s = BinanceSortedGroupV4.parseIds(ids);
                setRowsAsUnsupported(rows, "One or more rows in group " + "( rows: " + s + ") is unsupported;" + " " + eMess);
            }
        }
        return result;
    }

    @Override
    public Map<Instant,List<BinanceBeanV4A>> createGroupsFromRows(List<BinanceBeanV4A> rows) {
        return rows.stream().collect(groupingBy(BinanceBeanV4A::getDate));
    }

}
