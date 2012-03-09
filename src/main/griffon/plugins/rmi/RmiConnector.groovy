/*
 * Copyright 2009-2011 the original author or authors.
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

import griffon.util.CallableWithArgs
import java.util.concurrent.ConcurrentHashMap

/**
 * @author Andres Almiray
 */
@Singleton
class RmiConnector implements RmiProvider {
    private final Map CLIENTS = new ConcurrentHashMap()

    Object withRmi(Map params, Closure closure) {
        return doWithClient(params, closure)
    }

    public <T> T withRmi(Map params, CallableWithArgs<T> callable) {
        return doWithClient(params, callable)
    }

    // ======================================================

    private doWithClient(Map params, Closure closure) {
        def client = configureClient(params)

        if (closure) {
            closure.delegate = client
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            return closure()
        }
        return null
    }

    private <T> T doWithClient(Map params, CallableWithArgs<T> callable) {
        def client = configureClient(params)

        if (callable) {
            callable.args = [client] as Object[]
            return callable.run()
        }
        return null
    }

    private configureClient(Map params) {
        def client = null
        if (params.id) {
            String id = params.remove('id').toString()
            client = CLIENTS[id]
            if(client == null) {
                client = makeClient(params)
                CLIENTS[id] = client 
            }
        } else {
            client = makeClient(params)
        }
        client
    }

    private makeClient(Map params) {
        def host = params.remove('host') ?: 'localhost'
        def port = params.remove('port') ?: 1199
        def lazy = params.remove('lazy') ?: true
        try {
            return new RMIClient(host, port as int, lazy)
        } catch(Exception e) {
            throw new RuntimeException("Failed to create RMI client, reason: $e", e)
        }
    }
}
