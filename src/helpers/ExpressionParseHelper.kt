package helpers

import models.queryParts.Expression
import java.util.*


/*
* Хелпер для работы с выражениями в WHERE и JOIN.
* */
object ExpressionParseHelper {
    /*
    * Извлекаем следующее выражение из строки.
    * Выражение это пара условий, между которыми есть действие: and или or.
    * operation подается в виде " and ", слева и справа ограничен пробелами,
    * потому что сам оператор может встретиться как часть названия или алиаса.keywordBoundaryMark
    * */
    private fun getNextExpression(input: String, operation: String, priority: Int) : Expression {
        var startLeft = input.indexOf(operation)
        var startRight = startLeft + operation.length

        // Временные значения, которые будем менять.
        var sl = startLeft - 1 // пропускаем пробел, который слева от оператора.
        var sr = startRight

        // Левая группа операторов.
        var leftExpression = StringBuilder()
        while (sl >= 0 && input[sl] != ' ') {
            leftExpression.append(input[sl])
            sl--
        }

        // Правая группа операторов.
        var rightExpression = StringBuilder()
        while (sr < input.length && input[sr] != ' ') {
            rightExpression.append(input[sr])
            sr++
        }

        // В исходной строке заменяем выражение на id.
        var id = UUID.randomUUID().toString()

        var leftBoundary = startLeft - leftExpression.length
        var rightBoundary = startRight + rightExpression.length

        // Делаем замену в строке input выражения вида a=b and c=d на id.
        var newInput = input.substring(0, leftBoundary) + id + input.substring(rightBoundary)

        var expLeft = reUnderscoreOperators(leftExpression.reverse().toString())
        var expRight = reUnderscoreOperators(rightExpression.toString())

        // Возвращаем скобки для in.
        if (expLeft.contains("in", ignoreCase = true)) {
            expLeft = recoverBracketsForIn(expLeft)
        }

        if (expRight.contains("in", ignoreCase = true)) {
            expRight = recoverBracketsForIn(expRight)
        }

        return Expression(expLeft, expRight, operation.trim(), id, newInput, priority)
    }

    /*
    * Упрощает выражение, удаляя лишние скобки, например в таких выражениях (a=b) --> на выходе будет a=b.
    * */
    private fun deleteExtraBrackets(input: String, pos: Pair<Int, Int>) : String {
        var before = ""
        var after = ""
        var within = input.substring(pos.first + 1, pos.second)

        if (pos.first != 0) {
            before = input.substring(0, pos.first)
        }

        if (pos.second != input.length - 1) {
            after = input.substring(pos.second + 1, input.length)
        }

        return "$before$within$after"
    }

    /*
    * Устанавливает в выражение идентификатор упрощенного подвыражения.
    * */
    private fun removeSubexpression(input: String, subExprId: String, pos: Pair<Int, Int>) : String {
        var before = ""
        var after = ""

        before = input.substring(0, pos.first + 1)
        after = input.substring(pos.second)

        return "$before$subExprId$after"
    }

    /*
    * Поиск следующих скобок для парсинга.
    * */
    private fun getNextBrackets(input: String) : Pair<Int, Int> {
        var brackets = mutableMapOf<Int,Pair<Int, Int>>()
        // Triple: openedMarker, index, относиться к функции (true) или нет (false).
        var openedBrackets = ArrayDeque<Triple<Int, Int, Boolean>>()
        var openedMarker = 0

        var symbol: Char = ' '
        var prevSymbol: Char = ' '

        for (i in input.indices) {
            if (i > 0) {
                prevSymbol = symbol
            }
            symbol = input[i]

            if (symbol == '(') {
                // Скобка для функции.
                if ("abcdefghijklmnopqrstuvwxyz1234567890".contains(prevSymbol, ignoreCase = true)) {
                    openedBrackets.push(Triple(openedMarker, i, true))
                    openedMarker++
                    continue
                }

                // Просто скобка.
                openedBrackets.push(Triple(openedMarker, i, false))
                openedMarker++
                continue
            }

            if (symbol == ')') {
                var lastOpened = openedBrackets.pop()

                // Если скобка не относится к функции, то сохраняем пару скобок.
                if (!lastOpened.third) {
                    brackets[lastOpened.first] = Pair(lastOpened.second, i)
                }
            }
        }

        // Определяем максимально "глубокую" пару скобок по маркеру и возвращаем ее.
        var bracketsPos = brackets.maxBy { it.key }

        // Если скобок в последовательности нет, то возвращаем Pair(-1, -1).
        return bracketsPos?.value ?: Pair(-1, -1)
    }

