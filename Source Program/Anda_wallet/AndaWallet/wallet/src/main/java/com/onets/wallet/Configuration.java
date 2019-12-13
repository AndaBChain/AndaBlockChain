package com.onets.wallet;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.text.format.DateUtils;

import com.google.common.collect.ImmutableMap;
import com.onets.core.coins.CoinType;
import com.onets.core.coins.Value;
import com.onets.wallet.util.WalletUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import javax.annotation.Nullable;

/**
 * Configuration 配置文件
 * @author Yu K.Q.
 * @author Yu K.Q.
 */
public class Configuration {

    public final int lastVersionCode;//最近版本代码

    private final SharedPreferences prefs;//数据存储

    private static final String PREFS_KEY_LAST_VERSION = "last_version";//最近版本
    private static final String PREFS_KEY_LAST_USED = "last_used";//最近使用
    @Deprecated
    private static final String PREFS_KEY_LAST_POCKET = "last_pocket";//最近钱包
    private static final String PREFS_KEY_LAST_ACCOUNT = "last_account";//最近账号

    /* Preference keys. Check also res/xml/preferences.xml */
    public static final String PREFS_KEY_BTC_PRECISION = "btc_precision";//比特币精度
    public static final String PREFS_KEY_CONNECTIVITY_NOTIFICATION = "connectivity_notification";
    public static final String PREFS_KEY_EXCHANGE_CURRENCY = "exchange_currency";//货币兑换
    public static final String PREFS_KEY_FEES = "fees";//费用
    public static final String PREFS_KEY_DISCLAIMER = "disclaimer";
    public static final String PREFS_KEY_SELECTED_ADDRESS = "selected_address";

    private static final String PREFS_KEY_LABS_QR_PAYMENT_REQUEST = "labs_qr_payment_request";

    private static final String PREFS_KEY_CACHED_EXCHANGE_LOCAL_CURRENCY = "cached_exchange_local_currency";
    private static final String PREFS_KEY_CACHED_EXCHANGE_RATES_JSON = "cached_exchange_rates_json";

    private static final String PREFS_KEY_LAST_EXCHANGE_DIRECTION = "last_exchange_direction";
    private static final String PREFS_KEY_CHANGE_LOG_VERSION = "change_log_version";
    public static final String PREFS_KEY_REMIND_BACKUP = "remind_backup";

    public static final String PREFS_KEY_MANUAL_RECEIVING_ADDRESSES = "manual_receiving_addresses";

    public static final String PREFS_KEY_DEVICE_COMPATIBLE = "device_compatible";

    public static final String PREFS_KEY_TERMS_ACCEPTED = "terms_accepted";

    private static final int PREFS_DEFAULT_BTC_SHIFT = 3;
    private static final int PREFS_DEFAULT_BTC_PRECISION = 2;

    private static final Logger log = LoggerFactory.getLogger(Configuration.class);

    public Configuration(final SharedPreferences prefs) {
        this.prefs = prefs;
        this.lastVersionCode = prefs.getInt(PREFS_KEY_LAST_VERSION, 0);
    }

    /**
     * 在SharedPreference上更改监听注册
     * @param listener
     */
    public void registerOnSharedPreferenceChangeListener(final OnSharedPreferenceChangeListener listener) {
        prefs.registerOnSharedPreferenceChangeListener(listener);
    }

