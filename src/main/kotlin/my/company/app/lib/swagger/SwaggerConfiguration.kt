package my.company.app.lib.swagger

import com.google.common.collect.ArrayListMultimap
import io.ktor.http.ContentType
import io.swagger.models.Contact
import io.swagger.models.Info
import io.swagger.models.License
import my.company.app.lib.swagger.SwaggerHelper.toModel
import org.springframework.http.HttpMethod
import springfox.documentation.builders.DocumentationBuilder
import springfox.documentation.schema.Model
import springfox.documentation.service.ApiDescription
import springfox.documentation.service.ApiListing
import springfox.documentation.service.Documentation
import springfox.documentation.service.Parameter
import springfox.documentation.service.Tag
import springfox.documentation.spring.web.json.JsonSerializer
import springfox.documentation.swagger2.configuration.Swagger2JacksonModule
import springfox.documentation.swagger2.mappers.ModelMapperImpl
import springfox.documentation.swagger2.mappers.ParameterMapperImpl
import springfox.documentation.swagger2.mappers.SecurityMapperImpl
import springfox.documentation.swagger2.mappers.ServiceModelToSwagger2Mapper
import springfox.documentation.swagger2.mappers.ServiceModelToSwagger2MapperImpl
import springfox.documentation.swagger2.mappers.VendorExtensionsMapperImpl
import kotlin.reflect.KClass

data class OperationContext(
    val method: HttpMethod,
    val path: String,
    val name: String
)

class SwaggerConfiguration {
    private val operationParameterInterceptors = mutableListOf<(OperationContext) -> List<Parameter>>()

    private val apiDescriptions = mutableListOf<ApiDescription>()
    private val tags = mutableSetOf<Tag>()
    private val models = mutableMapOf<String, Model>()

    fun registerModel(clazz: KClass<*>): Model {
        val model = toModel(clazz)

        val existingModel = models[model.name]
        if (existingModel != null) {
            if (existingModel.id != model.id) {
                throw IllegalStateException("Model with name ${model.name} already exists. Existing ModelId: ${existingModel.id}. New model id: ${model.id}")
            }
        } else {
            models[model.name] = model
        }

        return model
    }

    fun registerApi(apiDescription: ApiDescription) {
        apiDescriptions += apiDescription
    }

    fun registerTag(tag: Tag) {
        tags += tag
    }

    fun registerOperationParameterInterceptor(interceptor: (OperationContext) -> List<Parameter>): SwaggerConfiguration {
        operationParameterInterceptors += interceptor
        return this
    }

    fun applyInterceptors(context: OperationContext): List<Parameter> {
        return operationParameterInterceptors.flatMap { it(context) }
    }

    fun init(): Documentation {
        val apiListing = ApiListing(
            "apiVersion",
            "basePath",
            "resourcePath",
            setOf("prod"),
            setOf("consum"),
            "host",
            setOf("protocols"),
            mutableListOf(),
            apiDescriptions,
            models,
            "descritp",
            1,
            mutableSetOf()
        )
        val listings = ArrayListMultimap.create<String, ApiListing>()
        listings.put("main", apiListing)
        return DocumentationBuilder()
            .apiListingsByResourceGroupName(listings)
            .produces(setOf(ContentType.Application.Json.toString()))
            .consumes(setOf(ContentType.Application.Json.toString()))
            .tags(tags.sortedBy { it.name }.toMutableSet())
            .name("groupName")
            .build()
    }

    fun render(): String {
        val vendorExtensionMapper = VendorExtensionsMapperImpl()
        val parameterMapper = ParameterMapperImpl()
        val modelMapper = ModelMapperImpl()
        val securityMapper = SecurityMapperImpl()

        val mapper: ServiceModelToSwagger2Mapper = ServiceModelToSwagger2MapperImpl()
        mapper.javaClass.getDeclaredField("vendorExtensionsMapper").also {
            it.isAccessible = true
            it.set(mapper, vendorExtensionMapper)
        }
        mapper.javaClass.getDeclaredField("parameterMapper").also {
            it.isAccessible = true
            it.set(mapper, parameterMapper)
        }
        mapper.javaClass.getDeclaredField("modelMapper").also {
            it.isAccessible = true
            it.set(mapper, modelMapper)
        }
        mapper.javaClass.getDeclaredField("securityMapper").also {
            it.isAccessible = true
            it.set(mapper, securityMapper)
        }

        val doc = init()
        val swagger = mapper.mapDocumentation(doc)
        swagger.info = Info().also {
            it.title = "API Documentation"
            it.version = "<version>"
            it.contact = Contact()
            it.license = License().also {
                it.name = "<license>"
            }
        }

        val jsonSerializer = JsonSerializer(listOf(Swagger2JacksonModule()))
        val json = jsonSerializer.toJson(swagger)
        return json.value()
    }
}
