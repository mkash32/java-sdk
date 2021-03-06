/*
 * Copyright (c) Microsoft Corporation.
 * Licensed under the MIT License.
 */
package io.dapr.client;

import io.dapr.client.domain.State;
import io.dapr.client.domain.StateOptions;
import io.dapr.client.domain.Verb;
import okhttp3.OkHttpClient;
import okhttp3.mock.Behavior;
import okhttp3.mock.MockInterceptor;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Mono;

import java.util.*;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

public class DaprClientHttpTest {

  private static final String STATE_STORE_NAME = "MyStateStore";

  private static final String SECRET_STORE_NAME = "MySecretStore";

  private DaprClientHttp daprClientHttp;

  private DaprHttp daprHttp;

  private OkHttpClient okHttpClient;

  private MockInterceptor mockInterceptor;

  private final String EXPECTED_RESULT =
      "{\"data\":\"ewoJCSJwcm9wZXJ0eUEiOiAidmFsdWVBIiwKCQkicHJvcGVydHlCIjogInZhbHVlQiIKCX0=\"}";

  @Before
  public void setUp() throws Exception {
    mockInterceptor = new MockInterceptor(Behavior.UNORDERED);
    okHttpClient = new OkHttpClient.Builder().addInterceptor(mockInterceptor).build();
  }

  @Test
  public void publishEventInvokation() {
    mockInterceptor.addRule()
      .post("http://127.0.0.1:3000/v1.0/publish/A")
      .respond(EXPECTED_RESULT);
    String event = "{ \"message\": \"This is a test\" }";
    daprHttp = new DaprHttp(3000, okHttpClient);
    DaprClientHttp daprClientHttp = new DaprClientHttp(daprHttp);
    Mono<Void> mono = daprClientHttp.publishEvent("A", event, null);
    assertNull(mono.block());
  }

  @Test
  public void publishEvent() {
    mockInterceptor.addRule()
      .post("http://127.0.0.1:3000/v1.0/publish/A")
      .respond(EXPECTED_RESULT);
    String event = "{ \"message\": \"This is a test\" }";
    daprHttp = new DaprHttp(3000, okHttpClient);
    daprClientHttp = new DaprClientHttp(daprHttp);
    Mono<Void> mono = daprClientHttp.publishEvent("A", event);
    assertNull(mono.block());
  }

  @Test(expected = IllegalArgumentException.class)
  public void publishEventIfTopicIsNull() {
    mockInterceptor.addRule()
      .post("http://127.0.0.1:3000/v1.0/publish/A")
      .respond(EXPECTED_RESULT);
    String event = "{ \"message\": \"This is a test\" }";
    daprHttp = new DaprHttp(3000, okHttpClient);
    daprClientHttp = new DaprClientHttp(daprHttp);
    Mono<Void> mono = daprClientHttp.publishEvent("", event);
    assertNull(mono.block());
  }

  @Test
  public void publishEventNoHotMono() {
    mockInterceptor.addRule()
        .post("http://127.0.0.1:3000/v1.0/publish/A")
        .respond(EXPECTED_RESULT);
    String event = "{ \"message\": \"This is a test\" }";
    daprHttp = new DaprHttp(3000, okHttpClient);
    daprClientHttp = new DaprClientHttp(daprHttp);
    daprClientHttp.publishEvent("", event);
    // Should not throw exception because did not call block() on mono above.
  }

  @Test(expected = IllegalArgumentException.class)
  public void invokeServiceVerbNull() {
    mockInterceptor.addRule()
      .post("http://127.0.0.1:3000/v1.0/publish/A")
      .respond(EXPECTED_RESULT);
    String event = "{ \"message\": \"This is a test\" }";
    daprHttp = new DaprHttp(3000, okHttpClient);
    daprClientHttp = new DaprClientHttp(daprHttp);
    Mono<Void> mono = daprClientHttp.invokeService(null, "", "", null, null, null);
    assertNull(mono.block());
  }

