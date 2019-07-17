package my.company.app.lib

object EnumParser {

    private enum class Hack

    fun parseGenericEnum(clazz: Class<*>, value: String?): Enum<*>? {
        value ?: return null
        val enumClass = clazz.asSubclass(Enum::class.java)
        return helper<Hack>(enumClass, value)
    }

    // PLU: As of this writing, kotlin has an open issue that needs to be resolved so that java.lang.Enum.valueOf() can be called without a specific type.
    // See: https://youtrack.jetbrains.com/issue/KT-18002
    // Luckily, kotlin compiles to java which drops generics during compilation, making this hack work.
    // This hack was taken from SO: https://stackoverflow.com/a/46422600/1064622
    @Suppress("UNCHECKED_CAST")
    private fun <T : Enum<T>> helper(enumClass: Class<*>, value: String): Enum<*> {
        return java.lang.Enum.valueOf(enumClass as Class<T>, value)
    }
}
