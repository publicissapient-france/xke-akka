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

        private var requestedFileFormats = HashSet.empty[FileFormat] // TODO: change to Map[File, FileFormat]
        private var pendingEncodings = HashSet.empty[FileFormat]
        private var masterActorMap = Map.empty[File, ActorRef] // store the actor that send the request

        def receive = {
            case ProvisioningRequest(file) => businessPartners.foreach(_ ! GetRequiredFormats(file))
            case RequiredFormat(fileFormat) => storageManager ! CheckAvailability(fileFormat)
            case FileAvailability(fileFormat, true) => getEncodingRequest(fileFormat) foreach { transcoder ! _ }
            case FileEncoded(fileFormat) => processFileEncoded(fileFormat) foreach { masterActorMap(fileFormat.master) ! _ }
        }

        def getEncodingRequest(fileFormat:FileFormat):Option[EncodeFile] = {
            if (requestedFileFormats.contains(fileFormat)) {
                None
            } else {
                requestedFileFormats += fileFormat
                pendingEncodings += fileFormat
                if (self.sender != None) masterActorMap += (fileFormat.master -> self.sender.get)
                Some(EncodeFile(fileFormat))
            }
        }

        def processFileEncoded(fileFormat:FileFormat):Option[ProvisioningDone] = {
            pendingEncodings -= fileFormat
            if (pendingEncodings.isEmpty) {
                val provisioningDone = ProvisioningDone(requestedFileFormats.toList)
                requestedFileFormats = HashSet.empty[FileFormat]
                Some(provisioningDone)
            } else {
                None
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
        }
    }

    class TranscoderActor extends LameTranscoder with Actor {

        def receive = {
            case EncodeFile(fileFormat) => transcode(fileFormat); self reply FileEncoded(fileFormat) // TODO manage failure
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