package my.company

import org.flywaydb.core.Flyway
import org.jooq.codegen.GenerationTool
import org.jooq.impl.DSL
import org.jooq.meta.jaxb.Configuration
import java.io.File
import java.io.FileInputStream

class JooqCodeGenerator {
    fun generateClasses() {
        println("GenerateJooqClasses WORKING_DIR: " + File("").absolutePath)
        val dbPlaceholder = "{DATABASE}"
        val config = FileInputStream(File("src/main/resources/jooq-config.xml")).use(GenerationTool::load)!!

        val maintenanceDb = config.generator.database.properties.find { it.key == "maintenance-db" }?.value
            ?: throw IllegalStateException("Missing config for maintenance db name")

        val codegenDb = config.generator.database.properties.find { it.key == "codegen-db" }?.value
            ?: throw IllegalStateException("Missing config for codegen db name")

        val urlTemplate = config.jdbc.url
        config.jdbc.url = urlTemplate.replace(dbPlaceholder, codegenDb)

        createDb(urlTemplate.replace(dbPlaceholder, maintenanceDb), config.jdbc.user, config.jdbc.password, codegenDb)
        migrateDb(config)

        val targetDir = File(
            File(config.generator.target.directory),
            config.generator.target.packageName.replace(".", File.separator)
        )
        println("Clearing target dir: " + targetDir.absolutePath)
        targetDir.deleteRecursively()

        GenerationTool().run(config)

        addImports(targetDir)
    }

    private fun addImports(file: File) {
        if (file.isDirectory) {
            file.list().forEach { addImports(File(file, it)) }
            return
        }
        val content = file.readText(Charsets.UTF_8)
        val newContent =
            content.replace("import javax.validation.constraints.NotNull;", "import org.jetbrains.annotations.NotNull;")
        file.writeText(newContent, Charsets.UTF_8)
    }

    private fun createDb(url: String, username: String, password: String, database: String) {
        DSL.using(url, username, password)
            .use { dsl ->
                dsl.execute("DROP DATABASE IF EXISTS $database")
                dsl.execute("CREATE DATABASE $database")
            }
    }

    private fun migrateDb(config: Configuration) {
        val flyway = Flyway(
            Flyway.configure()
                .dataSource(config.jdbc.url, config.jdbc.user, config.jdbc.password)
                .schemas("public")
                .locations("filesystem:../src/main/resources/db/migration/public")
        )
        flyway.migrate()
    }
}

fun main() {
    JooqCodeGenerator().generateClasses()
}
