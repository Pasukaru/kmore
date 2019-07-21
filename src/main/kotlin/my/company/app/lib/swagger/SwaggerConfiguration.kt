package my.company.app.lib.swagger

import com.fasterxml.classmate.TypeResolver
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.common.base.Optional
import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.ImmutableSet
import io.swagger.models.properties.Property
import io.swagger.models.properties.StringProperty
import org.springframework.http.HttpMethod
import org.springframework.plugin.core.PluginRegistry
import org.springframework.plugin.core.PluginRegistrySupport
import springfox.documentation.builders.DocumentationBuilder
import springfox.documentation.builders.ModelBuilder
import springfox.documentation.builders.ModelPropertyBuilder
import springfox.documentation.schema.DefaultGenericTypeNamingStrategy
import springfox.documentation.schema.DefaultModelDependencyProvider
import springfox.documentation.schema.DefaultModelProvider
import springfox.documentation.schema.JacksonEnumTypeDeterminer
import springfox.documentation.schema.Model
import springfox.documentation.schema.ModelProperty
import springfox.documentation.schema.ModelRef
import springfox.documentation.schema.ModelReference
import springfox.documentation.schema.TypeNameExtractor
import springfox.documentation.schema.configuration.ObjectMapperConfigured
import springfox.documentation.schema.plugins.SchemaPluginsManager
import springfox.documentation.schema.property.FactoryMethodProvider
import springfox.documentation.schema.property.ObjectMapperBeanPropertyNamingStrategy
import springfox.documentation.schema.property.OptimizedModelPropertiesProvider
import springfox.documentation.schema.property.bean.AccessorsProvider
import springfox.documentation.schema.property.field.FieldProvider
import springfox.documentation.service.AllowableListValues
import springfox.documentation.service.ApiDescription
import springfox.documentation.service.ApiListing
import springfox.documentation.service.Documentation
import springfox.documentation.service.Operation
import springfox.documentation.service.Parameter
import springfox.documentation.service.ResponseMessage
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.schema.AlternateTypeProvider
import springfox.documentation.spi.schema.ModelBuilderPlugin
import springfox.documentation.spi.schema.ModelPropertyBuilderPlugin
import springfox.documentation.spi.schema.SyntheticModelProviderPlugin
import springfox.documentation.spi.schema.TypeNameProviderPlugin
import springfox.documentation.spi.schema.contexts.ModelContext
import springfox.documentation.spring.web.json.JsonSerializer
import springfox.documentation.swagger.schema.ApiModelBuilder
import springfox.documentation.swagger.schema.ApiModelTypeNameProvider
import springfox.documentation.swagger2.configuration.Swagger2JacksonModule
import springfox.documentation.swagger2.mappers.ModelMapperImpl
import springfox.documentation.swagger2.mappers.ParameterMapperImpl
import springfox.documentation.swagger2.mappers.SecurityMapperImpl
import springfox.documentation.swagger2.mappers.ServiceModelToSwagger2Mapper
import springfox.documentation.swagger2.mappers.ServiceModelToSwagger2MapperImpl
import springfox.documentation.swagger2.mappers.VendorExtensionsMapperImpl
import java.lang.reflect.ParameterizedType
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMemberProperties

data class ExampleModel(
    val name: String
)

fun toModel(model: KClass<*>): Pair<String, Model> {
    val builder = ModelBuilder()
        .type(typeResolver.resolve(model.java))
        .properties(model.declaredMemberProperties.associate {
            it.name to toModelProperty(it)
        }.toMap())

    return model.simpleName!! to builder.build()
}

fun toModel2(model: KClass<*>): Pair<String, Model> {
    val typeParameters = (model.java.genericSuperclass as ParameterizedType).actualTypeArguments

    val builder = ModelBuilder()
        .type(typeResolver.resolve(model.java, *typeParameters))
        .properties(model.declaredMemberProperties.associate {
            it.name to toModelProperty(it)
        }.toMap())

    return model.simpleName!! + "List" to builder.build()
}

