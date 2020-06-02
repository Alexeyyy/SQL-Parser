package models.clauses.core

import com.sun.org.apache.xpath.internal.operations.Bool

interface Parser {
    /*
    * Метод парсинга для элемента запроса: для каждого своя реализация.
    * */
    fun parse() {}

    /*
    * Метод отображения извлеченных парсером данных.
    * */
    fun print() {}

    /*
    * Метод восстановления части запроса по тем данным, которые удалось извлечь.
    * */
    fun recover() : String

    /*
    * Метод восстановления запроса в виде строки. Аналогичен recover(), но сохраняет результат в виде строки.
    * */
    fun recoverInline() : String
}