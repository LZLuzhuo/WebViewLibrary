package me.luzhuo.webviewdemo.webview.enums;

import me.luzhuo.webviewdemo.R;

/**
 * HeaderWebView左侧的按钮
 */
public enum WebViewLeftButton {
    /**
     * 返回
     */
    Return(R.mipmap.icon_toolbar_return_w, R.mipmap.icon_toolbar_return),
    /**
     * 关闭
     */
    Close(R.mipmap.icon_toolbar_close_w, R.mipmap.icon_toolbar_close),
    /**
     * 没有设置图标
     */
    None(R.mipmap.icon_toolbar_close_w, R.mipmap.icon_toolbar_close),
    /**
     * 上次记录的图标
     */
    Default(-1, -1);

    public int iconWhite;
    public int iconBlack;

    private WebViewLeftButton(int iconWhite, int iconBlack) {
        this.iconWhite = iconWhite;
        this.iconBlack = iconBlack;
    }
}
