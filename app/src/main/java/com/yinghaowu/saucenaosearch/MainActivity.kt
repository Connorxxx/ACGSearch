package com.yinghaowu.saucenaosearch

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.yinghaowu.saucenaosearch.logic.model.JsonFeeds
import com.yinghaowu.saucenaosearch.logic.network.SauceNaoNetwork
import com.yinghaowu.saucenaosearch.ui.AboutActivity
import com.yinghaowu.saucenaosearch.ui.CardAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.coroutines.suspendCoroutine


class MainActivity : AppCompatActivity() {

    private var requestUrl = "https://saucenao.com/search.php?&output_type=2&numres=12"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.statusBarColor = getColor(R.color.colorStatusDark)
        setSupportActionBar(toolbar)
        recyclerView.layoutManager = LinearLayoutManager(this)

        actionShare()
        fab_upload.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            Log.d("MainActivity", "Get local image!")
            val localImageUri = data?.data
            Glide.with(imgView.context).load(localImageUri)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(imgView)
            val requestBody = uriToByteArray(localImageUri!!).toRequestBody(
                "image/*".toMediaTypeOrNull(),
                0,
                uriToByteArray(localImageUri).size
            )
            val multipartBody = MultipartBody
                .Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "image.bmp", requestBody)
                .build()
            val requestByImage = Request
                .Builder()
                .url(requestUrl)
                .post(multipartBody)
                .build()
            GlobalScope.launch {
                getResponseJson(requestByImage)
            }
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
        }
        return super.onOptionsItemSelected(item)
    }

    private fun actionShare() {
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                if ("text/plain" == intent.type) {
                    val imageUrl = handleSendText(intent)
                    val requestByUrl = Request
                        .Builder()
                        .url("$requestUrl&url=$imageUrl")
                        .build()
                    Glide.with(this).load(imageUrl)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE).into(imgView)
                    GlobalScope.launch {
                        getResponseJson(requestByUrl)
                    }
                } else if (intent.type?.startsWith("image/")  == true) {
                    val imageByteArray = uriToByteArray(handleSendImage(intent))
                    Glide.with(this).load(imageByteArray)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE).into(imgView)
                    val requestBody =
                        imageByteArray.toRequestBody(
                            "image/*".toMediaTypeOrNull(), 0, imageByteArray.size)
                    val multipartBody = MultipartBody
                        .Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", "image.bmp", requestBody)
                        .build()
                    val requestByImage = Request
                        .Builder()
                        .url(requestUrl)
                        .post(multipartBody)
                        .build()
                    GlobalScope.launch {
                        getResponseJson(requestByImage)
                        Log.d("JsonUri", "actionShare: $requestByImage")
                    }
                }
            }
        }
    }

    private suspend fun getResponseJson(request: Request) =
        request(applicationContext, request, mainCoordinatorLayout, recyclerView)

    private suspend fun request(context: Context,
                                request: Request,
                                view: View,
                                recyclerView: RecyclerView): Request {
        return suspendCoroutine {
            SauceNaoNetwork.getResponseJson(request, object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Snackbar.make(view, getString(R.string.network_error),
                        Snackbar.LENGTH_LONG).show()
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()
                    val json = Gson().fromJson<JsonFeeds>(body, JsonFeeds::class.java)

                    when {
                        json.header.status == -2 -> {
                            Snackbar.make(view, getString(R.string.ip_exceeded_limit),
                                Snackbar.LENGTH_LONG).show()
                        }
                        json.header.status > 0 -> {
                            Snackbar.make(view, getString(R.string.snack_server_errors),
                                Snackbar.LENGTH_LONG).show()
                            runOnUiThread {
                                recyclerView.adapter = CardAdapter(json, context)
                            }
                        }
                        json.header.status < 0 -> {
                            Snackbar.make(view, getString(R.string.snack_wrong_url),
                                Snackbar.LENGTH_LONG).show()
                        }
                        else -> {
                            runOnUiThread {
                                recyclerView.adapter = CardAdapter(json, context)
                            }
                        }
                    }
                }
            })
        }
    }


    private fun handleSendText(intent: Intent) =
        intent.getStringExtra(Intent.EXTRA_TEXT)

    private fun handleSendImage(intent: Intent) =
        intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as Uri

    private fun uriToByteArray(uri: Uri): ByteArray {
        val imageInputStream = contentResolver.openInputStream(uri)
        val byteArrayOutputStream = ByteArrayOutputStream()
        imageInputStream.use { inputStream ->
            byteArrayOutputStream.use { outPutStream ->
                inputStream?.copyTo(outPutStream)
            }
        }
        imageInputStream?.close()
        byteArrayOutputStream.close()
        return byteArrayOutputStream.toByteArray()
    }
}

