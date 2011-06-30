package fr.xebia.xkeakka

/**
 * @author David Galichet.
 */

package util {

import java.lang.ProcessBuilder
import io.Source.fromInputStream
import scala.collection.JavaConversions._

case class CommandBuilder(command:String, args:List[String], inputHandler:String => Unit, errorHandler:String => Unit) {

    val pb = new ProcessBuilder(command::args)
    def call() = {
        val process = pb.start()
        val status = process.waitFor()
        inputHandler(fromInputStream(process.getInputStream()).getLines().mkString("\n"))
        errorHandler(fromInputStream(process.getErrorStream()).getLines().mkString("\n"))
        status == 0
    }
}

}