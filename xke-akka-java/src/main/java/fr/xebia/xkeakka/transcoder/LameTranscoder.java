package fr.xebia.xkeakka.transcoder;

import java.io.IOException;

public class LameTranscoder extends Transcoder {
    @Override
    public void transcode(FileFormat input) {
        try {
            Process p = Runtime.getRuntime().exec(buildCommand(input));
            p.waitFor();
        } catch (Exception e) {
            throw new TranscodeException(e);
        }
    }

    private String buildCommand(FileFormat input) {
        return "./lame.sh " + input.master.getAbsolutePath() + " " + input.encodedFilePath;
    }
}
