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

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import me.luzhuo.lib_core.app.base.CoreBaseApplication;
import me.luzhuo.lib_core.app.base.CoreBaseFragment;
import me.luzhuo.lib_core.app.pattern.PatternCheck;
import me.luzhuo.lib_core.app.pattern.RegularType;
import me.luzhuo.lib_core.ui.toast.ToastManager;
import me.luzhuo.lib_file.FileManager;
import me.luzhuo.lib_permission.Permission;
import me.luzhuo.lib_permission.PermissionCallback;
import me.luzhuo.lib_webview.cache.WebViewCacheManager;
import me.luzhuo.lib_webview.callback.ProgressListener;
import me.luzhuo.lib_webview.callback.WebViewEventListener;

/**
 * Description: 基础的 WebView
 * @Author: Luzhuo
 * @Creation Date: 2021/11/27 2:25
 * @Copyright: Copyright 2021 Luzhuo. All rights reserved.
 **/
public class BaseWebViewFragment extends CoreBaseFragment {
    protected @Nullable WebView webView;
    protected WebViewConfig config;
    protected PatternCheck patternCheck = new PatternCheck();
    protected FileManager fileManager = new FileManager(CoreBaseApplication.appContext);
    protected static Handler mainThread = new Handler(Looper.getMainLooper());
    private MutableLiveData<String> urlLiveData = new MutableLiveData<>();

    private ProgressListener progressListener;
    private WebViewEventListener eventListener;

    protected BaseWebViewFragment() { }

    public static BaseWebViewFragment instance(WebViewConfig config) {
        final BaseWebViewFragment fragment = new BaseWebViewFragment();
        Bundle args = new Bundle();
        args.putSerializable("config", config);
        fragment.setArguments(args);
        return fragment;
    }

    public static BaseWebViewFragment instance() {
        final BaseWebViewFragment fragment = new BaseWebViewFragment();
        Bundle args = new Bundle();
        args.putSerializable("config", new WebViewConfig());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate() {
        if (getArguments() != null) {
            config = (WebViewConfig) getArguments().getSerializable("config");
        }
    }

    @Override
    public View initView(LayoutInflater layoutInflater, ViewGroup viewGroup) {
        if (config.canHorizontalScroll) return layoutInflater.inflate(R.layout.webview_fragment_base_horizontal_scroll, viewGroup, false);
        else return layoutInflater.inflate(R.layout.webview_fragment_base_default, viewGroup, false);
    }

    @Override
    public void initData(Bundle bundle) {
        webView = getView().findViewById(R.id.webView_parent);

        WebSettings webSettings = webView.getSettings();
        // 启动JS
        webSettings.setJavaScriptEnabled(true);
        // H5需要的数据存储 LocalStorage
        webSettings.setDomStorageEnabled(true);
        // 应用缓存的最大值
        webSettings.setAppCacheMaxSize(config.appCacheMaxSize);
        // 是否支持 wide viewport, 为true, 宽度与设备像素无关
        webSettings.setUseWideViewPort(true);
        // 是否允许内容超出页面; 默认false, 缩小内容以适应屏幕宽度; true可以超出屏幕
        webSettings.setLoadWithOverviewMode(config.loadWithOverviewMode);
        // 是否允许访问文件, 默认true
        webSettings.setAllowFileAccess(true);
        // 缓存是否可用, 默认false
        webSettings.setAppCacheEnabled(true);
        // 缓存位置
        webSettings.setAppCachePath(fileManager.getCacheDirectory().getAbsolutePath());
        // 是否允许访问数据库, 默认false
        webSettings.setDatabaseEnabled(true);
        // 缓存模式, 默认LOAD_DEFAULT
        webSettings.setCacheMode(config.cacheMode);
        // 控制页面布局,
        webSettings.setLayoutAlgorithm(config.layoutAlgorithm);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // 自动播放, 无需用户触发(浏览器安全机制)
            webSettings.setMediaPlaybackRequiresUserGesture(!config.mediaAutoPlay);
        }
        // 降低安全模式, 支持http
        if (config.supportHttp) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        // 定位
        if (config.locationEnabled) {
            webSettings.setGeolocationEnabled(true);
            webSettings.setGeolocationDatabasePath(fileManager.getCacheDirectory().getAbsolutePath());
        } else {
            webSettings.setGeolocationEnabled(false);
        }

        // cookie
        CookieSyncManager.createInstance(requireContext());
        CookieManager cookieManager = CookieManager.getInstance();
        // 支持跨域 cookie 读取
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) cookieManager.setAcceptThirdPartyCookies(webView, true);
        // 是否接受 cookie, 默认为true
        cookieManager.setAcceptCookie(true);

