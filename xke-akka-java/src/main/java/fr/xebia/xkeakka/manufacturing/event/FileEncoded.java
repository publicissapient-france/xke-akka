package fr.xebia.xkeakka.manufacturing.event;


import fr.xebia.xkeakka.transcoder.FileFormat;

public class FileEncoded extends Event {

    public final FileFormat fileFormat;

    public FileEncoded(FileFormat fileFormat) {
        this.fileFormat = fileFormat;
    }
}
