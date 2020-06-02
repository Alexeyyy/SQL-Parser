package models.clauses.core

open class Clause(val name: String, private val contents: MutableList<String>) {
    protected fun formString() : String {
        return contents.joinToString(separator = " ")
    }
}