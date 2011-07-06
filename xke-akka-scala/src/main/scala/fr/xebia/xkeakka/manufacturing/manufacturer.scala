package fr.xebia.xkeakka.manufacturing

/**
 * @author David Galichet.
 */

package manufacturer {


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

}