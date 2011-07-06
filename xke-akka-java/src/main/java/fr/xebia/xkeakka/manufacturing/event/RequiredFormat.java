package fr.xebia.xkeakka.manufacturing.event;


import fr.xebia.xkeakka.transcoder.FileFormat;

public class RequiredFormat extends Event {

    public final FileFormat fileFormat;

    public RequiredFormat(FileFormat fileFormat) {
        this.fileFormat = fileFormat;
    }

}
