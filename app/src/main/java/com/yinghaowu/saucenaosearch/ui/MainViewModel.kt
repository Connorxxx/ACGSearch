package com.yinghaowu.saucenaosearch.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import coil.api.load
import com.google.android.material.snackbar.Snackbar
import com.yinghaowu.saucenaosearch.App
import com.yinghaowu.saucenaosearch.R
import com.yinghaowu.saucenaosearch.logic.Repository
import com.yinghaowu.saucenaosearch.logic.model.Result
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream

class MainViewModel() : ViewModel() {

    lateinit var imm: InputMethodManager
    lateinit var ptUrl: EditText
    lateinit var uri: Uri
    lateinit var imgView: ImageView
    lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private val searchLiveData = MutableLiveData<String>()
    private val postLiveData = MutableLiveData<MultipartBody>()

    val resultsList = ArrayList<Result>()

    val resultLiveData = Transformations.switchMap(searchLiveData) {
        Repository.searchSauceNao(it, ptUrl)
    }
    val postResultLiveData = Transformations.switchMap(postLiveData) {
        Repository.postImage(it, imgView)
    }

    fun searchSauceNao(url: String) {
        searchLiveData.value = url
    }

    fun postSauceNao(multipartBody: MultipartBody) {
        postLiveData.value = multipartBody
    }

    fun coilShow() {
        imgView.load(searchLiveData.value)
    }

    fun coilByImage() {
        if (::uri.isInitialized) imgView.load(uri)
    }

    fun searchByUrl() {
        if (ptUrl.visibility == View.GONE) {
            ptUrl.visibility = View.VISIBLE
            imm = ptUrl.context
                .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            ptUrl.apply {
                requestFocus()
                imm.showSoftInput(ptUrl, 0)
            }
        } else {
            searchWithUrl()
        }
    }

    fun actionDone() {
        ptUrl.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                searchByUrl()
            }
            return@setOnEditorActionListener true
        }
    }

    private fun searchWithUrl() {
        if (ptUrl.text.isEmpty()) {
            hideKeyboard()
            ptUrl.visibility = View.GONE
            Snackbar.make(
                ptUrl,
                App.context.getString(R.string.please_input_link),
                Snackbar.LENGTH_LONG
            )
                .setAction(App.context.getString(R.string.input_link)) {
                    ptUrl.visibility = View.VISIBLE
                    imm.showSoftInput(ptUrl, 0)
                }.show()
        } else {
            swipeRefreshLayout.isRefreshing = true
            hideKeyboard()
            searchSauceNao(ptUrl.text.toString())
            ptUrl.visibility = View.GONE
            ptUrl.setText("")
        }
    }

    private fun hideKeyboard() {
        imm = ptUrl.context
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(ptUrl.windowToken, 0)
    }

    fun uriToByteArray(): ByteArray {
        val imageInputStream = App.context.contentResolver.openInputStream(uri)
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

    fun handleSendText(intent: Intent?) = intent?.getStringExtra(Intent.EXTRA_TEXT)

    fun handleSendImage(intent: Intent) =
        intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as Uri
}

