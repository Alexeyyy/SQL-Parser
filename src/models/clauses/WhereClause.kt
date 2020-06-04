package models.clauses

import helpers.ExpressionParseHelper
import models.clauses.core.Clause
import models.clauses.core.Parser
import models.queryParts.Expression

/*
* Класс, отвечающий за парсинг where части запроса.
* */
class WhereClause : Clause, Parser {
    private var expressions: MutableList<Expression> = mutableListOf()

    constructor(name: String, contents: MutableList<String>) : super (name, contents)

    /*
    * Парсинг строки, содержащей условия.
    * */
    override fun parse() {
        var whereContents = formString()

        // Выделяем все сложные операторы, чтобы оставить их на уровне подвыражения.
        whereContents = ExpressionParseHelper.underscoreOperators(whereContents)

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
        where.append(ExpressionParseHelper.restoreExpression(expressions))

        return where.toString()
    }

    override fun recoverInline(): String {
        return recover()
    }
}