  @Test
  public void invokeServiceIllegalArgumentException() {
    mockInterceptor.addRule()
      .post("http://127.0.0.1:3000/v1.0/publish/A")
      .respond(EXPECTED_RESULT);
    String event = "{ \"message\": \"This is a test\" }";
    daprHttp = new DaprHttp(3000, okHttpClient);
    daprClientHttp = new DaprClientHttp(daprHttp);
    assertThrows(IllegalArgumentException.class, () -> {
      daprClientHttp.invokeService(null, "", "", null, null, null).block();
    });
    assertThrows(IllegalArgumentException.class, () -> {
      daprClientHttp.invokeService(Verb.POST, null, "", null, null, null).block();
    });
    assertThrows(IllegalArgumentException.class, () -> {
      daprClientHttp.invokeService(Verb.POST, "", "", null, null, null).block();
    });
    assertThrows(IllegalArgumentException.class, () -> {
      daprClientHttp.invokeService(Verb.POST, "1", null, null, null, null).block();
    });
    assertThrows(IllegalArgumentException.class, () -> {
      daprClientHttp.invokeService(Verb.POST, "1", "", null, null, null).block();
    });
  }


  @Test(expected = IllegalArgumentException.class)
  public void invokeServiceMethodNull() {
    mockInterceptor.addRule()
      .post("http://127.0.0.1:3000/v1.0/publish/A")
      .respond(EXPECTED_RESULT);
    String event = "{ \"message\": \"This is a test\" }";
    daprHttp = new DaprHttp(3000, okHttpClient);
    daprClientHttp = new DaprClientHttp(daprHttp);
    Mono<Void> mono = daprClientHttp.invokeService(Verb.POST, "1", "", null, null, null);
    assertNull(mono.block());
  }

  @Test
  public void invokeService() {
    mockInterceptor.addRule()
      .get("http://127.0.0.1:3000/v1.0/invoke/41/method/neworder")
      .respond("\"hello world\"");
    daprHttp = new DaprHttp(3000, okHttpClient);
    daprClientHttp = new DaprClientHttp(daprHttp);
    Mono<String> mono = daprClientHttp.invokeService(Verb.GET, "41", "neworder", null, null, String.class);
    assertEquals("hello world", mono.block());
  }

  @Test
  public void simpleInvokeService() {
    Map<String, String> map = new HashMap<>();
    mockInterceptor.addRule()
      .get("http://127.0.0.1:3000/v1.0/invoke/41/method/neworder")
      .respond(EXPECTED_RESULT);
    daprHttp = new DaprHttp(3000, okHttpClient);
    daprClientHttp = new DaprClientHttp(daprHttp);
    Mono<byte[]> mono = daprClientHttp.invokeService(Verb.GET, "41", "neworder", null, byte[].class);
    assertEquals(new String(mono.block()), EXPECTED_RESULT);
  }

  @Test
  public void invokeServiceWithMaps() {
    Map<String, String> map = new HashMap<>();
    mockInterceptor.addRule()
      .get("http://127.0.0.1:3000/v1.0/invoke/41/method/neworder")
      .respond(EXPECTED_RESULT);
    daprHttp = new DaprHttp(3000, okHttpClient);
    daprClientHttp = new DaprClientHttp(daprHttp);
    Mono<byte[]> mono = daprClientHttp.invokeService(Verb.GET, "41", "neworder", (byte[]) null, map);
    String monoString = new String(mono.block());
    assertEquals(monoString, EXPECTED_RESULT);
  }

  @Test
  public void invokeServiceWithOutRequest() {
    Map<String, String> map = new HashMap<>();
    mockInterceptor.addRule()
      .get("http://127.0.0.1:3000/v1.0/invoke/41/method/neworder")
      .respond(EXPECTED_RESULT);
    daprHttp = new DaprHttp(3000, okHttpClient);
    daprClientHttp = new DaprClientHttp(daprHttp);
    Mono<Void> mono = daprClientHttp.invokeService(Verb.GET, "41", "neworder", map);
    assertNull(mono.block());
  }

