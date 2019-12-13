package com.onets.wallet.util;

import android.content.res.AssetManager;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import java.util.HashMap;

/**
 * @author Yu K.Q.
 * 字体工具类
 */
public class Fonts {

    private final static HashMap<Font, Typeface> typefaces = new HashMap<Font, Typeface>();

    /**
     * 定义字体
     */
    public enum Font {
        OPENWALLET_FONT_ICONS("fonts/openwallet-font-icons.ttf");

        private final String fontPath;

        private Font(final String path) {
            this.fontPath = path;
        }
    }

    /**
     * 初始化字体
     * @param assets
     */
    public static synchronized void initFonts(AssetManager assets) {
        if (!typefaces.isEmpty()) return; // already initialized

        for (Font font : Font.values()) {
            Typeface typeface = Typeface.createFromAsset(assets, font.fontPath);
            typefaces.put(font, typeface);
        }
    }

    /**
     * 设置类型
     * @param textView
     * @param font
     */
    public static synchronized void setTypeface(View textView, Font font) {
        if (typefaces.containsKey(font) && textView instanceof TextView) {
            ((TextView)textView).setTypeface(typefaces.get(font));
        }
    }

    public static void strikeThrough(TextView textView) {
        textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }
}
