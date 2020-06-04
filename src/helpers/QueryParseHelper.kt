package helpers

import java.lang.Exception
import java.util.ArrayDeque
import kotlin.collections.ArrayList


/*
* Хелпер, содержащий вспомогательные методы для работы со строками.
* */
object QueryParseHelper {
    /*
    * Осуществляет поиск подзапросов и возвращает их позиции в строке.
    * */
    fun findQueriesBounds(input: String) : ArrayList<IntRange> {
        var searchStr = "${KeyWords.keywordBoundaryMark}select${KeyWords.keywordBoundaryMark}"

        // Находим все вхождения @@@select@@@ в строке через регулярные выражения.
        var regex = Regex(searchStr, RegexOption.IGNORE_CASE)

        // Поиск всех запросов и подзапросов (они же все на select начинаются).
        var matches = regex.findAll(input)

        // Преобразуем в последовательность, чтобы границы каждого запроса найти отдельно.
        var ranges = matches.map { m -> m.range }

        // Получаем позиции всех запросов.
        var startPos = -1
        var lastPos = -1

        var queriesBounds = arrayListOf<IntRange>()

        ranges.forEachIndexed { index, range ->
            // Корневой запрос.
            if (index == 0) {
                startPos = 0
                lastPos = input.length

                queriesBounds.add(IntRange(startPos, lastPos))
            }

            // Подзапросы, а они все выделены скобками, согласно правилам SQL, сохраняем.
            if (index > 0) {
                // На всякий случай делаем проверку. Хотя по идее условие всегда должно выполняться.
                if (range.first - 1 > 0 && input[range.first - 1] == '(') {
                    startPos = range.first //- 1
                    lastPos = findCloseBracketPos(input, range.first - 1)

                    queriesBounds.add(IntRange(startPos, lastPos))
                }
                else {
                    throw Exception("Запрос скорее всего составлен некорректно или напишите в телеграм @alex_zhelepov – я исправлю!")
                }
            }
        }

        return queriesBounds
    }

    /*
    * Поиск соответствующей закрывающей скобки.
    * input - строка запроса.
    * start - позиция скобки в запросе, от которой начинается поиск.
    * */
    private fun findCloseBracketPos(input: String, start: Int) : Int {
        var marker = 1
        var index = start + 1

        while (index < input.length) {
            when (input[index]) {
                '(' -> marker++
                ')' -> marker--
            }

            if (marker == 0) {
                return index
            }

            index++
        }

        return -1
    }