  @Test
  public void invokeServiceWithRequest() {
    Map<String, String> map = new HashMap<>();
    mockInterceptor.addRule()
      .get("http://127.0.0.1:3000/v1.0/invoke/41/method/neworder")
      .respond(EXPECTED_RESULT);
    daprHttp = new DaprHttp(3000, okHttpClient);
    daprClientHttp = new DaprClientHttp(daprHttp);
    Mono<Void> mono = daprClientHttp.invokeService(Verb.GET, "41", "neworder", "", map);
    assertNull(mono.block());
  }

  @Test
  public void invokeServiceNoHotMono() {
    Map<String, String> map = new HashMap<>();
    mockInterceptor.addRule()
        .get("http://127.0.0.1:3000/v1.0/invoke/41/method/neworder")
        .respond(500);
    daprHttp = new DaprHttp(3000, okHttpClient);
    daprClientHttp = new DaprClientHttp(daprHttp);
    daprClientHttp.invokeService(Verb.GET, "41", "neworder", "", map);
    // No exception should be thrown because did not call block() on mono above.
  }

  @Test
  public void invokeBinding() {
    Map<String, String> map = new HashMap<>();
    mockInterceptor.addRule()
      .post("http://127.0.0.1:3000/v1.0/bindings/sample-topic")
      .respond(EXPECTED_RESULT);
    daprHttp = new DaprHttp(3000, okHttpClient);
    daprClientHttp = new DaprClientHttp(daprHttp);
    Mono<Void> mono = daprClientHttp.invokeBinding("sample-topic", "");
    assertNull(mono.block());
  }

  @Test(expected = IllegalArgumentException.class)
  public void invokeBindingNullName() {
    Map<String, String> map = new HashMap<>();
    mockInterceptor.addRule()
      .post("http://127.0.0.1:3000/v1.0/bindings/sample-topic")
      .respond(EXPECTED_RESULT);
    daprHttp = new DaprHttp(3000, okHttpClient);
    daprClientHttp = new DaprClientHttp(daprHttp);
    Mono<Void> mono = daprClientHttp.invokeBinding(null, "");
    assertNull(mono.block());
  }

  @Test
  public void bindingNoHotMono() {
    Map<String, String> map = new HashMap<>();
    mockInterceptor.addRule()
        .post("http://127.0.0.1:3000/v1.0/bindings/sample-topic")
        .respond(EXPECTED_RESULT);
    daprHttp = new DaprHttp(3000, okHttpClient);
    daprClientHttp = new DaprClientHttp(daprHttp);
    daprClientHttp.invokeBinding(null, "");
    // No exception is thrown because did not call block() on mono above.
  }

  @Test
  public void getStates() {
    StateOptions stateOptions = mock(StateOptions.class);
    State<String> stateKeyValue = new State("value", "key", "etag", stateOptions);
    State<String> stateKeyNull = new State("value", null, "etag", stateOptions);
    mockInterceptor.addRule()
      .get("http://127.0.0.1:3000/v1.0/state/MyStateStore/key")
      .respond("\"" + EXPECTED_RESULT + "\"");
    daprHttp = new DaprHttp(3000, okHttpClient);
    daprClientHttp = new DaprClientHttp(daprHttp);
    assertThrows(IllegalArgumentException.class, () -> {
      daprClientHttp.getState(STATE_STORE_NAME, stateKeyNull, String.class).block();
    });
    Mono<State<String>> mono = daprClientHttp.getState(STATE_STORE_NAME, stateKeyValue, String.class);
    assertEquals(mono.block().getKey(), "key");
  }

  @Test
  public void getStatesEmptyEtag() {
    State<String> stateEmptyEtag = new State("value", "key", "", null);
    mockInterceptor.addRule()
      .get("http://127.0.0.1:3000/v1.0/state/MyStateStore/key")
      .respond("\"" + EXPECTED_RESULT + "\"");
    daprHttp = new DaprHttp(3000, okHttpClient);
    daprClientHttp = new DaprClientHttp(daprHttp);
    Mono<State<String>> monoEmptyEtag = daprClientHttp.getState(STATE_STORE_NAME, stateEmptyEtag, String.class);
    assertEquals(monoEmptyEtag.block().getKey(), "key");
  }

