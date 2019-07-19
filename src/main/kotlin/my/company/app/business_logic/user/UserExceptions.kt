@file:Suppress("MatchingDeclarationName")

package my.company.app.business_logic.user

class UserNotFoundByEmailException(val email: String?) : RuntimeException()
