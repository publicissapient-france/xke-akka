package fr.xebia.xkeakka.manufacturing.event;

import fr.xebia.xkeakka.transcoder.FileFormat;

public class CheckAvailability extends Event{

    public final FileFormat fileFormat;

    public CheckAvailability(FileFormat fileFormat) {
        this.fileFormat = fileFormat;
    }
}
