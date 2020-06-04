package helpers

import java.util.*
import kotlin.collections.ArrayList

/*
* Ключевые элементы запроса.
* */
object KeyWords {
    // Ключевые части запроса.
    val clauses: ArrayList<String> = arrayListOf(
            "select", "from", "where", "group by", "order by", "limit", "having",
            "join", "inner join", "left outer join", "right outer join", "left join", "right join",
            "full outer join", "full join", "desc", "asc", "offset"
    )

    // Ключевые слова запроса, которые обрабатываются парсером.
    val keyWords : ArrayList<String> = arrayListOf(
            "select", "as", "from", "where", "order",
            "group", "by", "having", "inner", "left", "right", "offset",
            "full", "outer", "join", "on", "and", "or", "limit", "desc", "asc",
            "is", "not", "like", "all", "in", "between", "distinct")

    // Определение границ ключевых слов. Используется в процессе нормализации строки запроса.
    val keywordBoundaryMark : String = UUID.randomUUID().toString()

    // Операторы выражений.
    val operators: ArrayList<String> = arrayListOf(
            "between", "not", "like", "in", "is"
    )

    // Граница для операторов.
    val operatorBoundaryMark : String = UUID.randomUUID().toString()
}