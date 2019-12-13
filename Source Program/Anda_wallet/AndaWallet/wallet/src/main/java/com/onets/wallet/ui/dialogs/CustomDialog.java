package com.onets.wallet.ui.dialogs;

/**
 * Created by Hasee on 2018/1/15.
 */
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

import com.onets.wallet.R;

/**
 * 自定义对话框
 */
public class CustomDialog extends Dialog {

    public CustomDialog(Context context) {
        super(context);
    }

    public CustomDialog(Context context, int theme) {
        super(context, theme);
    }

    public static class Builder {
        private Context context;
        private String title;//标题
        private String positiveButtonText;//positive按钮文字
        private String negativeButtonText;//negative按钮文字
        private DialogInterface.OnClickListener positiveButtonClickListener;//positive按钮点击监听事件
        private DialogInterface.OnClickListener negativeButtonClickListener;//negative按钮点击监听事件

        public Builder(Context context) {
            this.context = context;
        }

        /**
         * Set the Dialog title from resource
         * 从资源中设置对话框标题
         * @param title resource ID
         * @return
         */
        public Builder setTitle(int title) {
            this.title = (String) context.getText(title);
            return this;
        }

        /**
         * Set the Dialog title from String
         * 从字符串中设置对话框标题
         * @param title String of input
         * @return
         */
        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        /**
         * Set the positive button resource and it's listener
         * 设置positive按钮资源和它的监听器
         * @param positiveButtonText
         * @return
         */
        public Builder setPositiveButton(int positiveButtonText,
                                         DialogInterface.OnClickListener listener) {
            this.positiveButtonText = (String) context
                    .getText(positiveButtonText);
            this.positiveButtonClickListener = listener;
            return this;
        }

        public Builder setPositiveButton(String positiveButtonText,
                                         DialogInterface.OnClickListener listener) {
            this.positiveButtonText = positiveButtonText;
            this.positiveButtonClickListener = listener;
            return this;
        }

        /**
         * Set the negative button resource and it's listener
         * 设置negative按钮资源和它的监听器
         * @param negativeButtonText
         * @param listener
         * @return
         */
        public Builder setNegativeButton(int negativeButtonText,
                                         DialogInterface.OnClickListener listener) {
            this.negativeButtonText = (String) context
                    .getText(negativeButtonText);
            this.negativeButtonClickListener = listener;
            return this;
        }

        public Builder setNegativeButton(String negativeButtonText,
                                         DialogInterface.OnClickListener listener) {
            this.negativeButtonText = negativeButtonText;
            this.negativeButtonClickListener = listener;
            return this;
        }

        /**
         * create custom dialog
         * 创建自定义对话框
         * @return
         */
        public TokenDialog create() {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // instantiate the dialog with the custom Theme
            // 用定制主题实例化对话框
            final TokenDialog dialog = new TokenDialog(context,R.style.Dialog);
            //建立 or 使用
            View layout = inflater.inflate(R.layout.dialog_create_use_wallet, null);
            dialog.addContentView(layout, new LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            // set the dialog title 设置对话框标题
            ((TextView) layout.findViewById(R.id.title)).setText(title);

            // set the confirm button 确认按钮设置：文字、监听
            if (positiveButtonText != null) {
                ((Button) layout.findViewById(R.id.positiveButton))
                        .setText(positiveButtonText);
                if (positiveButtonClickListener != null) {
                    ((Button) layout.findViewById(R.id.positiveButton))
                            .setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    positiveButtonClickListener.onClick(dialog,
                                            DialogInterface.BUTTON_POSITIVE);
                                }
                            });
                }
            } else {
                // if no confirm button just set the visibility to GONE
                //按钮设为隐藏
                layout.findViewById(R.id.positiveButton).setVisibility(
                        View.GONE);
            }

            // set the cancel button 取消按钮设置
            if (negativeButtonText != null) {
                ((Button) layout.findViewById(R.id.negativeButton))
                        .setText(negativeButtonText);
                if (negativeButtonClickListener != null) {
                    ((Button) layout.findViewById(R.id.negativeButton))
                            .setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    negativeButtonClickListener.onClick(dialog,
                                            DialogInterface.BUTTON_NEGATIVE);
                                }
                            });
                }
            } else {
                // if no confirm button just set the visibility to GONE
                //按钮设为隐藏
                layout.findViewById(R.id.negativeButton).setVisibility(
                        View.GONE);
            }

            dialog.setContentView(layout);
            return dialog;
        }
    }
}
