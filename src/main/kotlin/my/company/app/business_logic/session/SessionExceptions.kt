@file:Suppress("MatchingDeclarationName")

package my.company.app.business_logic.session

import my.company.app.lib.ServiceException

class InvalidLoginCredentialsException : ServiceException("{login.credentials.invalid}")
