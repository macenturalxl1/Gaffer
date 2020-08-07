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

package uk.gov.gchq.gaffer.serialisation;

import org.junit.jupiter.api.Test;

import uk.gov.gchq.gaffer.commonutil.pair.Pair;
import uk.gov.gchq.gaffer.exception.SerialisationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class SerialisationTest<INPUT, OUTPUT> {
    protected final Serialiser<INPUT, OUTPUT> serialiser;
    protected final Pair<INPUT, OUTPUT>[] historicSerialisationPairs;

    public SerialisationTest() {
        this.serialiser = getSerialisation();
        this.historicSerialisationPairs = getHistoricSerialisationPairs();
    }

    @Test
    public void shouldSerialiseWithHistoricValues() throws Exception {
        assertNotNull(historicSerialisationPairs, "historicSerialisationPairs should not be null.");
        assertNotEquals(0, historicSerialisationPairs.length, "historicSerialisationPairs should not be empty.");
        for (final Pair<INPUT, OUTPUT> pair : historicSerialisationPairs) {
            assertNotNull(pair.getFirst(), "historicSerialisationPairs first value should not be null");
            serialiseFirst(pair);
            assertNotNull(pair.getSecond(), "historicSerialisationPairs second value should not be null");
            deserialiseSecond(pair);
        }
    }

    protected void deserialiseSecond(final Pair<INPUT, OUTPUT> pair) throws SerialisationException {
        assertEquals(pair.getFirst(), serialiser.deserialise(pair.getSecond()));
    }

    protected void serialiseFirst(final Pair<INPUT, OUTPUT> pair) throws SerialisationException {
        assertEquals(pair.getSecond(), serialiser.serialise(pair.getFirst()));
    }

    @Test
    public abstract void shouldSerialiseNull() throws SerialisationException;

    @Test
    public abstract void shouldDeserialiseEmpty() throws SerialisationException;

    public abstract Serialiser<INPUT, OUTPUT> getSerialisation();

    public abstract Pair<INPUT, OUTPUT>[] getHistoricSerialisationPairs();
}
