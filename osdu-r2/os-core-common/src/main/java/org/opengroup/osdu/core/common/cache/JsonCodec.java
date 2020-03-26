// Copyright 2017-2019, Schlumberger
// Copyright 2020 Google LLC
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

package org.opengroup.osdu.core.common.cache;

import com.google.gson.Gson;
import com.lambdaworks.redis.codec.RedisCodec;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;

class JsonCodec<K, V> implements RedisCodec<K, V> {

    private final Class<K> classOfK;
    private final Class<V> classOfV;
    private final Gson gson = new Gson();

    public JsonCodec(Class<K> classOfK, Class<V> classOfV) {
        this.classOfK = classOfK;
        this.classOfV = classOfV;
    }

    @Override
    public K decodeKey(ByteBuffer bytes) {
        return decode(bytes, classOfK);
    }

    @Override
    public V decodeValue(ByteBuffer bytes) {
        return decode(bytes, classOfV);
    }

    @Override
    public ByteBuffer encodeKey(K k) {
        return k == null ? ByteBuffer.wrap(new byte[0]) : ByteBuffer.wrap(gson.toJson(k).getBytes());
    }

    @Override
    public ByteBuffer encodeValue(V v) {
        return v == null ? ByteBuffer.wrap(new byte[0]) : ByteBuffer.wrap(gson.toJson(v).getBytes());
    }

    private <T> T decode(ByteBuffer bytes, Class<T> classOf) {
        byte[] array = new byte[bytes.remaining()];
        bytes.get(array);
        try (InputStreamReader stream = new InputStreamReader(new ByteArrayInputStream(array))) {
            return gson.fromJson(stream, classOf);
        } catch (IOException e) {
            System.err.println(String.format("Unexpected error decoding from redis cache: %s", e.getMessage()));
            return null;
        }
    }
}
