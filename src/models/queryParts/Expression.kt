package models.queryParts

/*
* Класс описывающий выражение вида:
*      4.      4.
*      ↓       ↓
* a=b and g=d or r>3
*  ↑   ↑   ↑
*  1.  3.  2.
* */
class Expression(
    val leftExpression: String, // 1.
    val rightExpression: String?, // 2.
    val operator: String?, // 3.
    val id: String,
    val entireExpression: String,
    val priority: Int // 4.
)