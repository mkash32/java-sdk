package io.dapr.actors.client;

import io.dapr.actors.ActorId;
import io.dapr.actors.utils.ObjectSerializer;
import org.junit.Assert;
import org.junit.Test;

public class ActorProxyImplTest {

    @Test()
    public void constructorActorProxyTest() {
        final ActorProxyHttpAsyncClient actorProxyAsyncClient = (ActorProxyHttpAsyncClient)new ActorProxyClientBuilder().buildAsyncClient();
        final ActorProxyImpl actorProxy= new ActorProxyImpl(
          "myActorType",
          new ActorId("100"),
          new ObjectSerializer(),
          actorProxyAsyncClient);
        Assert.assertEquals(actorProxy.getActorId().toString(),"100");
        Assert.assertEquals(actorProxy.getActorType(),"myActorType");
    }
}