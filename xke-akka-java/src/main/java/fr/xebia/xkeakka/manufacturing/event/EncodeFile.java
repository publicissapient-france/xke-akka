package fr.xebia.xkeakka.manufacturing.event;


import fr.xebia.xkeakka.manufacturing.FileFormat;

public class EncodeFile extends Event {
    public final FileFormat fileFormat;

    public EncodeFile(FileFormat fileFormat) {
        this.fileFormat = fileFormat;
    }
}