package com.yinghaowu.saucenaosearch.logic.network

import com.yinghaowu.saucenaosearch.logic.model.SauceNaoResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface SauceNaoService {

    @GET("search.php?&output_type=2&numres=12&")
    fun searchSauce(@Query("url") url: String): Call<SauceNaoResponse>

    @POST("search.php?&output_type=2&numres=12")
    fun postImage(@Body multipartBody: MultipartBody): Call<SauceNaoResponse>
}