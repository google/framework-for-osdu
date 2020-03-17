/*
 * Copyright 2020 Google LLC
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

import com.google.common.base.Strings;

import java.util.Objects;
import java.util.function.Function;

public final class Preconditions {

    private static Function<String, Boolean> isNotNullOrEmptyPredicate = (s) -> !Strings.isNullOrEmpty(s);

    public static <T> T checkArgument(T argument, Function<T, Boolean> predicate, Object errorMessage) {
        if (!predicate.apply(argument)) {
            throw new IllegalArgumentException(String.valueOf(errorMessage));
        } else {
            return argument;
        }
    }

    public static <T> T checkNotNull(T argument) {
        return checkNotNull(argument, "Argument should be not null");
    }

    public static String checkNotNullOrEmpty(String argument, Object errorMessage) {
        return checkArgument(argument, isNotNullOrEmptyPredicate, errorMessage);
    }

    public static <T> T checkNotNull(T argument, Object errorMessage) {
        return checkArgument(argument, Objects::nonNull, errorMessage);
    }
}
