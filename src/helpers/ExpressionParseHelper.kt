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
    * потому что сам оператор может встретиться как часть названия или алиаса.
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

        // Проверка для not оператора при движении влево.
        if (sl > 0 && input[sl] == ' ') {
            // Если not может стоять перед выражением.
            if (sl - 3 >= 0) {
                // Далее все равно будет reverse().
                var operator = "${input[sl - 1]}${input[sl - 2]}${input[sl - 3]}"

                // Проверяем что, перед not ничего нет или стоит также пробел.
                if ((sl - 4 == -1 || sl - 4 >= 0) && input[sl - 4] == ' ') {
                    leftExpression.append(" ").append(operator)
                }
            }
        }

        // Правая группа операторов.
        var rightExpression = StringBuilder()
        while (sr < input.length && input[sr] != ' ') {
            rightExpression.append(input[sr])
            sr++

            // Если есть not , то там еще есть пробел.
            if (arrayListOf("not", "like").any { it.contains(rightExpression.toString(), ignoreCase = true) }  && sr < input.length && input[sr] == ' ') {
                rightExpression.append(" ")
                sr++
            }
        }

        // В исходной строке заменяем выражение на id.
        var id = UUID.randomUUID().toString()

        var leftBoundary = startLeft - leftExpression.length
        var rightBoundary = startRight + rightExpression.length

        // Делаем замену в строке input выражения вида a=b and c=d на id.
        var newInput = input.substring(0, leftBoundary) + id + input.substring(rightBoundary)

        // Возвращает информацию об
        return Expression(leftExpression.reverse().toString(), rightExpression.toString(), operation.trim(), id, newInput, priority)
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
        //var prevSymbol = ' '

        var symbol: Char = ' '
        var prevSymbol: Char = ' '

        for (i in input.indices) {
            if (i > 0) {
                prevSymbol = symbol
            }
            symbol = input[i]

            if (symbol == '(') {
                // Скобка для функции.
                if ("abcdefghijklmnopqrstuvwxyz".contains(prevSymbol, ignoreCase = true)) {
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
            expressions.add(Expression(inputExpression, null, null, UUID.randomUUID().toString(), inputExpression, 1))
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
}