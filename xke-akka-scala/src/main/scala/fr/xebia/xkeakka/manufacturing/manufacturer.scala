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
import akka.event.EventHandler

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

    trait ProvisioningActor extends Actor {

        val businessPartners:List[ActorRef]
        val transcoder:ActorRef
        val storageManager:ActorRef

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
            EventHandler.info(this, "processing request for " + master.getName)
            // store caller reference (for response) :
            if (self.sender != None) masterActorMap += (master -> self.sender.get)
            // submit the master file to Business Partners :
            businessPartners.foreach(_ ! GetRequiredFormats(master))
        }

        def processBPResponse(fileFormat:FileFormat) {
            EventHandler.info(this, "processing BP response " + fileFormat)
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
            EventHandler.info(this, "processing transcoder response " + fileFormat)
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

    class BusinessPartnerActor(val requiredFormats:List[EncodingFormat]) extends Actor {

        def receive = {
            case GetRequiredFormats(master) =>
                requiredFormats.foreach( f => self reply RequiredFormat(FileFormat(master, f.encoderType, f.bitRate)))
        }
    }

    class StorageManagerActor extends Actor {

        var filesAvailable = HashSet.empty[File]

        def receive = {
            case CheckAvailability(fileFormat) => self reply FileAvailability(fileFormat, filesAvailable.contains(fileFormat.encodedFile))
            case StoreFile(fileFormat) => {
                EventHandler.info(this, "storing file " + fileFormat)
                filesAvailable += fileFormat.encodedFile
            }
        }
    }

    trait TranscoderActor extends LameTranscoder with Actor {

        val storageManager:ActorRef

        def receive = {
            case EncodeFile(fileFormat) => encodeFile(fileFormat)
        }

        def encodeFile(fileFormat:FileFormat) {
            EventHandler.info(this, "encoding file")
            if (transcode(fileFormat)) {
                self reply FileEncoded(fileFormat)
                storageManager ! StoreFile(fileFormat)
            }
        }
    }

    class ProvisioningService(val transcoderPorts:List[Int]) extends ProvisioningActor {

        val bp1 = actorOf(new BusinessPartnerActor(List(EncodingFormat("mp3", 32768), EncodingFormat("mp3", 32768*2)))).start()
        val bp2 = actorOf(new BusinessPartnerActor(List(EncodingFormat("flacc", 32768)))).start()

        val transcoders = transcoderPorts map { Actor.remote.actorFor("transcoder:service", "localhost", _) }

        val businessPartners = List( bp1, bp2 )
        val storageManager = actorOf[StorageManagerActor].start()
        val transcoder = Routing.loadBalancerActor(CyclicIterator(transcoders)).start()

        override def preStart() = {
            EventHandler.info(this, "starting provisioning")
            remote.start("localhost", 1551)
            remote.register("provisioning:service", self)
            remote.register("storage:service", storageManager)
        }

        override def postStop() = {
            EventHandler.info(this, "provisioning stopped")
            businessPartners.foreach(_.stop())
            storageManager.stop()
            transcoder.stop()
        }
    }

    class TranscoderService(val port:Int) extends TranscoderActor {

        lazy val storageManager = Actor.remote.actorFor("storage:service", "localhost", 1551)

        override def preStart() = {
            EventHandler.info(this, "starting transcoder on port " + port)
            remote.start("localhost", port)
            remote.register("transcoder:service", self)
        }
     }

    class ProvisioningClient extends Actor {

        def receive = {
            case ProvisioningDone(fileFormats) => fileFormats foreach { fileFormat =>
                println("This fileFormat has been processed" + fileFormat.toString)
            }
            case master:String => callProvisioning(new File(master))
        }

        def callProvisioning(master:File) {
            EventHandler.info(this, "processing " + master.getName)
            val provisioning = Actor.remote.actorFor("provisioning:service", "localhost", 1551)
            provisioning ! ProvisioningRequest(master)
        }
    }
}