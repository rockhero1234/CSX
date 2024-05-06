package com.megix

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class VegaMovies: Plugin() {
    override fun load(context: Context) {
        // All providers should be added in this manner. Please don't edit the providers list directly.
        registerMainAPI(VegaMoviesProvider())
        registerMainAPI(LuxMoviesProvider())
        registerExtractorAPI(VCloud())
        registerExtractorAPI(Gofile())
        //registerExtractorAPI(PixelDrain())
    }
}
