package coil

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import coil.annotation.ExperimentalCoilApi
import coil.decode.DataSource
import coil.decode.Decoder
import coil.decode.Options
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.map.Mapper
import coil.map.MeasuredMapper
import coil.request.ImageRequest
import coil.size.Size
import coil.size.SizeResolver
import coil.transform.Transformation
import coil.transition.Transition
import coil.transition.TransitionTarget

/**
 * An [ImageLoader]-scoped listener for tracking the progress of an image request.
 * This class is useful for measuring analytics, performance, or other metrics tracking.
 *
 * @see ImageLoader.Builder.eventListener
 */
@ExperimentalCoilApi
interface EventListener : ImageRequest.Listener {

    /**
     * Called immediately after the request is dispatched.
     */
    @MainThread
    fun onDispatch(request: ImageRequest) {}

    /**
     * Called before any [Mapper]s and/or [MeasuredMapper]s are called to convert the request's data.
     *
     * @param data The data that will be converted.
     */
    @MainThread
    fun mapStart(request: ImageRequest, data: Any) {}

    /**
     * Called after the request's data has been converted by all applicable [Mapper]s and/or [MeasuredMapper]s.
     *
     * @param mappedData The data after it has been converted.
     *  If there were no applicable mappers, [mappedData] will be the same as [ImageRequest.data].
     */
    @MainThread
    fun mapEnd(request: ImageRequest, mappedData: Any) {}

    /**
     * @see ImageRequest.Listener.onStart
     */
    @MainThread
    override fun onStart(request: ImageRequest) {}

    /**
     * Called before [SizeResolver.size].
     */
    @MainThread
    fun resolveSizeStart(request: ImageRequest) {}

    /**
     * Called after [SizeResolver.size].
     *
     * @param size The resolved [Size] for this request.
     */
    @MainThread
    fun resolveSizeEnd(request: ImageRequest, size: Size) {}

    /**
     * Called before [Fetcher.fetch].
     *
     * @param fetcher The [Fetcher] that will be used to handle the request.
     * @param options The [Options] that will be passed to [Fetcher.fetch].
     */
    @WorkerThread
    fun fetchStart(request: ImageRequest, fetcher: Fetcher<*>, options: Options) {}

    /**
     * Called after [Fetcher.fetch].
     *
     * @param fetcher The [Fetcher] that was used to handle the request.
     * @param options The [Options] that were passed to [Fetcher.fetch].
     */
    @WorkerThread
    fun fetchEnd(request: ImageRequest, fetcher: Fetcher<*>, options: Options) {}

    /**
     * Called before [Decoder.decode].
     *
     * This is skipped if [Fetcher.fetch] does not return a [SourceResult].
     *
     * @param decoder The [Decoder] that will be used to handle the request.
     * @param options The [Options] that will be passed to [Decoder.decode].
     */
    @WorkerThread
    fun decodeStart(request: ImageRequest, decoder: Decoder, options: Options) {}

    /**
     * Called after [Decoder.decode].
     *
     * This is skipped if [Fetcher.fetch] does not return a [SourceResult].
     *
     * @param decoder The [Decoder] that was used to handle the request.
     * @param options The [Options] that were passed to [Decoder.decode].
     */
    @WorkerThread
    fun decodeEnd(request: ImageRequest, decoder: Decoder, options: Options) {}

    /**
     * Called before any [Transformation]s are applied.
     *
     * This is skipped if [ImageRequest.transformations] is empty.
     */
    @WorkerThread
    fun transformStart(request: ImageRequest) {}

    /**
     * Called after any [Transformation]s are applied.
     *
     * This is skipped if [ImageRequest.transformations] is empty.
     */
    @WorkerThread
    fun transformEnd(request: ImageRequest) {}

    /**
     * Called before [Transition.transition].
     *
     * This is skipped if [ImageRequest.transition] is [Transition.NONE]
     * or [ImageRequest.target] does not implement [TransitionTarget].
     */
    @MainThread
    fun transitionStart(request: ImageRequest, transition: Transition) {}

    /**
     * Called after [Transition.transition].
     *
     * This is skipped if [ImageRequest.transition] is [Transition.NONE]
     * or [ImageRequest.target] does not implement [TransitionTarget].
     */
    @MainThread
    fun transitionEnd(request: ImageRequest, transition: Transition) {}

    /**
     * @see ImageRequest.Listener.onSuccess
     */
    @MainThread
    override fun onSuccess(request: ImageRequest, source: DataSource) {}

    /**
     * @see ImageRequest.Listener.onCancel
     */
    @MainThread
    override fun onCancel(request: ImageRequest) {}

    /**
     * @see ImageRequest.Listener.onError
     */
    @MainThread
    override fun onError(request: ImageRequest, throwable: Throwable) {}

    /** A factory that creates new [EventListener] instances. */
    interface Factory {

        companion object {
            @JvmField val NONE = Factory(EventListener.NONE)

            /** Create an [EventListener.Factory] that always returns [listener]. */
            @JvmStatic
            @JvmName("create")
            operator fun invoke(listener: EventListener): Factory {
                return object : Factory {
                    override fun create(request: ImageRequest) = listener
                }
            }
        }

        /** Return a new [EventListener]. */
        fun create(request: ImageRequest): EventListener
    }

    companion object {
        @JvmField val NONE = object : EventListener {}
    }
}
