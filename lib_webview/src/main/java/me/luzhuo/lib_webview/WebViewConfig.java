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

import android.webkit.WebSettings;

import java.io.Serializable;

import static android.webkit.WebSettings.LOAD_DEFAULT;
import static android.webkit.WebSettings.LayoutAlgorithm.SINGLE_COLUMN;

/**
 * Description: WebView 的配置
 * @Author: Luzhuo
 * @Creation Date: 2021/11/27 2:25
 * @Copyright: Copyright 2021 Luzhuo. All rights reserved.
 **/
public class WebViewConfig implements Serializable {
    /**
     * 是否支持水平滚动
     * WebView默认不支持水平滚动, 所以把WebView放在ViewPager里, 会与WebView里的滚动事件冲突
     * 如果为true, 则使用 HorizontalScrollWebView, 解决了WebView与WebView里面的控件, 水平方向滚动冲突的问题
     */
    public boolean canHorizontalScroll = false;

    /**
     * 应用缓存的最大值
     */
    public int appCacheMaxSize = Integer.MAX_VALUE;

    /**
     * 是否允许内容超出页面;
     * 默认false, 缩小内容以适应屏幕宽度; true可以超出屏幕
     */
    public boolean loadWithOverviewMode = true;

    /**
     * 缓存模式
     * LOAD_DEFAULT: (默认)根据 cache-control 决定是否从网络上取数据
     * LOAD_CACHE_ELSE_NETWORK: 只要本地有, 无论是否过期, 或者no-cache, 都使用缓存中的数据
     * LOAD_NO_CACHE: 永远不使用缓存, 只从网络获取
     * LOAD_CACHE_ONLY: 只使用缓存, 不使用网络, 即使本地没有资源
     */
    public int cacheMode = LOAD_DEFAULT;

    /**
     * 页面布局
     * NORMAL: 正常显示
     * SINGLE_COLUMN: 把所有内容放大与 WebView 等宽
     * NARROW_COLUMNS: 尽量时所有列的宽度不超过屏幕宽度
     * TEXT_AUTOSIZING: 文字自动设置大小
     */
    public WebSettings.LayoutAlgorithm layoutAlgorithm = SINGLE_COLUMN;

    /**
     * 是否支持Http的链接
     * 如果不支持, 会导致http的图片加载不出来
     * 实现方式, 是通过降低安全模式实现的
     */
    public boolean supportHttp = true;

    /**
     * 是否支持H5的定位请求
     */
    public boolean locationEnabled = true;

    /**
     * 媒体文件是否自动播放
     */
    public boolean mediaAutoPlay = false;

    /**
     * 是否对常见的静态资源文件进行本地缓存, 缓存后, 请求该链接时将不再请求网络, 而是从本地直接读取
     * @see me.luzhuo.lib_webview.cache.WebViewCacheType
     */
    public boolean cacheStaticRes = false;
    
}
