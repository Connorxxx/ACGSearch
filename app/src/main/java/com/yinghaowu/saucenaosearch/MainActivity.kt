package com.yinghaowu.saucenaosearch

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import coil.api.load
import com.google.android.material.snackbar.Snackbar
import com.yinghaowu.saucenaosearch.ui.AboutActivity
import com.yinghaowu.saucenaosearch.ui.MainViewModel
import com.yinghaowu.saucenaosearch.ui.SauceNaoAdapter
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody

class MainActivity : AppCompatActivity() {

    private val viewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    lateinit var adapter: SauceNaoAdapter

    private val fromAlbum = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.statusBarColor = getColor(R.color.colorStatusDark)
        setSupportActionBar(toolbar)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        swipeRefreshLayout.isEnabled = false
        adapter = SauceNaoAdapter(this, viewModel.resultsList)
        recyclerView.adapter = adapter
        viewModel.imgView = imgView
        viewModel.ptUrl = ptUrl
        viewModel.swipeRefreshLayout = swipeRefreshLayout
        swipeRefreshLayout.setColorSchemeResources(R.color.lightColorPrimary)
        fabUpload.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, fromAlbum)
        }
        fabUploadResult()
        viewModel.resultLiveData.observe(this, Observer {
            val sauceNao = it.getOrNull()
            if (sauceNao != null) {
                viewModel.coilShow()
                viewModel.resultsList.clear()
                viewModel.resultsList.addAll(sauceNao)
                adapter.notifyDataSetChanged()
            }
            swipeRefreshLayout.isRefreshing = false
        })
        viewModel.postResultLiveData.observe(this, Observer {
            val sauceNao = it.getOrNull()
            if (sauceNao != null) {
                viewModel.coilByImage()
                viewModel.resultsList.clear()
                viewModel.resultsList.addAll(sauceNao)
                adapter.notifyDataSetChanged()
            } else {
                Snackbar.make(
                    ptUrl, getString(R.string.ip_exceeded_limit),
                    Snackbar.LENGTH_LONG
                ).show()
            }
            swipeRefreshLayout.isRefreshing = false
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == fromAlbum && resultCode == Activity.RESULT_OK && data != null) {
            Log.d("MainActivity", "Get local image!")
            swipeRefreshLayout.isRefreshing = true
            val localImageUri = data.data!!
            viewModel.uri = localImageUri
            viewModel.coilByImage()
            val multipartBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file", "image.bmp", RequestBody.create(
                        MediaType.parse("image/*"),
                        viewModel.uriToByteArray()
                    )
                ).build()
            viewModel.postSauceNao(multipartBody)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_about -> {
                val intent = Intent(this, AboutActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_link -> {
                ptUrl.apply {
                    viewModel.searchByUrl()
                }
                viewModel.actionDone()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun fabUploadResult() {
        when (intent.action) {
            Intent.ACTION_SEND -> {
                if ("text/plain" == intent.type) {
                    swipeRefreshLayout.isRefreshing = true
                    val imageUrl = viewModel.handleSendText(intent)!!
                    viewModel.searchSauceNao(imageUrl)
                } else if (intent.type?.startsWith("image/") == true) {
                    swipeRefreshLayout.isRefreshing = true
                    val imageByteArray = viewModel.handleSendImage(intent)
                    viewModel.uri = imageByteArray
                    viewModel.coilByImage()
                    val multipartBody = MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart(
                            "file", "image.bmp", RequestBody.create(
                                MediaType.parse("image/*"),
                                viewModel.uriToByteArray()
                            )
                        ).build()
                    viewModel.postSauceNao(multipartBody)
                }
            }
        }
    }

    override fun onBackPressed() {
        if (ptUrl.visibility == View.VISIBLE) {
            ptUrl.visibility = View.GONE
        } else super.onBackPressed()
    }
}

