package fr.xebia.xkeakka.manufacturing

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfterAll, WordSpec}
import akka.actor.Actor._
import fr.xebia.xkeakka.manufacturing.manufacturer.{RequiredFormat, Provisioning}
import fr.xebia.xkeakka.manufacturing.transcoder.FileFormat
import akka.testkit.{TestActorRef, TestKit}
import java.io.File

/**
 * @author David Galichet.
 */

class ManufacturerTests extends WordSpec with ShouldMatchers with BeforeAndAfterAll with TestKit {

    "A provisioning system" should {
        "manage the list of file formats to compute" in {
            val provisioningRef = TestActorRef(new Provisioning(Nil, null, null))
            val provisioning = provisioningRef.underlyingActor
            val fileFormat1 = FileFormat(new File("file1"), "mp3", 32768)
            val fileFormat2 = FileFormat(new File("file1"), "mp3", 65536)
            val fileFormat3 = FileFormat(new File("file2"), "mp3", 32764)
            var encodeFileRequest = provisioning.getEncodingRequest(fileFormat1)
            encodeFileRequest should not be (None)
            encodeFileRequest.get.fileFormat should be(fileFormat1)
            encodeFileRequest = provisioning.getEncodingRequest(fileFormat1)
            encodeFileRequest should be (None)
            encodeFileRequest = provisioning.getEncodingRequest(fileFormat2)
            encodeFileRequest should not be (None)
            encodeFileRequest.get.fileFormat should be(fileFormat2)
            encodeFileRequest = provisioning.getEncodingRequest(fileFormat3)
            encodeFileRequest should not be (None)
            encodeFileRequest.get.fileFormat should be(fileFormat3)
        }

        "manage file encoded notification" in {
            val provisioningRef = TestActorRef(new Provisioning(Nil, null, null))
            val provisioning = provisioningRef.underlyingActor
            val fileFormat1 = FileFormat(new File("file1"), "mp3", 32768)
            val fileFormat2 = FileFormat(new File("file1"), "mp3", 65536)
            val fileFormat3 = FileFormat(new File("file2"), "mp3", 32764)
            provisioning.getEncodingRequest(fileFormat1)
            provisioning.getEncodingRequest(fileFormat2)
            provisioning.getEncodingRequest(fileFormat3)
            provisioning.processFileEncoded(fileFormat1) should be(None)
            provisioning.processFileEncoded(fileFormat3) should be(None)
            val encodedList = provisioning.processFileEncoded(fileFormat2)
            encodedList should not be(None)
            encodedList.get.formats should contain(fileFormat1)
            encodedList.get.formats should contain(fileFormat2)
            encodedList.get.formats should contain(fileFormat3)
        }
    }
}