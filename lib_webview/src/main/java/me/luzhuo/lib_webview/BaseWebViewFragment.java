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
 * Description: ????????? WebView
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
        // ??????JS
        webSettings.setJavaScriptEnabled(true);
        // H5????????????????????? LocalStorage
        webSettings.setDomStorageEnabled(true);
        // ????????????????????????
        webSettings.setAppCacheMaxSize(config.appCacheMaxSize);
        // ???????????? wide viewport, ???true, ???????????????????????????
        webSettings.setUseWideViewPort(true);
        // ??????????????????????????????; ??????false, ?????????????????????????????????; true??????????????????
        webSettings.setLoadWithOverviewMode(config.loadWithOverviewMode);
        // ????????????????????????, ??????true
        webSettings.setAllowFileAccess(true);
        // ??????????????????, ??????false
        webSettings.setAppCacheEnabled(true);
        // ????????????
        webSettings.setAppCachePath(fileManager.getCacheDirectory().getAbsolutePath());
        // ???????????????????????????, ??????false
        webSettings.setDatabaseEnabled(true);
        // ????????????, ??????LOAD_DEFAULT
        webSettings.setCacheMode(config.cacheMode);
        // ??????????????????,
        webSettings.setLayoutAlgorithm(config.layoutAlgorithm);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // ????????????, ??????????????????(?????????????????????)
            webSettings.setMediaPlaybackRequiresUserGesture(!config.mediaAutoPlay);
        }
        // ??????????????????, ??????http
        if (config.supportHttp) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        // ??????
        if (config.locationEnabled) {
            webSettings.setGeolocationEnabled(true);
            webSettings.setGeolocationDatabasePath(fileManager.getCacheDirectory().getAbsolutePath());
        } else {
            webSettings.setGeolocationEnabled(false);
        }

        // cookie
        CookieSyncManager.createInstance(requireContext());
        CookieManager cookieManager = CookieManager.getInstance();
        // ???????????? cookie ??????
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) cookieManager.setAcceptThirdPartyCookies(webView, true);
        // ???????????? cookie, ?????????true
        cookieManager.setAcceptCookie(true);

        webView.setWebViewClient(webViewClient);
        webView.setWebChromeClient(webChromeClient);
        webView.setDownloadListener(downloadListener);

        // ===

        // ????????????, WebView???????????????, ????????????????????????WebView
        initWebView(webView);

        // JS?????????????????????
        if (eventListener != null) eventListener.addJavascriptInterface(webSettings);
        // UserAgent
        final String newUserAgent = eventListener != null ? eventListener.setUserAgentString(webSettings.getUserAgentString()) : null;
        if (!TextUtils.isEmpty(newUserAgent)) webSettings.setUserAgentString(newUserAgent);

        urlLiveData.observe(this, url -> webView.loadUrl(url));
    }

    /**
     * ????????????
     * @param url ????????????
     */
    @CallSuper
    public void loadUrl(String url) {
        urlLiveData.setValue(url);
    }

    protected WebViewClient webViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            boolean isOverrider = eventListener != null && eventListener.shouldOverrideUrlLoading(webView, url);

            // true ??????????????????, ?????????????????????
            if (isOverrider) return true;

            // ??????, ??????????????????
            if (patternCheck.check(RegularType.HttpUrl, url)) {
                view.loadUrl(url);
            } else {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                } catch (Exception e) {
                    ToastManager.show("????????????????????????");
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
            // ??????SSL??????
            handler.proceed();
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int statusCode = errorResponse.getStatusCode();
                String description = errorResponse.getReasonPhrase();
                String failingUrl = request.getUrl().toString();
                // Android5.0+ ?????????
                if (eventListener != null) eventListener.onReceivedHttpError(statusCode, description, failingUrl);
            }
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            // ???????????????????????????????????????
        }

        /**
         * ??????????????????????????????(GET/POST), ??????????????????????????????, ????????????????????????, ????????????????????????, ???????????????????????????????????????.
         * ??????????????????, ?????????????????????????????????, ???????????????????????????;
         * ?????????????????????, ??????????????????????????????????????????;
         * ??????WebView????????????
         *
         * ??????: ?????????????????????????????????
         * @param view WebView
         * @param url ?????????????????????
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
            // ?????????, ??????false, ?????????
            return super.onJsAlert(view, url, message, result);
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
            // ?????????, ??????false, ?????????
            return super.onJsPrompt(view, url, message, defaultValue, result);
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
            // ?????????, ??????false, ?????????
            return super.onJsConfirm(view, url, message, result);
        }

        /**
         * web <input type="file" /> ??????, ?????? ???????????? / ?????? / ?????? ?????????
         * Android4.4+ ????????? openFileChooser ?????????????????????
         * ????????? android5.0+
         */
        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            return BaseWebViewFragment.this.onShowFileChooser(webView, filePathCallback, fileChooserParams);
        }

        /**
         * ??????????????????
         * api24+ ??????https??????, http????????????
         */
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            Permission.request(BaseWebViewFragment.this, new PermissionCallback() {
                @Override
                public void onRequst(boolean isAllGranted, List<String> denieds, List<String> foreverDenieds) {
                    // ????????????????????????????????????, ????????????????????????
                    callback.invoke(origin, true/*true??????????????????, false??????????????????*/, false/*false??????H5????????????????????????, true???????????????, ??????H5????????????*/);
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

            // ????????????
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

    // ================================================= ????????????????????? =================================================

    /**
     * ?????? Android5.0+ ??????, ??????Android5.0?????????
     */
    protected boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) { return false; }

    /**
     * ???????????????WebView?????????
     * @param webView
     */
    protected void initWebView(WebView webView) {}
    // ================================================= ????????????????????? =================================================

    /**
     * ????????????????????????
     */
    public void setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    /**
     * ??????WebView????????????
     */
    public void setWebEventListener(WebViewEventListener eventListener) {
        this.eventListener = eventListener;
    }

    /**
     * ???????????????????????????
     * @return true????????????????????????, false????????????????????????
     */
    public boolean canGoBack() {
        if (!checkWebViewInitialized()) return false;
        return webView.canGoBack();
    }

    /**
     * ???????????????
     */
    public void goBack() {
        if (!checkWebViewInitialized()) return;
        webView.goBack();
    }

    /**
     * ???????????????Url
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
