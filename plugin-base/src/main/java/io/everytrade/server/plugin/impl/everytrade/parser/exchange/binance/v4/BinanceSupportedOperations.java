package io.everytrade.server.plugin.impl.everytrade.parser.exchange.binance.v4;

import java.util.ArrayList;
import java.util.List;

public class BinanceSupportedOperations {

    public static final List<String> SUPPORTED_ACCOUNT_TYPES = new ArrayList<>();
    public static final List<String> SUPPORTED_OPERATION_TYPES = new ArrayList<>();

    public static final List<String> BUY_SELL_OPERATION_TYPES = new ArrayList<>();
    public static final List<String> DEPOSIT_WITHDRAWAL_OPERATION_TYPES = new ArrayList<>();
    public static final List<String> FEE_OPERATION_TYPES = new ArrayList<>();
    public static final List<String> REWARD_OPERATION_TYPES = new ArrayList<>();

    public static final List<String> UNSUPPORTED_ACCOUNT_TYPES = new ArrayList<>();
    public static final List<String> UNSUPPORTED_OPERATION_TYPES = new ArrayList<>();

    public static final List<String> WRITE_ORIGINAL_OPERATION_AS_NOTE = new ArrayList<>();

    public static final List<String> CREATE_ONE_ROW_TRANSACTION_WHEN_EXCEPTION = new ArrayList<>();

    public static final List<String> REQUIRED_NEGATIVE_VALUE_TYPES = new ArrayList<>();

