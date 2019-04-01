package es.jepp.legomachinelearning

import java.io.File
import java.io.IOException

class Helpers {
    companion object {
        fun isFilenameValid(filename: String) : Boolean {
            val f = File(filename)
            try {
                f.getCanonicalPath()
                return true
            } catch (e: IOException) {
                return false
            }

        }
    }
}