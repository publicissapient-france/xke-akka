package fr.xebia.xkeakka.transcoder;

import java.io.File;

public final class FileFormat {

    public final String encodedFilePath;

    public final File master;

    public FileFormat(File master, String encoderType, int bitRate) {
        this.master = master;
        int index = master.getName().lastIndexOf(".");
        String masterExtensionLess;

        if (index >= 0) {
            masterExtensionLess = master.getName().substring(0, index);
        } else {
            masterExtensionLess = master.getName();
        }
        encodedFilePath = master.getAbsoluteFile().getParent() + "/encoded/%s_%s.%s".format(masterExtensionLess, bitRate, encoderType);
    }

    public String getFileName() {
        File file = new File(encodedFilePath);
        String name = file.getName();
        return name;
    }
}

