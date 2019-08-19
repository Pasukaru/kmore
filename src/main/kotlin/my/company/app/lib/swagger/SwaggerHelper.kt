package my.company.app.lib.swagger

import com.fasterxml.classmate.TypeResolver
import springfox.documentation.builders.ModelBuilder
import springfox.documentation.builders.ModelPropertyBuilder
import springfox.documentation.schema.Model
import springfox.documentation.schema.ModelProperty
import springfox.documentation.schema.ModelRef
import java.time.Instant
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMemberProperties

object SwaggerHelper {
    private val typeResolver = TypeResolver()

    fun toModel(model: KClass<*>): Model {
        val builder = ModelBuilder()
            .name(model.simpleName)
            .type(typeResolver.resolve(model.java))

        val props = model.declaredMemberProperties
            .associateTo(mutableMapOf()) { it.name to toModelProperty(it) }

        builder.properties(props)

        return builder.build()
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun toModelProperty(prop: KProperty<*>): ModelProperty {
        val type = prop.returnType
        val kClazz = (type.classifier as KClass<*>)
        val clazz = kClazz.java

        val property = ModelPropertyBuilder()
            .type(typeResolver.resolve(clazz))
            .required(!type.isMarkedNullable)
            .isHidden(false)
            .name(prop.name)
            .qualifiedType(clazz.canonicalName)
            .readOnly(false)
            .description("")
            .build()

        property.updateModelRef { _ ->
            when (kClazz) {
                String::class -> ModelRef("string")
                UUID::class -> ModelRef("uuid")
                Instant::class -> ModelRef("date-time")
                else -> throw IllegalStateException("Unsupported property: $clazz - $prop")
            }
        }

        return property
    }
}
