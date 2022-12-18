package me.luzhuo.webviewdemo.bugs;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import me.luzhuo.lib_core.ui.calculation.UICalculation;

/**
 * 这是一个关于WebView的软键盘系统级BUG,
 * 即软键盘已弹出, 而布局未上移
 * 1. 页面是非全屏模式的情况下，给activity设置adjustPan会失效
 * 2. 页面是全屏模式的情况，adjustPan跟adjustResize都会失效
 * 这里的全屏模式即是页面是全屏的，包括Application或activity使用了Fullscreen主题、使用了『状态色着色』、『沉浸式状态栏』、『Immersive Mode』等等——总之，基本上只要是App自己接管了状态栏的控制，就会产生这种问题。
 *
 * issue 5497
 * 这个BUG从Android1.x时代（2009年）就被报告了，而一直到了如今的Android7.0（2016年）还是没有修复……
 *
 * 参考资料:
 * 1. Issue 5497 - android -WebView adjustResize windowSoftInputMode breaks when activity is fullscreen - Android Open Source Project - Issue Tracker - Google Project Hosting
 * 2. https://www.cxymm.net/article/feihaokui/85234483
 *
 * 使用场景:
 * 1. [全屏]模式下的WebView, 在竖屏模式下使用
 * 2. 在 setContentView() 之后使用
 */
public class AndroidBug5497Workaround {

    public static void assistActivity(Activity activity) {
        new AndroidBug5497Workaround(activity);
    }

    private View mChildOfContent;
    private int usableHeightPrevious;
    private FrameLayout.LayoutParams frameLayoutParams;
    private UICalculation ui;

    private AndroidBug5497Workaround(Activity activity) {
        ui = new UICalculation(activity);
        FrameLayout content = (FrameLayout) activity.findViewById(android.R.id.content);
        mChildOfContent = content.getChildAt(0);
        mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                possiblyResizeChildOfContent();
            }
        });
        frameLayoutParams = (FrameLayout.LayoutParams) mChildOfContent.getLayoutParams();
        possiblyResizeChildOfContent();
    }

    private void possiblyResizeChildOfContent() {
        int usableHeightNow = computeUsableHeight();
        if (usableHeightNow != usableHeightPrevious) {
            int usableHeightSansKeyboard = mChildOfContent.getRootView().getHeight();
            int heightDifference = usableHeightSansKeyboard - usableHeightNow;
            if (heightDifference > (usableHeightSansKeyboard / 4)) {
                // keyboard probably just became visible
                frameLayoutParams.height = usableHeightSansKeyboard - heightDifference + ui.getCurrentNavigationBar();
            } else {
                // keyboard probably just became hidden
                frameLayoutParams.height = usableHeightSansKeyboard;
            }
            mChildOfContent.requestLayout();
            usableHeightPrevious = usableHeightNow;
        }
    }

    private int computeUsableHeight() {
        Rect rect = new Rect();
        mChildOfContent.getWindowVisibleDisplayFrame(rect);
        // rect.top其实是状态栏的高度，如果是全屏主题，直接 return rect.bottom就可以了
        return rect.bottom;
        // return (rect.bottom - rect.top);
    }
}