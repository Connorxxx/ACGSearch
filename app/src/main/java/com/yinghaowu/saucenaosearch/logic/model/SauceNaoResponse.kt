package com.yinghaowu.saucenaosearch.logic.model

data class SauceNaoResponse(
    val header: Header,
    val results: List<Result>
)

data class Header(
    val user_id: String,
    val account_type: String,
    val short_limit: String,
    val long_limit: String,
    val long_remaining: Int,
    val short_remaining: Int,
    val status: Int,
    val results_requested: String,
    val search_depth: String,
    val minimum_similarity: Double,
    val query_image_display: String,
    val query_image: String,
    val results_returned: Int
)

data class Result(
    val header: HeaderX,
    val data: Data
)

data class HeaderX(
    val similarity: String,
    val thumbnail: String,
    val index_id: Int,
    val index_name: String
)

data class Data(
    var ext_urls: List<String>,
    val title: String,
    val member_name: String, // pixiv
    val member_id: Int, // pixiv_id
    val characters: String,
    val creator: Any,
    val material: String,
    val sankaku_id: Int, // sankaku
    val source: String,
    val author_name: String,
    val part: String?,
    val author: String
)