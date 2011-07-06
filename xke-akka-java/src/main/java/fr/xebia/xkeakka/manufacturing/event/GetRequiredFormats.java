package fr.xebia.xkeakka.manufacturing.event;


import java.io.File;

public class GetRequiredFormats extends Event{

    public final File file;

    public GetRequiredFormats(File file) {
        this.file = file;
    }
}
