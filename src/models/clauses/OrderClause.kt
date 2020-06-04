package models.clauses

import helpers.QueryParseHelper
import models.clauses.core.Clause
import models.clauses.core.Parser
import models.queryParts.Column


/*
* Класс, отвечающий за парсинг order by части запроса.
* */
class OrderClause : Clause, Parser {
    private var orderedBy: MutableList<Column> = mutableListOf()
    private var orderType: String = "" //ASC, DESC

    fun setOrderType(value: String) {
        this.orderType = value
    }

    constructor(name: String, contents: MutableList<String>) : super (name, contents)

    override fun parse() {
        var orderContents = formString()
        var items = orderContents.split(',')

        for (item in items) {
            // Вложенный запрос?
            if (QueryParseHelper.containsRegex(item, "([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})")) {
                var name = QueryParseHelper.removeExtraBracketsForSubQuery(item)
                orderedBy.add(Column(name, null, "subquery"))
                continue
            }

            // Колонка.
            orderedBy.add(Column(item, null, "column"))
        }
    }

    override fun print() {
        val format = "| %-3d | %-60s | %-20s | %-22s |%n"

        println("========= ORDER BY =========\n")

        println("+-----+--------------------------------------------------------------+----------------------+------------------------+")
        println("| №   | Value                                                        | Alias                | Type                   |")
        println("+-----+--------------------------------------------------------------+----------------------+------------------------+")
        orderedBy.forEachIndexed { index, col ->
            System.out.format(format, index + 1, col.value, col.alias ?: "no alias provided", col.type)
        }
        println("+-----+--------------------------------------------------------------+----------------------+------------------------+")
        println("Restored part: ${this.recover()}\n")
    }

    override fun recover(): String {
        var order = StringBuilder()

        // Сам order.
        order.append("ORDER BY").append(" ")

        // Отдельные части.
        orderedBy.forEachIndexed { index, col ->
            order.append(col.value)

            if (index != orderedBy.size - 1) {
                order.append(",")
                order.append(" ")
            }
        }

        // Тип (по умолчанию asc).
        if (orderType != null) {
            order.append(" ")
            order.append(orderType.toUpperCase())
        }

        return order.toString()
    }

    override fun recoverInline(): String {
        return recover()
    }
}