    /*
    * Функция постобработки строки запроса.
    * Обрабатывает интересные кейсы повторений разделителей.
    * */
    private fun postProcessing(input: String) : String {
        var queryStr = input

        // Комбинации из двух слов.
        var combinations = arrayListOf(
                Pair("group", "by"),
                Pair("order", "by"),
                Pair("inner", "join"),
                Pair("right", "join"),
                Pair("left", "join"),
                Pair("full", "join"),
                Pair("is", "not"),
                Pair("not", "like")
        )

        combinations.forEach { comb ->
            var from = "${KeyWords.keywordBoundaryMark}${comb.first}${KeyWords.keywordBoundaryMark}${KeyWords.keywordBoundaryMark}${comb.second}${KeyWords.keywordBoundaryMark}"
            var to = "${KeyWords.keywordBoundaryMark}${comb.first} ${comb.second}${KeyWords.keywordBoundaryMark}"
            queryStr = queryStr.replace(from, to, ignoreCase = true)
        }

        // Не комбинации, но слова нужно отделить.
        var separatedWords = arrayListOf(
                Pair("select", "distinct"),
                Pair("and", "not"),
                Pair("or", "not"),
                Pair("not", "in"),
                Pair("not", "between")
        )
        separatedWords.forEach { comb ->
            var from = "${KeyWords.keywordBoundaryMark}${comb.first}${KeyWords.keywordBoundaryMark}${KeyWords.keywordBoundaryMark}${comb.second}${KeyWords.keywordBoundaryMark}"
            var to = "${KeyWords.keywordBoundaryMark}${comb.first}${KeyWords.keywordBoundaryMark}${comb.second}${KeyWords.keywordBoundaryMark}"
            queryStr = queryStr.replace(from, to, ignoreCase = true)
        }

        // Комбинации из трех слова.
        var tripleWords = arrayListOf<Triple<String, String, String>>(
                Triple("full", "outer", "join"),
                Triple("right", "outer", "join"),
                Triple("left", "outer", "join")
        )

        tripleWords.forEach { comb ->
            var from = "${KeyWords.keywordBoundaryMark}${comb.first}${KeyWords.keywordBoundaryMark}${KeyWords.keywordBoundaryMark}${comb.second}${KeyWords.keywordBoundaryMark}${KeyWords.keywordBoundaryMark}${comb.third}${KeyWords.keywordBoundaryMark}"
            var to = "${KeyWords.keywordBoundaryMark}${comb.first} ${comb.second} ${comb.third}${KeyWords.keywordBoundaryMark}"
            queryStr = queryStr.replace(from, to, ignoreCase = true)
        }

        //asc и desc (потому что они в конце строки запроса).
        var specialCases = arrayListOf<String>("asc", "desc")
        specialCases.forEach { comb ->
            var from = "${comb}${KeyWords.keywordBoundaryMark}${KeyWords.keywordBoundaryMark}"
            var to = "${comb}${KeyWords.keywordBoundaryMark}"
            queryStr = queryStr.replace(from, to, ignoreCase = true)
        }

        return queryStr
    }

    /*
    * Удаление лишних пробелов в запросе, приведение его в нормальную форму.
    * Нормальная форма подразумевает выделение ключевых слов в запросе особыми символами для упрощения его дальнейшего парсинга.
    * */
    fun normalizeQueryString(input: String): String {
        var newStr = StringBuilder()
        var index = 0

        var prevKeyword = ""

        // Проходим посимвольно строку.
        while(index < input.length) {
            // Если символ не пробел, то сохраняем его в подстроку.
            if (input[index] != ' ') {
                var part = StringBuilder()

                // Если встретились скобки, то сохраняем их по одной и выходим на следующую итерацию.
                // Учитываются особые случаи, когда, например, "(select sum()".
                if (input[index] in arrayListOf('(', ')')) {
                    when (input[index]) {
                        '(' -> newStr.append('(')
                        ')' -> newStr.append(')')
                    }
                    index++
                    continue
                }

                // Если скобок нет, то идем дальше.
                var keyword = StringBuilder()

                // Не забываем исключить скобки.
                while (index < input.length && input[index] != ' ' && input[index] !in arrayListOf('(', ')')) {
                    keyword.append(input[index])
                    index++
                }

                var keywordStr = keyword.toString().toLowerCase()

                when (keywordStr) {
                    in KeyWords.keyWords -> part.append("${KeyWords.keywordBoundaryMark}${keyword}${KeyWords.keywordBoundaryMark}")
                    else -> part.append(keyword)
                }

                // Недостающие AS для алиасов.
                // Если предыдущая часть не пуста, то проверяем нужно ли обозначать алиас.
                if (prevKeyword.isNotEmpty()) {
                    // Проверка 1. Предыдущее слово и текущее слово не ключевые.
                    if (prevKeyword !in KeyWords.keyWords && keywordStr !in KeyWords.keyWords) {

                        // Проверка 2. Алиас всегда состоит только из букв, цифр, некоторых знаков: _
                        if (matchRegex(keywordStr, "^\\d*[a-zA-Zа-яА-Я_\"][\"a-zA-Zа-яА-Я_\\d]*\$")) {

                            // Проверка 3. Предыдущее слово не оканчивается на ',' и другие знаки.
                            var checkSet = arrayListOf(',', '!', '=', '>', '<')
                            var containsIt = false

                            for (i in checkSet.indices) {
                                if (prevKeyword.contains(checkSet[i], ignoreCase = true) || keywordStr.contains(checkSet[i], ignoreCase = true)) {
                                    containsIt = true
                                    break
                                }
                            }

                            if (!containsIt) {
                                newStr.append("${KeyWords.keywordBoundaryMark}AS${KeyWords.keywordBoundaryMark}")
                            }
                        }
                    }
                }

                prevKeyword = keywordStr
                newStr.append(part)
            }
            else {
                index++
            }
        }

        return postProcessing(newStr.toString())
    }

