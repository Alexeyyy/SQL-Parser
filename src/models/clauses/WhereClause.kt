package models.clauses

import helpers.ExpressionParseHelper
import models.clauses.core.Clause
import models.clauses.core.Parser
import models.queryParts.Expression

class WhereClause : Clause, Parser {
    private var expressions: MutableList<Expression> = mutableListOf()

    constructor(name: String, contents: MutableList<String>) : super (name, contents)

    /*
    * Парсинг строки, содержащей условия.
    * */
    override fun parse() {
        var whereContents = formString()
        expressions = ExpressionParseHelper.parseExpressionToTable(whereContents)
    }

    override fun print() {
        val format = "| %-9d | %-60s | %-60s | %-10s | %-40s |%n"

        println("========= WHERE =========\n")

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

    /*
    * Восстановление условия WHERE запроса по данным.
    * */
    override fun recover(): String {
        var where = StringBuilder()

        where.append("WHERE").append(" ")

        var condition = expressions.maxBy { it.priority }
        var priority = condition!!.priority

        // Условие состоит из одного выражения.
        if (priority == 1) {
            where.append(condition.leftExpression)
            return where.toString()
        }

        // Собираем выражение.
        var resultExpression = "(${condition.leftExpression} ${condition.operator} ${condition.rightExpression})"
        priority--

        while (priority != 0) {
            condition = expressions.single { it.priority == priority }
            resultExpression = resultExpression.replace(condition.id, "(${condition.leftExpression} ${condition.operator} ${condition.rightExpression})")
            priority--
        }

        where.append(resultExpression)

        return where.toString()
    }

    override fun recoverInline(): String {
        return recover()
    }
}