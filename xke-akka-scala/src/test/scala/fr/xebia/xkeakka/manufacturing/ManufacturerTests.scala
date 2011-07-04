package fr.xebia.xkeakka.manufacturing

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfterAll, WordSpec}
import akka.actor.Actor._
import akka.testkit.{TestActorRef, TestKit}
import java.io.File
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import akka.actor.ActorRef
import fr.xebia.xkeakka.manufacturing.transcoder.{EncodingFormat, FileFormat}
import akka.util.duration._
import fr.xebia.xkeakka.manufacturing.manufacturer._

/**
 * @author David Galichet.
 */

class ManufacturerTests extends WordSpec with ShouldMatchers with BeforeAndAfterAll with TestKit with MockitoSugar {

    val fileFormat1 = FileFormat(new File("file1"), "mp3", 32768)
    val fileFormat2 = FileFormat(new File("file1"), "mp3", 65536)
    val fileFormat3 = FileFormat(new File("file2"), "mp3", 32764)

    val bpActor1 = TestActorRef(new BusinessPartner(List(EncodingFormat("mp3", 32768)))).start()
    val bp1 = bpActor1.underlyingActor
    val bpActor2 = TestActorRef(new BusinessPartner(List(EncodingFormat("flac", 32768), EncodingFormat("flac", 32768)))).start()

    val storageManagerActor = TestActorRef[StorageManager].start()
    val storageManager = storageManagerActor.underlyingActor

    val transcoderActor = TestActorRef(new TranscoderActor(storageManagerActor)).start()

    val provisioningActor = TestActorRef(new Provisioning(List(bpActor1, bpActor2), transcoderActor, storageManagerActor)).start()

    override protected def afterAll() = {
        stopTestActor
        provisioningActor.stop()
        transcoderActor.stop()
        bpActor1.stop()
        storageManagerActor.stop()
    }

    "A business partner" should {
        "request required formats" in {
            val master = new File("file1")

            bpActor1 ! GetRequiredFormats(master)
            within (100 millis) {
                expectMsg(RequiredFormat(FileFormat(master, "mp3", 32768)))
            }
        }
    }

    "storage manager" should {
        "store encoded files" in {
            storageManagerActor ! StoreFile(fileFormat1)
        }
        "retrieve stored files" in {
            storageManagerActor ! CheckAvailability(fileFormat1)
            storageManagerActor ! CheckAvailability(fileFormat2)
            within( 100 millis ) {
                expectMsg(FileAvailability(fileFormat1, true))
                expectMsg(FileAvailability(fileFormat2, false))
            }
        }
    }

    "transcoder actor" should {
        "encode files" in {
            transcoderActor ! EncodeFile(fileFormat3)
            within( 100 millis ) {
                expectMsg(FileEncoded(fileFormat3))
            }
        }
        "store file un storage manager" in {
            storageManagerActor ! CheckAvailability(fileFormat3)
            within( 100 millis ) {
                expectMsg(FileAvailability(fileFormat3, true))
            }
        }
    }

    "A provisioning system" should {
        "encode required files" in {
            provisioningActor ! ProvisioningRequest(new File("music.wav"))
            within( 5000 millis ) {
                expectMsg(ProvisioningDone(Nil))
            }
        }
    }
}
