package com.onets.core.exchange.shapeshift.data;

import com.onets.core.coins.CoinType;
import com.onets.core.coins.Value;
import com.onets.core.exchange.shapeshift.ShapeShift;
import com.onets.core.util.ExchangeRate;
import com.onets.core.wallet.AbstractAddress;

import org.json.JSONObject;

import java.util.Date;

/**
 * @author Yu K.Q.
 */
public class ShapeShiftAmountTx extends ShapeShiftBase {
    public final String pair;
    public final AbstractAddress deposit;
    public final Value depositAmount;
    public final AbstractAddress withdrawal;
    public final Value withdrawalAmount;
    public final Date expiration;
    public final ExchangeRate rate;

    public ShapeShiftAmountTx(JSONObject data) throws ShapeShiftException {
        super(data);
        if (!isError) {
            try {
                JSONObject innerData = data.getJSONObject("success");
                pair = innerData.getString("pair");
                CoinType[] coinTypePair = ShapeShift.parsePair(pair);
                CoinType typeDeposit = coinTypePair[0];
                CoinType typeWithdrawal = coinTypePair[1];
                deposit = typeDeposit.newAddress(innerData.getString("deposit"));
                depositAmount = Value.parse(typeDeposit, innerData.getString("depositAmount"));
                withdrawal = typeWithdrawal.newAddress(innerData.getString("withdrawal"));
                withdrawalAmount = Value.parse(typeWithdrawal,
                        innerData.getString("withdrawalAmount"));
                expiration = new Date(innerData.getLong("expiration"));
                rate = new ShapeShiftExchangeRate(typeDeposit, typeWithdrawal,
                        innerData.getString("quotedRate"), innerData.optString("minerFee", null));
            } catch (Exception e) {
                throw new ShapeShiftException("Could not parse object", e);
            }
        } else {
            pair = null;
            deposit = null;
            depositAmount = null;
            withdrawal = null;
            withdrawalAmount = null;
            expiration = null;
            rate = null;
        }
    }
}
