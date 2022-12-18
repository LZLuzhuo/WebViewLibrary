/* Copyright 2021 Luzhuo. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.luzhuo.webviewdemo.lib_base;

import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;

import androidx.fragment.app.FragmentActivity;

import me.luzhuo.lib_core.app.appinfo.AppManager;

/**
 * 状态栏管理
 */
public class StatusBarManager {

    /**
     * 初始化状态栏文字的颜色, 目前只有黑色和白色;
     * 文字颜色的修改, Android6.0才支持
     * 如果是深色主题, 则初始化成白色(默认色);
     * 如果是浅色主题, 则初始化成黑色.
     */
    public void defaultState(FragmentActivity activity) {
        // if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) return;

        final View decorView = activity.getWindow().getDecorView();
        if (!new AppManager().isDarkTheme()) decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    /**
     * 透明的状态栏
     * @param activity
     * @isBlackText 文本是否是黑色的
     */
    public void transparent(FragmentActivity activity, boolean isBlackText) {
        statusBarColorNoStatusBar(activity, Color.TRANSPARENT, isBlackText);
    }

    /**
     * 没有状态栏, 修改状态栏颜色
     */
    public void statusBarColorNoStatusBar(FragmentActivity activity, int color, boolean isBlackText) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) return;

        final Window window = activity.getWindow();
        final View decorView = activity.getWindow().getDecorView();
        if (isBlackText) decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        else decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        window.setStatusBarColor(color);
    }

    /**
     * 有状态栏, 修改状态栏颜色
     */
    public void statusBarColor(FragmentActivity activity, int color, boolean isBlackText) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) return;

        final Window window = activity.getWindow();
        final View decorView = activity.getWindow().getDecorView();
        if (isBlackText) decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        else decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        window.setStatusBarColor(color);
    }
}