    /*
    * Парсит выражения для JOIN и WHERE.
    * */
    fun parseExpressionToTable(inputExpression: String) : MutableList<Expression> {
        var expressionCopy = inputExpression

        var expressions = mutableListOf<Expression>()

        // Приоритет выполнения операции.
        var priority = 1

        // Всевозможные условные операторы для join / where.
        val actions = arrayListOf(" and ", " or ")

        // Идентификатор последней разобранной группы операндов.
        var lastId = ""

        // Условие состоящие из одного выражения. Например, a=b.
        if (!actions.any { inputExpression.contains(it) }) {
            var exp = reUnderscoreOperators(inputExpression)
            expressions.add(Expression(exp, null, null, UUID.randomUUID().toString(), inputExpression, 1))
        }
        // Сложное составное условие с and или or, или даже скобками!
        else {
            while (actions.any { expressionCopy.contains(it) }) {
                val pos = getNextBrackets(expressionCopy)
                var subexpression = ""

                // Скобки найдены.
                if (pos.first != -1) {
                    // Проверяем не просто ли выражение ограничено скобками.
                    subexpression = expressionCopy.substring(pos.first + 1, pos.second)

                    // Выражение просто ограничено скобками, например (a=b).
                    // Удаляем скобки и переходим к следующей итерации.
                    if (!actions.any { subexpression.contains(it) }) {
                        expressionCopy = deleteExtraBrackets(expressionCopy, pos)
                        continue
                    }
                }
                // Копируем оставшееся выражение, так как скобок больше нет.
                else {
                    subexpression = expressionCopy
                }

                // В подвыражении в скобках есть действия.
                while (actions.any { subexpression.contains(it) }) {
                    // Первоприоритетная операция and.
                    if (subexpression.contains(" and ")) {
                        var expression = getNextExpression(subexpression, " and ", priority)
                        expressions.add(expression)
                        priority++

                        // Текущее состояние выражения после внесенных изменений.
                        subexpression = expression.entireExpression
                        lastId = expression.id

                        continue
                    }

                    // Вторая по приоритету операция or.
                    if (subexpression.contains(" or ")) {
                        var expression = getNextExpression(subexpression, " or ", priority)
                        expressions.add(expression)
                        priority++

                        // Текущее состояние выражения после внесенных изменений.
                        subexpression = expression.entireExpression
                        lastId = expression.id

                        continue
                    }
                }

                // Проверяем: работали ли с подвыражением в скобках. Если да, то продолжим упрощение.
                if (pos.first != -1) {
                    expressionCopy = removeSubexpression(expressionCopy, lastId, pos)
                    continue
                }
                // Если скобок не было, то работали с целым выражением, поэтому просто выходим из цикла
                else {
                    break
                }
            }
        }

        return expressions
    }

    fun restoreExpression(expressions: MutableList<Expression>) : String {
        var condition = expressions.maxBy { it.priority }
        var priority = condition!!.priority

        // Условие состоит из одного выражения.
        if (priority == 1) {
            // Если условие не из двух частей.
            if (condition.rightExpression == null) {
                return condition.leftExpression
            }

            return "${condition.leftExpression} ${condition.operator} ${condition.rightExpression}"
        }

        // Собираем выражение.
        var resultExpression = "(${condition.leftExpression} ${condition.operator} ${condition.rightExpression})"
        priority--

        while (priority != 0) {
            condition = expressions.single { it.priority == priority }
            resultExpression = resultExpression.replace(condition.id, "(${condition.leftExpression} ${condition.operator} ${condition.rightExpression})")
            priority--
        }

        return resultExpression
    }

    /*
    * Выделяет сложные операторы строками, для упрощения процедуры парсинга.
    * Метод должен вызываться перед парсингом строк таким clause, как where, having, join.
    * */
    fun underscoreOperators(input: String) : String {
        var str = StringBuilder()
        var part = StringBuilder()

        var index = 0
        var nextAnd = false

        while(index <= input.length) {
            if (index != input.length) {
                when {
                    input[index] != ' ' -> {
                        part.append(input[index])
                    }
                    part.isEmpty() -> { }
                    else -> {
                        var partStr = part.toString()

                        if (partStr in KeyWords.operators) {
                            str.append("${KeyWords.operatorBoundaryMark}${partStr}${KeyWords.operatorBoundaryMark}")

                            // Необходимо найти следующий and и выделить его также.
                            if (partStr.equals("between", ignoreCase = true)) {
                                nextAnd = true
                            }
                        } else {
                            // Нужно выделить and.
                            if (nextAnd && partStr.equals("and", ignoreCase = true)) {
                                str.append("${KeyWords.operatorBoundaryMark}${partStr}${KeyWords.operatorBoundaryMark}")
                                nextAnd = false
                            }
                            else {
                                str.append(partStr)
                                str.append(' ')
                            }
                        }
                        part = StringBuilder()
                    }
                }
            }
            else {
                var partStr = part.toString()
                if (partStr in KeyWords.operators) {
                    str.append("${KeyWords.operatorBoundaryMark}${partStr}")
                } else {
                    str.append(partStr)
                }
            }
            index++
        }

        var result = str.toString().replace(" ${KeyWords.operatorBoundaryMark}", "${KeyWords.operatorBoundaryMark}")
        result = result.replace("${KeyWords.operatorBoundaryMark}${KeyWords.operatorBoundaryMark}", KeyWords.operatorBoundaryMark)

        if (result.startsWith(KeyWords.operatorBoundaryMark)) {
            result = result.substring(KeyWords.operatorBoundaryMark.length)
        }

        result += KeyWords.operatorBoundaryMark

        if (result.endsWith(KeyWords.operatorBoundaryMark)) {
            result = result.substring(0, result.length - KeyWords.operatorBoundaryMark.length)
        }

        return result
    }

    /*
    * Удаляет выделения сложных операторов подвыражений при сохранении их в таблицы.
    * Также это делается, чтобы при восстановлении запроса он выглядел по-человечески безо вских лишних операторов.
    * */
    private fun reUnderscoreOperators(input: String) : String {
        return input.replace(KeyWords.operatorBoundaryMark, " ")
    }

    /*
    * Восставливает скобки для "in" оператора.
    * */
    private fun recoverBracketsForIn(input: String) : String {
        var result = input

        var searchStr = " in "
        var pos = input.indexOf(searchStr)
        pos += searchStr.length

        // На всякий случай, уже паранойя по этому поводу =)
        if (pos < input.length) {
            result = "${input.substring(0, pos)}(${input.substring(pos)})"
        }

        return result
    }
}