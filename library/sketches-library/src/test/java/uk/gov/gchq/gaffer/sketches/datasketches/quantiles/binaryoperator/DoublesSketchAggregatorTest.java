/*
 * Copyright 2017-2020 Crown Copyright
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
package uk.gov.gchq.gaffer.sketches.datasketches.quantiles.binaryoperator;

import com.yahoo.sketches.quantiles.DoublesSketch;
import com.yahoo.sketches.quantiles.UpdateDoublesSketch;
import org.junit.Before;
import org.junit.Test;

import uk.gov.gchq.gaffer.commonutil.JsonAssert;
import uk.gov.gchq.gaffer.exception.SerialisationException;
import uk.gov.gchq.gaffer.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.koryphe.binaryoperator.BinaryOperatorTest;

import java.util.function.BinaryOperator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DoublesSketchAggregatorTest extends BinaryOperatorTest {
    private static final double DELTA = 0.01D;
    private UpdateDoublesSketch sketch1;
    private UpdateDoublesSketch sketch2;

    @Before
    public void setup() {
        sketch1 = DoublesSketch.builder().build();
        sketch1.update(1.0D);
        sketch1.update(2.0D);
        sketch1.update(3.0D);

        sketch2 = DoublesSketch.builder().build();
        sketch2.update(4.0D);
        sketch2.update(5.0D);
        sketch2.update(6.0D);
        sketch2.update(7.0D);
    }

    @Test
    public void testAggregate() {
        final DoublesSketchAggregator sketchAggregator = new DoublesSketchAggregator();
        DoublesSketch currentState = sketch1;
        assertEquals(3L, currentState.getN());
        assertEquals(2.0D, currentState.getQuantile(0.5D), DELTA);

        currentState = sketchAggregator.apply(currentState, sketch2);
        assertEquals(7L, currentState.getN());
        assertEquals(4.0D, currentState.getQuantile(0.5D), DELTA);
    }

    @Test
    public void testEquals() {
        assertEquals(new DoublesSketchAggregator(), new DoublesSketchAggregator());
    }

    @Override
    public void shouldJsonSerialiseAndDeserialise() throws SerialisationException {
        // Given
        final DoublesSketchAggregator aggregator = new DoublesSketchAggregator();

        // When 1
        final String json = new String(JSONSerialiser.serialise(aggregator, true));
        // Then 1
        JsonAssert.assertJsonEquals(String.format("{%n" +
                "  \"class\" : \"uk.gov.gchq.gaffer.sketches.datasketches.quantiles.binaryoperator.DoublesSketchAggregator\"%n" +
                "}"), json);

        // When 2
        final DoublesSketchAggregator deserialisedAggregator = JSONSerialiser
                .deserialise(json.getBytes(), DoublesSketchAggregator.class);
        // Then 2
        assertNotNull(deserialisedAggregator);
    }

    @Override
    protected Class<? extends BinaryOperator> getFunctionClass() {
        return DoublesSketchAggregator.class;
    }

    @Override
    protected DoublesSketchAggregator getInstance() {
        return new DoublesSketchAggregator();
    }
}
