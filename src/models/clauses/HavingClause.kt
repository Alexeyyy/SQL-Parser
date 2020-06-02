package models.clauses

import helpers.ExpressionParseHelper
import models.clauses.core.Clause
import models.clauses.core.Parser
import models.queryParts.Expression

class HavingClause : Clause, Parser {
    private var expressions: MutableList<Expression> = mutableListOf()

    constructor(name: String, contents: MutableList<String>) : super (name, contents)

    override fun parse() {
        var havingContents = formString()
        expressions = ExpressionParseHelper.parseExpressionToTable(havingContents)
    }

    override fun print() {
        val format = "| %-9d | %-60s | %-60s | %-10s | %-40s |%n"

        println("========= HAVING =========\n")

        println("TABLE. Expression priority map")
        println("+-----------+--------------------------------------------------------------+--------------------------------------------------------------+------------+------------------------------------------+")
        println("| Priority  | Left expression                                              | Right expression                                             | Operator   | ID                                       |")
        println("+-----------+--------------------------------------------------------------+--------------------------------------------------------------+------------+------------------------------------------+")
        expressions.forEach { exp ->
            System.out.format(format, exp.priority, exp.leftExpression, exp.rightExpression ?: "—", exp.operator ?: "—", exp.id)
        }
        println("+-----------+--------------------------------------------------------------+--------------------------------------------------------------+------------+------------------------------------------+")

        println("Restored part: ${this.recover()}\n")
    }

    override fun recover(): String {
        var having = StringBuilder()

        having.append("HAVING").append(" ")
        having.append(ExpressionParseHelper.restoreExpression(expressions))

        return having.toString()
    }

    override fun recoverInline(): String {
        return recover()
    }
}