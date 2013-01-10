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

/**
 * @author Andres Almiray
 */
@Singleton
class RmiConnector {
    public RmiClient createClient(Map params) {
        def host = params.remove('host') ?: 'localhost'
        def port = params.remove('port') ?: 1199
        def lazy = params.remove('lazy') ?: true
        try {
            return new RmiClient(host, port as int, lazy)
        } catch(Exception e) {
            throw new RuntimeException("Failed to create RMI client, reason: $e", e)
        }
    }
}
