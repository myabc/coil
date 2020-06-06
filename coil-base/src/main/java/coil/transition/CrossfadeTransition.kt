package coil.transition

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import coil.annotation.ExperimentalCoilApi
import coil.decode.DataSource
import coil.drawable.CrossfadeDrawable
import coil.request.ErrorResult
import coil.request.RequestResult
import coil.request.SuccessResult
import coil.size.Scale
import coil.util.scale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** A [Transition] that crossfades from the current drawable to a new one. */
@ExperimentalCoilApi
class CrossfadeTransition @JvmOverloads constructor(
    val durationMillis: Int = CrossfadeDrawable.DEFAULT_DURATION
) : Transition {

    init {
        require(durationMillis > 0) { "durationMillis must be > 0." }
    }

    override suspend fun transition(
        target: TransitionTarget<*>,
        result: RequestResult
    ) {
        // Don't animate if the request was fulfilled by the memory cache.
        if (result is SuccessResult && result.source == DataSource.MEMORY_CACHE) {
            target.onSuccess(result.drawable)
            return
        }

        // Don't animate if the view is not visible as CrossfadeDrawable.onDraw
        // won't be called until the view becomes visible.
        if (!target.view.isVisible) {
            when (result) {
                is SuccessResult -> target.onSuccess(result.drawable)
                is ErrorResult -> target.onError(result.drawable)
            }
            return
        }

        // Animate the drawable and suspend until the animation is completes.
        var outerCrossfade: CrossfadeDrawable? = null
        try {
            suspendCancellableCoroutine<Unit> { continuation ->
                val innerCrossfade = CrossfadeDrawable(
                    start = target.drawable,
                    end = result.drawable,
                    scale = (target.view as? ImageView)?.scale ?: Scale.FILL,
                    durationMillis = durationMillis
                )
                outerCrossfade = innerCrossfade
                innerCrossfade.registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
                    override fun onAnimationEnd(drawable: Drawable?) {
                        innerCrossfade.unregisterAnimationCallback(this)
                        continuation.resume(Unit)
                    }
                })
                when (result) {
                    is SuccessResult -> target.onSuccess(innerCrossfade)
                    is ErrorResult -> target.onError(innerCrossfade)
                }
            }
        } catch (_: CancellationException) {
            // Ensure cancellation is handled on the main thread.
            outerCrossfade?.stop()
        }
    }
}
