package my.company.app.web

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
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

class SwaggerContentConverter(objectMapper: ObjectMapper) : ContentConverter {
    private val jacksonConverter = JacksonConverter(objectMapper)

    override suspend fun convertForReceive(context: PipelineContext<ApplicationReceiveRequest, ApplicationCall>): Any? {
        if (!context.isSwaggerRequest()) return null
        return jacksonConverter.convertForReceive(context)
    }

    override suspend fun convertForSend(context: PipelineContext<Any, ApplicationCall>, contentType: ContentType, value: Any): Any? {
        if (!context.isSwaggerRequest()) return value
        val result = jacksonConverter.convertForSend(context, contentType, value)
        return result
    }
}

fun PipelineContext<*, ApplicationCall>.isSwaggerRequest(): Boolean {
    val uri = call.request.local.uri
    return uri.startsWith("/swagger")
}

fun ContentNegotiation.Configuration.swagger(
    contentType: ContentType = ContentType.Application.Json
) {
    val mapper = jacksonObjectMapper()
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL) // Swagger breaks when JSON includes the NULL values
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false) // Instead of throwing an exception, ignore additional json properties that don't exist in our DTOs
    register(contentType, SwaggerContentConverter(mapper))
}
