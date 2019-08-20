package my.company.jooq.generator

import org.jooq.codegen.JavaGenerator
import org.jooq.codegen.JavaWriter
import org.jooq.meta.TableDefinition

class JooqCodeGenerator : JavaGenerator() {
    override fun generateRecordClassFooter(table: TableDefinition, out: JavaWriter) {
        super.generateRecordClassFooter(table, out)
        val recordName = table.name.split("_").joinToString("") { it.capitalize() }
        out.println()
        out.println(
            """
            |    @Override
            |    public String toString() {
            |        StringBuilder sb = new StringBuilder("$recordName(");
            |        for (Field<?> f : fields()) {
            |            sb.append(" [").append(f.getName()).append("=").append(this.get(f)).append("]");
            |        }
            |        return sb.append(" )").toString();
            |    }
            """.trimMargin()
        )
    }
}
