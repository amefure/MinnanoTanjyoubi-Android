package com.amefure.minnanotanjyoubi.View.Compose.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.amefure.minnanotanjyoubi.BuildConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun BannerAdView(
    size: AdSize = AdSize.BANNER,
    @SuppressLint("ModifierParameter")
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(size)
                adUnitId = if (BuildConfig.DEBUG) {
                    BuildConfig.ADMOB_BANNER_ID_TEST
                } else {
                    BuildConfig.ADMOB_BANNER_ID_PROD
                }
                loadAd(AdRequest.Builder().build())
            }
        },
    )
}