  @Test
  public void getStatesNullEtag() {
    State<String> stateNullEtag = new State("value", "key", null, null);
    mockInterceptor.addRule()
      .get("http://127.0.0.1:3000/v1.0/state/MyStateStore/key")
      .respond("\"" + EXPECTED_RESULT + "\"");
    daprHttp = new DaprHttp(3000, okHttpClient);
    daprClientHttp = new DaprClientHttp(daprHttp);
    Mono<State<String>> monoNullEtag = daprClientHttp.getState(STATE_STORE_NAME, stateNullEtag, String.class);
    assertEquals(monoNullEtag.block().getKey(), "key");
  }

  @Test
  public void getStatesNoHotMono() {
    State<String> stateNullEtag = new State("value", "key", null, null);
    mockInterceptor.addRule()
        .get("http://127.0.0.1:3000/v1.0/state/MyStateStore/key")
        .respond(500);
    daprHttp = new DaprHttp(3000, okHttpClient);
    daprClientHttp = new DaprClientHttp(daprHttp);
    daprClientHttp.getState(STATE_STORE_NAME, stateNullEtag, String.class);
    // No exception should be thrown since did not call block() on mono above.
  }

  @Test
  public void saveStates() {
    State<String> stateKeyValue = new State("value", "key", "etag", null);
    List<State<?>> stateKeyValueList = Arrays.asList(stateKeyValue);
    mockInterceptor.addRule()
      .post("http://127.0.0.1:3000/v1.0/state/MyStateStore")
      .respond(EXPECTED_RESULT);
    daprHttp = new DaprHttp(3000, okHttpClient);
    daprClientHttp = new DaprClientHttp(daprHttp);
    Mono<Void> mono = daprClientHttp.saveStates(STATE_STORE_NAME, stateKeyValueList);
    assertNull(mono.block());
  }

  @Test
  public void saveStatesNull() {
    State<String> stateKeyValue = new State("value", "key", "", null);
    List<State<?>> stateKeyValueList = new ArrayList();
    mockInterceptor.addRule()
      .post("http://127.0.0.1:3000/v1.0/state/MyStateStore")
      .respond(EXPECTED_RESULT);
    daprHttp = new DaprHttp(3000, okHttpClient);
    daprClientHttp = new DaprClientHttp(daprHttp);
    Mono<Void> mono = daprClientHttp.saveStates(STATE_STORE_NAME, null);
    assertNull(mono.block());
    Mono<Void> mono1 = daprClientHttp.saveStates(STATE_STORE_NAME, stateKeyValueList);
    assertNull(mono1.block());
  }

  @Test
  public void saveStatesEtagNull() {
    State<String> stateKeyValue = new State("value", "key", null, null);
    List<State<?>> stateKeyValueList = Arrays.asList(stateKeyValue);
    mockInterceptor.addRule()
      .post("http://127.0.0.1:3000/v1.0/state/MyStateStore")
      .respond(EXPECTED_RESULT);
    daprHttp = new DaprHttp(3000, okHttpClient);
    daprClientHttp = new DaprClientHttp(daprHttp);
    Mono<Void> mono = daprClientHttp.saveStates(STATE_STORE_NAME, stateKeyValueList);
    assertNull(mono.block());
  }

  @Test
  public void saveStatesEtagEmpty() {
    State<String> stateKeyValue = new State("value", "key", "", null);
    List<State<?>> stateKeyValueList = Arrays.asList(stateKeyValue);
    mockInterceptor.addRule()
      .post("http://127.0.0.1:3000/v1.0/state/MyStateStore")
      .respond(EXPECTED_RESULT);
    daprHttp = new DaprHttp(3000, okHttpClient);
    daprClientHttp = new DaprClientHttp(daprHttp);
    Mono<Void> mono = daprClientHttp.saveStates(STATE_STORE_NAME, stateKeyValueList);
    assertNull(mono.block());
  }