    static {
        // supported accounts
        SUPPORTED_ACCOUNT_TYPES.add(BinanceOperationTypeV4.ACCOUNT_TYPE_SPOT.code);
        SUPPORTED_ACCOUNT_TYPES.add(BinanceOperationTypeV4.ACCOUNT_TYPE_CARD.code);
        SUPPORTED_ACCOUNT_TYPES.add(BinanceOperationTypeV4.ACCOUNT_TYPE_EARN.code);
        // supported operations
        SUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_BUY.code);
        SUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_SELL.code);
        SUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_TRANSACTION_RELATED.code);
        SUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_DEPOSIT.code);
        SUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_WITHDRAWAL.code);
        SUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_FEE.code);
        SUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_TRANSACTION_FEE.code);
        SUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_DISTRIBUTION.code);
        SUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_FIAT_DEPOSIT.code);
        SUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_FIAT_WITHDRAWAL.code);
        SUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_CARD_CASHBACK.code);
        SUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_COMMISSION_REBATE.code);
        SUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_LARGE_OTC_TRADING.code);
        SUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_SMALL_ASSETS_EXCHANGE_BNB.code);
        SUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_SIMPLE_EARN_FLEXIBLE_INTEREST.code);
        SUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_BINANCE_CONVERT.code);
        SUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_TRANSACTION_BUY.code);
        SUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_TRANSACTION_SPEND.code);
        SUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_TRANSACTION_REVENUE.code);
        SUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_TRANSACTION_SOLD.code);
        SUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_BUY_CRYPTO.code);
        SUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_STAKING_REWARDS.code);
        SUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_STAKING_REDEMPTION.code);
        SUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_STAKING_PURCHASE.code);
        SUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_CASHBACK_VOUCHER.code);
        SUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_SIMPLE_EARN_FLEXIBLE_REDEMPTION.code);
        SUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_SAVING_DISTRIBUTION.code);
        SUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_SIMPLE_EARN_FLEXIBLE_SUBSCRIPTION.code);
        SUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_BNB_VAULT_REWARDS.code);
        SUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_SIMPLE_EARN_LOCKED_REWARDS.code);
        SUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_FIAT_WITHDRAW.code);
        SUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_C2C_TRANSFER.code);
        SUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_SIMPLE_EARN_LOCKED_REDEMPTION.code);
        SUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_REFERRAL_COMMISSION.code);
        SUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_ETH2_0_STAKING_REWARDS.code);
        SUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_ETH2_0_STAKING.code);
        SUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_AIRDROP_ASSETS.code);
        SUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_BINANCE_CARD_SPENDING.code);

        CREATE_ONE_ROW_TRANSACTION_WHEN_EXCEPTION.add(BinanceOperationTypeV4.OPERATION_TYPE_FEE.code);
        CREATE_ONE_ROW_TRANSACTION_WHEN_EXCEPTION.add(BinanceOperationTypeV4.OPERATION_TYPE_TRANSACTION_FEE.code);
        CREATE_ONE_ROW_TRANSACTION_WHEN_EXCEPTION.add(BinanceOperationTypeV4.OPERATION_TYPE_BUY.code);
        CREATE_ONE_ROW_TRANSACTION_WHEN_EXCEPTION.add(BinanceOperationTypeV4.OPERATION_TYPE_SELL.code);

        BUY_SELL_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_BUY.code);
        BUY_SELL_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_SELL.code);
        BUY_SELL_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_TRANSACTION_RELATED.code);

        DEPOSIT_WITHDRAWAL_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_DEPOSIT.code);
        DEPOSIT_WITHDRAWAL_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_WITHDRAWAL.code);

        FEE_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_FEE.code);
        FEE_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_TRANSACTION_FEE.code);

        REWARD_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_DISTRIBUTION.code);
        REWARD_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_STAKING_REWARDS.code);
        REWARD_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_STAKING_REDEMPTION.code);
        REWARD_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_STAKING_PURCHASE.code);

        // Write original operation
        WRITE_ORIGINAL_OPERATION_AS_NOTE.add(BinanceOperationTypeV4.OPERATION_TYPE_SMALL_ASSETS_EXCHANGE_BNB.code);
        WRITE_ORIGINAL_OPERATION_AS_NOTE.add(BinanceOperationTypeV4.OPERATION_TYPE_LARGE_OTC_TRADING.code);
        WRITE_ORIGINAL_OPERATION_AS_NOTE.add(BinanceOperationTypeV4.OPERATION_TYPE_SIMPLE_EARN_FLEXIBLE_INTEREST.code);
        WRITE_ORIGINAL_OPERATION_AS_NOTE.add(BinanceOperationTypeV4.OPERATION_TYPE_BINANCE_CONVERT.code);
        WRITE_ORIGINAL_OPERATION_AS_NOTE.add(BinanceOperationTypeV4.OPERATION_TYPE_TRANSACTION_SPEND.code);
        WRITE_ORIGINAL_OPERATION_AS_NOTE.add(BinanceOperationTypeV4.OPERATION_TYPE_TRANSACTION_BUY.code);
        WRITE_ORIGINAL_OPERATION_AS_NOTE.add(BinanceOperationTypeV4.OPERATION_TYPE_TRANSACTION_REVENUE.code);
        WRITE_ORIGINAL_OPERATION_AS_NOTE.add(BinanceOperationTypeV4.OPERATION_TYPE_TRANSACTION_SOLD.code);
        WRITE_ORIGINAL_OPERATION_AS_NOTE.add(BinanceOperationTypeV4.OPERATION_TYPE_BUY_CRYPTO.code);
        WRITE_ORIGINAL_OPERATION_AS_NOTE.add(BinanceOperationTypeV4.OPERATION_TYPE_STAKING_REWARDS.code);
        WRITE_ORIGINAL_OPERATION_AS_NOTE.add(BinanceOperationTypeV4.OPERATION_TYPE_STAKING_REDEMPTION.code);
        WRITE_ORIGINAL_OPERATION_AS_NOTE.add(BinanceOperationTypeV4.OPERATION_TYPE_STAKING_PURCHASE.code);
        WRITE_ORIGINAL_OPERATION_AS_NOTE.add(BinanceOperationTypeV4.OPERATION_TYPE_CASHBACK_VOUCHER.code);
        WRITE_ORIGINAL_OPERATION_AS_NOTE.add(BinanceOperationTypeV4.OPERATION_TYPE_SIMPLE_EARN_FLEXIBLE_REDEMPTION.code);
        WRITE_ORIGINAL_OPERATION_AS_NOTE.add(BinanceOperationTypeV4.OPERATION_TYPE_SAVING_DISTRIBUTION.code);
        WRITE_ORIGINAL_OPERATION_AS_NOTE.add(BinanceOperationTypeV4.OPERATION_TYPE_SIMPLE_EARN_FLEXIBLE_SUBSCRIPTION.code);
        WRITE_ORIGINAL_OPERATION_AS_NOTE.add(BinanceOperationTypeV4.OPERATION_TYPE_BNB_VAULT_REWARDS.code);
        WRITE_ORIGINAL_OPERATION_AS_NOTE.add(BinanceOperationTypeV4.OPERATION_TYPE_SIMPLE_EARN_LOCKED_REWARDS.code);
        WRITE_ORIGINAL_OPERATION_AS_NOTE.add(BinanceOperationTypeV4.OPERATION_TYPE_COMMISSION_REBATE.code);
        WRITE_ORIGINAL_OPERATION_AS_NOTE.add(BinanceOperationTypeV4.OPERATION_TYPE_CARD_CASHBACK.code);
        WRITE_ORIGINAL_OPERATION_AS_NOTE.add(BinanceOperationTypeV4.OPERATION_TYPE_FIAT_WITHDRAW.code);
        WRITE_ORIGINAL_OPERATION_AS_NOTE.add(BinanceOperationTypeV4.OPERATION_TYPE_C2C_TRANSFER.code);
        WRITE_ORIGINAL_OPERATION_AS_NOTE.add(BinanceOperationTypeV4.OPERATION_TYPE_SIMPLE_EARN_LOCKED_REDEMPTION.code);
        WRITE_ORIGINAL_OPERATION_AS_NOTE.add(BinanceOperationTypeV4.OPERATION_TYPE_REFERRAL_COMMISSION.code);

        //unsupported accounts
        UNSUPPORTED_ACCOUNT_TYPES.add(BinanceOperationTypeV4.ACCOUNT_TYPE_CROSS_MARGIN.code);
        UNSUPPORTED_ACCOUNT_TYPES.add(BinanceOperationTypeV4.ACCOUNT_TYPE_ISOLATED_MARGIN.code);
        UNSUPPORTED_ACCOUNT_TYPES.add(BinanceOperationTypeV4.ACCOUNT_TYPE_P2P.code);
        UNSUPPORTED_ACCOUNT_TYPES.add(BinanceOperationTypeV4.ACCOUNT_TYPE_POOL.code);
        UNSUPPORTED_ACCOUNT_TYPES.add(BinanceOperationTypeV4.ACCOUNT_TYPE_USDT_FUTURES.code);

        //unsupported operations
        UNSUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_MARGIN_LOAN.code);
        UNSUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_MARGIN_REPAYMENT.code);
        UNSUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_BNB_DEDUCT_FEE.code);
        UNSUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_ISOLATED_MARGIN_LOAN.code);
        UNSUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_ISOLATED_MARGIN_REPAYMENT.code);
        UNSUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_TRANSFER_IN.code);
        UNSUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_TRANSFER_OUT.code);
        UNSUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_P2P_TRADING.code);
        UNSUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_POOL_DISTRIBUTION.code);
        UNSUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_BNB_DEDUCTS_FEE.code);
        UNSUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_COMMISSION_FEE_SHARED_WITH_YOU.code);
        UNSUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_COMMISSION_HISTORY.code);
        UNSUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_DISTRIBUTION.code);
        UNSUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_LAUNCHPOOL_INTEREST.code);
        UNSUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_LIQUID_SWAP_ADD.code);
        UNSUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_POS_SAVINGS_INTEREST.code);
        UNSUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_POS_SAVINGS_PURCHASE.code);
        UNSUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_POS_SAVINGS_REDEMPTION.code);
        UNSUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_SAVINGS_INTEREST.code);
        UNSUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_SAVINGS_PRINCIPAL_REDEMPTION.code);
        UNSUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_SAVINGS_PURCHASE.code);
        UNSUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_SUPER_BNB_MINING.code);
        UNSUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_FUNDING_FEE.code);
        UNSUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_INSURANCE_FUND_COMPENSATION.code);
        UNSUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_REALIZE_PROFIT_AND_LOSS.code);
        UNSUPPORTED_OPERATION_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_REFEREE_REBATES.code);

        REQUIRED_NEGATIVE_VALUE_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_FIAT_WITHDRAW.code);
        REQUIRED_NEGATIVE_VALUE_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_C2C_TRANSFER.code);
        REQUIRED_NEGATIVE_VALUE_TYPES.add(BinanceOperationTypeV4.OPERATION_TYPE_BINANCE_CARD_SPENDING.code);

    }
}
