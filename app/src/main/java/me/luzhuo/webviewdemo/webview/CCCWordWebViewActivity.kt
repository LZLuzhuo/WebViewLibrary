package me.luzhuo.webviewdemo.webview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.webkit.WebView
import kotlinx.android.synthetic.main.webview_word.*
import me.luzhuo.lib_common_ui.toolbar.OnToolBarCallback
import me.luzhuo.lib_core.ui.fragment.FragmentManager
import me.luzhuo.lib_core_ktx.gone
import me.luzhuo.lib_core_ktx.visible
import me.luzhuo.lib_webview.callback.ProgressListener
import me.luzhuo.webviewdemo.R
import me.luzhuo.webviewdemo.lib_base.BaseActivity
import me.luzhuo.webviewdemo.webview.callback.CCCWebViewListener

/**
 * 文章的WebView
 * 请使用[CCCHeaderWebViewActivity]
 */
@Deprecated("请使用CCCHeaderWebViewActivity")
class CCCWordWebViewActivity : BaseActivity(),
    CCCWebViewListener {
    private lateinit var fragmentManger : FragmentManager
    private lateinit var fragment: CCCWebViewFragment2
    private var url: String? = null
    private var orientation: WebViewOrientation? = null

    companion object {
        fun start(context: Context, url: String) {
            context.startActivity(Intent(context, CCCWordWebViewActivity::class.java).apply {
                putExtra("url", url)
            })
        }
    }

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.webview_word)

        url = intent.getStringExtra("url")
        orientation = intent.getSerializableExtra("orientation") as? WebViewOrientation

        fragmentManger = FragmentManager(this, R.id.webView_frame)
        fragment = CCCWebViewFragment2.instance()
        fragmentManger.replaceFragment(fragment)
        fragment.setCCCWebViewListener(this)

        fragment.loadUrl(url)

        initView()
    }

    private fun initView() {
        webView_word_toolbar.setOnToolBarCallback(object : OnToolBarCallback() {
            override fun onRightButton() {
                fragment.showShareDialog()
            }
        })

        fragment.setProgressListener(object : ProgressListener {
            override fun start() {
                visible(webView_progress)
                webView_progress.max = 100
            }

            override fun progress(progress: Int) {
                webView_progress.progress = progress
            }

            override fun end(isEnd: Boolean) {
                if (!isEnd) gone(webView_progress)
            }
        })
    }

    override fun onTitle(title: String?) {
        webView_word_toolbar.setTitle(title)
    }

    override fun onUserAgent(userAgent: String): String = userAgent

    override fun onWebViewFinish(webView: WebView?) {

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && fragment.canGoBack()) {
            fragment.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}