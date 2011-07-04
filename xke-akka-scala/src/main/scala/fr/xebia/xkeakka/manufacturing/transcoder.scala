package fr.xebia.xkeakka.manufacturing

/**
 * @author David Galichet.
 */

package transcoder {

import java.io.File
import akka.actor.Actor
import akka.event.EventHandler
import fr.xebia.xkeakka.util.CommandBuilder

sealed trait Encoding

case class EncodingFormat(encoderType:String, bitRate:Int)

case class FileFormat(master:File, encoderType:String, bitRate:Int) {
    val encodedFile = {
        val index = master.getName.lastIndexOf(".")
        val masterExtensionLess = if (index >= 0) master.getName.substring(0, index) else master.getName
        new File(master.getAbsoluteFile.getParent + "/encoded/%s_%s.%s".format(masterExtensionLess, bitRate, encoderType))
    }
}

abstract class Transcoder {

    def transcode(input:FileFormat):Boolean

    def handleInput(message:String):Unit = EventHandler.info(this, "Transcoding result \n" + message)
    def handleError(message:String):Unit = EventHandler.error(this, "Transcoding error : \n" + message)

}

trait LameTranscoder extends Transcoder { //self:Actor =>

    // lame
    def transcode(fileFormat:FileFormat) = {
        val (command, args) = commandBuilder(fileFormat)
        CommandBuilder(command, args, handleInput, handleError).call
    }

    def commandBuilder(fileFormat:FileFormat):(String, List[String]) = { // TODO: build lame options
        var args = List.empty[String]
        args ::= fileFormat.master.getAbsolutePath
        args ::= fileFormat.encodedFile.getAbsolutePath
        ("./lame.sh", args.reverse)
    }
}

}