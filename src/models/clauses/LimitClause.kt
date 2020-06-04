package models.clauses

import helpers.QueryParseHelper
import models.clauses.core.Clause
import models.clauses.core.Parser

/*
* Класс, отвечающий за парсинг limit части запроса.
* */
class LimitClause : Clause, Parser {
    // Значение может быть ограничено и подзапросом, поэтому String.
    private var limitValue: String = ""

    constructor(name: String, contents: MutableList<String>) : super (name, contents)

    override fun parse() {
        var limitContents = formString()
        limitValue = limitContents
    }

    override fun print() {
        println("========= LIMIT =========\n")

        if (QueryParseHelper.containsRegex(limitValue, "([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})")) {
            limitValue = QueryParseHelper.removeExtraBracketsForSubQuery(limitValue)
            println("The limit is represented by subquery. Watch $limitValue for more details")
        }
        else {
            println("The limit is $limitValue")
        }

        println("Restored part: ${this.recover()}\n")
    }

    override fun recover(): String {
        var limit = StringBuilder()

        limit.append("LIMIT").append(" ")
        limit.append(limitValue)

        return limit.toString()
    }

    override fun recoverInline(): String {
        return recover()
    }
}