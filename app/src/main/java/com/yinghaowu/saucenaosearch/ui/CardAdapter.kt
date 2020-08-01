package com.yinghaowu.saucenaosearch.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.snackbar.Snackbar
import com.yinghaowu.saucenaosearch.R
import com.yinghaowu.saucenaosearch.logic.model.JsonFeeds
import kotlinx.android.synthetic.main.item_card.view.*

val pixivImageServer = arrayOf(
    "i.pximg.net",
    "i1.pixiv.net",
    "i2.pixiv.net",
    "i3.pixiv.net",
    "i4.pixiv.net",
    "img51.pixiv.net",
    "img43.pixiv.net")

class CardAdapter(
    private val jsonFeeds: JsonFeeds,
    private var ctx: Context
) : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val cardForRow = layoutInflater.inflate(R.layout.item_card, parent, false)
        return ViewHolder(cardForRow)
    }
    override fun getItemCount() = jsonFeeds.results.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val format = ctx.getString(R.string.similarity)
        val similarity = jsonFeeds.results[position].header.similarity
        holder.view.stSimilarity.text = String.format(format, "$similarity%")
        Glide.with(ctx)
            .load(jsonFeeds.results[position].header.thumbnail)
            .into(holder.view.imgMain)

        //Danbooru 9 / Gelbooru / yande.re 12
        val index3rd = arrayOf(9, 12, 26)
        if (jsonFeeds.results[position].header.index_id in index3rd) {
            holder.view.stTitle.text = jsonFeeds.results[0].data.title ?: ctx.getString(R.string.title_empty)
            holder.view.stTinyTitle.text = jsonFeeds.results[position].data.creator.toString()
            val sourceURL = jsonFeeds.results[position].data.source.toUri()
            when (sourceURL.host) {
                in pixivImageServer -> {
                    holder.view.stSite.text = ctx.getString(R.string.pixiv)
                }
                "www.pixiv.net" -> {
                    holder.view.stSite.text = ctx.getString(R.string.pixiv)
                }
                "twitter.com" -> holder.view.stSite.text = ctx.getString(R.string.twitter)
                "exhentai.org" -> holder.view.stSite.text = ctx.getString((R.string.exhentai))
                else -> {
                    holder.view.stSite.text = ctx.getString(R.string.source_empty)
                    Log.d("nonAddedLink",
                        "onBindViewHolder: ${jsonFeeds.results[position].data.source}")
                    Log.d("nonAddedLink", "index_id: ${jsonFeeds.results[position].header.index_id}")
                    Toast.makeText(ctx, jsonFeeds.results[position].data.source, Toast.LENGTH_SHORT).show()
                }
            }
            holder.ext_urls = jsonFeeds.results[position].data.ext_urls
            holder.source = jsonFeeds.results[position].data.source
        }
        // nico
        if (jsonFeeds.results[position].header.index_id == 8) {
            holder.view.stTitle.text = jsonFeeds.results[position].data.title
            holder.view.stTinyTitle.text = jsonFeeds.results[position].data.member_name
            holder.view.stSite.text = ctx.getString(R.string.nico)
            holder.source = jsonFeeds.results[position].data.ext_urls[0]
        }
        // Nijie
        if (jsonFeeds.results[position].header.index_id == 11) {
            holder.view.stTitle.text = jsonFeeds.results[position].data.title
            holder.view.stTinyTitle.text = jsonFeeds.results[position].data.member_name
            holder.view.stSite.text = ctx.getString(R.string.nijie)
            holder.source = jsonFeeds.results[position].data.ext_urls[0]
        }
        if (jsonFeeds.results[position].data.ext_urls.isNullOrEmpty()) {
            holder.view.stTitle.text = ctx.getString(R.string.isNull)
            holder.view.stTinyTitle.text = ""
            holder.view.stSite.text = ctx.getString(R.string.source_empty)
            Log.d("nullExt_urls", "indexId: ${jsonFeeds.results[position].header.index_id}")
        } else {
            val notElse = arrayOf(9, 12, 8, 11)
            if (jsonFeeds.results[position].header.index_id !in notElse) {
                holder.source = jsonFeeds.results[position].data.ext_urls[0]
                val sourceURL = jsonFeeds.results[position].data.ext_urls[0].toUri()
                when (sourceURL.host) {
                    "anidb.net" -> {
                        holder.view.stTitle.text = jsonFeeds.results[position].data.source
                        holder.view.stTinyTitle.text = ""
                        holder.view.stSite.text = ctx.getString(R.string.anidb)
                    }
                    "www.imdb.com" -> {
                        holder.view.stTitle.text = jsonFeeds.results[position].data.source
                        holder.view.stTinyTitle.text = jsonFeeds.results[position].data.part ?: ""
                        holder.view.stSite.text = ctx.getString(R.string.imdb)
                    }
                    "deviantart.com" -> {
                        holder.view.stTitle.text = jsonFeeds.results[position].data.title
                        holder.view.stTinyTitle.text = jsonFeeds.results[position].data.author_name
                        holder.view.stSite.text = ctx.getString(R.string.deviantart)
                    }
                    "www.pixiv.net" -> {
                        holder.view.stTitle.text = jsonFeeds.results[position].data.title
                        val pixivTitle = holder.view.stTitle.text
                        holder.view.stTinyTitle.text = jsonFeeds.results[position].data.member_name
                        holder.view.stSite.text = ctx.getString(R.string.pixiv)
                    }
                    "gelbooru.com" -> {
                        holder.view.stTitle.text = jsonFeeds.results[position].data.material
                        holder.view.stTinyTitle.text = jsonFeeds.results[position].data.characters
                        holder.view.stSite.text = ctx.getString(R.string.gelbooru)
                    }
                    "chan.sankakucomplex.com" -> {
                        holder.view.stTitle.text = jsonFeeds.results[position].data.material
                        holder.view.stTinyTitle.text = jsonFeeds.results[position].data.characters
                        holder.view.stSite.text = ctx.getString(R.string.sankakucomplex)
                    }
                    else -> {
                        holder.view.stTitle.text = ctx.getString(R.string.unknown_title)
                        holder.view.stTinyTitle.text = ""
                        holder.view.stSite.text = ctx.getString(R.string.unknown_site)
                    }
                }
            }
        }
    }
    }



