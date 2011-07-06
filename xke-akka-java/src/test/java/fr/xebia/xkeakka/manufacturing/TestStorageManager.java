package fr.xebia.xkeakka.manufacturing.todelete;

import akka.actor.*;
import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.Actors;
import akka.actor.UntypedActor;
import akka.dispatch.CompletableFuture;
import akka.dispatch.Future;
import akka.dispatch.Futures;
import akka.japi.Procedure;
import fr.xebia.xkeakka.manufacturing.FileFormat;
import fr.xebia.xkeakka.manufacturing.event.*;
import org.junit.Test;
import scala.Option;

import java.io.File;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertTrue;


public class TestStorageManager {

    private ActorRef actorRef = Actors.actorOf((Class<? extends Actor>) StorageManager.class).start();

    private static final int NB_MESSAGE = 500000;


    @Test
    public void send_one_file_to_store() {
        actorRef.sendRequestReply(new StoreFile(new FileFormat(new File("/mock/file1"), "test", 128)));
        assertTrue(((FileAvailability) actorRef.sendRequestReply(new CheckAvailability(new FileFormat(new File("/mock/file1"), "test", 128)))).available);
    }

    @Test
    public void send_one_billion_files_to_store() {
        long startTime = System.nanoTime();
        for (int i = 0; i < NB_MESSAGE; i++) {
            actorRef.sendRequestReply(new StoreFile(new FileFormat(new File("/mock/file" + i), "test_" + i, 128)));
            assertTrue(((FileAvailability) actorRef.sendRequestReply(new CheckAvailability(
                    new FileFormat(new File("/mock/file" + i), "test_" + i, 128)))).available);
        }
        actorRef.sendRequestReply(new CleanStorage());
        System.out.println("Take : " + (System.nanoTime() - startTime) / 1000000 + " ms");
    }

    @Test
    public void send_one_billion_files_to_store_with_future() throws InterruptedException {
        final CountDownLatch counter = new CountDownLatch(NB_MESSAGE);
        final long startTime = System.nanoTime();
        for (int i = 0; i < NB_MESSAGE; i++) {
            actorRef.sendRequestReplyFuture(new StoreFile(new FileFormat(new File("/mock/file" + i), "test_" + i, 128)))
                    .onComplete(new Procedure() {
                @Override
                public void apply(Object param) {
                    actorRef.sendRequestReplyFuture(new CheckAvailability(((Future<FileStored>) param).get().fileFormat))
                            .onComplete(new Procedure() {
                        @Override
                        public void apply(Object param) {
                            assertTrue(((Future<FileAvailability>) param).get().available);
                            counter.countDown();
                        }
                    });
                }
            });
        }
Futures.awaitOne()
        counter.await();
        System.out.println("Take : " + (System.nanoTime() - startTime) / 1000000 + " ms");
        actorRef.sendRequestReply(new CleanStorage());
    }

    @Test
    public void send_one_billion_files_to_store_with_actor() throws InterruptedException {
        ActorRef actor = Actors.actorOf(new akka.actor.UntypedActorFactory() {
            public UntypedActor create() {
                return new TestActor(actorRef, NB_MESSAGE);
            }
        }).start();
        actor.sendRequestReply(new StartTest());

    }
}


class TestActor extends UntypedActor {

    private ActorRef testedActor;

    private int nbMessageToSend;

    private int nbFileValidated = 0;

    private long startTime;
    
    private Option<CompletableFuture<Object>> sender;

    public TestActor(ActorRef testedActor, int nbMessageToSend) {
        this.testedActor = testedActor;
        this.nbMessageToSend = nbMessageToSend;
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof StartTest) {
            sender = getContext().getSenderFuture();
            startTime = System.nanoTime();
            for (int i = 0; i < nbMessageToSend; i++) {
                testedActor.sendOneWay(new StoreFile(new FileFormat(new File("/mock/file" + i), "test_" + i, 128)), getContext());
            }
        } else if (message instanceof FileStored) {
            testedActor.sendOneWay(new CheckAvailability(((FileStored) message).fileFormat), getContext());
        } else if (message instanceof FileAvailability) {
            assertTrue(((FileAvailability) message).available);
            nbFileValidated++;
            if (nbFileValidated == nbMessageToSend) {
                System.out.println("Take : " + (System.nanoTime() - startTime) / 1000000 + " ms");
                sender.get().completeWithResult(new Object());
            }
        }
    }
}

class StartTest {

}