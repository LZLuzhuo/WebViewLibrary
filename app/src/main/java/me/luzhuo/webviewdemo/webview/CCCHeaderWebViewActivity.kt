package me.luzhuo.webviewdemo.webview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.webkit.WebView
import kotlinx.android.synthetic.main.layout_webview_header.*
import me.luzhuo.lib_core.app.appinfo.AppManager
import me.luzhuo.lib_core.ui.fragment.FragmentManager
import me.luzhuo.lib_core.ui.toast.ToastManager
import me.luzhuo.lib_core_ktx.int
import me.luzhuo.lib_core_ktx.invisible
import me.luzhuo.lib_core_ktx.visible
import me.luzhuo.webviewdemo.R
import me.luzhuo.webviewdemo.lib_base.BaseActivity
import me.luzhuo.webviewdemo.lib_base.StatusBarManager
import me.luzhuo.webviewdemo.webview.callback.CCCWebViewJSListener
import me.luzhuo.webviewdemo.webview.callback.CCCWebViewListener
import me.luzhuo.webviewdemo.webview.enums.WebViewLeftButton
import me.luzhuo.webviewdemo.webview.enums.WebViewLeftEvent
import org.json.JSONObject
import java.lang.Exception

/**
 * 带有JS可控制头的WebView
 * 用来替代之前的[CCCWordWebViewActivity]
 */
class CCCHeaderWebViewActivity : BaseActivity(),
    CCCWebViewListener {
    private val TAG = CCCHeaderWebViewActivity::class.java.simpleName
    private lateinit var fragmentManger : FragmentManager
    private lateinit var fragment: CCCWebViewFragment2
    private var url: String? = null
    private var orientation: WebViewOrientation? = null
    private val statusBarManager = StatusBarManager()

    companion object {
        fun start(context: Context, url: String?) {
            if (TextUtils.isEmpty(url)) {
                ToastManager.show("url为空")
                return
            }
            context.startActivity(Intent(context, CCCHeaderWebViewActivity::class.java).apply {
                putExtra("url", url)
                putExtra("orientation", WebViewOrientation.Default)
            })
        }
    }

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.webview_header)

        url = intent.getStringExtra("url")
        orientation = intent.getSerializableExtra("orientation") as? WebViewOrientation
        if (orientation != WebViewOrientation.Default) overridePendingTransition(orientation?.firstEnterActivityAnimation.int, orientation?.firstExitActivityAnimation.int)

        fragmentManger = FragmentManager(this, R.id.webView_frame)
        fragment = CCCWebViewFragment2.instance()
        fragmentManger.replaceFragment(fragment)
        fragment.setCCCWebViewListener(this)
        fragment.setCCCWebViewJSListener(jsListener)

        fragment.loadUrl(url)

        initView()
    }

    override fun finish() {
        super.finish()
        if (orientation != WebViewOrientation.Default) overridePendingTransition(orientation?.secondEnterActivityAnimation.int, orientation?.secondExitActivityAnimation.int)
    }

    private fun initView() {
        webView_header_menu.setOnClickListener { fragment.showShareDialog() }
        webView_header_back.setOnClickListener {
            when (fragment.currentLeftEvent) {
                WebViewLeftEvent.Return -> {
                    if (fragment.canGoBack()) fragment.goBack()
                    else finish()
                }
                WebViewLeftEvent.Close -> finish()
                else -> finish()
            }
        }
    }

    override fun onTitle(title: String?) {
        webView_header_title.text = title
    }

    override fun onUserAgent(userAgent: String): String {
        try {
            val json = JSONObject().apply {
                put("header", 1) // // 浏览器头  1有    2无
            }
            if (AppManager().isDebug) Log.e(TAG, json.toString())
            return "$userAgent  [${json}]"
        } catch (e: Exception) {
            return userAgent
        }
    }

    override fun onWebViewFinish(webView: WebView?) {
        showReturn()
    }

    private fun showReturn() {
        if (fragment.canGoBack()) {
            // 是否可返回, 如果可返回则使用JS定义的, 否则强制使用自己的
        } else {
            // 不可返回的时候, 系统的优先级最高
            // 如果是最后一页, 并且JS给的有文字, 那么就将其强制改为关闭, 事件强制改为关闭, 图标保持上级的图标
            if (!TextUtils.isEmpty(fragment.currentLeftStr)) updateHeaderView(WebViewLeftButton.Default, "关闭")
        }
    }

    private val jsListener = object: CCCWebViewJSListener(){
        override fun js_setHeaders() {
            updateHeaderView(fragment.currentLeftIcon, fragment.currentLeftStr)
        }
    }

    private fun updateHeaderView(leftIcon: WebViewLeftButton?, leftText: String?) {
        statusBarManager.statusBarColor(this, fragment.currentBackgroundColor, fragment.currentIsBlack)
        webView_header_parent.setBackgroundColor(fragment.currentBackgroundColor)
        webView_header_back.text = if (fragment.canGoBack()) leftText else "关闭"

        if (fragment.currentIsShowRightButton) visible(webView_header_menu) else invisible(webView_header_menu)

        if (fragment.currentIsBlack) {

            webView_header_back.setTextColor(0xff000000.toInt()) // 返回的文本颜色
            webView_header_menu.setImageResource(R.mipmap.icon_toolbar_more)
            when(leftIcon) { // 返回的图标
                WebViewLeftButton.Default -> { /* 使用上次操作的图标*/ }
                else -> webView_header_back.setCompoundDrawablesRelativeWithIntrinsicBounds(leftIcon?.iconBlack.int, 0, 0, 0) /* js设置的图标 */
            }
        } else {

            // 返回的文本颜色
            webView_header_back.setTextColor(0xffffffff.toInt()) // 返回的文本颜色
            webView_header_menu.setImageResource(R.mipmap.icon_toolbar_more_w)
            when(leftIcon) { // 返回的图标
                WebViewLeftButton.Default -> { /* 使用上次操作的图标*/ }
                else -> webView_header_back.setCompoundDrawablesRelativeWithIntrinsicBounds(leftIcon?.iconWhite.int, 0, 0, 0) /* js设置的图标 */
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && fragment.canGoBack()) {
            fragment.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}