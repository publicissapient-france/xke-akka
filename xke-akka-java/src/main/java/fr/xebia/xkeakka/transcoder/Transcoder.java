package fr.xebia.xkeakka.transcoder;


import akka.event.EventHandler;

import java.io.IOException;

public abstract class Transcoder {

    public abstract void transcode(FileFormat input) throws IOException, InterruptedException;

}
