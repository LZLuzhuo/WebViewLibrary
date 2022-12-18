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
package me.luzhuo.lib_webview.callback;

import android.webkit.WebSettings;
import android.webkit.WebView;

import me.luzhuo.lib_webview.WebViewManager;

/**
 * Description: WebView的一些事件
 * @Author: Luzhuo
 * @Creation Date: 2021/11/27 2:25
 * @Copyright: Copyright 2021 Luzhuo. All rights reserved.
 **/
public abstract class WebViewEventListener {

    /**
     * 网页标题
     */
    public void onReceivedTitle(String title) { }

    /**
     * 添加JS相互调用
     * Note: JS回调的运行于子线程中
     * @param webSettings
     * @return 默认false, 不是用JS互相调用, 如果启用, 则返回true
     */
    public void addJavascriptInterface(WebSettings webSettings) { }

    /**
     * 设置UserAgent
     * @param userAgent WebView原始的UserAgent
     * @return 修改后的UserAgent, 如果不需要修改, 请返回""/null
     */
    public String setUserAgentString(String userAgent) {
        return null;
    }

    /**
     * 加载Url
     * @return 默认返回false, 需要自定义加载, 则返回true
     */
    public boolean shouldOverrideUrlLoading(WebView view, String url) { return false; }

    /**
     * 加载失败
     * ERROR_HOST_LOOKUP:主机地址错误 ERROR_CONNECT:断网 ERROR_TIMEOUT:连接超时
     *
     * 可以进行异常上报;
     * 监控异常页面, 过期页面, 及时反馈给运营或前端修改;
     * 处理ssl错误时, 遇到不信任的证书可以进行特殊处理, 对本公司的域名放心, 防止进入丑陋的错误证书页面
     *
     * @param errorCode 错误码
     * @param description 错误描述
     * @param failingUrl 错误地址
     */
    public void onReceivedError(int errorCode, String description, String failingUrl) { }

    /**
     * 网页请求错误的状态码
     * Android5.0+ api21+ 才调用
     * @param statusCode 404 / 500 ...
     */
    public void onReceivedHttpError(int statusCode, String description, String failingUrl) { }

    /**
     * 下载文件
     * @param url 路径路径
     * @param userAgent
     * @param contentDisposition
     * @param mimetype
     * @param contentLength
     */
    public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
        WebViewManager.downloadByBrowser(url);
    }
}
