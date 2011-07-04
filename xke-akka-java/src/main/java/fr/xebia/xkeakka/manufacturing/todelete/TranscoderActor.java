package fr.xebia.xkeakka.manufacturing.todelete;

import akka.actor.UntypedActor;
import fr.xebia.xkeakka.manufacturing.event.EncodeFile;
import fr.xebia.xkeakka.manufacturing.event.FileEncoded;
import fr.xebia.xkeakka.manufacturing.event.FileStored;
import fr.xebia.xkeakka.manufacturing.event.StoreFile;

public class TranscoderActor extends UntypedActor {

    @Override
    public void onReceive(Object message) {
        if (message instanceof EncodeFile) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (getContext().getSender().isDefined()) {
                getContext().getSender().get().sendOneWay(new FileEncoded(((EncodeFile)message).fileFormat));
            } else {
                getContext().getChannel().sendOneWay(new FileEncoded(((EncodeFile) message).fileFormat));
            }
        }
    }
}
