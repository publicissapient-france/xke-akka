package fr.xebia.xkeakka.manufacturing.event;

import fr.xebia.xkeakka.transcoder.FileFormat;

public class FileAvailability extends Event{

    public final FileFormat fileFormat;

    public final boolean available;

    public FileAvailability(FileFormat fileFormat, boolean available) {
        this.fileFormat = fileFormat;
        this.available = available;
    }

}
