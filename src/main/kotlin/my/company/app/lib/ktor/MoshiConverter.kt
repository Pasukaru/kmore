package my.company.app.lib.ktor

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.features.ContentConverter
import io.ktor.features.ContentNegotiation
import io.ktor.features.suitableCharset
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import io.ktor.http.withCharset
import io.ktor.request.ApplicationReceiveRequest
import io.ktor.request.contentCharset
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.io.ByteReadChannel
import kotlinx.coroutines.io.jvm.javaio.toInputStream
import my.company.app.lib.InvalidJsonException
import my.company.app.lib.moshi.UUIDAdapter
import java.io.IOException
import kotlin.reflect.full.isSubclassOf

class MoshiConverter(private val moshi: Moshi) : ContentConverter {
    override suspend fun convertForSend(context: PipelineContext<Any, ApplicationCall>, contentType: ContentType, value: Any): Any? {
        val clazz = if (value::class.isSubclassOf(List::class)) List::class else value::class
        @Suppress("EXPERIMENTAL_API_USAGE")
        return TextContent(moshi.adapter<Any>(clazz.java).toJson(value), contentType.withCharset(context.call.suitableCharset()))
    }

    override suspend fun convertForReceive(context: PipelineContext<ApplicationReceiveRequest, ApplicationCall>): Any? {
        val request = context.subject
        val type = request.type
        val value = request.value as? ByteReadChannel ?: return null
        val json = value.toInputStream().reader(context.call.request.contentCharset() ?: Charsets.UTF_8).use { it.readText() }
        try {
            val adapter = moshi.adapter(type.javaObjectType)
            @Suppress("BlockingMethodInNonBlockingContext")
            return adapter.fromJson(json)
        } catch (e: IOException) {
            throw InvalidJsonException(e)
        }
    }
}

fun ContentNegotiation.Configuration.moshi(
    contentType: ContentType = ContentType.Application.Json,
    block: Moshi.Builder.() -> Unit = {}
): Moshi {
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .add(UUIDAdapter())
        .apply(block)
        .build()

    register(contentType, MoshiConverter(moshi))
    return moshi
}
