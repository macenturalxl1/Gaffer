/*
 * Copyright 2018-2020 Crown Copyright
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

package uk.gov.gchq.gaffer.sparkaccumulo.operation.rfilereaderrdd;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AccumuloTabletTest {

    @DisplayName("Equals and hashcode test coverage")
    @Test
    public void testAccumuloTabletEquals() {
        final AccumuloTablet accumuloTablet = new AccumuloTablet(0, 0, "a", "b");

        final AccumuloTablet expected = new AccumuloTablet(0, 0, "a", "b");
        assertTrue(accumuloTablet.equals(expected));
    }
}