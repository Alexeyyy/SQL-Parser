package models.clauses

import helpers.QueryParseHelper
import models.clauses.core.Clause
import models.clauses.core.Parser


/*
* Класс, отвечающий за парсинг offset части запроса.
* */
class OffsetClause : Clause, Parser {
    private var offsetValue: String = ""

    constructor(name: String, contents: MutableList<String>) : super (name, contents)

    override fun parse() {
        var offsetContents = formString()
        offsetValue = offsetContents
    }

    override fun print() {
        println("========= OFFSET =========\n")

        if (QueryParseHelper.containsRegex(offsetValue, "([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})")) {
            offsetValue = QueryParseHelper.removeExtraBracketsForSubQuery(offsetValue)
            println("The offset is represented by subquery. Watch $offsetValue for more details")
        }
        else {
            println("The offset is $offsetValue")
        }

        println("Restored part: ${this.recover()}\n")
    }

    override fun recover(): String {
        var offset = StringBuilder()

        offset.append("OFFSET").append(" ")
        offset.append(offsetValue)

        return offset.toString()
    }

    override fun recoverInline(): String {
        return recover()
    }
}