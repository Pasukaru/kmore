package my.company.app.lib

import de.nielsfalk.ktor.swagger.CustomContentTypeResponse
import de.nielsfalk.ktor.swagger.ResponseType
import io.ktor.http.ContentType

fun plain(): ResponseType = CustomContentTypeResponse(ContentType.Text.Plain)
