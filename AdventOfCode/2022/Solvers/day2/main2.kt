import java.io.BufferedReader
import java.io.File

fun main(args: Array<String>) {
    val bufferedReader: BufferedReader = File("Solvers/tests/day1.txt").bufferedReader()
    val inputString = bufferedReader.use {
        it.readLine()
    }
    File("Solvers/tests/day2.txt").readLines().forEach {
    println(it)
    }

}
