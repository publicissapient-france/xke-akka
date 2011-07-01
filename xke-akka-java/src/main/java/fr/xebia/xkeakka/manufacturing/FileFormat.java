package fr.xebia.xkeakka.manufacturing;

import java.io.File;

public final class FileFormat {

    public final File encodedFile;

    public FileFormat(File master, String encoderType, int bitRate) {

        int index = master.getName().lastIndexOf(".");
        String masterExtensionLess;

        if (index >= 0) {
            masterExtensionLess = master.getName().substring(0, index);
        } else {
            masterExtensionLess = master.getName();
        }
        encodedFile = new File(master.getAbsoluteFile().getParent() + "/encoded/%s_%s.%s".format(masterExtensionLess, bitRate, encoderType));
    }
}