  @Test
  public void simpleSaveStates() {
    mockInterceptor.addRule()
      .post("http://127.0.0.1:3000/v1.0/state/MyStateStore")
      .respond(EXPECTED_RESULT);
    StateOptions stateOptions = mock(StateOptions.class);
    daprHttp = new DaprHttp(3000, okHttpClient);
    daprClientHttp = new DaprClientHttp(daprHttp);
    Mono<Void> mono = daprClientHttp.saveState(STATE_STORE_NAME, "key", "etag", "value", stateOptions);
    assertNull(mono.block());
  }

  @Test
  public void saveStatesNoHotMono() {
    mockInterceptor.addRule()
        .post("http://127.0.0.1:3000/v1.0/state/MyStateStore")
        .respond(500);
    StateOptions stateOptions = mock(StateOptions.class);
    daprHttp = new DaprHttp(3000, okHttpClient);
    daprClientHttp = new DaprClientHttp(daprHttp);
    daprClientHttp.saveState(STATE_STORE_NAME, "key", "etag", "value", stateOptions);
    // No exception should be thrown because we did not call block() on the mono above.
  }

  @Test
  public void deleteState() {
    StateOptions stateOptions = mock(StateOptions.class);
    State<String> stateKeyValue = new State("value", "key", "etag", stateOptions);
    mockInterceptor.addRule()
      .delete("http://127.0.0.1:3000/v1.0/state/MyStateStore/key")
      .respond(EXPECTED_RESULT);
    daprHttp = new DaprHttp(3000, okHttpClient);
    daprClientHttp = new DaprClientHttp(daprHttp);
    Mono<Void> mono = daprClientHttp.deleteState(STATE_STORE_NAME, stateKeyValue.getKey(), stateKeyValue.getEtag(), stateOptions);
    assertNull(mono.block());
  }

  @Test
  public void deleteStateNoHotMono() {
    StateOptions stateOptions = mock(StateOptions.class);
    State<String> stateKeyValue = new State("value", "key", "etag", stateOptions);
    mockInterceptor.addRule()
        .delete("http://127.0.0.1:3000/v1.0/state/MyStateStore/key")
        .respond(500);
    daprHttp = new DaprHttp(3000, okHttpClient);
    daprClientHttp = new DaprClientHttp(daprHttp);
    daprClientHttp.deleteState(STATE_STORE_NAME, stateKeyValue.getKey(), stateKeyValue.getEtag(), stateOptions);
    // No exception should be thrown because we did not call block() on the mono above.
  }

  @Test
  public void deleteStateNullEtag() {
    State<String> stateKeyValue = new State("value", "key", null, null);
    mockInterceptor.addRule()
      .delete("http://127.0.0.1:3000/v1.0/state/MyStateStore/key")
      .respond(EXPECTED_RESULT);
    daprHttp = new DaprHttp(3000, okHttpClient);
    daprClientHttp = new DaprClientHttp(daprHttp);
    Mono<Void> mono = daprClientHttp.deleteState(STATE_STORE_NAME, stateKeyValue.getKey(), stateKeyValue.getEtag(), null);
    assertNull(mono.block());
  }

  @Test
  public void deleteStateEmptyEtag() {
    State<String> stateKeyValue = new State("value", "key", "", null);
    mockInterceptor.addRule()
      .delete("http://127.0.0.1:3000/v1.0/state/MyStateStore/key")
      .respond(EXPECTED_RESULT);
    daprHttp = new DaprHttp(3000, okHttpClient);
    daprClientHttp = new DaprClientHttp(daprHttp);
    Mono<Void> mono = daprClientHttp.deleteState(STATE_STORE_NAME, stateKeyValue.getKey(), stateKeyValue.getEtag(), null);
    assertNull(mono.block());
  }

