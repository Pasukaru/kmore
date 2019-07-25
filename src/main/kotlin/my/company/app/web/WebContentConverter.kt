package my.company.app.web

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.features.ContentConverter
import io.ktor.features.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.jackson.JacksonConverter
import io.ktor.request.ApplicationReceiveRequest
import io.ktor.util.pipeline.PipelineContext
import my.company.app.web.controller.WebLocation

class WebContentConverter(objectMapper: ObjectMapper) : ContentConverter {
    private val jacksonConverter = JacksonConverter(objectMapper)

    override suspend fun convertForReceive(context: PipelineContext<ApplicationReceiveRequest, ApplicationCall>): Any? {
        if (!context.isWebRequest()) return null
        return jacksonConverter.convertForReceive(context)
    }

    override suspend fun convertForSend(context: PipelineContext<Any, ApplicationCall>, contentType: ContentType, value: Any): Any? {
        if (!context.isWebRequest()) return null
        return jacksonConverter.convertForSend(context, contentType, value)
    }
}

fun PipelineContext<*, ApplicationCall>.isWebRequest(): Boolean {
    return call.isWebRequest()
}

fun ApplicationCall.isWebRequest(): Boolean {
    return request.local.uri.startsWith(WebLocation.PATH)
}

fun PipelineContext<*, ApplicationCall>.isSwaggerRequest(): Boolean {
    val uri = call.request.local.uri
    return uri.startsWith("/swagger")
}

fun ContentNegotiation.Configuration.jacksonWeb(
    contentType: ContentType = ContentType.Application.Json
): ObjectMapper {
    val mapper = jacksonObjectMapper()
    mapper.apply {
        setDefaultPrettyPrinter(DefaultPrettyPrinter().apply {
            indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance)
            indentObjectsWith(DefaultIndenter("  ", "\n"))
        })

    }
    val converter = WebContentConverter(mapper)
    register(contentType, converter)
    return mapper
}
