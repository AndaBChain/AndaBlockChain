package com.onets.wallet.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.onets.wallet.Constants;
import com.onets.wallet.R;
import com.onets.wallet.util.Fonts;
import com.onets.wallet.util.Keyboard;
import com.onets.wallet.util.PasswordQualityChecker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fragment that sets a password
 * 设置密码保护证包，在创建新证包部分
 */
public class SetPasswordFragment extends Fragment {
    private static final Logger log = LoggerFactory.getLogger(SetPasswordFragment.class);
    private static final String TAG = "SetPasswordFragment";

    private Listener listener;
    private boolean isPasswordGood;//密码是否符合要求
    private boolean isPasswordsMatch;//密码是否匹配
    private PasswordQualityChecker passwordQualityChecker;//密码质量检测器
    private EditText password1;//输入密码
    private EditText password2;//确认密码
    private TextView errorPassword;//错误密码
    private TextView errorPasswordsMismatch;//密码不匹配

    public static SetPasswordFragment newInstance(Bundle args) {
        SetPasswordFragment fragment = new SetPasswordFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public SetPasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        passwordQualityChecker = new PasswordQualityChecker(getActivity());
        isPasswordGood = false;
        isPasswordsMatch = false;
    }

    /**
     * 创建视图
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_set_password, container, false);

        Fonts.setTypeface(view.findViewById(R.id.key_icon), Fonts.Font.OPENWALLET_FONT_ICONS);

        errorPassword = (TextView) view.findViewById(R.id.password_error);
        errorPasswordsMismatch = (TextView) view.findViewById(R.id.passwords_mismatch);

        clearError(errorPassword);
        clearError(errorPasswordsMismatch);

        password1 = (EditText) view.findViewById(R.id.password1);
        password2 = (EditText) view.findViewById(R.id.password2);

        password1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View textView, boolean hasFocus) {
                if (hasFocus) {
                    clearError(errorPassword);
                } else {
                    checkPasswordQuality();
                }
            }
        });

        password2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View textView, boolean hasFocus) {
                if (hasFocus) {
                    clearError(errorPasswordsMismatch);
                } else {
                    checkPasswordsMatch();
                }
            }
        });

        // Next button 输入密码后下一个
        Button finishButton = (Button) view.findViewById(R.id.button_next);
        finishButton.setOnClickListener(getOnFinishListener());
        finishButton.setImeOptions(EditorInfo.IME_ACTION_DONE);

        // Skip link 跳过密码
        view.findViewById(R.id.password_skip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialog = SkipPasswordDialogFragment.newInstance(getArguments());
                dialog.show(getFragmentManager(), null);
            }
        });

        return view;
    }

    /*检查密码质量*/
    private void checkPasswordQuality() {
        String pass = password1.getText().toString();
        isPasswordGood = false;
        try {
            passwordQualityChecker.checkPassword(pass);
            isPasswordGood = true;
            clearError(errorPassword);
        } catch (PasswordQualityChecker.PasswordTooCommonException e1) {
            log.info("Entered a too common password {}", pass);
            setError(errorPassword, R.string.password_too_common_error, pass);
        } catch (PasswordQualityChecker.PasswordTooShortException e2) {
            log.info("Entered a too short password");
            setError(errorPassword, R.string.password_too_short_error,
                    passwordQualityChecker.getMinPasswordLength());
        } catch (PasswordQualityChecker.PasswordTooSingleException e) {
            log.info("Entered a too single password");
            setError(errorPassword, R.string.password_too_single_error);
        }
        log.info("Password good = {}", isPasswordGood);
    }

    /*检查密码匹配*/
    private void checkPasswordsMatch() {
        String pass1 = password1.getText().toString();
        String pass2 = password2.getText().toString();
        isPasswordsMatch = pass1.equals(pass2);
        if (!isPasswordsMatch) showError(errorPasswordsMismatch);
        log.info("Passwords match = {}", isPasswordsMatch);
    }

    /*输入密码完成监听*/
    private View.OnClickListener getOnFinishListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Keyboard.hideKeyboard(getActivity());
                checkPasswordQuality();
                checkPasswordsMatch();
                if (isPasswordGood && isPasswordsMatch) {
                    //Bundle args = getArguments();
                    //args.putString(Constants.ARG_PASSWORD, password1.getText().toString());
                    getArguments().putString(Constants.ARG_PASSWORD, password1.getText().toString());
                    Log.d(TAG, "testClick: ARG_PASSWORD " + getArguments().getString(Constants.ARG_PASSWORD));
                    Constants.saveAndaPassword(getContext(), password1.getText().toString());

                    listener.onPasswordSet(getArguments());

                } else {
                    Toast.makeText(SetPasswordFragment.this.getActivity(),
                            R.string.password_error, Toast.LENGTH_LONG).show();
                }
            }
        };
    }

    /*跳过输入密码部分*/
    public static class SkipPasswordDialogFragment extends DialogFragment {
        private Listener mListener;

        public static SkipPasswordDialogFragment newInstance(Bundle args) {
            SkipPasswordDialogFragment dialog = new SkipPasswordDialogFragment();
            dialog.setArguments(args);
            return dialog;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            try {
                mListener = (Listener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString()
                        + " must implement " + Listener.class);
            }
        }

        /*创建dialog，用于跳过密码时提醒*/
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setMessage(getResources().getString(R.string.password_skip_warn))
                    .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                            Keyboard.hideKeyboard(getActivity());
                            getArguments().putString(Constants.ARG_PASSWORD, "");
                            Log.d(TAG, "onClick: ARG_PASSWORD " + getArguments().getString(Constants.ARG_PASSWORD));
                            mListener.onPasswordSet(getArguments());
                        }
                    })
                    .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                        }
                    });
            return builder.create();
        }
    }

    /**
     * 设置错误
     * @param errorView
     * @param messageId
     * @param formatArgs
     */
    private void setError(TextView errorView, int messageId, Object... formatArgs) {
        setError(errorView, getResources().getString(messageId, formatArgs));
    }

    /**
     * 设置错误
     * @param errorView
     * @param message
     */
    private void setError(TextView errorView, String message) {
        errorView.setText(message);
        showError(errorView);
    }

    /**
     * 展示错误
     * @param errorView
     */
    private void showError(TextView errorView) {
        errorView.setVisibility(View.VISIBLE);
    }

    /**
     * 清除错误
     * @param errorView
     */
    private void clearError(TextView errorView) {
        errorView.setVisibility(View.GONE);
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        try {
            listener = (Listener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement " + Listener.class);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    /**
     * 监听接口
     */
    public interface Listener {
        void onPasswordSet(Bundle args);
    }
}
