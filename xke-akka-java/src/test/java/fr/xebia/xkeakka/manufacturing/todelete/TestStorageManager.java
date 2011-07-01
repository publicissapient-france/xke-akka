package fr.xebia.xkeakka.manufacturing.todelete;


import akka.actor.*;
import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.Actors;
import akka.actor.UntypedActor;
import fr.xebia.xkeakka.manufacturing.FileFormat;
import fr.xebia.xkeakka.manufacturing.event.CheckAvailability;
import fr.xebia.xkeakka.manufacturing.event.StoreFile;
import org.junit.Test;
import akka.testkit.TestActorRef;

import java.io.File;

import static org.junit.Assert.assertTrue;


public class TestStorageManager {

    private ActorRef actorRef = Actors.actorOf((Class<? extends Actor>) StorageManager.class).start();
    private TestingActor testingActor;
    private ActorRef testingActorRef = Actors.actorOf(new akka.actor.UntypedActorFactory() {
        public UntypedActor create() {
            testingActor = new TestingActor();
            return testingActor;
        }
    });

    @Test
    public void send_one_file_to_store() {
        testingActor.send_one_file_request_reply(new StoreFile(new FileFormat(new File("/mock/file1"), "test", 128)));
        assertTrue((Boolean) actorRef.sendRequestReply(new CheckAvailability(new FileFormat(new File("/mock/file1"), "test", 128))));
    }

    @Test
    public void send_one_billion_files_to_store() {

    }


    class TestingActor extends UntypedActor {

        // Return when file is realy stored
        public void send_one_file_request_reply(StoreFile storeFile) {
            actorRef.sendRequestReply(storeFile);
        }

        // Return when file is realy stored
        public boolean chec_availability_request_reply(CheckAvailability checkAvailability) {
            return false;
        }

        @Override
        public void onReceive(Object message) {
            if(message instanceof FileStored){

            }
        }
    }
}