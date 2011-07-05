package fr.xebia.xkeakka.manufacturing.todelete;

import akka.actor.UntypedActor;
import fr.xebia.xkeakka.manufacturing.FileFormat;
import fr.xebia.xkeakka.manufacturing.event.*;

import java.util.HashMap;
import java.util.Map;

public class StorageManager extends UntypedActor {

    private final Map<String, FileFormat> internalStore = new HashMap<String, FileFormat>();

    @Override
    public void onReceive(Object message) {
        if (message instanceof CheckAvailability) {
            if (getContext().getSender().isDefined()) {
                getContext().getSender().get().sendOneWay(new FileAvailability(((CheckAvailability) message).fileFormat, isFileAvailable((CheckAvailability) message)));

            } else {
                getContext().getChannel().sendOneWay(new FileAvailability(((CheckAvailability) message).fileFormat, isFileAvailable((CheckAvailability) message)));
            }
        } else if (message instanceof StoreFile) {
            storeFile((StoreFile) message);
            if (getContext().getSender().isDefined()) {
                getContext().getSender().get().sendOneWay(new FileStored(((StoreFile) message).fileFormat));
            } else {
                getContext().getChannel().sendOneWay(new FileStored(((StoreFile) message).fileFormat));
            }
        } else if (message instanceof CleanStorage) {
            internalStore.clear();
            if (getContext().getSender().isDefined()) {
                getContext().getSender().get().sendOneWay(new Object());
            } else {
                getContext().getChannel().sendOneWay(new Object());
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private void storeFile(StoreFile storeFile) {
        internalStore.put(storeFile.fileFormat.getFileName(), storeFile.fileFormat);
    }

    private boolean isFileAvailable(CheckAvailability checkAvailability) {
        return internalStore.containsKey(checkAvailability.fileFormat.getFileName());
    }
}
