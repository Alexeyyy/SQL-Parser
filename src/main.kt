import test.TestHelper
import java.io.File
import java.io.PrintStream


fun main(args: Array<String>) {
    // Перенаправляем поток в файл.
    val o = PrintStream(File("result.txt"))
    System.setOut(o)

    // Подробности в TestHelper.
    var data = TestHelper.loadData("testData.txt")
    TestHelper.testQueries(data, "log.txt")
}