package my.company.app.db.jooq.binding

import org.jooq.Binding
import org.jooq.BindingGetResultSetContext
import org.jooq.BindingGetSQLInputContext
import org.jooq.BindingGetStatementContext
import org.jooq.BindingRegisterContext
import org.jooq.BindingSQLContext
import org.jooq.BindingSetSQLOutputContext
import org.jooq.BindingSetStatementContext
import org.jooq.Converter
import org.jooq.conf.ParamType
import org.jooq.impl.DSL
import org.jooq.impl.DefaultBinding
import org.jooq.tools.JooqLogger
import java.sql.SQLFeatureNotSupportedException
import java.sql.Timestamp
import java.sql.Types
import java.time.Instant

class TimestampInstantBinding : Binding<Timestamp, Instant> {

    companion object InstantConverter : Converter<Timestamp, Instant> {
        private val bindingLogger = JooqLogger.getLogger(DefaultBinding::class.java)!!
        private val FROM_TYPE = Timestamp::class.java
        private val TO_TYPE = Instant::class.java
        override fun from(databaseObject: Timestamp?): Instant? = databaseObject?.toInstant()
        override fun to(userObject: Instant?): Timestamp? = userObject?.let { Timestamp.from(it) }
        override fun fromType() = FROM_TYPE
        override fun toType() = TO_TYPE
    }

    override fun converter(): Converter<Timestamp, Instant> = InstantConverter

    override fun sql(ctx: BindingSQLContext<Instant>) {
        val renderCtx = ctx.render()
        if (renderCtx.paramType() == ParamType.INLINED) {
            val value = ctx.convert(converter()).value()
            renderCtx.visit(DSL.inline(value))
        } else renderCtx.sql("?")
    }

    override fun register(ctx: BindingRegisterContext<Instant>) {
        ctx.statement().registerOutParameter(ctx.index(), Types.TIMESTAMP)
    }

    override fun set(ctx: BindingSetStatementContext<Instant>) {
        val index = ctx.index()
        val value = ctx.convert(converter()).value()
        bindingLogger.trace("Binding variable $index", "$value ($TO_TYPE)")
        ctx.statement().setTimestamp(index, value)
    }

    override fun get(ctx: BindingGetResultSetContext<Instant>) {
        ctx.convert(converter()).value(ctx.resultSet().getTimestamp(ctx.index()))
    }

    override fun get(ctx: BindingGetStatementContext<Instant>) {
        ctx.convert(converter()).value(ctx.statement().getTimestamp(ctx.index()))
    }

    override fun set(ctx: BindingSetSQLOutputContext<Instant>?) {
        throw SQLFeatureNotSupportedException()
    }

    override fun get(ctx: BindingGetSQLInputContext<Instant>) {
        throw SQLFeatureNotSupportedException()
    }
}
