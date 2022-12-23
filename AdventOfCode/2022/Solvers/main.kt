import java.io.BufferedReader
import java.io.File


fun main(args: Array<String>) {
    val bufferedReader: BufferedReader = File("Solvers/tests/day1.txt").bufferedReader()
    val inputString = bufferedReader.use {
        it.readLine()
    }
    var underdog = 0
    var count = 0
    var winners = arrayOf(0, 0, 0)
    File("Solvers/tests/day1.txt").readLines().forEach {
        count++
        // println(underdog)
        if (it.isNotEmpty() && it != "") {
            underdog += it.toInt()
        }
        if (it.isEmpty() && it == "") {
            var checked = false
            if (winners[0] < underdog) {
                val tmp1 = winners[0]
                winners[0] = underdog
                val tmp2 = winners[1]
                winners[1] = tmp1
                winners[2] = tmp2
                checked = true
            }
            var checked2 = false
            if (winners[1] < underdog && !checked) {
                val tmp1 = winners[1]
                winners[1] = underdog
                winners[2] = tmp1
                checked2 = true
            }
            if (winners[2] < underdog && !checked && !checked2) {
                winners[2] = underdog
            }




            println("1st: ${winners[0]}, 2nd: ${winners[1]}, 3rd: ${winners[2]} ")
            underdog = 0
        }
    }
    println("total of top 3 is ${winners[0] + winners[1] + winners[2]}")
}
