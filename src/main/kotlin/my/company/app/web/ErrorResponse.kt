package my.company.app.web

data class ErrorResponse(
    val errorMessage: String,
    val validationErrors: List<ValidationError> = emptyList()
)

data class ValidationError(
    val propertyPath: String,
    val errorMessage: String
)
