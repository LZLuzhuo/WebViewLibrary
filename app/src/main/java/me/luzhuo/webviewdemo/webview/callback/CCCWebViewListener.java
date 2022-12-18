package me.luzhuo.webviewdemo.webview.callback;

import android.webkit.WebView;

public interface CCCWebViewListener {
    /**
     * 标题
     * @param title
     */
    public void onTitle(String title);

    /**
     * 设置UserAgent
     * @param userAgent
     */
    public String onUserAgent(String userAgent);

    /**
     * WebView 初始化完成之后再调用WebView相关的操作, 否则会空指针
     */
    public void onWebViewFinish(WebView webView);
}
