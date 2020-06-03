package helpers

import java.util.*
import kotlin.collections.ArrayList

object KeyWords {
    val clauses: ArrayList<String> = arrayListOf(
            "select", "from", "where", "group by", "order by", "limit", "having",
            "join", "inner join", "left outer join", "right outer join", "left join", "right join",
            "full outer join", "full join", "desc", "asc", "offset"
    )

    val keyWords : ArrayList<String> = arrayListOf(
            "select", "as", "from", "where", "order",
            "group", "by", "having", "inner", "left", "right", "offset",
            "full", "outer", "join", "on", "and", "or", "limit", "desc", "asc",
            "is", "not", "like", "all", "in", "between", "distinct")

    val keywordBoundaryMark : String = UUID.randomUUID().toString()

    val operators: ArrayList<String> = arrayListOf(
            "between", "not", "like", "in"
    )

    val operatorBoundaryMark : String = UUID.randomUUID().toString()
}