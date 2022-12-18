package me.luzhuo.webviewdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import me.luzhuo.lib_core.ui.adapter.ViewPagerAdapter
import me.luzhuo.lib_webview.BaseWebViewFragment
import me.luzhuo.lib_webview.WebViewConfig

class MainActivity : AppCompatActivity() {
    private lateinit var tablayout: TabLayout
    private lateinit var viewpager: ViewPager
    private val fragments = arrayListOf(
        ViewPagerAdapter.ViewPagerBean(BaseWebViewFragment.instance(WebViewConfig().apply { canHorizontalScroll = true}).apply { loadUrl("https://www.baidu.com") }, "A"),
        ViewPagerAdapter.ViewPagerBean(FragmentB(), "B"),
        ViewPagerAdapter.ViewPagerBean(FragmentC(), "C")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tablayout = findViewById<TabLayout>(R.id.tab_layout)
        viewpager = findViewById<ViewPager>(R.id.viewpager)

        initView()
    }

    private fun initView() {
        viewpager.adapter = ViewPagerAdapter(this, fragments)
        viewpager.currentItem = 0
        viewpager.offscreenPageLimit = fragments.size
        tablayout.setupWithViewPager(viewpager)
    }
}