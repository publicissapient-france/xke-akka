package fr.xebia.xkeakka.manufacturing.event;


import fr.xebia.xkeakka.manufacturing.FileFormat;

public class FileEncoded extends Event {

    public final FileFormat fileFormat;

    public FileEncoded(FileFormat fileFormat) {
        this.fileFormat = fileFormat;
    }
}
