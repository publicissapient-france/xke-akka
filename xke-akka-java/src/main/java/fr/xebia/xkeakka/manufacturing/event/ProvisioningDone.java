package fr.xebia.xkeakka.manufacturing.event;


import fr.xebia.xkeakka.transcoder.FileFormat;

import java.util.List;

public class ProvisioningDone extends Event {

    public final List<FileFormat> fileFormat;

    public ProvisioningDone(List<FileFormat> fileFormat) {
        this.fileFormat = fileFormat;
    }

}
