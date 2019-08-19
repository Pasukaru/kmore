package my.company.app.web

import com.google.common.base.Optional
import com.google.common.collect.ArrayListMultimap
import io.ktor.http.HttpStatusCode
import my.company.app.lib.koin.lazy
import my.company.app.lib.swagger.OperationContext
import my.company.app.lib.swagger.SwaggerConfiguration
import my.company.app.lib.swagger.SwaggerHelper
import org.springframework.http.HttpMethod
import springfox.documentation.schema.ModelRef
import springfox.documentation.service.ApiDescription
import springfox.documentation.service.Operation
import springfox.documentation.service.Parameter
import springfox.documentation.service.ResponseMessage
import kotlin.reflect.KClass

interface SwaggerOperationBuilder {
    fun build(): ApiDescription
}

abstract class AbstractOperationBuilder<SELF : AbstractOperationBuilder<SELF>> : SwaggerOperationBuilder {
    @Suppress("UNCHECKED_CAST")
    protected val self: SELF
        get() = this as SELF

    protected val swagger: SwaggerConfiguration by lazy()

    protected lateinit var name: String
    protected lateinit var path: String
    protected lateinit var method: HttpMethod
    protected val tags = mutableListOf<String>()
    protected val parameters = mutableListOf<Parameter>()
    protected val responses = mutableListOf<ResponseMessage>()

    override fun build(): ApiDescription {
        val additionalParameters = swagger.applyInterceptors(OperationContext(
            method = method,
            path = path,
            name = name
        ))

        return ApiDescription(
            "",
            path,
            "",
            mutableListOf(Operation(
                method,
                name,
                null,
                null,
                null,
                0,
                tags.toSet(),
                setOf(),
                setOf(),
                setOf(),
                mutableListOf(),
                parameters + additionalParameters,
                responses.toSet(),
                null,
                false,
                setOf()
            )),
            false
        )
    }

    fun tag(tag: String): SELF {
        tags += tag
        return self
    }

    inline fun <reified T : Any> req() = req(T::class)

    fun req(type: KClass<*>): SELF {
        val model = swagger.registerModel(type)

        parameters += Parameter(
            "body",
            "",
            "",
            true,
            false,
            false,
            ModelRef(model.name),
            Optional.absent(),
            null,
            "body",
            "",
            false,
            null,
            null,
            0,
            null,
            ArrayListMultimap.create(),
            mutableListOf()
        )

        return self
    }

    inline fun <reified T : Any> query() = query(T::class)
    fun query(type: KClass<*>): SELF {
        val model = SwaggerHelper.toModel(type)

        model.properties.forEach { (_, property) ->
            parameters += Parameter(
                property.name,
                property.description,
                property.defaultValue,
                property.isRequired,
                false,
                property.isAllowEmptyValue,
                property.modelRef,
                Optional.of(property.type),
                property.allowableValues,
                "query",
                "",
                property.isHidden,
                property.pattern,
                null,
                0,
                property.example,
                ArrayListMultimap.create(),
                property.vendorExtensions
            )
        }

        return self
    }

    fun res(response: ResponseMessage): SELF {
        responses += response
        return self
    }

    fun res(statusCode: HttpStatusCode, type: KClass<*>): SELF {
        val model = swagger.registerModel(type)
        responses.add(ResponseMessage(
            statusCode.value,
            model.name,
            ModelRef(model.name),
            mutableMapOf(),
            mutableListOf()
        ))
        return self
    }

    fun resList(statusCode: HttpStatusCode, type: KClass<*>): SELF {
        val model = swagger.registerModel(type)
        responses.add(ResponseMessage(
            statusCode.value,
            model.name,
            ModelRef("array", ModelRef(model.name)),
            mutableMapOf(),
            mutableListOf()
        ))
        return self
    }
}

class Post(
    path: String,
    name: String
) : AbstractOperationBuilder<Post>() {

    init {
        method = HttpMethod.POST
        this.path = path
        this.name = name
    }

    inline fun <reified T : Any> res() = res(T::class)
    fun res(type: KClass<*>) = res(HttpStatusCode.Created, type)

    inline fun <reified T : Any> resList() = resList(T::class)
    fun resList(type: KClass<*>) = resList(HttpStatusCode.Created, type)
}

class Get(
    path: String,
    name: String
) : AbstractOperationBuilder<Get>() {

    init {
        method = HttpMethod.GET
        this.path = path
        this.name = name
    }

    inline fun <reified T : Any> res() = res(T::class)
    fun res(type: KClass<*>) = res(HttpStatusCode.OK, type)

    inline fun <reified T : Any> resList() = resList(T::class)
    fun resList(type: KClass<*>) = resList(HttpStatusCode.OK, type)
}