class ViewHolder(val view: View, var source: String? = null, var ext_urls: List<String>? = null)
    : RecyclerView.ViewHolder(view) {
    init {
        view.setOnClickListener {
            var openURI = ""
            if (source != null) {
                val sourceUri = source!!.toUri()
                if (sourceUri.toString().startsWith("http")) {
                    openURI = when (sourceUri.host) {
                        in pixivImageServer -> "https://www.pixiv.net/member_illust.php?mode=medium&illust_id=${sourceUri.lastPathSegment?.split(
                            "_"
                        )?.first()}"
                        else -> sourceUri.toString()
                    }
                    val builder = CustomTabsIntent.Builder()
                    builder.setToolbarColor(view.context.getColor(R.color.lightColorPrimary))
                    val customTabsIntent = builder.build()
                    customTabsIntent.launchUrl(view.context, openURI.toUri())
                } else {
                    Snackbar.make(view, "This source $source, is not URL", Snackbar.LENGTH_LONG).show()
                }
            } else {
                Snackbar.make(view, "There's NO source here!", Snackbar.LENGTH_LONG).show()
            }
        }
        view.setOnLongClickListener {
            val link: Uri
            if (source != null) {
                val sourceURI = source!!.toUri()
                if (sourceURI.toString().startsWith("http")) {
                    link = when (sourceURI.host) {
                        in pixivImageServer ->
                            "https://www.pixiv.net/member_illust.php?mode=medium&illust_id=${sourceURI.lastPathSegment?.split(
                                "_"
                            )?.first()}"
                                .toUri()
                        else -> sourceURI
                    }
                    val sharedIntent = Intent.createChooser( Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, link.toString())
                        type = "text/*"

                    }, null)
                    view.context.startActivity(sharedIntent)
                } else {
                    Snackbar.make(view, "This source $source, is not URL", Snackbar.LENGTH_LONG).show()
                }
            } else {
                Snackbar.make(view, "There's NO source here!", Snackbar.LENGTH_LONG).show()
            }
            return@setOnLongClickListener true
        }
    }
}
