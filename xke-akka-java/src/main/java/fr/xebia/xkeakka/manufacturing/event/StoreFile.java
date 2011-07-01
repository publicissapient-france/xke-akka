package fr.xebia.xkeakka.manufacturing.event;


import fr.xebia.xkeakka.manufacturing.FileFormat;

public class StoreFile extends Event {

    public final FileFormat fileFormat;

    public StoreFile(FileFormat fileFormat) {
        this.fileFormat = fileFormat;
    }
}
