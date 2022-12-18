package me.luzhuo.webviewdemo.webview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.webkit.WebView
import kotlinx.android.synthetic.main.base_layout_webview_miniprogram.*
import me.luzhuo.lib_core.ui.fragment.FragmentManager
import me.luzhuo.lib_core_ktx.int
import me.luzhuo.lib_core_ktx.invisible
import me.luzhuo.lib_core_ktx.visible
import me.luzhuo.lib_webview.callback.ProgressListener
import me.luzhuo.webviewdemo.R
import me.luzhuo.webviewdemo.lib_base.BaseActivity
import me.luzhuo.webviewdemo.webview.callback.CCCWebViewListener

/**
 * 小程序的WebView
 * 请使用[CCCHeaderWebViewActivity]
 */
@Deprecated("请使用CCCFullWebViewActivity")
class CCCMiniProgramActivity : BaseActivity(), CCCWebViewListener {
    private lateinit var fragmentManger : FragmentManager
    private lateinit var fragment: CCCWebViewFragment2
    private var url: String? = null
    private var orientation: WebViewOrientation? = null

    companion object {
        fun start(context: Context, url: String) {
            context.startActivity(Intent(context, CCCMiniProgramActivity::class.java).apply {
                putExtra("url", url)
                putExtra("orientation", WebViewOrientation.Bottom2Top)
            })
        }
    }

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.webview_miniprogram)

        url = intent.getStringExtra("url")
        orientation = intent.getSerializableExtra("orientation") as? WebViewOrientation
        if (orientation != WebViewOrientation.Default) overridePendingTransition(orientation?.firstEnterActivityAnimation.int, orientation?.firstExitActivityAnimation.int)

        fragmentManger = FragmentManager(this, R.id.webView_frame)
        fragment = CCCWebViewFragment2.instance()
        fragmentManger.replaceFragment(fragment)
        fragment.setCCCWebViewListener(this)

        fragment.loadUrl(url)

        initView()
    }

    override fun finish() {
        super.finish()
        if (orientation != WebViewOrientation.Default) overridePendingTransition(orientation?.secondEnterActivityAnimation.int, orientation?.secondExitActivityAnimation.int)
    }

    private fun initView() {
        ccc_mini_program_close.setOnClickListener { finish() }
        ccc_mini_program_menu.setOnClickListener {
            fragment.showShareDialog()
        }
        ccc_mini_program_back.setOnClickListener {
            if (fragment.canGoBack()) fragment.goBack()
            else finish()
        }

        fragment.setProgressListener(object : ProgressListener {
            override fun start() {}
            override fun progress(progress: Int) {
                showReturn()
            }

            override fun end(isEnd: Boolean) {}
        })
    }

    override fun onTitle(title: String?) {
        ccc_mini_program_title.text = title
    }

    override fun onUserAgent(userAgent: String): String = userAgent

    override fun onWebViewFinish(webView: WebView?) {
        showReturn()
    }

    private fun showReturn() {
        if (fragment.canGoBack()) visible(ccc_mini_program_back)
        else invisible(ccc_mini_program_back)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && fragment.canGoBack()) {
            fragment.goBack()
            showReturn()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}