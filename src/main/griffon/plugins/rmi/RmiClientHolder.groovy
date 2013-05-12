/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package griffon.plugins.rmi

import java.util.concurrent.ConcurrentHashMap

/**
 * @author Andres Almiray
 */
class RmiClientHolder {
    private static final RmiClientHolder INSTANCE

    static {
        INSTANCE = new RmiClientHolder()
    }

    static RmiClientHolder getInstance() {
        INSTANCE
    }

    private RmiClientHolder() {}

    private final Map<String, RmiClient> CLIENTS = new ConcurrentHashMap<String, RmiClient>()

    String[] getRmiClientIds() {
        List<String> ids = []
        ids.addAll(CLIENTS.keySet())
        ids.toArray(new String[ids.size()])
    }

    RmiClient getRmiClient(String id) {
        CLIENTS[id]
    }

    void setRmiClient(String id, RmiClient client) {
        CLIENTS[id] = client
    }

    // ======================================================

    RmiClient fetchRmiClient(Map<String, Object> params) {
        RmiClient client = CLIENTS[(params.id).toString()]
        if (client == null) {
            String id = params.id ? params.remove('id').toString() : '<EMPTY>'
            client = RmiConnector.instance.createClient(params)
            if (id != '<EMPTY>') CLIENTS[id] = client
        }
        client
    }
}
