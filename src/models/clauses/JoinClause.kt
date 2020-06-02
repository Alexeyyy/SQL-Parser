package models.clauses

import helpers.ExpressionParseHelper
import helpers.QueryParseHelper
import models.clauses.core.Clause
import models.clauses.core.Parser
import models.queryParts.Expression
import java.lang.StringBuilder

class JoinClause : Clause, Parser {
    private var source: String = ""
    private var alias: String? = null
    private var type: String = ""
    private var expressions: MutableList<Expression> = mutableListOf()

    constructor(name: String, contents: MutableList<String>) : super (name, contents)

    /*
    * Парсинг строки части запроса.
    * Типовой join: "join table as a on a.id = b.id"
    * */
    override fun parse() {
        val joinContents = formString()

        // Находим тип join-а.
        type = name.substring(0, name.indexOf("join", ignoreCase = true)).trim()
        if (type == "") {
            type = "inner"
        }

        // Находим источник и его алиас.
        val onPos = joinContents.indexOf("on", ignoreCase = true)
        val sal = joinContents.substring(0, onPos).trim()
        val sourceAndAlias = QueryParseHelper.getValueAndAlias(sal)
        source = sourceAndAlias.first
        alias = sourceAndAlias.second

        // Находим все условия для join-ов. +2, пропускаем само слово on.
        val conditions = joinContents.substring(onPos + 2).trim()

        this.expressions = ExpressionParseHelper.parseExpressionToTable(conditions)
    }

    /*
    * Вывод условий и приоритетов их выполнения в рамках join.
    * */
    override fun print() {
        val format = "| %-9d | %-60s | %-60s | %-10s | %-40s |%n"

        println("========= JOIN =========\n")

        println("Type: $type")
        println("Source: $source")
        println("Alias: ${alias ?: "no alias provided"}")
        println()

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

    override fun recover() : String {
        var join = StringBuilder()

        // Тип.
        join.append(type.toUpperCase()).append(" ")

        // Join.
        join.append("JOIN").append(" ")

        // На какой источник.
        join.append(source).append(" ")

        // Алиас, если он есть.
        if (alias != null) {
            join.append("AS").append(" ").append(alias).append(" ")
        }

        // Восстановление условий.
        join.append("ON").append(" ")
        var condition = expressions.maxBy { it.priority }
        var priority = condition!!.priority

        // Условие состоит из одного выражения.
        if (priority == 1) {
            join.append(condition.leftExpression)
            return join.toString()
        }

        // Собираем выражение.
        var resultExpression = "(${condition.leftExpression} ${condition.operator} ${condition.rightExpression})"
        priority--

        while (priority != 0) {
            condition = expressions.single { it.priority == priority }
            resultExpression = resultExpression.replace(condition.id, "(${condition.leftExpression} ${condition.operator} ${condition.rightExpression})")
            priority--
        }

        join.append(resultExpression)

        return join.toString()
    }

    override fun recoverInline(): String {
        return recover()
    }
}