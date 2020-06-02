package models.queryParts

class Expression(
    val leftExpression: String,
    val rightExpression: String?,
    val operator: String?,
    val id: String,
    val entireExpression: String,
    val priority: Int
)