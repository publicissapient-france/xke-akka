package fr.xebia.xkeakka.manufacturing

/**
 * @author David Galichet.
 */

package manufacturer {

import akka.actor.Actor._
import akka.actor.{ActorRef, Actor}
import akka.routing.{CyclicIterator, Routing}
import java.io.File
import collection.immutable.HashSet
import fr.xebia.xkeakka.manufacturing.transcoder.{LameTranscoder, EncodingFormat, FileFormat}

    sealed trait Event
    case class ProvisioningRequest(master:File) extends Event
    case class ProvisioningDone(formats:List[FileFormat]) extends Event
    case class GetRequiredFormats(master:File) extends Event
    case class RequiredFormat(fileFormat:FileFormat) extends Event
    case class CheckAvailability(fileFormat:FileFormat) extends Event
    case class FileAvailability(fileFormat:FileFormat, available:Boolean) extends Event
    case class EncodeFile(fileFormat:FileFormat) extends Event
    case class FileEncoded(fileFormat:FileFormat) extends Event
    case class StoreFile(fileFormat:FileFormat) extends Event

    class Provisioning(val businessPartners:List[ActorRef],
                       val transcoder:ActorRef,
                       val storageManager:ActorRef) extends Actor {

        private var requestedFileFormats = HashSet.empty[FileFormat]
        private var availableFiles = List.empty[FileFormat]
        private var masterActorMap = Map.empty[File, ActorRef] // store the actor that send the request

        def receive = {
            case ProvisioningRequest(master) => processProvisioningRequest(master)
            case RequiredFormat(fileFormat) => processBPResponse(fileFormat)
            case FileAvailability(fileFormat, true) => availableFiles ::= fileFormat
            case FileAvailability(fileFormat, false) =>  transcoder ! getEncodingRequest(fileFormat)
            case FileEncoded(fileFormat) => processFileEncoded(fileFormat)
        }

        def processProvisioningRequest(master:File) {
            // store caller reference (for response) :
            if (self.sender != None) masterActorMap += (master -> self.sender.get)
            // submit the master file to Business Partners :
            businessPartners.foreach(_ ! GetRequiredFormats(master))
        }

        def processBPResponse(fileFormat:FileFormat) {
            // process only files that don't have been already required
            if (!requestedFileFormats.contains(fileFormat)) {
                requestedFileFormats += fileFormat
                storageManager ! CheckAvailability(fileFormat)
            }
        }

        def getEncodingRequest(fileFormat:FileFormat):EncodeFile = {
                EncodeFile(fileFormat)
        }

        def processFileEncoded(fileFormat:FileFormat) {
            availableFiles ::= fileFormat
            // check if all requested files are available :
            val requiredFiles = requestedFileFormats.filter(_.master == fileFormat.master)
            val encodedFiles = availableFiles.filter(_.master == fileFormat.master)
            if (requiredFiles.size == encodedFiles.size) {
                // send response to sender
                masterActorMap.get(fileFormat.master) foreach { _ ! availableFiles }
            }
        }
    }

    class BusinessPartner(val requiredFormats:List[EncodingFormat]) extends Actor {

        def receive = {
            case GetRequiredFormats(master) =>
                requiredFormats.foreach( f => self reply RequiredFormat(FileFormat(master, f.encoderType, f.bitRate)))
        }
    }

    class StorageManager extends Actor {

        var filesAvailable = HashSet.empty[File]

        def receive = {
            case CheckAvailability(fileFormat) =>
                self reply FileAvailability(fileFormat, filesAvailable.contains(fileFormat.encodedFile))
            case StoreFile(fileFormat) => filesAvailable += fileFormat.encodedFile
        }
    }

    class TranscoderActor(storageManager:ActorRef) extends LameTranscoder with Actor {

        def receive = {
            case EncodeFile(fileFormat) => encodeFile(fileFormat)
        }

        def encodeFile(fileFormat:FileFormat) {
            if (transcode(fileFormat)) {
                self reply FileEncoded(fileFormat)
                storageManager ! StoreFile(fileFormat)
            }
        }
    }

    class ManufacturingApp {
        val businessPartners = List( actorOf(new BusinessPartner(Nil)).start(), actorOf(new BusinessPartner(Nil)).start() )
        val storageManager = actorOf[StorageManager].start()
        val transcoder = Routing.loadBalancerActor(CyclicIterator(Nil)).start() // TODO: specify transcoders (remote)
        val provisioningActor = actorOf(new Provisioning(businessPartners, transcoder, storageManager)).start()

        // TODO: define main class
    }
}