package com.onets.wallet.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.onets.core.wallet.Wallet;
import com.onets.wallet.Constants;
import com.onets.wallet.R;
import com.onets.wallet.util.Fonts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates and shows a new passphrase
 * 助记词生成并显示，创建钱包用到
 */
public class SeedFragment extends Fragment {
    private static final Logger log = LoggerFactory.getLogger(SeedFragment.class);
    private static final String TAG = "SeedFragment";

    private WelcomeFragment.Listener listener;
    private boolean hasExtraEntropy = false ;
    private TextView mnemonicView;

    public SeedFragment() { }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_seed, container, false);

        TextView seedFontIcon = view.findViewById(R.id.seed_icon);
        Fonts.setTypeface(seedFontIcon, Fonts.Font.OPENWALLET_FONT_ICONS);

        //next button
        final Button buttonNext = view.findViewById(R.id.button_next);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                log.info("Clicked restore wallet");
                if (listener != null) {
                    listener.onSeedCreated(mnemonicView.getText().toString());
                }
            }
        });
        buttonNext.setEnabled(false);

        //助记词显示
        mnemonicView = (TextView) view.findViewById(R.id.seed);
        generateNewMnemonic();

        //复选框，已经将助记词记录
        final CheckBox backedUpSeed = (CheckBox) view.findViewById(R.id.backed_up_seed);
        backedUpSeed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonNext.setEnabled(isChecked);
            }
        });

        View.OnClickListener generateNewSeedListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateNewMnemonic();
            }
        };

        return view;
    }

    /*生成新的助记词*/
    private void generateNewMnemonic() {
        log.info("Clicked generate a new mnemonic");
        String mnemonic;
        if (hasExtraEntropy) {
            //256位助记词
            mnemonic = Wallet.generateMnemonicString(Constants.SEED_ENTROPY_EXTRA);
            Log.d(TAG, Constants.LOG_LABLE + "generateNewMnemonic:256 mnemonic " + mnemonic);
        } else {
            //192位助记词
            mnemonic = Wallet.generateMnemonicString(Constants.SEED_ENTROPY_DEFAULT);
            Log.d(TAG, Constants.LOG_LABLE + "generateNewMnemonic:192 mnemonic " + mnemonic);
        }
        mnemonicView.setText(mnemonic);
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        try {
            listener = (WelcomeFragment.Listener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement " + WelcomeFragment.Listener.class);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
