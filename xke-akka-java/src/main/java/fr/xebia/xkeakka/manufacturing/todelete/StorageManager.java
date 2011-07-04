package fr.xebia.xkeakka.manufacturing.todelete;

import akka.actor.UntypedActor;
import fr.xebia.xkeakka.manufacturing.FileFormat;
import fr.xebia.xkeakka.manufacturing.event.CheckAvailability;
import fr.xebia.xkeakka.manufacturing.event.FileAvailability;
import fr.xebia.xkeakka.manufacturing.event.FileStored;
import fr.xebia.xkeakka.manufacturing.event.StoreFile;

import java.util.HashMap;
import java.util.Map;

public class StorageManager extends UntypedActor {

    private final Map<String, FileFormat> internalStore = new HashMap<String, FileFormat>();

    @Override
    public void onReceive(Object message) {
        if (message instanceof CheckAvailability) {
            if (getContext().getSender().isDefined()) {
                getContext().getSender().get().sendOneWay(new FileAvailability(((CheckAvailability) message).fileFormat, isFileAvailable((CheckAvailability) message)));
            }
        } else if (message instanceof StoreFile) {
            storeFile((StoreFile) message);
            getContext().getSender().get().sendOneWay(new FileStored(((StoreFile) message).fileFormat));
        }
    }

    private void storeFile(StoreFile storeFile) {
        internalStore.put(storeFile.fileFormat.encodedFile.getName(), storeFile.fileFormat);
    }

    private boolean isFileAvailable(CheckAvailability checkAvailability) {
        return internalStore.containsKey(checkAvailability.fileFormat.encodedFile.getName());
    }
}
