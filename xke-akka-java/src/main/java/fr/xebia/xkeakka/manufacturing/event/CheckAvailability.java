package fr.xebia.xkeakka.manufacturing.event;

import fr.xebia.xkeakka.manufacturing.FileFormat;

public class CheckAvailability extends Event{

    public final FileFormat fileFormat;

    public CheckAvailability(FileFormat fileFormat) {
        this.fileFormat = fileFormat;
    }
}
