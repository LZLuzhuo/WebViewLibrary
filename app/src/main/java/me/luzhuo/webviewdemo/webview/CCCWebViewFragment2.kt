package me.luzhuo.webviewdemo.webview

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.webkit.*
import android.webkit.WebChromeClient.FileChooserParams
import androidx.core.util.Pair
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.webview_share_bottom_dialog.view.*
import me.luzhuo.lib_core.app.appinfo.AppManager
import me.luzhuo.lib_core.app.pattern.RegularType
import me.luzhuo.lib_core.data.UriManager
import me.luzhuo.lib_core.data.clipboard.ClipboardManager
import me.luzhuo.lib_core.ui.dialog.BottomDialog
import me.luzhuo.lib_core.ui.toast.ToastManager
import me.luzhuo.lib_core_ktx.*
import me.luzhuo.lib_image_select.bean.ImageSelectBean
import me.luzhuo.lib_image_select.callback.SelectCallBack
import me.luzhuo.lib_image_select.utils.ImageSelectManager
import me.luzhuo.lib_webview.BaseWebViewFragment
import me.luzhuo.lib_webview.WebViewConfig
import me.luzhuo.lib_webview.callback.WebViewEventListener
import me.luzhuo.webviewdemo.R
import me.luzhuo.webviewdemo.lib_base.WebViewShareAdapter
import me.luzhuo.webviewdemo.webview.callback.CCCWebViewJSListener
import me.luzhuo.webviewdemo.webview.callback.CCCWebViewListener
import me.luzhuo.webviewdemo.webview.enums.WebViewLeftButton
import me.luzhuo.webviewdemo.webview.enums.WebViewLeftEvent
import org.json.JSONObject
import java.util.*

// @WechatLogin(applicationId = BuildConfig.APPLICATION_ID, entryTemplete = WechatLoginTemplate::class)
class CCCWebViewFragment2 : BaseWebViewFragment() {
    private val TAG = CCCWebViewFragment2::class.java.simpleName
    private val appManager = AppManager()

    private var listener: CCCWebViewListener? = null
    private var jsListener: CCCWebViewJSListener? = null

    /**
     * JS???????????????, ?????????????????????
     */
    private val jsWhiteList = listOf(
        "lkkjjt.com", "mp.wang", "expection.cn", "lookfs.com"
    )

    companion object {
        fun instance(): CCCWebViewFragment2 {
            return CCCWebViewFragment2().apply {
                arguments = Bundle().apply {
                    putSerializable("config", WebViewConfig())
                }
            }
        }

        fun instance(config: WebViewConfig): CCCWebViewFragment2 {
            return CCCWebViewFragment2().apply {
                arguments = Bundle().apply {
                    putSerializable("config", config)
                }
            }
        }
    }

    override fun loadUrl(url: String?) {
        val newUrl = UriManager(url).addQueryParameter("token", "asfsadfasdf")
        super.loadUrl(newUrl.toString())
    }

    override fun initWebView(webView: WebView?) {
        setWebEventListener(eventListener)
        listener?.onWebViewFinish(webView)
    }

    fun setCCCWebViewListener(listener: CCCWebViewListener) {
        this.listener = listener
    }

    fun setCCCWebViewJSListener(jsListener: CCCWebViewJSListener) {
        this.jsListener = jsListener
    }

    private val eventListener: WebViewEventListener = object : WebViewEventListener() {
        override fun onReceivedTitle(title: String) {
            if (patternCheck.check(RegularType.Chinese, title)) {
                listener?.onTitle(title)
            }
        }

        override fun addJavascriptInterface(webSettings: WebSettings) {
            webView?.addJavascriptInterface(JSCallJava(), "android")
        }

        override fun setUserAgentString(userAgent: String): String {
            var newUserAgent = "$userAgent; cccApp"
            newUserAgent = listener?.onUserAgent(newUserAgent) ?: newUserAgent
            return newUserAgent
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            if (patternCheck.check(RegularType.HttpUrl, url)) {
                if (!TextUtils.isEmpty(wxPaylinks)) {
                    // ???????????????????????????
                    val extraHeaders: MutableMap<String, String?> = HashMap()
                    extraHeaders["Referer"] = wxPaylinks
                    webView?.loadUrl(url, extraHeaders)
                    return true
                } else {
                    view.loadUrl(url)
                }
            } else {
                try {
                    startActivity(Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(url)
                    })
                } catch (e: Exception) {
                    ToastManager.show2("????????????????????????")
                }
            }
            return true
        }

        override fun onReceivedError(errorCode: Int, description: String?, failingUrl: String?) {
            Log.e(TAG, "errorCode: ${errorCode}, description: ${description}, failingUrl: ${failingUrl}")
        }