fun toProperty(prop: KProperty<*>): Property {
    return when (prop.returnType.classifier) {
        String::class -> StringProperty()
        else -> throw IllegalStateException("Unsupported type: $prop")
    }
}

fun toModelProperty(prop: KProperty<*>): ModelProperty {
    val clazz = (prop.returnType.classifier as KClass<*>).java
    val property = ModelPropertyBuilder()
        .type(typeResolver.resolve(clazz))
        .required(false)
        .isHidden(false)
        .name(prop.name)
        .qualifiedType(clazz.canonicalName)
        .readOnly(false)
        .description("Description for ${prop.name}")
        .build()

    property.updateModelRef { _ ->
        ModelRef("string")
    }

    return property
}

val typeResolver = TypeResolver()

class SwaggerConfiguration(
    val models: Map<String, Model>
) {

    fun init(): Documentation {
        val listings = ArrayListMultimap.create<String, ApiListing>()
        listings.put("apilistname", apiListing())
        return DocumentationBuilder()
            .apiListingsByResourceGroupName(listings)
            .produces(setOf("PRODUCE-JSON"))
            .consumes(setOf("CONSUME-JSON"))
            .name("groupName")
            .build()
    }

    fun apiListing(): ApiListing {
        return ApiListing(
            "apiVersion",
            "basePath",
            "resourcePath",
            setOf("prod"),
            setOf("consum"),
            "host",
            setOf("protocols"),
            mutableListOf(),
            listOf(apiDesc()),
            models,
            "descritp",
            1,
            mutableSetOf()
        )
    }

    fun apiDesc(): ApiDescription {
        return ApiDescription(
            "groupName",
            "/path",
            "Descritpion",
            mutableListOf(operation()),
            false
        )
    }

    fun operation(): Operation {
        return Operation(
            HttpMethod.DELETE,
            "summary",
            "notes",
            modelRef(),
            "uniqueid",
            1,
            setOf(),
            setOf(),
            setOf(),
            setOf(),
            mutableListOf(),
            listOf(param()),
            setOf(ResponseMessage(
                101,
                "message",
                modelRef(),
                mutableMapOf(),
                mutableListOf()
            )),
            "deprecated",
            false,
            setOf()
        )
    }

    fun param(): Parameter {
        return Parameter(
            "name",
            "description",
            "defaultValue",
            true,
            false,
            true,
            modelRef(),
            Optional.absent(),
            AllowableListValues(listOf(), "string"),
            "body",
            "paramAccess",
            false,
            null,
            "",
            1,
            null,
            ArrayListMultimap.create(),
            mutableListOf()
        )
    }

    fun modelRef(): ModelReference {
        return ModelRef("ExampleModel")
    }
}

