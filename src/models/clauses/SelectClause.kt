package models.clauses

import helpers.QueryParseHelper
import models.clauses.core.Clause
import models.clauses.core.Parser
import models.queryParts.Column

class SelectClause : Clause, Parser {
    private var columns: MutableList<Column> = mutableListOf()

    constructor(name: String, contents: MutableList<String>) : super (name, contents)

    /*
    * Реализация парсинга для SELECT строки запроса.
    * */
    override fun parse() {
        var selectContents = formString()
        var cols = selectContents.split(',')

        // Парсим каждую из колонок и сохраням информацию о ней.
        for (col in cols) {
            var res = QueryParseHelper.getValueAndAlias(col)

            // Вложенный запрос?
            if (QueryParseHelper.containsRegex(col, "([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})")) {
                columns.add(Column(res.second, res.first, "subquery"))
                continue
            }

            // Это функция? Например, count(*), sum(*) или нечто подобное.
            if (QueryParseHelper.containsRegex(col, "[a-zA-Z]*[(]")) {
                columns.add(Column(res.second, res.first, "function"))
                continue
            }

            // Имеется ли в виду выборка всех колонок?
            if (col.contains('*')) {
                columns.add(Column(res.second, res.first, "selects everything"))
                continue
            }

            columns.add(Column(res.second, res.first, "column"))
        }
    }

    override fun print() {
        val format = "| %-3d | %-60s | %-20s | %-22s |%n"

        println("========= SELECT =========\n")

        println("+-----+--------------------------------------------------------------+----------------------+------------------------+")
        println("| №   | Value                                                        | Alias                | Type                   |")
        println("+-----+--------------------------------------------------------------+----------------------+------------------------+")
        columns.forEachIndexed { index, col ->
            System.out.format(format, index + 1, col.value, col.alias ?: "no alias provided", col.type)
        }
        println("+-----+--------------------------------------------------------------+----------------------+------------------------+")

        println("Restored part: ${this.recover()}\n")
    }

    /*
    * Восстановление строки запроса.
    * */
    override fun recover(): String {
        var select = StringBuilder()

        // Сам Select.
        select.append("SELECT").append(" ")

        // Перечисление колонок, функций, подзапросов с алиасами и без них.
        columns.forEachIndexed { index, col ->
            select.append("${col.value}")

            if (col.alias != null) {
                select.append(" ")
                select.append("AS ${col.alias}")
            }

            if (index != columns.size - 1) {
                select.append(",")
                select.append(" ")
            }
        }

        return select.toString()
    }

    override fun recoverInline(): String {
        return recover()
    }
}