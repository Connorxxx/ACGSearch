package com.yinghaowu.saucenaosearch.ui

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import com.yinghaowu.saucenaosearch.R
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.developer_card.*
import kotlinx.android.synthetic.main.explain2_card.*

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!getDarkModeStatus(this)) {
            window.statusBarColor = getColor(R.color.lightColorPrimaryDark)
        }
        setContentView(R.layout.activity_about)
        setSupportActionBar(toolbar2)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        explain2_card.setOnClickListener {
            val url = "https://www.pixiv.net/artworks/69554856"
            val builder = CustomTabsIntent.Builder()
            builder.setToolbarColor(getColor(R.color.lightColorPrimary))
            builder.build().launchUrl(this, url.toUri())
        }
        feed_back.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = "mailto:wyh2542367414d@gmail.com".toUri()
            startActivity(intent)
        }
        github.setOnClickListener {
            val url = "https://github.com/wuyinghao1/ACGSearch"
            val builder = CustomTabsIntent.Builder()
            builder.setToolbarColor(getColor(R.color.lightColorPrimary))
            builder.build().launchUrl(this, url.toUri())
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
    private fun getDarkModeStatus(context: Context): Boolean {
        val mode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return mode == Configuration.UI_MODE_NIGHT_YES
    }
}