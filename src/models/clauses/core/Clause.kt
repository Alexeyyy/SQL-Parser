package models.clauses.core

/*
* Базовый класс для части запроса. Часть - это where, select и т.д.
* */
open class Clause(val name: String, private val contents: MutableList<String>) {
    protected fun formString() : String {
        return contents.joinToString(separator = " ")
    }
}