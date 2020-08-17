package com.yinghaowu.saucenaosearch.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.google.android.material.snackbar.Snackbar
import com.yinghaowu.saucenaosearch.R
import com.yinghaowu.saucenaosearch.logic.model.Result
import kotlinx.android.synthetic.main.item_card.view.*

class SauceNaoAdapter(private val ctx: Context, private val sauceList: List<Result>) :
    RecyclerView.Adapter<SauceNaoAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var source: String? = null
        val imgMain: ImageView = view.imgMain
        val stTitle: TextView = view.stTitle
        val stTinyTitle: TextView = view.stTinyTitle
        val stSimilarity: TextView = view.stSimilarity
        val stSite: TextView = view.stSite
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card, parent, false)
        val holder = ViewHolder(view)
        holder.itemView.setOnClickListener {

            val source = holder.source
            if (source != null) {
                val sourceUri = source.toUri()
                if (sourceUri.toString().startsWith("http")) {
                    val openURI = sourceUri.toString()
                    val builder = CustomTabsIntent.Builder()
                    builder.setToolbarColor(view.context.getColor(R.color.lightColorPrimary))
                    val customTabsIntent = builder.build()
                    customTabsIntent.launchUrl(view.context, openURI.toUri())
                } else {
                    Snackbar.make(view, "this $source is not a URL", Snackbar.LENGTH_LONG).show()
                }
            } else {
                Snackbar.make(view, "There's NO source here!", Snackbar.LENGTH_LONG).show()
            }
        }

        holder.itemView.setOnLongClickListener {
            val link: Uri
            val source = holder.source
            if (source != null) {
                val sourceUri = source.toUri()
                if (sourceUri.toString().startsWith("http")) {
                    link = sourceUri
                    val sharedIntent = Intent.createChooser(Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, link.toString())
                        type = "text/*"
                    }, null)
                    view.context.startActivity(sharedIntent)
                } else {
                    Snackbar.make(view, "this $source is not a URL", Snackbar.LENGTH_LONG).show()
                }
            } else {
                Snackbar.make(view, "There's NO source here!", Snackbar.LENGTH_LONG).show()
            }
            return@setOnLongClickListener true
        }
        return holder
    }

    override fun getItemCount() = sauceList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val animation =
            AnimationUtils.loadAnimation(
                ctx,
                R.anim.anim_recycler_item_show
            )
        holder.itemView.startAnimation(animation)
        val aa1 = AlphaAnimation(1.0f, 0.1f)
        aa1.duration = 400
        holder.imgMain.startAnimation(aa1)

        val aa = AlphaAnimation(0.1f, 1.0f)
        aa.duration = 400
        holder.imgMain.startAnimation(aa)
        val result = sauceList[position]
        val format = ctx.getString(R.string.similarity)
        holder.imgMain.load(sauceList[position].header.thumbnail)
        val similarity = result.header.similarity
        holder.stSimilarity.text = String.format(format, "$similarity%")
        if (result.data.ext_urls.isNullOrEmpty()) {
            holder.stTitle.text = ctx.getString(R.string.isNull)
            holder.stTinyTitle.text = ""
            holder.stSite.text = ctx.getString(R.string.source_empty)
            holder.source = null
        } else {
            holder.source = result.data.ext_urls[0]
            val sourceURL = result.data.ext_urls[0].toUri()
            when (sourceURL.host) {
                "anidb.net" -> {
                    holder.stTitle.text = result.data.source
                    holder.stTinyTitle.text = ""
                    holder.stSite.text = ctx.getString(R.string.anidb)
                }
                "www.imdb.com" -> {
                    holder.stTitle.text = result.data.source
                    holder.stTinyTitle.text = result.data.part ?: ""
                    holder.stSite.text = ctx.getString(R.string.imdb)
                }
                "deviantart.com" -> {
                    holder.stTitle.text = result.data.title
                    holder.stTinyTitle.text = result.data.author_name
                    holder.stSite.text = ctx.getString(R.string.deviantart)
                }
                "www.pixiv.net" -> {
                    holder.stTitle.text = result.data.title
                    holder.stTinyTitle.text = result.data.member_name
                    holder.stSite.text = ctx.getString(R.string.pixiv)
                }
                "gelbooru.com" -> {
                    holder.stTitle.text = result.data.material
                    holder.stTinyTitle.text = result.data.characters
                    holder.stSite.text = ctx.getString(R.string.gelbooru)
                }
                "chan.sankakucomplex.com" -> {
                    holder.stTitle.text = result.data.material
                    holder.stTinyTitle.text = result.data.characters
                    holder.stSite.text = ctx.getString(R.string.sankakucomplex)
                }
                "yande.re" -> {
                    holder.stTitle.text = result.data.material
                    holder.stTinyTitle.text = result.data.creator.toString()
                    holder.stSite.text = ctx.getString(R.string.yande)
                }
                "danbooru.donmai.us" -> {
                    holder.stTitle.text = result.data.material
                    holder.stTinyTitle.text =
                        result.data.creator.toString()
                    holder.stSite.text = ctx.getString(R.string.danbooru)
                }
                "konachan.com" -> {
                    holder.stTitle.text = result.data.material
                    holder.stTinyTitle.text = result.data.creator.toString()
                    holder.stSite.text = ctx.getString(R.string.danbooru)
                }
                "mangadex.org" -> {
                    holder.stTitle.text = result.data.source
                    holder.stTinyTitle.text = result.data.author
                    holder.stSite.text = ctx.getString(R.string.mangadex)
                }
                "www.mangaupdates.com" -> {
                    holder.stTitle.text = result.data.source
                    holder.stTinyTitle.text = result.data.part
                    holder.stSite.text = ctx.getString(R.string.mangadex)
                }
                "seiga.nicovideo.jp" -> {
                    holder.stTitle.text = result.data.title
                    holder.stTinyTitle.text = result.data.member_name
                    holder.stSite.text = ctx.getString(R.string.nico)
                }
                "nijie.info" -> {
                    holder.stTitle.text = result.data.title
                    holder.stTinyTitle.text = result.data.member_name
                    holder.stSite.text = ctx.getString(R.string.nijie)
                }
                else -> {
                    holder.stTitle.text = ctx.getString(R.string.unknown_title)
                    holder.stTinyTitle.text = ""
                    holder.stSite.text = ctx.getString(R.string.unknown_site)
                }
            }
        }
    }

}
