package models.clauses

import helpers.QueryParseHelper
import models.clauses.core.Clause
import models.clauses.core.Parser
import models.queryParts.Source

/*
* Класс, отвечающий за парсинг from части запроса.
* */
class FromClause : Clause, Parser {
    private var sources: MutableList<Source> = mutableListOf()

    constructor(name: String, contents: MutableList<String>) : super (name, contents)

    /*
    * Реализация парсинга данных для FROM.
    * */
    override fun parse() {
        var fromContents = formString()
        var ss = fromContents.split(',')

        for (s in ss) {
            var res = QueryParseHelper.getValueAndAlias(s)

            // Вложенный запрос?
            if (QueryParseHelper.containsRegex(s, "([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})")) {
                var name = QueryParseHelper.removeExtraBracketsForSubQuery(res.first)
                sources.add(Source(name, res.second, "subquery"))
                continue
            }
            // Источник данных или view, или table.
            sources.add(Source(res.first, res.second, "table/view"))
        }
    }

    override fun print() {
        val format = "| %-3d | %-60s | %-20s | %-22s |%n"

        println("========= FROM =========\n")

        println("+-----+--------------------------------------------------------------+----------------------+------------------------+")
        println("| №   | Source                                                       | Alias                | Type                   |")
        println("+-----+--------------------------------------------------------------+----------------------+------------------------+")
        sources.forEachIndexed { index, s ->
            System.out.format(format, index + 1, s.name, s.alias ?: "no alias provided", s.type)
        }
        println("+-----+--------------------------------------------------------------+----------------------+------------------------+")

        println("Restored part: ${this.recover()}\n")
    }

    override fun recover() : String {
        var from = StringBuilder()

        // Сам from.
        from.append("FROM").append(" ")

        // Отдельные его части.
        sources.forEachIndexed { index, source ->
            from.append("${source.name}")

            if (source.alias != null) {
                from.append(" ")
                from.append("AS ${source.alias}")
            }

            if (index != sources.size - 1) {
                from.append(",")
                from.append(" ")
            }
        }

        return from.toString()
    }

    override fun recoverInline(): String {
        return recover()
    }
}