package my.company.app.lib.jooq

import my.company.jooq.tables.Session
import my.company.jooq.tables.User
import org.jooq.Condition
import org.jooq.JoinType
import org.jooq.Record
import org.jooq.SelectJoinStep
import org.jooq.SelectOnConditionStep
import org.jooq.SelectOptionalOnStep
import org.jooq.impl.DSL.and

// <editor-fold desc="Joins">

typealias OnCondition = () -> Condition?

val defaultOnCondition: OnCondition = { null }

fun <R : Record> SelectOptionalOnStep<R>.on(staticCondition: Condition, optionalCondition: OnCondition = defaultOnCondition): SelectOnConditionStep<R> {
    return this.on(and(listOfNotNull(staticCondition, optionalCondition())))
}

fun <R : Record> SelectJoinStep<R>.joinOn(session: Session, user: User, joinType: JoinType = JoinType.LEFT_OUTER_JOIN, on: OnCondition = defaultOnCondition): SelectOnConditionStep<R> {
    return this.join(user, joinType).on(user.ID.eq(session.USER_ID), on)
}

// </editor-fold>