  @Test
  public void deleteStateIllegalArgumentException() {
    State<String> stateKeyValueNull = new State("value", null, "etag", null);
    State<String> stateKeyValueEmpty = new State("value", "", "etag", null);
    mockInterceptor.addRule()
      .delete("http://127.0.0.1:3000/v1.0/state/MyStateStore/key")
      .respond(EXPECTED_RESULT);
    daprHttp = new DaprHttp(3000, okHttpClient);
    daprClientHttp = new DaprClientHttp(daprHttp);
    assertThrows(IllegalArgumentException.class, () -> {
      daprClientHttp.deleteState(STATE_STORE_NAME, null, null, null).block();
    });
    assertThrows(IllegalArgumentException.class, () -> {
      daprClientHttp.deleteState(STATE_STORE_NAME, "", null, null).block();
    });
    assertThrows(IllegalArgumentException.class, () -> {
      daprClientHttp.deleteState(STATE_STORE_NAME, " ", null, null).block();
    });
    assertThrows(IllegalArgumentException.class, () -> {
      daprClientHttp.deleteState(null, "key", null, null).block();
    });
    assertThrows(IllegalArgumentException.class, () -> {
      daprClientHttp.deleteState("", "key", null, null).block();
    });
    assertThrows(IllegalArgumentException.class, () -> {
      daprClientHttp.deleteState(" ", "key", null, null).block();
    });
  }

  @Test
  public void getSecrets() {
    mockInterceptor.addRule()
      .get("http://127.0.0.1:3000/v1.0/secrets/MySecretStore/key")
      .respond("{ \"mysecretkey\": \"mysecretvalue\"}");
    daprHttp = new DaprHttp(3000, okHttpClient);
    daprClientHttp = new DaprClientHttp(daprHttp);
    assertThrows(IllegalArgumentException.class, () -> {
      daprClientHttp.getSecret(SECRET_STORE_NAME, null).block();
    });
    Map<String, String> secret = daprClientHttp.getSecret(SECRET_STORE_NAME, "key").block();

    assertEquals(1, secret.size());
    assertEquals("mysecretvalue", secret.get("mysecretkey"));
  }

  @Test
  public void getSecretsEmpty() {
    mockInterceptor.addRule()
      .get("http://127.0.0.1:3000/v1.0/secrets/MySecretStore/key")
      .respond("");
    daprHttp = new DaprHttp(3000, okHttpClient);
    daprClientHttp = new DaprClientHttp(daprHttp);
    assertThrows(IllegalArgumentException.class, () -> {
      daprClientHttp.getSecret(SECRET_STORE_NAME, null).block();
    });
    Map<String, String> secret = daprClientHttp.getSecret(SECRET_STORE_NAME, "key").block();

    assertTrue(secret.isEmpty());
  }

  @Test
  public void getSecrets404() {
    mockInterceptor.addRule()
      .get("http://127.0.0.1:3000/v1.0/secrets/MySecretStore/key")
      .respond(404);
    daprHttp = new DaprHttp(3000, okHttpClient);
    daprClientHttp = new DaprClientHttp(daprHttp);
    assertThrows(IllegalStateException.class, () -> {
      daprClientHttp.getSecret(SECRET_STORE_NAME, "key").block();
    });
  }

  @Test
  public void getSecretsWithMetadata() {
    mockInterceptor.addRule()
      .get("http://127.0.0.1:3000/v1.0/secrets/MySecretStore/key")
      .respond("{ \"mysecretkey\": \"mysecretvalue\"}");
    mockInterceptor.addRule()
      .get("http://127.0.0.1:3000/v1.0/secrets/MySecretStore/key?metakey=metavalue")
      .respond("{ \"mysecretkey2\": \"mysecretvalue2\"}");
    daprHttp = new DaprHttp(3000, okHttpClient);
    daprClientHttp = new DaprClientHttp(daprHttp);
    assertThrows(IllegalArgumentException.class, () -> {
      daprClientHttp.getSecret(SECRET_STORE_NAME, null).block();
    });

    {
      Map<String, String> secret = daprClientHttp.getSecret(
        SECRET_STORE_NAME,
        "key",
        null).block();

      assertEquals(1, secret.size());
      assertEquals("mysecretvalue", secret.get("mysecretkey"));
    }

    {
      Map<String, String> secret = daprClientHttp.getSecret(
        SECRET_STORE_NAME,
        "key",
        Collections.singletonMap("metakey", "metavalue")).block();

      assertEquals(1, secret.size());
      assertEquals("mysecretvalue2", secret.get("mysecretkey2"));
    }
  }
}