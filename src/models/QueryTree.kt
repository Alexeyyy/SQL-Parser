package models

import models.clauses.core.Parser
import java.util.*
import kotlin.collections.ArrayList

class QueryTree : Parser {
    private var relations: MutableList<Pair<Int, Int>> = mutableListOf()
    private var bounds: ArrayList<IntRange> = ArrayList()
    private var queries: MutableList<Query> = mutableListOf()

    /*
    * Конструктор собирает дерево связей между подзапросами.
    * */
    constructor(input: String, queryBounds: ArrayList<IntRange>) {
        this.bounds = queryBounds

        // Если нет подзапросов, единственный select.
        if (bounds.size == 1) {
            queries.add(Query(createQueryId(), input))
            return
        }

        // Получаем дерево индексов.
        buildTreeRelations(bounds)

        // Строим дерево запросов.
        var queriesId = relations.map { it.first }.toMutableList()
        queriesId.addAll(relations.map { it.second }.toMutableList())
        queriesId = queriesId.distinct().toMutableList()

        // Сгенерированные ID-запросов.
        var generatedIds = generateQueriesIds()

        queriesId.forEach { i ->
            var boundaries = bounds[i]

            // Каждый раз копируем строку, чтобы правильно вырезать подстроку подзапроса.
            var queryString = input.substring(boundaries.first, boundaries.last)

            // Сдвиг относительно замены подстроки на строку.
            var shift = 0
            var prevIdLength = 0

            // Смотрим с какими подзапросами он в прямой связи.
            for (rel in relations.filter { it.first == i }) {
                // Нормируем границы по отношению к новой подстроке.
                var inA = bounds[rel.second].first - boundaries.first - shift + prevIdLength
                var inB = inA + (bounds[rel.second].last - bounds[rel.second].first)

                // Меняем подзапрос в строке queryString на ID найденного запроса.
                //var id = createQueryId(rel.second)
                var id = generatedIds[rel.second]

                queryString = queryString.substring(0, inA) + id + queryString.substring(inB)

                // Меняем сдвиг.
                shift += inB - inA
                prevIdLength += id.length
            }

            queries.add(Query(generatedIds[i], queryString))
        }
    }

    /*
    * Метод, формирующий связи между подзапросами.
    * */
    private fun buildTreeRelations(queryBounds: ArrayList<IntRange>) {
        // Строим дерево индексов.
        queryBounds.forEachIndexed { i, qb ->
            queryBounds.forEachIndexed { j, qb_inner ->
                // Не проверяем одни и те же запросы.
                if (i != j) {
                    // Проверяем является ли один запрос подзапросом другого.
                    if (qb.first <= qb_inner.first && qb.last > qb_inner.last) {
                        // Проверка: был ли ранее учтен подзапрос? Если да, то удалить.
                        this.relations = this.relations.filter { it.second != j }.toMutableList()
                        this.relations.add(Pair(i, j))
                    }
                }
            }
        }
    }

    /*
    * Метод создающий уникальный идентификатор для подзапроса.
    * Идентификатор заменяет строку подзапроса в строке.
    * */
    private fun createQueryId() : String {
        return UUID.randomUUID().toString()
    }

    /*
    * Генерация ID запросов. Впоследствии id-шники используются в запросе в качестве подстановки и замены строк подзапросов.
    * */
    private fun generateQueriesIds() : MutableList<String> {
        var ids = mutableListOf<String>()
        var min = 0
        var max = maxOf((relations.maxBy { it.second })!!.second,(relations.maxBy { it.first })!!.first)

        // Генерирует id для запросов.
        while (min <= max) {
            ids.add(createQueryId())
            min++
        }

        return ids
    }

    override fun parse() {
        queries.forEach { it.parse() }
    }

    override fun print() {
        queries.forEach { it.print() }
        println("Restored query string:\n\n${this.recover()}")
    }

    override fun recover(): String {
        var recoveredQueries = ArrayList<Pair<String, String>>()
        queries.forEachIndexed { index, query ->
            recoveredQueries.add(Pair(query.getId(), if (index == 0) query.recover() else query.recoverInline()))
        }

        // Восстанавлием все запросы исходя из дерева отношений.
        var recoveredQuery = ""
        recoveredQueries.forEachIndexed { i, q ->
            if (i == 0) {
                recoveredQuery = q.second
            }
            else {
                recoveredQueries.forEach { sq ->
                    recoveredQuery = recoveredQuery.replace(sq.first, "(${sq.second})")
                }
            }
        }

        return recoveredQuery
    }

    override fun recoverInline(): String {
        return recover()
    }
}
