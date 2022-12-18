package me.luzhuo.webviewdemo.webview

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.ViewGroup
import android.webkit.WebView
import kotlinx.android.synthetic.main.webview_full.*
import me.luzhuo.lib_core.app.appinfo.AppManager
import me.luzhuo.lib_core.ui.calculation.UICalculation
import me.luzhuo.lib_core.ui.fragment.FragmentManager
import me.luzhuo.lib_core.ui.toast.ToastManager
import me.luzhuo.lib_core_ktx.invisible
import me.luzhuo.lib_core_ktx.visible
import me.luzhuo.webviewdemo.R
import me.luzhuo.webviewdemo.bugs.AndroidBug5497Workaround
import me.luzhuo.webviewdemo.lib_base.BaseActivity
import me.luzhuo.webviewdemo.lib_base.StatusBarManager
import me.luzhuo.webviewdemo.webview.callback.CCCWebViewJSListener
import me.luzhuo.webviewdemo.webview.callback.CCCWebViewListener
import org.json.JSONObject
import java.lang.Exception

/**
 * 全屏展示的WebView
 * 用来替代之前的[CCCMiniProgramActivity]
 */
class CCCFullWebViewActivity : BaseActivity(),
    CCCWebViewListener {
    private val TAG = CCCFullWebViewActivity::class.java.simpleName
    private lateinit var fragmentManger : FragmentManager
    private lateinit var fragment: CCCWebViewFragment2
    private var url: String? = null
    private lateinit var ui: UICalculation
    private val statusBarManager = StatusBarManager()

    companion object {
        fun start(context: Context, url: String?) {
            if (TextUtils.isEmpty(url)) {
                ToastManager.show("url为空")
                return
            }
            context.startActivity(Intent(context, CCCFullWebViewActivity::class.java).apply {
                putExtra("url", url)
            })
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle, EnterAnimation.Bottom2Top)
        statusBarManager.transparent(this, true)
        setContentView(R.layout.webview_full)
        AndroidBug5497Workaround.assistActivity(this)
        ui = UICalculation()

        val marginLayoutParams = webView_full_close_parent.layoutParams as? ViewGroup.MarginLayoutParams
        marginLayoutParams?.topMargin = ui.getStatusBarHeight(webView_full_close_parent)
        navigationbar.layoutParams.height = ui.currentNavigationBar

        url = intent.getStringExtra("url")

        fragmentManger = FragmentManager(this, R.id.webView_frame)
        fragment = CCCWebViewFragment2.instance()
        fragmentManger.replaceFragment(fragment)
        fragment.setCCCWebViewListener(this)
        fragment.setCCCWebViewJSListener(jsListener)

        fragment.loadUrl(url)

        initView()
    }

    override fun onStart() {
        super.onStart()
        // 测试在关闭和显示时, 在设置页设置了按钮
        navigationbar.layoutParams.height = ui.currentNavigationBar
    }

    private fun initView() {
        webView_full_close.setOnClickListener { finish() }
    }

    override fun onTitle(title: String?) { }

    override fun onUserAgent(userAgent: String): String {
        try {
            val json = JSONObject().apply {
                put("header", 2) // // 浏览器头  1有    2无
                put("statusbar", ui.px2dp(ui.getStatusBarHeight(webView_full_close_parent).toFloat()))
            }
            if (AppManager().isDebug) Log.e(TAG, json.toString())
            return "$userAgent  [${json}]"
        } catch (e: Exception) {
            return userAgent
        }
    }

    override fun onWebViewFinish(webView: WebView?) {
    }

    private val jsListener = object: CCCWebViewJSListener(){
        override fun js_setHeaders() {
            updateHeaderView()
        }
    }

    private fun updateHeaderView() {
        // 是否显示右侧关闭按钮
        if (fragment.currentIsShowRightButton) {
            visible(webView_full_close)
        } else {
            invisible(webView_full_close)
        }
        // 状态栏的颜色
        statusBarManager.transparent(this, fragment.currentIsBlack)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && fragment.canGoBack()) {
            fragment.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}