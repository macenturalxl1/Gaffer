/*
 * Copyright 2016 Crown Copyright
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

package uk.gov.gchq.gaffer.accumulostore.operation.handler;

import uk.gov.gchq.gaffer.accumulostore.AccumuloStore;
import uk.gov.gchq.gaffer.accumulostore.key.IteratorSettingFactory;
import uk.gov.gchq.gaffer.accumulostore.key.exception.IteratorSettingException;
import uk.gov.gchq.gaffer.accumulostore.retriever.impl.AccumuloAllElementsRetriever;
import uk.gov.gchq.gaffer.commonutil.iterable.CloseableIterable;
import uk.gov.gchq.gaffer.data.element.Element;
import uk.gov.gchq.gaffer.operation.OperationException;
import uk.gov.gchq.gaffer.operation.impl.get.GetAllElements;
import uk.gov.gchq.gaffer.store.Context;
import uk.gov.gchq.gaffer.store.Store;
import uk.gov.gchq.gaffer.store.StoreException;
import uk.gov.gchq.gaffer.store.operation.handler.OperationHandler;
import uk.gov.gchq.gaffer.user.User;

public class GetAllElementsHandler implements OperationHandler<GetAllElements> {
    @Override
    public CloseableIterable<Element> doOperation(final GetAllElements operation, final Context context, final Store store)
            throws OperationException {
        return doOperation(operation, context.getUser(), (AccumuloStore) store);
    }

    public CloseableIterable<Element> doOperation(final GetAllElements operation, final User user, final AccumuloStore store) throws OperationException {
        final IteratorSettingFactory iteratorFactory = store.getKeyPackage().getIteratorFactory();
        try {
            return new AccumuloAllElementsRetriever(store, operation, user, iteratorFactory.getElementPropertyRangeQueryFilter(operation),
                    iteratorFactory.getElementPreAggregationFilterIteratorSetting(operation.getView(), store),
                    iteratorFactory.getElementPostAggregationFilterIteratorSetting(operation.getView(), store),
                    iteratorFactory.getEdgeEntityDirectionFilterIteratorSetting(operation),
                    iteratorFactory.getQueryTimeAggregatorIteratorSetting(operation.getView(), store));
        } catch (IteratorSettingException | StoreException e) {
            throw new OperationException("Failed to get elements", e);
        }
    }
}
