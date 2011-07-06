package fr.xebia.xkeakka.manufacturing.event;

import fr.xebia.xkeakka.transcoder.FileFormat;

public class FileStored extends Event {

    public final FileFormat fileFormat;

    public FileStored(FileFormat fileFormat) {
        this.fileFormat = fileFormat;
    }
}
