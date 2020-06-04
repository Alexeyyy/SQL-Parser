package test

import helpers.QueryParseHelper
import models.QueryTree
import java.io.File
import java.io.FileNotFoundException
import java.lang.Exception

object TestHelper {

    /* Загружает тестовые данные из файла.
    *  Данные в файле находятся в формате:
    * =====================================
    * <ЗАПРОС>
    * =====================================
    * */
    fun loadData(filePath: String): ArrayList<String> {
        val queries = arrayListOf<String>()

        try {
            var regex = Regex("[#]+", RegexOption.IGNORE_CASE)
            var queryStr = StringBuilder()

            var file = File(filePath)
            file.forEachLine { line ->
                // Если разделитель.
                if (regex.matches(line)) {
                    queries.add(queryStr.toString())
                    queryStr = queryStr.clear()
                }
                // Иначе сохраняем запрос.
                else {
                    queryStr.append(line).append("\n")
                }
            }
        }
        catch (e: FileNotFoundException) {
            println("$filePath does not exist")
            return arrayListOf()
        }
        catch (e: Exception) {
            println("An error occurred ${e.message}")
            return arrayListOf()
        }

        return queries
    }

    /*
    * Запускает парсер запросов для каждого из них.
    * Результат логгирует в файл, указанный в качестве аргумента.
    * */
    fun testQueries(queries: ArrayList<String>, logFilePath: String) {
        try {
            for (i in 0 until queries.size) {
                var queryStr = queries[i]

                try {
                    // 1. Преобразование запроса в строку без переносов (\n) и табуляций (\t).
                    var inlineQuery = QueryParseHelper.convertQueryToLine(queryStr)

                    // 2. "Нормализация" запроса.
                    var normalizedQuery = QueryParseHelper.normalizeQueryString(inlineQuery)

                    // 3. Поиск границ запроса и его подзапросов.
                    var subQueriesBounds = QueryParseHelper.findQueriesBounds(normalizedQuery)

                    // 4. Создание дерева запросов.
                    var tree = QueryTree(normalizedQuery, subQueriesBounds, queryStr)

                    // 5. Парсинг запросов внутри дерева.
                    tree.parse()

                    // 6. Вывод на экран и в файл логирования.
                    tree.print()
                } catch (e: Exception) {
                    println("The error raised while parsing the query number $i ")
                }
                finally {
                    println("**********************************************************************************")
                    print("\n\n\n\n\n")
                }
            }
        } catch (e: Exception) {
            println("Something serious occurred! The message is ${e.message}")
        }
    }
}