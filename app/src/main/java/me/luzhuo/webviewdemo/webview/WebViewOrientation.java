package me.luzhuo.webviewdemo.webview;

import me.luzhuo.webviewdemo.R;

/**
 * WebView 方向
 */
public enum WebViewOrientation {
    /**
     * 进入第二页: 从下往上弹出
     * 退出第二页: 从上往下回收
     */
    Bottom2Top(R.anim.base_activity_enter_bottom2top, R.anim.base_activity_normal, R.anim.base_activity_normal, R.anim.base_activity_exit_bottom2top),
    /**
     * 按照系统的默认效果执行
     */
    Default(0, 0, 0, 0);

    public int firstEnterActivityAnimation;
    public int firstExitActivityAnimation;
    public int secondEnterActivityAnimation;
    public int secondExitActivityAnimation;

    private WebViewOrientation(int firstEnterActivityAnimation, int firstExitActivityAnimation, int secondEnterActivityAnimation, int secondExitActivityAnimation) {
        this.firstEnterActivityAnimation = firstEnterActivityAnimation;
        this.firstExitActivityAnimation = firstExitActivityAnimation;
        this.secondEnterActivityAnimation = secondEnterActivityAnimation;
        this.secondExitActivityAnimation = secondExitActivityAnimation;
    }
}