    /**
     * 在SharedPreference上更改监听注销
     * @param listener
     */
    public void unregisterOnSharedPreferenceChangeListener(final OnSharedPreferenceChangeListener listener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener);
    }

    /**
     * 更新上一个版本代码
     * @param currentVersionCode
     */
    public void updateLastVersionCode(final int currentVersionCode) {
        if (currentVersionCode != lastVersionCode) {
            prefs.edit().putInt(PREFS_KEY_LAST_VERSION, currentVersionCode).apply();
        }

        if (currentVersionCode > lastVersionCode)
            log.info("detected app upgrade: " + lastVersionCode + " -> " + currentVersionCode);
        else if (currentVersionCode < lastVersionCode)
            log.warn("detected app downgrade: " + lastVersionCode + " -> " + currentVersionCode);

        applyUpdates();
    }

    /**
     * 应用更新
     */
    private void applyUpdates() {
        if (prefs.contains(PREFS_KEY_LAST_POCKET)) {
            prefs.edit().remove(PREFS_KEY_LAST_POCKET).apply();
        }
    }

    public long getLastUsedAgo() {
        final long now = System.currentTimeMillis();

        return now - prefs.getLong(PREFS_KEY_LAST_USED, 0);
    }

    /**
     * 最后一次触摸
     */
    public void touchLastUsed() {
        final long prefsLastUsed = prefs.getLong(PREFS_KEY_LAST_USED, 0);
        final long now = System.currentTimeMillis();
        prefs.edit().putLong(PREFS_KEY_LAST_USED, now).apply();

        log.info("just being used - last used {} minutes ago", (now - prefsLastUsed) / DateUtils.MINUTE_IN_MILLIS);
    }

    /**
     * 获取最后的账户ID
     * @return
     */
    @Nullable
    public String getLastAccountId() {
        return prefs.getString(PREFS_KEY_LAST_ACCOUNT, null);
    }

    /**
     * 最后一次账户ID触摸
     * @param accountId
     */
    public void touchLastAccountId(String accountId) {
        String lastAccountId = prefs.getString(PREFS_KEY_LAST_ACCOUNT, Constants.DEFAULT_COIN.getId());
        if (!lastAccountId.equals(accountId)) {
            prefs.edit().putString(PREFS_KEY_LAST_ACCOUNT, accountId).apply();
            log.info("last used wallet account id: {}", accountId);
        }
    }

    /**
     * 获取费用集
     * @return
     */
    public Map<CoinType, Value> getFeeValues() {
        JSONObject feesJson = getFeesJson();
        ImmutableMap.Builder<CoinType, Value> feesMapBuilder = ImmutableMap.builder();

        for (CoinType type : Constants.SUPPORTED_COINS) {
            Value fee = getFeeFromJson(feesJson, type);
            feesMapBuilder.put(type, fee);
        }

        return feesMapBuilder.build();
    }

    /**
     * 获取费用
     * @param type
     * @return
     */
    public Value getFeeValue(CoinType type) {
        return getFeeFromJson(getFeesJson(), type);
    }

    /**
     * 重置费用
     * @param type
     */
    public void resetFeeValue(CoinType type) {
        JSONObject feesJson = getFeesJson();
        feesJson.remove(type.getId());
        prefs.edit().putString(PREFS_KEY_FEES, feesJson.toString()).apply();

    }

    /**
     * 设置费用
     * @param feeValue
     */
    public void setFeeValue(final Value feeValue) {
        JSONObject feesJson = getFeesJson();
        try {
            feesJson.put(feeValue.type.getId(), feeValue.toUnitsString());
        } catch (JSONException e) {
            // Should not happen
            log.error("Error setting fee value", e);
        }
        prefs.edit().putString(PREFS_KEY_FEES, feesJson.toString()).apply();
    }

    /**
     * 从json获取费用
     * @param feesJson
     * @param type
     * @return
     */
    private Value getFeeFromJson(JSONObject feesJson, CoinType type) {
        String feeStr = feesJson.optString(type.getId());
        if (feeStr.isEmpty()) {
            return type.getDefaultFeeValue();
        } else {
            return Value.valueOf(type, feeStr);
        }
    }

    /**
     * 获取费用--输出json对象
     * @return
     */
    private JSONObject getFeesJson() {
        try {
            return new JSONObject(prefs.getString(PREFS_KEY_FEES, ""));
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    /**
     * Returns the user selected currency. If defaultFallback is set to true it return a default
     * currency is no user selected setting found.
     */
    @Nullable
    public String getExchangeCurrencyCode(boolean useDefaultFallback) {
        String defaultCode = null;
        if (useDefaultFallback) {
            defaultCode = WalletUtils.localeCurrencyCode();
            defaultCode = defaultCode == null ? Constants.DEFAULT_EXCHANGE_CURRENCY : defaultCode;
        }
        return prefs.getString(PREFS_KEY_EXCHANGE_CURRENCY, defaultCode);
    }

    /**
     * Returns the user selected currency or if not set the default
     */
    public String getExchangeCurrencyCode() {
        return getExchangeCurrencyCode(true);
    }

    public void setExchangeCurrencyCode(final String exchangeCurrencyCode) {
        prefs.edit().putString(PREFS_KEY_EXCHANGE_CURRENCY, exchangeCurrencyCode).apply();
    }

    public JSONObject getCachedExchangeRatesJson() {
        try {
            return new JSONObject(prefs.getString(PREFS_KEY_CACHED_EXCHANGE_RATES_JSON, ""));
        } catch (JSONException e) {
            return null;
        }
    }

    public String getCachedExchangeLocalCurrency() {
        return prefs.getString(PREFS_KEY_CACHED_EXCHANGE_LOCAL_CURRENCY, null);
    }

    public void setCachedExchangeRates(String currency, JSONObject exchangeRatesJson) {
        final SharedPreferences.Editor edit = prefs.edit();
        edit.putString(PREFS_KEY_CACHED_EXCHANGE_LOCAL_CURRENCY, currency);
        edit.putString(PREFS_KEY_CACHED_EXCHANGE_RATES_JSON, exchangeRatesJson.toString());
        edit.apply();
    }

    public boolean getLastExchangeDirection() {
        return prefs.getBoolean(PREFS_KEY_LAST_EXCHANGE_DIRECTION, true);
    }

    public void setLastExchangeDirection(final boolean exchangeDirection) {
        prefs.edit().putBoolean(PREFS_KEY_LAST_EXCHANGE_DIRECTION, exchangeDirection).apply();
    }

    public boolean isManualAddressManagement() {
        return prefs.getBoolean(PREFS_KEY_MANUAL_RECEIVING_ADDRESSES, false);
    }

    public void setDeviceCompatible(final boolean isDeviceCompatible) {
        prefs.edit().putBoolean(PREFS_KEY_DEVICE_COMPATIBLE, isDeviceCompatible).apply();
    }

    public boolean isDeviceCompatible() {
        return prefs.getBoolean(PREFS_KEY_DEVICE_COMPATIBLE, false);
    }

    public boolean getTermsAccepted() {
        return prefs.getBoolean(PREFS_KEY_TERMS_ACCEPTED, false);
    }

    public void setTermAccepted(final boolean isTermsAccepted) {
        prefs.edit().putBoolean(PREFS_KEY_TERMS_ACCEPTED, isTermsAccepted).apply();
    }

}
