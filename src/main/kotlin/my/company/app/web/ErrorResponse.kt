package my.company.app.web

class ErrorResponse(
    val errorMessage: String,
    val validationErrors: List<ValidationError> = emptyList()
)

class ValidationError(
    val propertyPath: String,
    val errorMessage: String
)
