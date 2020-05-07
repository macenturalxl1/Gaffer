/*
 * Copyright 2016-2020 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.gchq.gaffer.jsonSerialisation;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import uk.gov.gchq.gaffer.commonutil.CommonConstants;
import uk.gov.gchq.gaffer.commonutil.pair.Pair;
import uk.gov.gchq.gaffer.exception.SerialisationException;
import uk.gov.gchq.gaffer.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.gaffer.jsonserialisation.JSONSerialiserModules;
import uk.gov.gchq.gaffer.serialisation.ParameterisedTestObject;
import uk.gov.gchq.gaffer.serialisation.SimpleTestObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.gchq.gaffer.commonutil.JsonAssert.assertJsonEquals;

public class JSONSerialiserTest {

    private final Pair<Object, byte[]>[] historicSerialisationPairs;

    @SuppressWarnings("unchecked")
    public JSONSerialiserTest() {
        ParameterisedTestObject<Object> paramTest = new ParameterisedTestObject<>();
        paramTest.setX("Test");
        paramTest.setK(2);
        SimpleTestObject simpleTestObject = new SimpleTestObject();
        simpleTestObject.setX("Test");

        this.historicSerialisationPairs = new Pair[] {
                new Pair(simpleTestObject, new byte[] {123, 34, 120, 34, 58, 34, 84, 101, 115, 116, 34, 125}),
                new Pair(paramTest, new byte[] {123, 34, 120, 34, 58, 34, 84, 101, 115, 116, 34, 44, 34, 107, 34, 58, 50, 125})
        };
    }

    @BeforeEach
    @AfterEach
    public void cleanUp() {
        System.clearProperty(JSONSerialiser.JSON_SERIALISER_CLASS_KEY);
        System.clearProperty(JSONSerialiser.JSON_SERIALISER_MODULES);
        JSONSerialiser.update();
    }

    @Test
    public void testPrimitiveSerialisation() throws IOException {
        final byte[] b = JSONSerialiser.serialise(2);

        final Object o = JSONSerialiser.deserialise(b, Object.class);

        assertEquals(Integer.class, o.getClass());
        assertEquals(2, o);
    }

    @Test
    public void canHandleUnParameterisedDAO() {
        assertTrue(JSONSerialiser.canHandle(SimpleTestObject.class));
    }

    @Test
    public void testDAOSerialisation() throws SerialisationException {
        final SimpleTestObject test = new SimpleTestObject();
        test.setX("Test");
        final byte[] b = JSONSerialiser.serialise(test);

        final Object o = JSONSerialiser.deserialise(b, SimpleTestObject.class);

        assertEquals(SimpleTestObject.class, o.getClass());
        assertEquals("Test", ((SimpleTestObject) o).getX());
    }

    @Test
    public void shouldNotPrettyPrintByDefaultWhenSerialising() throws SerialisationException {
        final SimpleTestObject test = new SimpleTestObject();
        test.setX("TestValue1");

        final byte[] bytes = JSONSerialiser.serialise(test);

        assertEquals("{\"x\":\"TestValue1\"}", new String(bytes));
    }

    @Test
    public void shouldPrettyPrintWhenSerialisingAndSetToPrettyPrint() throws SerialisationException {
        final SimpleTestObject test = new SimpleTestObject();
        test.setX("TestValue1");

        final byte[] bytes = JSONSerialiser.serialise(test, true);

        assertJsonEquals(String.format("{%n  \"x\" : \"TestValue1\"%n}"), new String(bytes));
    }

    @Test
    public void canHandleParameterisedDAO() {
        assertTrue(JSONSerialiser.canHandle(ParameterisedTestObject.class));
    }

    @Test
    public void testParameterisedDAOSerialisation() throws SerialisationException {
        final ParameterisedTestObject<Integer> test = new ParameterisedTestObject<>();
        test.setX("Test");
        test.setK(2);
        final byte[] b = JSONSerialiser.serialise(test);

        final Object o = JSONSerialiser.deserialise(b, ParameterisedTestObject.class);

        assertEquals(ParameterisedTestObject.class, o.getClass());
        assertEquals("Test", ((ParameterisedTestObject) o).getX());
        assertEquals(Integer.class, ((ParameterisedTestObject) o).getK().getClass());
        assertEquals(2, ((ParameterisedTestObject) o).getK());
    }

    @Test
    public void testParameterisedDAOTypeRefDeserialisation() throws SerialisationException {
        final ParameterisedTestObject<Integer> test = new ParameterisedTestObject<>();
        test.setX("Test");
        test.setK(2);
        final byte[] b = JSONSerialiser.serialise(test);

        ParameterisedTestObject<Integer> o = JSONSerialiser.deserialise(b, new TypeReference<ParameterisedTestObject<Integer>>() {
        });

        assertEquals("Test", o.getX());
        assertEquals(Integer.valueOf(2), o.getK());
    }

    @Test
    public void testParameterisedDeserialisationOfComplexObject() throws SerialisationException {
        final SimpleTestObject test = new SimpleTestObject();
        test.setX("Test");
        final byte[] b = JSONSerialiser.serialise(test);

        final SimpleTestObject o = JSONSerialiser.deserialise(b, SimpleTestObject.class);

        assertEquals(SimpleTestObject.class, o.getClass());
        assertEquals("Test", o.getX());
    }

    @Test
    public void testParameterisedDeserialisationOfParameterisedComplexObject() throws SerialisationException {
        final ParameterisedTestObject<Integer> test = new ParameterisedTestObject<>();
        test.setX("Test");
        test.setK(2);
        byte[] b = JSONSerialiser.serialise(test);

        final ParameterisedTestObject o = JSONSerialiser.deserialise(b, ParameterisedTestObject.class);

        assertEquals(ParameterisedTestObject.class, o.getClass());
        assertEquals("Test", o.getX());
        assertEquals(Integer.class, o.getK().getClass());
        assertEquals(2, o.getK());
    }


    @Test
    public void testParameterisedDeserialisationOfComplexObjectToIncorrectType() throws SerialisationException {
        final SimpleTestObject test = new SimpleTestObject();
        test.setX("Test");

        final byte[] b = JSONSerialiser.serialise(test);

        assertThrows(SerialisationException.class, () -> JSONSerialiser.deserialise(b, Integer.class));
    }

    @Test
    public void shouldSerialiseObjectWithoutFieldX() throws Exception {
        final SimpleTestObject obj = new SimpleTestObject();

        final String json = new String(JSONSerialiser.serialise(obj, "x"), CommonConstants.UTF_8);

        assertFalse(json.contains("x"));
    }

    @Test
    public void shouldSerialiseObjectWithFieldX() throws Exception {
        final SimpleTestObject obj = new SimpleTestObject();

        final String json = new String(JSONSerialiser.serialise(obj), CommonConstants.UTF_8);

        assertTrue(json.contains("x"));
    }

    @Test
    public void shouldSerialiseWithHistoricValues() throws Exception {
        assertNotNull(historicSerialisationPairs);

        for (final Pair<Object, byte[]> pair : historicSerialisationPairs) {
            serialiseFirst(pair);
            deserialiseSecond(pair);
        }
    }

    @Test
    public void shouldThrowExceptionWhenUpdateInstanceWithInvalidClassName() {
        System.setProperty(JSONSerialiser.JSON_SERIALISER_CLASS_KEY, "invalidClassName");

        final Exception exception = assertThrows(IllegalArgumentException.class, () -> JSONSerialiser.update());
        final String expected = "Property gaffer.serialiser.json.class must be set to a class that is a sub class of " +
                "uk.gov.gchq.gaffer.jsonserialisation.JSONSerialiser. This class is not valid: invalidClassName";
        assertEquals(expected, exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenUpdateInstanceWithInvalidModuleClass() {
        // Given
        System.setProperty(JSONSerialiser.JSON_SERIALISER_MODULES, "module1");

        // When / Then
        final Exception exception = assertThrows(IllegalArgumentException.class, () -> JSONSerialiser.update());
        final String expected = "Property gaffer.serialiser.json.modules must be set to a csv of classes that are a sub " +
                "class of uk.gov.gchq.gaffer.jsonserialisation.JSONSerialiserModules. These classes are not valid: module1";
        assertEquals(expected, exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenUpdateInstanceWithInvalidModulesValue() {
        // Given
        final String invalidValue = TestCustomJsonModules1.class.getName() + "-" + TestCustomJsonModules2.class.getName();
        System.setProperty(JSONSerialiser.JSON_SERIALISER_MODULES, invalidValue);

        final Exception exception = assertThrows(IllegalArgumentException.class, () -> JSONSerialiser.update());
        final String expected = "Property gaffer.serialiser.json.modules must be set to a csv of classes that are a sub " +
                "class of uk.gov.gchq.gaffer.jsonserialisation.JSONSerialiserModules. These classes are not valid: " +
                "uk.gov.gchq.gaffer.jsonSerialisation.JSONSerialiserTest$TestCustomJsonModules1-uk.gov.gchq.gaffer.jsonSerialisation.JSONSerialiserTest$TestCustomJsonModules2";
        assertEquals(expected, exception.getMessage());
    }

    @Test
    public void shouldUpdateInstanceWithCustomSerialiser() {
        // Given
        TestCustomJsonSerialiser1.mapper = mock(ObjectMapper.class);
        System.setProperty(JSONSerialiser.JSON_SERIALISER_CLASS_KEY, TestCustomJsonSerialiser1.class.getName());

        // When
        JSONSerialiser.update();

        // Then
        assertEquals(TestCustomJsonSerialiser1.class, JSONSerialiser.getInstance().getClass());
        assertSame(TestCustomJsonSerialiser1.mapper, JSONSerialiser.getMapper());
    }

    @Test
    public void shouldUpdateInstanceWithCustomModule() throws Exception {
        // Given
        final JsonSerializer<String> serialiser = mock(JsonSerializer.class);
        TestCustomJsonModules1.modules = Collections.singletonList(
                new SimpleModule("module1", new Version(1, 0, 0, null, null, null))
                        .addSerializer(String.class, serialiser)
        );
        System.setProperty(JSONSerialiser.JSON_SERIALISER_MODULES, TestCustomJsonModules1.class.getName());

        // When
        JSONSerialiser.update();

        // Then
        assertEquals(JSONSerialiser.class, JSONSerialiser.getInstance().getClass());
        JSONSerialiser.serialise("test");
        verify(serialiser).serialize(Mockito.eq("test"), Mockito.any(), Mockito.any());
    }

    @Test
    public void shouldUpdateInstanceWithCustomProperties() {
        // Given
        TestCustomJsonSerialiser1.mapper = mock(ObjectMapper.class);
        System.setProperty(JSONSerialiser.JSON_SERIALISER_CLASS_KEY, TestCustomJsonSerialiser1.class.getName());
        TestCustomJsonModules1.modules = Arrays.asList(
                mock(Module.class),
                mock(Module.class)
        );
        TestCustomJsonModules2.modules = Arrays.asList(
                mock(Module.class),
                mock(Module.class)
        );
        System.setProperty(JSONSerialiser.JSON_SERIALISER_MODULES, TestCustomJsonModules1.class.getName() + "," + TestCustomJsonModules2.class.getName());
        System.setProperty(JSONSerialiser.STRICT_JSON, "false");

        // When
        JSONSerialiser.update();

        // Then
        assertEquals(TestCustomJsonSerialiser1.class, JSONSerialiser.getInstance().getClass());
        assertSame(TestCustomJsonSerialiser1.mapper, JSONSerialiser.getMapper());
        verify(TestCustomJsonSerialiser1.mapper).registerModules(TestCustomJsonModules1.modules);
        verify(TestCustomJsonSerialiser1.mapper).registerModules(TestCustomJsonModules2.modules);
        verify(TestCustomJsonSerialiser1.mapper).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    public void shouldUpdateInstanceTwiceWithCustomProperties() {
        // Given
        TestCustomJsonSerialiser1.mapper = mock(ObjectMapper.class);
        TestCustomJsonSerialiser2.mapper = mock(ObjectMapper.class);
        TestCustomJsonModules1.modules = Arrays.asList(
                mock(Module.class),
                mock(Module.class)
        );
        TestCustomJsonModules2.modules = Arrays.asList(
                mock(Module.class),
                mock(Module.class)
        );

        // When - initial update
        JSONSerialiser.update(TestCustomJsonSerialiser1.class.getName(), TestCustomJsonModules1.class.getName(), false);

        // Then
        assertEquals(TestCustomJsonSerialiser1.class, JSONSerialiser.getInstance().getClass());
        assertSame(TestCustomJsonSerialiser1.mapper, JSONSerialiser.getMapper());
        verify(TestCustomJsonSerialiser1.mapper).registerModules(TestCustomJsonModules1.modules);
        verify(TestCustomJsonSerialiser1.mapper, never()).registerModules(TestCustomJsonModules2.modules);
        verify(TestCustomJsonSerialiser1.mapper).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // When - second update
        JSONSerialiser.update(TestCustomJsonSerialiser2.class.getName(), TestCustomJsonModules2.class.getName(), true);

        // Then
        assertEquals(TestCustomJsonSerialiser2.class, JSONSerialiser.getInstance().getClass());
        assertSame(TestCustomJsonSerialiser2.mapper, JSONSerialiser.getMapper());
        verify(TestCustomJsonSerialiser2.mapper).registerModules(TestCustomJsonModules1.modules);
        verify(TestCustomJsonSerialiser2.mapper).registerModules(TestCustomJsonModules2.modules);
        verify(TestCustomJsonSerialiser2.mapper).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
    }

    @Test
    public void shouldDeserialiseClassWithUnknownFields() throws Exception {
        // Given
        JSONSerialiser.update(null, null, false);

        // When
        final TestPojo pojo = JSONSerialiser.deserialise("{\"field\": \"value\", \"unknown\": \"otherValue\"}", TestPojo.class);

        // Then
        assertEquals("value", pojo.field);
    }

    @Test
    public void shouldThrowExceptionWhenDeserialiseClassWithUnknownFieldsWhenStrict() {
        // Given
        JSONSerialiser.update(null, null, true);

        // When / Then
        final Exception exception = assertThrows(SerialisationException.class, () ->
                JSONSerialiser.deserialise("{\"field\": \"value\", \"unknown\": \"otherValue\"}", TestPojo.class));
        final String expected = "Unrecognized field \"unknown\" (class uk.gov.gchq.gaffer.jsonSerialisation.JSONSerialiserTest$TestPojo), " +
                "not marked as ignorable (one known property: \"field\"])\n" +
                " at [Source: {\"field\": \"value\", \"unknown\": \"otherValue\"};";
        assertTrue(exception.getMessage().contains(expected));
    }

    protected void deserialiseSecond(final Pair<Object, byte[]> pair) throws SerialisationException {
        assertEquals(pair.getFirst(), JSONSerialiser.deserialise(pair.getSecond(), pair.getFirst().getClass()));
    }

    protected void serialiseFirst(final Pair<Object, byte[]> pair) throws SerialisationException {
        byte[] serialise = JSONSerialiser.serialise(pair.getFirst());
        assertArrayEquals(pair.getSecond(), serialise);
    }

    public static final class TestCustomJsonSerialiser1 extends JSONSerialiser {
        public static ObjectMapper mapper;

        public TestCustomJsonSerialiser1() {
            super(mapper);
        }
    }

    public static final class TestCustomJsonSerialiser2 extends JSONSerialiser {
        public static ObjectMapper mapper;

        public TestCustomJsonSerialiser2() {
            super(mapper);
        }
    }

    public static final class TestCustomJsonModules1 implements JSONSerialiserModules {
        public static List<Module> modules;

        @Override
        public List<Module> getModules() {
            return modules;
        }
    }

    public static final class TestCustomJsonModules2 implements JSONSerialiserModules {
        public static List<Module> modules;

        @Override
        public List<Module> getModules() {
            return modules;
        }
    }

    private static final class TestPojo {
        public String field;
    }
}
