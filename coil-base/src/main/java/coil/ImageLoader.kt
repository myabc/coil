@file:Suppress("FunctionName", "NOTHING_TO_INLINE")

package coil

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.MainThread
import coil.bitmappool.BitmapPool
import coil.memory.PublicMemoryCache
import coil.request.ErrorResult
import coil.request.GetRequest
import coil.request.LoadRequest
import coil.request.Request
import coil.request.RequestDisposable
import coil.request.RequestResult
import coil.request.SuccessResult
import coil.target.Target

/**
 * A service class that loads images by executing [Request]s. Image loaders handle caching, data fetching,
 * image decoding, request management, bitmap pooling, memory management, and more.
 *
 * Image loaders are designed to be shareable and work best when you create a single instance and
 * share it throughout your app.
 *
 * It's recommended, though not required, to call [shutdown] when you've finished using an image loader.
 * This preemptively frees its memory and cleans up any observers.
 */
interface ImageLoader {

    companion object {
        /** Alias for [ImageLoaderBuilder]. */
        @JvmStatic
        @JvmName("builder")
        inline fun Builder(context: Context) = ImageLoaderBuilder(context)

        /** Create a new [ImageLoader] without configuration. */
        @JvmStatic
        @JvmName("create")
        inline operator fun invoke(context: Context) = ImageLoaderBuilder(context).build()
    }

    /**
     * The default options that are used to fill in unset [Request] values.
     */
    val defaults: DefaultRequestOptions

    /**
     * An in-memory cache of [Bitmap]s.
     */
    val memoryCache: PublicMemoryCache

    /**
     * An object pool of reusable [Bitmap]s.
     */
    val bitmapPool: BitmapPool

    /**
     * Launch an asynchronous operation that executes the [LoadRequest] and sets the result on its [Target].
     *
     * @param request The request to execute.
     * @return A [RequestDisposable] which can be used to cancel or check the status of the request.
     */
    fun execute(request: LoadRequest): RequestDisposable

    /**
     * Suspends and executes the [GetRequest]. Returns either [SuccessResult] or [ErrorResult] depending
     * on how the request completes.
     *
     * @param request The request to execute.
     * @return A [SuccessResult] if the request completes successfully. Else, returns an [ErrorResult].
     */
    suspend fun execute(request: GetRequest): RequestResult

    /**
     * Shutdown this image loader.
     *
     * All associated resources will be freed and any new requests will fail before starting.
     *
     * In progress [LoadRequest]s will be cancelled. In progress [GetRequest]s will continue until complete.
     */
    @MainThread
    fun shutdown()

    /**
     * Remove the value referenced by [key] from the memory cache.
     *
     * @param key The cache key to remove.
     */
    @Deprecated(
        message = "Call the memory cache directly.",
        replaceWith = ReplaceWith("memoryCache.remove(key)")
    )
    @MainThread
    fun invalidate(key: String) {
        memoryCache.remove(key)
    }

    /**
     * Clear this image loader's memory cache and bitmap pool.
     */
    @Deprecated(
        message = "Call the memory cache and bitmap pool directly.",
        replaceWith = ReplaceWith("" +
            "apply {\n" +
            "    memoryCache.clear()\n" +
            "    bitmapPool.clear()\n" +
            "}")
    )
    @MainThread
    fun clearMemory() {
        memoryCache.clear()
        bitmapPool.clear()
    }
}
