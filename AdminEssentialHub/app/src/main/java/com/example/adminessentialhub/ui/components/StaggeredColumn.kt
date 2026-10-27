package com.example.adminessentialhub.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlin.random.Random
import kotlinx.coroutines.delay

@Composable
fun ScatteredBackground(images: List<Int>) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        AutoScrollColumn(
            images = images,
            seed = 1,
            pixelsPerSecond = 160f,
            modifier = Modifier.weight(1f)
        )
        AutoScrollColumn(
            images = images,
            seed = 2,
            pixelsPerSecond = 80f,
            modifier = Modifier.weight(1f)
        )
        AutoScrollColumn(
            images = images,
            seed = 3,
            pixelsPerSecond = 100f,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun AutoScrollColumn(
    images: List<Int>,
    seed: Int,
    pixelsPerSecond: Float,
    modifier: Modifier = Modifier
) {
    val itemHeight = 110.dp
    val spacing = 24.dp   // Fixed - do images ke beech ka max gap, isse zyada nahi hoga

    // Same list ko baar baar use karenge - modulo se infinite loop milega
    val randomImages = remember(images, seed) {
        val random = Random(seed)
        List(images.size) { images[random.nextInt(images.size)] }
    }

    val listState = rememberLazyListState()

    // Continuous, frame-by-frame scroll - app khulte hi shuru, band hone tak chalta rahega
    LaunchedEffect(seed) {
        delay(650)
        var lastFrameTime = withFrameNanos { it }

        while (true) {
            val frameTime = withFrameNanos { it }
            val deltaSeconds = (frameTime - lastFrameTime) / 1_000_000_000f
            lastFrameTime = frameTime

            val deltaPixels = deltaSeconds * pixelsPerSecond
            listState.scrollBy(deltaPixels)
        }
    }

    Box(modifier = modifier.fillMaxHeight()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = false   // User manually touch karke scroll na kar paye
        ) {
            items(Int.MAX_VALUE) { index ->
                // index % list.size => list khatam hote hi wapas 0th se repeat, koi gap nahi
                val imageRes = randomImages[index % randomImages.size]
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .background(Color.White, RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.height(spacing))
            }
        }
    }
}