        webView.setWebViewClient(webViewClient);
        webView.setWebChromeClient(webChromeClient);
        webView.setDownloadListener(downloadListener);

        // ===

        // 告知子类, WebView已配置完毕, 可以选择继续配置WebView
        initWebView(webView);

        // JS调用由子类处理
        if (eventListener != null) eventListener.addJavascriptInterface(webSettings);
        // UserAgent
        final String newUserAgent = eventListener != null ? eventListener.setUserAgentString(webSettings.getUserAgentString()) : null;
        if (!TextUtils.isEmpty(newUserAgent)) webSettings.setUserAgentString(newUserAgent);

        urlLiveData.observe(this, url -> webView.loadUrl(url));
    }

    /**
     * 加载网页
     * @param url 网页地址
     */
    @CallSuper
    public void loadUrl(String url) {
        urlLiveData.setValue(url);
    }

    protected WebViewClient webViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            boolean isOverrider = eventListener != null && eventListener.shouldOverrideUrlLoading(webView, url);

            // true 子类自己处理, 父类就不处理了
            if (isOverrider) return true;

            // 否则, 执行原有逻辑
            if (patternCheck.check(RegularType.HttpUrl, url)) {
                view.loadUrl(url);
            } else {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                } catch (Exception e) {
                    ToastManager.show("未安装相应的应用");
                }
            }
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (progressListener != null) progressListener.start();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (progressListener != null) progressListener.end(true);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            if (eventListener != null) eventListener.onReceivedError(errorCode, description, failingUrl);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            // 忽略SSL错误
            handler.proceed();
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int statusCode = errorResponse.getStatusCode();
                String description = errorResponse.getReasonPhrase();
                String failingUrl = request.getUrl().toString();
                // Android5.0+ 才调用
                if (eventListener != null) eventListener.onReceivedHttpError(statusCode, description, failingUrl);
            }
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            // 加载页面每个资源的时候调用
        }

        /**
         * 无论是普通的页面请求(GET/POST), 还是页面中的异步请求, 页面中的资源请求, 都会回调这个方法, 给开发者一次拦截请求的机会.
         * 在这个方法里, 可以对静态资源进行拦截, 并使用缓存数据代替;
         * 也可以拦截页面, 使用自己的网络框架来请求数据;
         * 或者WebView免流方案
         *
         * 注意: 在多个不同的子线程运行
         * @param view WebView
         * @param url 请求的网络路径
         * @return WebResourceResponse response = new WebResourceResponse("image/jpg", "utf-8", is);
         */
        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            if (!config.cacheStaticRes) return null;
            return WebViewCacheManager.getInstance().shouldInterceptRequest(url);
        }
    };

    private final WebChromeClient webChromeClient = new WebChromeClient() {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (progressListener != null) {
                progressListener.progress(newProgress);
                if (newProgress > 80) progressListener.end(false);
            }
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            if (eventListener != null) eventListener.onReceivedTitle(title);
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            // 警告框, 默认false, 不处理
            return super.onJsAlert(view, url, message, result);
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
            // 提示框, 默认false, 不处理
            return super.onJsPrompt(view, url, message, defaultValue, result);
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
            // 确认框, 默认false, 不处理
            return super.onJsConfirm(view, url, message, result);
        }

        /**
         * web <input type="file" /> 标签, 选择 本地相册 / 拍照 / 文件 的实现
         * Android4.4+ 系统把 openFileChooser 回调函数去掉了
         * 仅兼容 android5.0+
         */
        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            return BaseWebViewFragment.this.onShowFileChooser(webView, filePathCallback, fileChooserParams);
        }

        /**
         * 获取定位权限
         * api24+ 只为https调用, http直接拒绝
         */
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            Permission.request(BaseWebViewFragment.this, new PermissionCallback() {
                @Override
                public void onRequst(boolean isAllGranted, List<String> denieds, List<String> foreverDenieds) {
                    // 每次的回调都必须给予调用, 否则下次不予提示
                    callback.invoke(origin, true/*true同意使用定位, false拒绝使用定位*/, false/*false每次H5请求时都检查权限, true只检查一次, 无论H5是否请求*/);
                }

                @Override
                public void onGranted() { }
            }, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION);
        }

        @Nullable
        private View mVideoView = null;
        private int mSystemUiVisibility = 0;
        private int mOrientation = 0;
        @Nullable
        private CustomViewCallback mVideoCallback = null;
        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            if (this.mVideoView != null) {
                onHideCustomView();
                return;
            }

            FragmentActivity activity = getActivity();
            if (activity == null) return;

            this.mVideoView = view;
            this.mSystemUiVisibility = activity.getWindow().getDecorView().getSystemUiVisibility();
            this.mOrientation = activity.getRequestedOrientation();
            this.mVideoCallback = callback;

            FrameLayout decorView = (FrameLayout) activity.getWindow().getDecorView();
            decorView.addView(mVideoView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            decorView.setSystemUiVisibility(3846 | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

            // 设置横屏
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        @Override
        public void onHideCustomView() {
            FragmentActivity activity = getActivity();
            if (activity == null) return;
            FrameLayout decorView = (FrameLayout) activity.getWindow().getDecorView();
            decorView.removeView(mVideoView);

            decorView.setSystemUiVisibility(mSystemUiVisibility);
            activity.setRequestedOrientation(mOrientation);

            if (mVideoCallback != null) mVideoCallback.onCustomViewHidden();
            mVideoView = null;
            mVideoCallback = null;
        }
    };

    private DownloadListener downloadListener = new DownloadListener() {
        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
            if (eventListener != null) eventListener.onDownloadStart(url, userAgent, contentDisposition, mimetype, contentLength);
        }
    };

    // ================================================= 可被重写的函数 =================================================

    /**
     * 仅在 Android5.0+ 回调, 小于Android5.0不支持
     */
    protected boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) { return false; }

    /**
     * 继续补充对WebView的设置
     * @param webView
     */
    protected void initWebView(WebView webView) {}
    // ================================================= 可被重写的函数 =================================================

    /**
     * 设置加载进度监听
     */
    public void setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    /**
     * 设置WebView事件监听
     */
    public void setWebEventListener(WebViewEventListener eventListener) {
        this.eventListener = eventListener;
    }

    /**
     * 是否还可返回上一页
     * @return true还可以返回上一页, false不可以返回上一页
     */
    public boolean canGoBack() {
        if (!checkWebViewInitialized()) return false;
        return webView.canGoBack();
    }

    /**
     * 返回上一页
     */
    public void goBack() {
        if (!checkWebViewInitialized()) return;
        webView.goBack();
    }

    /**
     * 获取原始的Url
     */
    public String getOriginalUrl() {
        if (!checkWebViewInitialized()) return "";
        return webView.getOriginalUrl();
    }

    protected boolean checkWebViewInitialized() {
        return webView != null;
    }

    @Override
    public void onDestroy() {
        webView.destroy();
        super.onDestroy();
        webView = null;
    }
}