fun main() {
    val typeNameProviderPlugin: TypeNameProviderPlugin = ApiModelTypeNameProvider()
    val typeNameProviders: PluginRegistry<TypeNameProviderPlugin, DocumentationType> = object : PluginRegistrySupport<TypeNameProviderPlugin, DocumentationType>(emptyList()) {
        override fun contains(plugin: TypeNameProviderPlugin?): Boolean {
            return true
        }

        override fun countPlugins(): Int {
            return 1
        }

        override fun hasPluginFor(delimiter: DocumentationType?): Boolean {
            return true
        }

        override fun getPluginFor(delimiter: DocumentationType?): TypeNameProviderPlugin {
            return typeNameProviderPlugin
        }

        override fun <E : Exception?> getPluginFor(delimiter: DocumentationType?, ex: E): TypeNameProviderPlugin {
            return typeNameProviderPlugin
        }

        override fun getPluginFor(delimiter: DocumentationType?, plugin: TypeNameProviderPlugin?): TypeNameProviderPlugin {
            return typeNameProviderPlugin
        }

        override fun getPluginsFor(delimiter: DocumentationType?): MutableList<TypeNameProviderPlugin> {
            return mutableListOf(typeNameProviderPlugin)
        }

        override fun <E : Exception?> getPluginsFor(delimiter: DocumentationType?, ex: E): MutableList<TypeNameProviderPlugin> {
            return mutableListOf(typeNameProviderPlugin)
        }

        override fun getPluginsFor(delimiter: DocumentationType?, plugins: MutableList<out TypeNameProviderPlugin>?): MutableList<TypeNameProviderPlugin> {
            return mutableListOf(typeNameProviderPlugin)
        }
    }
    val typeNameExtractor = TypeNameExtractor(typeResolver, typeNameProviders, JacksonEnumTypeDeterminer())

    val enumTypeDeterminer = JacksonEnumTypeDeterminer()
    val alternateTypeProvider = AlternateTypeProvider(listOf())
    val genericTypeNamingStrategy = DefaultGenericTypeNamingStrategy()

    val modelBuilderPlugin: ModelBuilderPlugin = ApiModelBuilder(typeResolver, typeNameExtractor)

    val modelEnrichters: PluginRegistry<ModelBuilderPlugin, DocumentationType> = object : PluginRegistrySupport<ModelBuilderPlugin, DocumentationType>(emptyList()) {
        override fun contains(plugin: ModelBuilderPlugin?): Boolean {
            return true
        }

        override fun countPlugins(): Int {
            return 1
        }

        override fun hasPluginFor(delimiter: DocumentationType?): Boolean {
            return true
        }

        override fun getPluginFor(delimiter: DocumentationType?): ModelBuilderPlugin {
            return modelBuilderPlugin
        }

        override fun <E : Exception?> getPluginFor(delimiter: DocumentationType?, ex: E): ModelBuilderPlugin {
            return modelBuilderPlugin
        }

        override fun getPluginFor(delimiter: DocumentationType?, plugin: ModelBuilderPlugin?): ModelBuilderPlugin {
            return modelBuilderPlugin
        }

        override fun getPluginsFor(delimiter: DocumentationType?): MutableList<ModelBuilderPlugin> {
            return mutableListOf(modelBuilderPlugin)
        }

        override fun <E : Exception?> getPluginsFor(delimiter: DocumentationType?, ex: E): MutableList<ModelBuilderPlugin> {
            return mutableListOf(modelBuilderPlugin)
        }

        override fun getPluginsFor(delimiter: DocumentationType?, plugins: MutableList<out ModelBuilderPlugin>?): MutableList<ModelBuilderPlugin> {
            return mutableListOf(modelBuilderPlugin)
        }
    }

    val syntheticModelProviders: PluginRegistry<SyntheticModelProviderPlugin, ModelContext> = object : PluginRegistrySupport<SyntheticModelProviderPlugin, ModelContext>(emptyList()) {
        override fun contains(plugin: SyntheticModelProviderPlugin?): Boolean {
            return false
        }

        override fun countPlugins(): Int {
            return 1
        }

        override fun hasPluginFor(delimiter: ModelContext?): Boolean {
            return false
        }

        override fun getPlugins(): MutableList<SyntheticModelProviderPlugin> {
            return mutableListOf()
        }

        override fun iterator(): MutableIterator<SyntheticModelProviderPlugin> {
            TODO("not implemented")
        }

        override fun getPluginFor(delimiter: ModelContext?): SyntheticModelProviderPlugin {
            TODO("not implemented")
        }

        override fun <E : java.lang.Exception?> getPluginFor(delimiter: ModelContext?, ex: E): SyntheticModelProviderPlugin {
            TODO("not implemented")
        }

        override fun getPluginFor(delimiter: ModelContext?, plugin: SyntheticModelProviderPlugin?): SyntheticModelProviderPlugin {
            TODO("not implemented")
        }

        override fun getPluginsFor(delimiter: ModelContext?): MutableList<SyntheticModelProviderPlugin> {
            return mutableListOf()
        }

        override fun <E : java.lang.Exception?> getPluginsFor(delimiter: ModelContext?, ex: E): MutableList<SyntheticModelProviderPlugin> {
            return mutableListOf()
        }

        override fun getPluginsFor(delimiter: ModelContext?, plugins: MutableList<out SyntheticModelProviderPlugin>?): MutableList<SyntheticModelProviderPlugin> {
            return mutableListOf()
        }
    }

    val propertyEnrichers: PluginRegistry<ModelPropertyBuilderPlugin, DocumentationType> = object : PluginRegistrySupport<ModelPropertyBuilderPlugin, DocumentationType>(emptyList()) {
        override fun contains(plugin: ModelPropertyBuilderPlugin?): Boolean {
            return false
        }

        override fun countPlugins(): Int {
            return 0
        }

        override fun hasPluginFor(delimiter: DocumentationType?): Boolean {
            return false
        }

        override fun getPluginFor(delimiter: DocumentationType?): ModelPropertyBuilderPlugin {
            TODO("not implemented")
        }

        override fun <E : java.lang.Exception?> getPluginFor(delimiter: DocumentationType?, ex: E): ModelPropertyBuilderPlugin {
            TODO("not implemented")
        }

        override fun getPluginFor(delimiter: DocumentationType?, plugin: ModelPropertyBuilderPlugin?): ModelPropertyBuilderPlugin {
            TODO("not implemented")
        }

        override fun getPluginsFor(delimiter: DocumentationType?): MutableList<ModelPropertyBuilderPlugin> {
            return mutableListOf()
        }

        override fun <E : java.lang.Exception?> getPluginsFor(delimiter: DocumentationType?, ex: E): MutableList<ModelPropertyBuilderPlugin> {
            return mutableListOf()
        }

        override fun getPluginsFor(delimiter: DocumentationType?, plugins: MutableList<out ModelPropertyBuilderPlugin>?): MutableList<ModelPropertyBuilderPlugin> {
            return mutableListOf()
        }
    }

    val schemaPluginsManager = object : SchemaPluginsManager(propertyEnrichers, modelEnrichters, syntheticModelProviders) {
        override fun syntheticModel(context: ModelContext?): Optional<Model> {
            return Optional.absent()
        }
    }

    val objectMapperBeanPropertyNamingStrategy = ObjectMapperBeanPropertyNamingStrategy()
    objectMapperBeanPropertyNamingStrategy.onApplicationEvent(ObjectMapperConfigured(Any(), jacksonObjectMapper()))

    val modelPropertiesProvider = OptimizedModelPropertiesProvider(
        AccessorsProvider(typeResolver),
        FieldProvider(typeResolver),
        FactoryMethodProvider(typeResolver),
        typeResolver,
        objectMapperBeanPropertyNamingStrategy,
        schemaPluginsManager,
        typeNameExtractor
    )
    modelPropertiesProvider.onApplicationEvent(ObjectMapperConfigured(Any(), jacksonObjectMapper()))

    val modelDependencyProvider = DefaultModelDependencyProvider(
        typeResolver,
        modelPropertiesProvider,
        typeNameExtractor,
        enumTypeDeterminer,
        schemaPluginsManager
    )

    val modelProvider = DefaultModelProvider(
        typeResolver,
        modelPropertiesProvider,
        modelDependencyProvider,
        schemaPluginsManager,
        typeNameExtractor,
        enumTypeDeterminer
    )

    val props = modelPropertiesProvider.propertiesFor(
        typeResolver.resolve(ExampleModel::class.java),
        ModelContext.inputParam(
            "groupName",
            ExampleModel::class.java,
            DocumentationType.SWAGGER_2,
            alternateTypeProvider,
            genericTypeNamingStrategy,
            ImmutableSet.copyOf(mutableSetOf())
        ))

    val model = modelProvider.modelFor(ModelContext.returnValue(
        "groupName",
        ExampleModel::class.java,
        DocumentationType.SWAGGER_2,
        alternateTypeProvider,
        genericTypeNamingStrategy,
        ImmutableSet.copyOf(mutableSetOf())
    )).get()

    val testModel = toModel(ExampleModel::class)

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

    val doc = SwaggerConfiguration(
        mapOf(
            toModel(ExampleModel::class)
        )
    ).init()
    val swagger = mapper.mapDocumentation(doc)

    var jsonSerializer: JsonSerializer = JsonSerializer(listOf(Swagger2JacksonModule()))

    val json = jsonSerializer.toJson(swagger)

    println(json.value())
}
