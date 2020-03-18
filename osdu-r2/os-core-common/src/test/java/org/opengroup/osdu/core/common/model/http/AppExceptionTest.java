// Copyright 2017-2019, Schlumberger
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.core.common.model.http;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.opengroup.osdu.core.common.model.http.AppError;
import org.opengroup.osdu.core.common.model.http.AppException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AppExceptionTest {

    @Test
    public void constructorTest() {
        AppException exception = new AppException(200, "unknown error", "this error occurred:");
        assertNotNull(exception);

        AppError error = exception.getError();
        assertNotNull(error);

        assertEquals(200, error.getCode());
        assertEquals("unknown error", error.getReason());
        assertEquals("this error occurred:", error.getMessage());
    }

    @Test
    public void testForbidden() {
        String debuggingInfo = "dummy debuggingInfo";
        AppException exception = AppException.createForbidden(debuggingInfo);
        assertNotNull(exception);

        AppError error = exception.getError();
        assertNotNull(error);

        assertEquals(HttpStatus.SC_FORBIDDEN, error.getCode());
        assertEquals("Access denied", error.getReason());
        assertEquals("The user is not authorized to perform this action", error.getMessage());
        assertEquals(debuggingInfo, error.getDebuggingInfo());
    }

    @Test
    public void testUnauthorized() {
        String debuggingInfo = "dummy debuggingInfo";
        AppException exception = AppException.createUnauthorized(debuggingInfo);
        assertNotNull(exception);

        AppError error = exception.getError();
        assertNotNull(error);

        assertEquals(HttpStatus.SC_UNAUTHORIZED, error.getCode());
        assertEquals("Unauthorized", error.getReason());
        assertEquals("The user is not authorized to perform this action", error.getMessage());
        assertEquals(debuggingInfo, error.getDebuggingInfo());
    }
}
