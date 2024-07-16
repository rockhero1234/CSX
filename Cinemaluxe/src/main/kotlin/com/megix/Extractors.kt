package com.megix

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import java.net.URI

open class Sharepoint : ExtractorApi() {
    override val name: String = "Sharepoint"
    override val mainUrl: String = "https://indjatin-my.sharepoint.com"
    override val requiresReferer = false

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        callback.invoke(
            ExtractorLink(
                this.name,
                this.name,
                url,
                referer = "",
                quality = Qualities.Unknown.value
            )
        )
    }
}

open class GDFlix : ExtractorApi() {
    override val name: String = "GDFlix"
    override val mainUrl: String = "https://new2.gdflix.cfd"
    override val requiresReferer = false

    private suspend fun extractbollytag(url:String): String {
        val tagdoc= app.get(url).text
        val tags ="""\b\d{3,4}p\b""".toRegex().find(tagdoc) ?. value ?. trim() ?:""
        return tags
    }

    private suspend fun extractbollytag2(url:String): String {
        val tagdoc= app.get(url).text
        val tags ="""\b\d{3,4}p\b\s(.*?)\[""".toRegex().find(tagdoc) ?. groupValues ?. get(1) ?. trim() ?:""
        return tags
    }

    private fun getBaseUrl(url: String): String {
        return URI(url).let {
            "${it.scheme}://${it.host}"
        }
    }

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        var url = url
        val tags = extractbollytag(url)
        val tagquality = extractbollytag2(url)
        if (url.startsWith("https://new2.gdflix.cfd/goto/token/"))
        {
            val partialurl = app.get(url).text.substringAfter("replace(\"").substringBefore("\")")
            url = mainUrl + partialurl
        }
        else
        {
            url = url
        }
        app.get(url).document.select("div.text-center a").apmap {
            if (it.select("a").text().contains("FAST CLOUD DOWNLOAD"))
            {
                val link=it.attr("href")
                val trueurl=app.get("https://new2.gdflix.cfd$link").document.selectFirst("a.btn-success")?.attr("href") ?:""
                callback.invoke(
                    ExtractorLink(
                        "GDFlix[Fast Cloud]", "GDFLix[Fast Cloud] $tagquality", trueurl
                            ?: "", "", getQualityFromName(tags)
                    )
                )
            }
            else
            if (it.select("a").text().contains("Instant Download"))
            {
                val Instant_link=it.attr("href")
                val token = Instant_link.substringAfter("url=")
                val domain= getBaseUrl(Instant_link)
                val downloadlink = app.post(
                    url = "$domain/api",
                    data = mapOf(
                        "keys" to token
                    ),
                    referer = Instant_link,
                    headers = mapOf(
                        "x-token" to "direct.zencloud.lol",
                        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:125.0) Gecko/20100101 Firefox/125.0"
                    )
                )
                val finaldownloadlink =
                    downloadlink.toString().substringAfter("url\":\"")
                        .substringBefore("\",\"name")
                        .replace("\\/", "/")
                val link = finaldownloadlink
                callback.invoke(
                    ExtractorLink(
                        "GDFlix[Instant Download]",
                        "GDFlix[Instant Download] $tagquality",
                        url = link,
                        "",
                        getQualityFromName(tags)
                    )
                )
            }
            else
            {
                val link=it.attr("href")
            }
        }
    }
}