    /*
    * Разделяет запрос на смысловые подстроки.
    * Смысловая - это GROUP BY, ORDER BY, LIMIT, SELECT и т.д.
    * */
    fun splitByClauses(input: String) : MutableList<Pair<String, MutableList<String>>> {
        var queryContents = input.split(KeyWords.keywordBoundaryMark)
        var queryByClauses = mutableListOf<Pair<String, MutableList<String>>>()

        var index = 0
        var part = ""

        while (index < queryContents.size) {
            part = queryContents[index]

            // Если пустышка, то игнорируем.
            if (part == "") {
                index++
                continue
            }

            // Если это ключевая комбинация, то начинаем сохранять данные этой Clause до следующей комбинации.
            if (KeyWords.clauses.any { it.equals(part, ignoreCase = true) }) {
                var data = Pair(part, mutableListOf<String>())

                index++
                part = queryContents[index]

                while (index < queryContents.size && !KeyWords.clauses.any { it.equals(part, ignoreCase = true) }) {
                    part = queryContents[index]

                    // Чтобы в следующей итерации рассмотреть эту Clause.
                    if (KeyWords.clauses.any { it.equals(part, ignoreCase = true) }) {
                        break
                    }

                    data.second.add(part)
                    index++
                }

                queryByClauses.add(data)
            }
        }

        return queryByClauses
    }

    /*
    * Возвращает Pair<значение , алиас> из последовательности вида d.* as a --> a
    * или d.col a --> a.
    * Если алиас отсутствует, то возвращает Pair <значение, null>.
    * */
    fun getValueAndAlias(input: String) : Pair<String, String?> {
        var alias: String? = null
        var value: String

        var split = input.split(" ", ignoreCase = true)

        if (split.size == 3) {
            value = split[0].trim()
            // [1] - as.
            alias = split[2].trim()
        }
        else {
            value = input.trim()
        }

        return Pair(value, alias)
    }

    /*
    * Проверяет есть ли в строке соответсвующая паттерну последовательность.
    * */
    fun containsRegex(input: String, pattern: String) : Boolean {
        var regex = Regex(pattern, RegexOption.IGNORE_CASE)
        var matches = regex.findAll(input)
        return matches.any()
    }

    /*
    * Проверяет насколько строка соответствует описанию.
    * */
    fun matchRegex(input: String, pattern: String) : Boolean {
        var regex = Regex(pattern, RegexOption.IGNORE_CASE)
        return regex.matches(input)
    }

    /*
    * Когда подзапрос заменяется на guid, то остается несколько скобок слева и справа.
    * "Красота требует жертв", как и вывод, поэтому удаляем их.
    * А практичность заключается в том, чтобы запрос не засорять скобками при восстановлении.
    * */
    fun removeExtraBracketsForSubQuery(input: String) : String {
        if (matchRegex(input, "[(]+([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})[)]+")) {
            var regex = Regex("([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})")
            var ranges = regex.findAll(input).map { it.range }

            if (ranges.count() == 1) {
                var range = ranges.first()
                return input.substring(range.first, range.last + 1) //+1 гарантированно есть, так как в input есть скобки.
            }
        }

        return input
    }

    /*
    * Убирает лишние символы из строки запроса.
    * */
    fun convertQueryToLine(input: String) : String {
        var newQuery = input.replace("\t", " ")
        newQuery = newQuery.replace("\n", " ")

        // Делается специально, чтобы правильно отработала постановка алиасов.
        newQuery = newQuery.replace(",", " , ")

        return newQuery
    }
}