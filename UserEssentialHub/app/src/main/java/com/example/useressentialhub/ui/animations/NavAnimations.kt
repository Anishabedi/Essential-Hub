package com.example.useressentialhub.ui.animations

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween

object NavAnimations {

    val enterFromRight: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(600)
        )
    }

    val exitToLeft: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(600)
        )
    }

    val enterFromLeft: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(600)
        )
    }

    val exitToRight: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(600)
        )
    }
}