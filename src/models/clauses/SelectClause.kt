package models.clauses

import helpers.QueryParseHelper
import models.clauses.core.Clause
import models.clauses.core.Parser
import models.queryParts.Column

/*
* Класс, отвечающий за парсинг select части запроса.
* */
class SelectClause : Clause, Parser {
    private var columns: MutableList<Column> = mutableListOf()
    private var isDistinct: Boolean = false

    constructor(name: String, contents: MutableList<String>) : super (name, contents)

    /*
    * Реализация парсинга для SELECT строки запроса.
    * */
    override fun parse() {
        var selectContents = formString()

        // Проверяем есть ли distinct?
        var distinctStr = "distinct "
        var distinctIndex = selectContents.indexOf(distinctStr, ignoreCase = true)

        // distinct может быть и алиасом, но если это ключевое слово, то стоит оно в начале запроса.
        if (distinctIndex == 0) {
            this.isDistinct = true
            selectContents = selectContents.substring(distinctStr.length)
        }

        var cols = selectContents.split(',')

        // Парсим каждую из колонок и сохраням информацию о ней.
        for (col in cols) {
            var res = QueryParseHelper.getValueAndAlias(col)

            // Вложенный запрос?
            if (QueryParseHelper.containsRegex(col, "([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})")) {
                var name = QueryParseHelper.removeExtraBracketsForSubQuery(res.first)
                columns.add(Column(name, res.second, "subquery"))
                continue
            }

            // Это функция? Например, count(*), sum(*) или нечто подобное.
            if (QueryParseHelper.containsRegex(col, "[a-zA-Z]+[(]+")) {
                columns.add(Column(res.first, res.second, "function"))
                continue
            }

            // Имеется ли в виду выборка всех колонок?
            if (col.contains('*')) {
                columns.add(Column(res.first, res.second, "selects everything"))
                continue
            }

            columns.add(Column(res.first, res.second, "column"))
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

        if (isDistinct) {
            select.append("DISTINCT").append(" ")
        }

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