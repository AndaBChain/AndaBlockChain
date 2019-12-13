package com.onets.wallet.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.onets.wallet.R;
import com.onets.wallet.ui.DialogBuilder;

/**
 * 使用条款对话框
 * @author Yu K.Q.
 */
public class TermsOfUseDialog extends DialogFragment {
    private Listener listener;

    public static TermsOfUseDialog newInstance() {
        return new TermsOfUseDialog();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof Listener) {
            listener = (Listener) activity;
        }
    }

    /**
     * 创建钱包后第一次进入交易时显示
     * @param savedInstanceState
     * @return
     */
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //创建dialog生成器 ，并添加标题及内容
        final DialogBuilder builder = new DialogBuilder(getActivity());
        builder.setTitle(R.string.terms_of_service_title);
        builder.setMessage(R.string.terms_of_service);

        if (listener != null) {
            DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (listener != null) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE://同意
                                listener.onTermsAgree();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE://否定
                                listener.onTermsDisagree();
                                break;
                        }
                    }
                    dismissAllowingStateLoss();
                }
            };
            builder.setNegativeButton(R.string.button_disagree, onClickListener);
            builder.setPositiveButton(R.string.button_agree, onClickListener);
        } else {
            builder.setPositiveButton(R.string.button_ok, null);
        }

        return builder.create();
    }

    public interface Listener {
        void onTermsAgree();//同意
        void onTermsDisagree();//不同意
    }
}
