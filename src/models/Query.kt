package models

import helpers.QueryParseHelper
import models.clauses.*
import models.clauses.core.Parser
import java.lang.StringBuilder

class Query : Parser {
    private var queryId: String = ""
    private var queryString: String = ""

    // Составляющие запроса, парсятся из queryString.
    private var select: SelectClause? = null
    private var from: FromClause? = null
    private var joins: MutableList<JoinClause> = mutableListOf()
    private var where: WhereClause? = null
    private var orderBy: OrderClause? = null
    private var groupBy: GroupClause? = null
    private var having: HavingClause? = null
    private var limit: LimitClause? = null
    private var offset: OffsetClause? = null

    constructor(queryId: String, queryString: String) {
        this.queryId = queryId
        this.queryString = queryString
    }

    fun getId() : String {
        return queryId
    }

    /*
    * Инициализирует все clause запроса, которые есть в его строковом представлении.
    * */
    private fun splitQueryString() {
        if (this.queryString.isEmpty()) {
            return
        }

        var result = QueryParseHelper.splitByClauses(this.queryString)

        result.forEach { clause ->
            if (clause.first.equals("select", true)) {
                this.select = SelectClause(clause.first, clause.second)
            }
            if (clause.first.equals("from", true)) {
                this.from = FromClause(clause.first, clause.second)
            }
            // Join-ы.
            if (clause.first.equals("join", true)) {
                this.joins.add(JoinClause(clause.first, clause.second))
            }
            if (clause.first.equals("inner join", true)) {
                this.joins.add(JoinClause(clause.first, clause.second))
            }
            if (clause.first.equals("full join", true)) {
                this.joins.add(JoinClause(clause.first, clause.second))
            }
            if (clause.first.equals("right join", true)) {
                this.joins.add(JoinClause(clause.first, clause.second))
            }
            if (clause.first.equals("left join", true)) {
                this.joins.add(JoinClause(clause.first, clause.second))
            }
            if (clause.first.equals("right outer join", true)) {
                this.joins.add(JoinClause(clause.first, clause.second))
            }
            if (clause.first.equals("left outer join", true)) {
                this.joins.add(JoinClause(clause.first, clause.second))
            }
            if (clause.first.equals("full outer join", true)) {
                this.joins.add(JoinClause(clause.first, clause.second))
            }
            // Остальное.
            if (clause.first.equals("where", true)) {
                this.where = WhereClause(clause.first, clause.second)
            }
            if (clause.first.equals("order by", true)) {
                this.orderBy = OrderClause(clause.first, clause.second)
            }
            // Достаточно хитро сделано, но desc и asc гарантированно (если они есть) следуют за order by (по правилам SQL).
            if (clause.first.equals("desc", true)) {
                this.orderBy!!.setOrderType("desc")
            }
            if (clause.first.equals("asc", true)) {
                this.orderBy!!.setOrderType("asc")
            }
            if (clause.first.equals("group by", true)) {
                this.groupBy = GroupClause(clause.first, clause.second)
            }
            if (clause.first.equals("having", true)) {
                this.having = HavingClause(clause.first, clause.second)
            }
            if (clause.first.equals("limit", true)) {
                this.limit = LimitClause(clause.first, clause.second)
            }
            if (clause.first.equals("offset", true)) {
                this.offset = OffsetClause(clause.first, clause.second)
            }
        }
    }

    /*
    * Парсит содержимое запроса.
    * */
    override fun parse() {
        // Раскладываем запрос по строка.
        splitQueryString()

        this.select?.parse()
        this.from?.parse()
        this.joins.forEach {
            it.parse()
        }
        this.where?.parse()
        this.groupBy?.parse()
        this.having?.parse()
        this.orderBy?.parse()
        this.limit?.parse()
        this.offset?.parse()
    }

    /*
    * Отображение результата парсинга запроса.
    * */
    override fun print() {
        println("+============================================================================================+")
        println("Query ID: $queryId")
        println("+--------------------------------------------------------------------------------------------+")
        this.select?.print()
        this.from?.print()

        if (this.joins.any()) {
            this.joins.forEach { it.print() }
        }
        this.where?.print()
        this.groupBy?.print()
        this.having?.print()
        this.orderBy?.print()
        this.limit?.print()
        this.offset?.print()
    }

    /*
    * Служебный метод по форматированию запроса.
    * */
    private fun getSpaceOrNewLine(inline: Boolean) : String {
        return if (inline) " " else "\n"
    }

    /*
    * Служебный метод класса, восстанавливающий запрос.
    * Может отформатировать его или же просто выдать в виде строки (удобно для подзапросов).
    * */
    private fun restoreQuery(inline: Boolean) : String {
        var query = StringBuilder()

        // В порядке очередности SQL-нотаций.
        if (select != null) {
            query.append(select?.recover())
            query.append(getSpaceOrNewLine(inline))
        }
        if (from != null) {
            query.append(from?.recover())
            query.append(getSpaceOrNewLine(inline))
        }
        joins.forEach { query.append(if (inline) it.recover() + " " else "\t" + it.recover() + "\n") }
        if (where != null) {
            query.append(where?.recover())
            query.append(getSpaceOrNewLine(inline))
        }
        if (groupBy != null) {
            query.append(groupBy?.recover())
            query.append(getSpaceOrNewLine(inline))
        }
        if (having != null) {
            query.append(having?.recover())
            query.append(getSpaceOrNewLine(inline))
        }
        if (orderBy != null) {
            query.append(orderBy?.recover())
            query.append(getSpaceOrNewLine(inline))
        }
        if (limit != null) {
            query.append(limit?.recover())
            query.append(getSpaceOrNewLine(inline))
        }
        if (offset != null) {
            query.append(offset?.recover())
            query.append(getSpaceOrNewLine(inline))
        }

        return query.toString()
    }

    /*
    * Восстановление запроса из полученных данных. Обратная парсингу операция.
    * Восстанавливает в отформатированном виде.
    * */
    override fun recover(): String {
        return restoreQuery(false)
    }

    /*
    * Восстанавливает запрос в виде строки.
    * */
    override fun recoverInline(): String {
        return restoreQuery(true)
    }
}