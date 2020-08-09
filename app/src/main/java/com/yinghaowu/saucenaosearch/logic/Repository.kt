package com.yinghaowu.saucenaosearch.logic

import android.util.Log
import android.view.View
import androidx.lifecycle.liveData
import com.google.android.material.snackbar.Snackbar
import com.yinghaowu.saucenaosearch.App
import com.yinghaowu.saucenaosearch.R
import com.yinghaowu.saucenaosearch.logic.network.SauceNaoNetwork
import kotlinx.coroutines.Dispatchers
import okhttp3.MultipartBody
import kotlin.coroutines.CoroutineContext

object Repository {

    fun searchSauceNao(url: String, view: View) = fire(Dispatchers.IO) {
        val sauceNaoResponse = SauceNaoNetwork.searchSauceNao(url)
        when {
            sauceNaoResponse.header.status == 0 -> {
                val result = sauceNaoResponse.results
                Log.d("Repository", sauceNaoResponse.header.status.toString())
                Result.success(result)
            }
            sauceNaoResponse.header.status == -2 -> {
                Log.d("Repository", sauceNaoResponse.header.status.toString())
                Snackbar.make(
                    view, App.context.getString(R.string.ip_exceeded_limit),
                    Snackbar.LENGTH_LONG
                ).show()
                Result.failure(RuntimeException("response status is -2"))
            }
            sauceNaoResponse.header.status < 0 -> {
                Log.d("Repository", sauceNaoResponse.header.status.toString())
                Snackbar.make(
                    view, App.context.getString(R.string.snack_wrong_url),
                    Snackbar.LENGTH_LONG
                ).show()
                Result.failure(RuntimeException("response status is <0"))
            }
            else -> {
                Snackbar.make(
                    view, App.context.getString(R.string.snack_server_errors),
                    Snackbar.LENGTH_LONG
                ).show()
                Log.d("Repository", sauceNaoResponse.header.status.toString())
                Result.failure(RuntimeException("response status is ${sauceNaoResponse.header.status}"))
            }
        }
    }

    fun postImage(multipartBody: MultipartBody, view: View) = fire(Dispatchers.IO) {
        val sauceNaoResponse = SauceNaoNetwork.postImage(multipartBody)
        if (sauceNaoResponse.header.status == 0) {
            Log.d("Repository", sauceNaoResponse.header.status.toString())
            val result = sauceNaoResponse.results
            Result.success(result)
        } else {
            Snackbar.make(
                view, App.context.getString(R.string.snack_server_errors),
                Snackbar.LENGTH_LONG
            ).show()
            Log.d("Repository", sauceNaoResponse.header.status.toString())
            Result.failure(RuntimeException("response status is ${sauceNaoResponse.header.status}"))
        }
    }

    private fun <T> fire(context: CoroutineContext, block: suspend () -> Result<T>) =
        liveData<Result<T>>(context) {
            val result = try {
                block()
            } catch (e: Exception) {
                Result.failure<T>(e)
            }
            emit(result)
        }
}