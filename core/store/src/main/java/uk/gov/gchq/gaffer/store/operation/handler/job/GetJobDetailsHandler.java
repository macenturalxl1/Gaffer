/*
 * Copyright 2016-2017 Crown Copyright
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

package uk.gov.gchq.gaffer.store.operation.handler.job;

import uk.gov.gchq.gaffer.jobtracker.JobDetail;
import uk.gov.gchq.gaffer.operation.OperationException;
import uk.gov.gchq.gaffer.operation.impl.job.GetJobDetails;
import uk.gov.gchq.gaffer.store.Context;
import uk.gov.gchq.gaffer.store.Store;
import uk.gov.gchq.gaffer.store.operation.handler.OperationHandler;

public class GetJobDetailsHandler implements OperationHandler<GetJobDetails> {
    @Override
    public JobDetail doOperation(final GetJobDetails operation, final Context context, final Store store) throws OperationException {
        if (null == store.getJobTracker()) {
            throw new OperationException("The Job Tracker has not been configured");
        }

        final String jobId = null != operation.getJobId() ? operation.getJobId() : context.getJobId();
        return store.getJobTracker().getJob(jobId, context.getUser());
    }
}
