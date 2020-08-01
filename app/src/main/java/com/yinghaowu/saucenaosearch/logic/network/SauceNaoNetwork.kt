package com.yinghaowu.saucenaosearch.logic.network

import okhttp3.*

object SauceNaoNetwork {

    fun getResponseJson(request: Request, callback: Callback) {
        val okHttp = OkHttpClient()
        okHttp.newCall(request).enqueue(callback)
    }


}