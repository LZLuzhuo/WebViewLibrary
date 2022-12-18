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
package me.luzhuo.lib_webview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.HorizontalScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

/**
 * Description: 解决了 Web 与 原生 水平方向滑动的冲突
 * @Author: Luzhuo
 * @Creation Date: 2021/11/4 0:20
 * @Copyright: Copyright 2021 Luzhuo. All rights reserved.
 **/
public class HorizontalScrollWebView extends WebView {
    public HorizontalScrollWebView(@NonNull Context context) {
        super(context);
    }

    public HorizontalScrollWebView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalScrollWebView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initView();
    }

    private ViewGroup scrollableParent = null;
    private void initView() {
        this.setOverScrollMode(WebView.OVER_SCROLL_IF_CONTENT_SCROLLS);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (scrollableParent == null) scrollableParent = findViewParentIfNeeds(this, 30);
            if (scrollableParent != null) scrollableParent.requestDisallowInterceptTouchEvent(true);
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        if (clampedX) {
            if (scrollableParent == null) scrollableParent = findViewParentIfNeeds(this, 30);
            if (scrollableParent != null) scrollableParent.requestDisallowInterceptTouchEvent(false);
        }
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
    }

    private @Nullable ViewGroup findViewParentIfNeeds(View tag, int depth) {
        if (depth < 0) return null;
        final ViewParent parent = tag.getParent();
        if (parent == null) return null;

        if (parent instanceof ViewGroup) {
            if (canScrollHorizontal((ViewGroup)parent)) return (ViewGroup) parent;
            else return findViewParentIfNeeds((ViewGroup)parent, depth - 1);
        } else return null;
    }

    /**
     * 是否可以横向滑动
     */
    private boolean canScrollHorizontal(View view) {
        boolean isViewPager = (view instanceof ViewPager || view instanceof ViewPager2 || view instanceof AbsListView || view instanceof HorizontalScrollView || view instanceof GridView);
        return isViewPager;
    }
}
