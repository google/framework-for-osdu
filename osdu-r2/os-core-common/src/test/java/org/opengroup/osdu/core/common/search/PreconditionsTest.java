/*
 * Copyright 2020 Google LLC
 * Copyright 2017-2019, Schlumberger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.core.common.search;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.util.Objects;

@RunWith(MockitoJUnitRunner.class)
@PrepareForTest({Preconditions.class})
public class PreconditionsTest {

    @Test(expected = IllegalArgumentException.class)
    public void should_return_error_when_value_is_null(){
        Preconditions.checkNotNull(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_return_error_when_argument_is_invalid(){
        Preconditions.checkArgument(null, Objects::nonNull, "value cannot be null");
    }

    @Test
    public void should_check_when_argument_is_valid(){
        Preconditions.checkArgument("", Objects::nonNull, "value cannot be null");
    }

    @Test
    public void should_check_null_empty_when_argument_is_valid(){
        Preconditions.checkNotNullOrEmpty("index", "value cannot be null");
    }

}
