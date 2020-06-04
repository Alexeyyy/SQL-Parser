package models.clauses

import com.sun.org.apache.xpath.internal.operations.Bool
import helpers.QueryParseHelper
import models.clauses.core.Clause
import models.clauses.core.Parser
import models.queryParts.Column

/*
* Класс, отвечающий за парсинг group by части запроса.
* */
class GroupClause : Clause, Parser {
    private var groupedBy: MutableList<Column> = mutableListOf()

    constructor(name: String, contents: MutableList<String>) : super (name, contents)

    override fun parse() {
        var groupedContents = formString()
        var items = groupedContents.split(',')

        for (item in items) {
            // Вложенный запрос?
            if (QueryParseHelper.containsRegex(item, "([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})")) {
                var name = QueryParseHelper.removeExtraBracketsForSubQuery(item)
                groupedBy.add(Column(name, null, "subquery"))
                continue
            }

            // Колонка.
            groupedBy.add(Column(item, null, "column"))
        }
    }

    override fun print() {
        val format = "| %-3d | %-60s | %-20s | %-22s |%n"

        println("========= GROUP BY =========\n")

        println("+-----+--------------------------------------------------------------+----------------------+------------------------+")
        println("| №   | Source                                                       | Alias                | Type                   |")
        println("+-----+--------------------------------------------------------------+----------------------+------------------------+")
        groupedBy.forEachIndexed { index, col ->
            System.out.format(format, index + 1, col.value, col.alias ?: "no alias provided", col.type)
        }
        println("+-----+--------------------------------------------------------------+----------------------+------------------------+")

        println("Restored: ${this.recover()}\n")
    }

    override fun recover(): String {
        var groupBy = StringBuilder()

        groupBy.append("GROUP BY").append(" ")

        groupedBy.forEachIndexed { index, col ->
            groupBy.append(col.value)

            if (index != groupedBy.size - 1) {
                groupBy.append(",")
                groupBy.append(" ")
            }
        }

        return groupBy.toString()
    }

    override fun recoverInline(): String {
        return recover()
    }
}