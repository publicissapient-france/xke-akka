package fr.xebia.xkeakka.manufacturing.event;

import java.io.File;

public class ProvisioningRequest extends Event {

    public final File file;

    public ProvisioningRequest(File file) {
        this.file = file;
    }

}
