package me.luzhuo.webviewdemo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import me.luzhuo.webviewdemo.webview.CCCFullWebViewActivity
import me.luzhuo.webviewdemo.webview.CCCHeaderWebViewActivity
import me.luzhuo.webviewdemo.webview.CCCMiniProgramActivity
import me.luzhuo.webviewdemo.webview.CCCWordWebViewActivity

class FragmentC : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_c, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.CCCFullWebViewActivity).setOnClickListener {
            CCCFullWebViewActivity.start(requireContext(), "https://www.baidu.com")
        }
        view.findViewById<View>(R.id.CCCHeaderWebViewActivity).setOnClickListener {
            CCCHeaderWebViewActivity.start(requireContext(), "https://www.baidu.com")
        }
        view.findViewById<View>(R.id.CCCMiniProgramActivity).setOnClickListener {
            CCCMiniProgramActivity.start(requireContext(), "https://www.baidu.com")
        }
        view.findViewById<View>(R.id.CCCWordWebViewActivity).setOnClickListener {
            CCCWordWebViewActivity.start(requireContext(), "https://www.baidu.com")
        }
    }
}