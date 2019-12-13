package com.onets.wallet.wxapi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.onets.wallet.R;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

/**
 * 微信PAY Activity
 */
public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {
	
	private static final String TAG = "WXPayEntryActivity";
	
    private IWXAPI api;

	/**
	 * 创建
	 * @param savedInstanceState
	 */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_result);

//    	api = WXAPIFactory.createWXAPI(this, Constants.WEIXIN_APP_ID);
		api = WXAPIFactory.createWXAPI(this, "wxb4ba3c02aa476ea1", true);
		api.handleIntent(getIntent(), this);
    }

	/**
	 * 新的Intent
	 * @param intent
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
        api.handleIntent(intent, this);
	}

	/**
	 * 请求
	 * @param req
	 */
	@Override
	public void onReq(BaseReq req) {
	}

	/**
	 * 响应
	 * @param resp
	 */
	@Override
	public void onResp(BaseResp resp) {
		Log.d(TAG, "onPayFinish, errCode = " + resp.errCode);

		if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.title_alert);
			builder.setMessage(String.format("微信支付结果：%s", String.valueOf(resp.errCode)));
			builder.show();
		}
	}
}