        override fun onReceivedHttpError(statusCode: Int, description: String?, failingUrl: String?) {
            Log.e(TAG, "errorCode: ${statusCode}, description: ${description}, failingUrl: ${failingUrl}")
        }
    }

    override fun onShowFileChooser(webView: WebView?, filePathCallback: ValueCallback<Array<Uri?>?>, fileChooserParams: FileChooserParams): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val acceptTypes = fileChooserParams.acceptTypes
            var acceptTypeInt = 0
            for (acceptType in acceptTypes) {
                acceptTypeInt = if (acceptType.startsWith("image/")) 1 else if (acceptType.startsWith("audio/")) 2 else if (acceptType.startsWith("video/")) 3 else 4
            }
            val selectManager: ImageSelectManager = ImageSelectManager(this@CCCWebViewFragment2).onSetCallbackListener(object : SelectCallBack {
                override fun onSelect(list: List<ImageSelectBean>) {
                    filePathCallback.onReceiveValue(arrayOf(Uri.parse("file://" + list[0].path)))
                }

                override fun onCancel() {
                    filePathCallback.onReceiveValue(null)
                }
            })
            when (acceptTypeInt) {
                1 -> selectManager.openImage(1)
                2 -> selectManager.openMediaWithFlags(16, 1)
                3 -> selectManager.openVideo(1)
                4 -> selectManager.openAll(1)
            }
        } else {
            ToastManager.show2("?????????Android5.0???????????????")
            return false // ??????false, ?????????????????????
        }
        return true // ??????true, ?????????, ??????filePathCallback?????????
    }

    // ===================================== JS ???????????? =====================================
    private var wxPaylinks: String? = null

    // ???????????????
    var currentLeftIcon: WebViewLeftButton? = null

    // ?????????????????????
    var currentLeftStr: String? = null

    // ???????????????, true??????, false??????, ???????????????, ???????????????????????????, ????????????????????????, ?????????????????????(??????+??????)
    var currentIsBlack = false

    // ????????? + ????????? ????????????
    var currentBackgroundColor = 0

    // ?????????????????????
    var currentLeftEvent: WebViewLeftEvent? = null

    // ???????????????????????????
    var currentIsShowRightButton = false

    inner class JSCallJava {
        /**
         * h5?????????js??????
         */
        @JavascriptInterface
        fun androidApp(json: String?) {
            if (TextUtils.isEmpty(json)) return
            if (context == null || activity == null) return
            mainThread.post {
                if (appManager.isDebug) ToastManager.show(json ?: "")
                if (!checkWhiteList()) return@post

                var type: String? = ""
                var data: JSONParseObject? = null
                try {
                    val jsonObject = json.jsonObj
                    type = jsonObject.stringOrNull("type")
                    data = jsonObject?.jsonObj("data")
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                when (type) {
                    "close_browser" -> ccc_close_browser(data.stringOrNull("status"))
                    "set_share" -> ccc_set_share(data.stringOrNull("logo"), data.stringOrNull("title"), data.stringOrNull("descs"), data.stringOrNull("link"))
                    "set_referer" -> ccc_set_referer(data.stringOrNull("link"))
                    "toIndex" -> toIndex()
                    "toHome" -> toHome()
                    "afterFollow" -> afterFollow(data.stringOrNull("unionid"))
                    "refreshWallet" -> refreshWallet()
                    "setToken" -> setToken(data.stringOrNull("token"))
                    "setHeaders" -> {
                        val left: JSONParseObject? = data?.jsonObj("left")
                        currentBackgroundColor = parseColor(data.stringOrNull("background"))
                        currentIsBlack = data.stringOrNull("fontcolor") != "w" // ?????????b

                        // back??????????????????    close????????????
                        val leftIconStr: String? = left?.stringOrNull("icon")
                        if (leftIconStr == "back") currentLeftIcon = WebViewLeftButton.Return else if (leftIconStr == "close") currentLeftIcon = WebViewLeftButton.Close else currentLeftIcon = WebViewLeftButton.None

                        // ??????: back???????????? close??????
                        val leftEventStr: String? = left?.stringOrNull("event")
                        if (leftEventStr == "back") currentLeftEvent = WebViewLeftEvent.Return else if (leftEventStr == "close") currentLeftEvent = WebViewLeftEvent.Close else currentLeftEvent = WebViewLeftEvent.Close
                        currentLeftStr = left?.stringOrNull("text")

                        val right: JSONParseObject? = data?.jsonObj("right")
                        // close ???????????????????????????  show???????????????????????????
                        val status: String? = right?.stringOrNull("status")
                        currentIsShowRightButton = status != "close"

                        // JS ??????
                        jsListener?.js_setHeaders()
                    }
                }
            }
        }
    }

    /**
     * ??????token
     */
    fun setToken(token: String?) {
        //CCCUserInfo.token = token
        //EventBus.getDefault().post(RefreshEvent(RefreshEvent.RefreshTokenInfo, null))
    }

    /**
     * ????????????
     */
    fun refreshWallet() {
        // EventBus.getDefault().post(RefreshEvent(RefreshEvent.RefreshMoneyInfo, null))
    }

    /**
     * ?????????
     */
    fun toIndex() {
        // EventBus.getDefault().post(HomeEvent())
        requireActivity().finish()
    }

    /**
     * ???????????????
     */
    fun toHome() {
        // EventBus.getDefault().post(HomeEvent(4))
        requireActivity().finish()
    }

    /**
     * ????????????
     */
    fun afterFollow(userId: String?) {
        // EventBus.getDefault().post(RefreshEvent(RefreshEvent.RefreshType_ContractList, null))
    }

    /**
     * ???????????????
     */
    fun ccc_close_browser(status: String?) {
        // if (status == "paysuccess") EventBus.getDefault().post(RefreshEvent(RefreshEvent.RefreshType_VIP, null)) // vip ????????????
        requireActivity().finish()
    }

    /**
     * ??????
     */
    fun ccc_set_share(logo: String?, title: String?, desc: String?, link: String?) {
        this.shareLogo = logo
        this.shareTitle = title
        this.shareDesc = desc
        this.shareLink = link
    }

    /**
     * ????????????????????? Referer
     */
    fun ccc_set_referer(wxPaylinks: String?) {
        this.wxPaylinks = wxPaylinks
    }

    fun getJsonObject(data: JSONObject, key: String?): JSONObject? {
        try {
            if (data.has(key)) return data.getJSONObject(key)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun getJsonString(data: JSONObject, key: String?): String? {
        try {
            if (data.has(key)) return data.getString(key)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return ""
    }

    private fun checkWhiteList(): Boolean {
        val authority = UriManager(originalUrl).authority()
        for (whiteList in jsWhiteList) {
            if (authority?.endsWith(whiteList).bool) return true
        }
        ToastManager.show2("??????JS??????")
        return false
    }

    private fun parseColor(colorStr: String?): Int {
        return try {
            Color.parseColor(colorStr)
        } catch (e: java.lang.Exception) {
            0xffffffff.toInt()
        }
    }
    // ===================================== JS ???????????? =====================================

    // ===================================== JS ???????????? =====================================
    fun getRealUrl(): String = UriManager(webView?.originalUrl).removeQueryParameter("token").toString()

    private var shareLogo: String? = null
    private var shareTitle: String? = null
    private var shareDesc: String? = null
    private var shareLink: String? = null
    private var dialog: BottomSheetDialog? = null
    fun showShareDialog() {
        val bottomDialog: View = LayoutInflater.from(requireContext()).inflate(R.layout.webview_share_bottom_dialog, null, false)
        val ccc_webView_share: RecyclerView = bottomDialog.ccc_webview_share
        val ccc_webView_share_other: RecyclerView = bottomDialog.ccc_webview_share_other
        bottomDialog.ccc_webview_cancel.setOnClickListener { v: View? -> dialog?.dismiss() }
        ccc_webView_share.layoutManager = GridLayoutManager(requireContext(), 5)
        ccc_webView_share_other.layoutManager = GridLayoutManager(requireContext(), 5)
        ccc_webView_share.adapter = WebViewShareAdapter(arrayListOf(Pair(R.mipmap.icon_miniprogram_share, "??????"), Pair(R.mipmap.icon_miniprogram_share_frind, "?????????")),
            WebViewShareAdapter.OnShareCallback { position: Int, data: Pair<Int?, String?> ->
                /*var where = WechatShareWhere.Friend
                when (data.second) {
                    "??????" -> where = WechatShareWhere.Friend
                    "?????????" -> where = WechatShareWhere.CircleOfFriends
                }
                if (!TextUtils.isEmpty(shareLogo) && !TextUtils.isEmpty(shareTitle) && !TextUtils.isEmpty(shareDesc) && !TextUtils.isEmpty(shareLink)) {
                    share(shareLogo, shareLink, shareTitle, shareDesc, where)
                } else {
                    share(getRealUrl(), webView?.title, getRealUrl(), where)
                }*/
                dialog?.dismiss()
            })
        ccc_webView_share_other.adapter = WebViewShareAdapter(arrayListOf(Pair(R.mipmap.icon_miniprogram_refresh, "??????"), Pair(R.mipmap.icon_miniprogram_link, "????????????")),
            WebViewShareAdapter.OnShareCallback { position: Int, data: Pair<Int?, String?> ->
                when (data.second) {
                    "??????" -> webView?.reload()
                    "????????????" -> {
                        ClipboardManager(this).copy(getRealUrl())
                        ToastManager.show2("??????????????????!")
                    }
                }
                dialog?.dismiss()
            })
        dialog = BottomDialog.instance().show(requireContext(), bottomDialog)
        dialog?.show()
    }

/*    private fun share(logo: String?, link: String?, title: String?, desc: String?, shareWhere: WechatShareWhere) {
        try {
            OKHttpManager().getBitmap(logo, object : IBitmapCallback {
                override fun onSuccess(i: Int, bitmap: Bitmap) {
                    WechatManager.getInstance(getContext()).share(WebMessage(link, title, desc, bitmap), shareWhere)
                }

                override fun onError(i: Int, s: String) {
                    share(link, title, desc, shareWhere)
                }
            })
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun share(link: String?, title: String?, desc: String?, shareWhere: WechatShareWhere) {
        WechatManager.getInstance(getContext()).share(WebMessage(getContext(), link, title, desc, R.mipmap.logo_share), shareWhere)
    }*/
}