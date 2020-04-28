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

package uk.gov.gchq.gaffer.commonutil;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public final class JsonAssert {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonAssert() {
        //Private to prevent instantiation
    }

    public static void assertJsonEquals(final String expectedJson, final String actualJson) {
        try {
            final Map expectedSchemaMap = null != expectedJson ? OBJECT_MAPPER.readValue(expectedJson, Map.class) : Collections.emptyMap();
            final Map actualSchemaMap = null != actualJson ? OBJECT_MAPPER.readValue(actualJson, Map.class) : Collections.emptyMap();
            assertEquals(expectedSchemaMap, actualSchemaMap);
            return;
        } catch (final IOException e) {
            // ignore the error and try using lists instead
        }

        try {
            final List expectedSchemaMap = null != expectedJson ? OBJECT_MAPPER.readValue(expectedJson, List.class) : Collections.emptyList();
            final List actualSchemaMap = null != actualJson ? OBJECT_MAPPER.readValue(actualJson, List.class) : Collections.emptyList();
            assertEquals(expectedSchemaMap, actualSchemaMap);
        } catch (final IOException e) {
            throw new AssertionError(expectedJson + " is not equal to " + actualJson, e);
        }
    }

    public static void assertJsonEquals(final byte[] expectedJson, final byte[] actualJson) {
        assertJsonEquals(null != expectedJson ? new String(expectedJson) : null, null != actualJson ? new String(actualJson) : null);
    }

    public static void assertJsonNotEqual(final String firstJson, final String secondJson) {
        try {
            final Map firstSchemaMap = null != firstJson ? OBJECT_MAPPER.readValue(firstJson, Map.class) : Collections.emptyMap();
            final Map secondSchemaMap = null != secondJson ? OBJECT_MAPPER.readValue(secondJson, Map.class) : Collections.emptyMap();
            assertNotEquals(firstSchemaMap, secondSchemaMap);
        } catch (final IOException e) {
            // ignore
        }
    }

    public static void assertJsonNotEqual(final byte[] firstJson, final byte[] secondJson) {
        assertJsonNotEqual(null != firstJson ? new String(firstJson) : null, null != secondJson ? new String(secondJson) : null);
    }
}
