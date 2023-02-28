package com.vitorpamplona.amethyst.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.baha.url.preview.IUrlPreviewCallback
import com.baha.url.preview.UrlInfoItem
import com.vitorpamplona.amethyst.R
import com.vitorpamplona.amethyst.model.UrlCachedPreviewer


@Composable
fun UrlPreview(url: String, urlText: String, showUrlIfError: Boolean = true) {
  var urlPreviewState by remember { mutableStateOf<UrlPreviewState>(UrlPreviewState.Loading) }
  var context = LocalContext.current
  // Doesn't use a viewModel because of viewModel reusing issues (too many UrlPreview are created).
  LaunchedEffect(url) {
    UrlCachedPreviewer.previewInfo(url, object : IUrlPreviewCallback {
      override fun onComplete(urlInfo: UrlInfoItem) {
        if (urlInfo.allFetchComplete() && urlInfo.url == url)
          urlPreviewState = UrlPreviewState.Loaded(urlInfo)
        else
          urlPreviewState = UrlPreviewState.Empty
      }

      override fun onFailed(throwable: Throwable) {
        urlPreviewState = UrlPreviewState.Error(context.getString(R.string.error_parsing_preview_for, url, throwable.message))
      }
    })
  }

  Crossfade(targetState = urlPreviewState, animationSpec = tween(durationMillis = 100)) { state ->
    when (state) {
      is UrlPreviewState.Loaded -> {
        UrlPreviewCard(url, state.previewInfo)
      }
      else -> {
        if (showUrlIfError) {
          ClickableUrl(urlText, url)
        }
      }
    }
  }

}

