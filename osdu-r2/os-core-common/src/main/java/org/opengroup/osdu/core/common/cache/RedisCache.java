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

package org.opengroup.osdu.core.common.cache;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.SetArgs;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.sync.RedisCommands;
import com.lambdaworks.redis.codec.CompressionCodec;

import java.util.concurrent.TimeUnit;

public class RedisCache<K, V> implements ICache<K, V>, AutoCloseable {

    private final StatefulRedisConnection<K, V> connection;
    private final RedisClient client;
    private final RedisCommands<K, V> commands;
    private final int expireLengthSeconds;

    public RedisCache(String host, int port, int expTimeSeconds, int database,
                      Class<K> classOfK, Class<V> classOfV) {
        RedisURI uri = new RedisURI(host, port, 30, TimeUnit.SECONDS);
        uri.setDatabase(database);
        client = RedisClient.create(uri);
        connection = client.connect(
                CompressionCodec.valueCompressor(new JsonCodec<>(classOfK, classOfV), CompressionCodec.CompressionType.GZIP));
        commands = connection.sync();
        expireLengthSeconds = expTimeSeconds;
    }

    public RedisCache(String host, int port, int expTimeSeconds, Class<K> classOfK, Class<V> classOfV) {
        this(host, port, expTimeSeconds, 0, classOfK, classOfV);
    }

    @Override
    public void put(K key, V value) {
        SetArgs args = new SetArgs();
        args.ex(expireLengthSeconds);
        commands.set(key, value, args);
    }

    @Override
    public V get(K key) {
        return commands.get(key);
    }

    @Override
    public void delete(K key) {
        commands.del(key);
    }

    @Override
    public void close() {
        if (connection != null)
            connection.close();
        if (client != null)
            client.shutdown();
    }

    @Override
    public void clearAll() {
        this.commands.flushdb();
    }
}
