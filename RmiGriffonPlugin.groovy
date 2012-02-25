/*
 * Copyright 2009-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author Andres Almiray
 */
class RmiGriffonPlugin {
    // the plugin version
    String version = '0.7'
    // the version or versions of Griffon the plugin is designed for
    String griffonVersion = '0.9.5 > *'
    // the other plugins this plugin depends on
    Map dependsOn = [:]
    // resources that are included in plugin packaging
    List pluginIncludes = []
    // the plugin license
    String license = 'Apache Software License 2.0'
    // Toolkit compatibility. No value means compatible with all
    // Valid values are: swing, javafx, swt, pivot, gtk
    List toolkits = []
    // Platform compatibility. No value means compatible with all
    // Valid values are:
    // linux, linux64, windows, windows64, macosx, macosx64, solaris
    List platforms = []
    // URL where documentation can be found
    String documentation = ''
    // URL where source can be found
    String source = 'https://github.com/griffon/griffon-rmi-plugin'

    List authors = [
        [
            name: 'Andres Almiray',
            email: 'aalmiray@yahoo.com'
        ]
    ]
    String title = 'RMI client'

    String description = '''
The RMI plugin adds a remoting client that uses the [Java RMI protocol][1]. It is compatible with [Grails' Remoting plugin 1.0][2].

Usage
-----
The plugin will inject the following dynamic methods:

* `withRmi(Map params, Closure stmts)` - executes stmts using the Rmi protocol

Where params may contain

| Property | Type     | Default   |
| -------- | -------- | --------- |
| host     | String   | localhost |
| port     | int      | 1199      |
| lazy     | boolean  | true      |

All dynamic methods will create a new client when invoked unless you define an `id:` attribute.
When this attribute is supplied the client will be stored in a cache managed by the `RmiProvider` that
handled the call. The plugin will attempt to locate the default RmiRegistry when the `lazy:` property is set to false.

These methods are also accessible to any component through the singleton `griffon.plugins.rmi.RmiEnhancer`.
You can inject these methods to non-artifacts via metaclasses. Simply grab hold of a particular metaclass and call
`RmiEnhancer.enhance(metaClassInstance)`.

Configuration
-------------
### Dynamic method injection

Dynamic methods will be added to controllers by default. You can
change this setting by adding a configuration flag in `griffon-app/conf/Config.groovy`

    griffon.rmi.injectInto = ['controller', 'service']

### Example

This example relies on [Grails][3] as the service provider. Follow these steps to configure the service on the Grails side:

1. Download a copy of [Grails][4] and install it.
2. Create a new Grails application. We'll pick 'exporter' as the application name.

        grails create-app exporter

3. Change into the application's directory. Install the remoting plugin.

        grails install-plugin remoting

4. Create the following interface in `src/groovy/exporter/Calculator.groovy`. This interface will be used on the Griffon side too.

        package exporter
        import java.rmi.Remote
        import java.rmi.RemoteException
        interface Calculator extends Remote {
            double add(double a, double b) throws RemoteException
        }
    
5. Create an implementation of the `Calculator` interface as a service

        grails create-service calculator
    
6. Paste the following code in `grails-app/services/exporter/CalculatorService.groovy`

        package exporter
        class CalculatorService implements Calculator {
            boolean transactional = false
            static expose = ['rmi']
 
            double add(double a, double b){
                println "add($a, $b)" // good old println() for quick debugging
                return a + b
            }
        }

7. Run the application

        grails run-app
    
Now we're ready to build the Griffon application

1. Create a new Griffon application. We'll pick `calculator` as the application name

        griffon create-app calculator
    
2. Install the rmi plugin

        griffon install-plugin rmi

3. Fix the view script to look like this

        package calculator
        application(title: 'Rmi Plugin Example',
          pack: true,
          locationByPlatform: true,
          iconImage: imageIcon('/griffon-icon-48x48.png').image,
          iconImages: [imageIcon('/griffon-icon-48x48.png').image,
                       imageIcon('/griffon-icon-32x32.png').image,
                       imageIcon('/griffon-icon-16x16.png').image]) {
            gridLayout(cols: 2, rows: 4)
            label('Num1:')
            textField(columns: 20, text: bind(target: model, targetProperty: 'num1'))
            label('Num2:')
            textField(columns: 20, text: bind(target: model, targetProperty: 'num2'))
            label('Result:')
            label(text: bind{model.result})
            button('Calculate', enabled: bind{model.enabled}, actionPerformed: controller.calculate)
        }

4. Let's add required properties to the model

        package calculator
        @Bindable
        class CalculatorModel {
           String num1
           String num2
           String result
           boolean enabled = true
        }

5. Now for the controller code. Notice that there is minimal error handling in place. If the user
types something that is not a number the client will surely break, but the code is sufficient for now.

        package calculator
        class CalculatorController {
            def model
 
            def calculate = { evt = null ->
                double a = model.num1.toDouble()
                double b = model.num2.toDouble()
                execSync { model.enabled = false }
                try {
                    def result = withRmi(host: 'localhost', port: 1199) {
                        service('exporter.Calculator') {
                            add(a, b)
                        }
                    }
                    execInsideUIAsync { model.result = result.toString() }
                } finally {
                    execInsideUIAsync { model.enabled = true }
                }
            }
        }
    
6. Locate the compiled classes from Grails; jar the calculator interface and place it in the lib directory
of the Griffon application. Assume $grailsProject points to the directory of the exporter application and
$griffonProject points to the calculator application

        cd $grailsProject/target/classes
        jar cvf $griffonProject/lib/exporter-api.jar exporter/Calculator.class
    
7. Run the application

    griffon run-app
    
The first argument to `service()` may be a String with the full qualified classname or a Class.

### Java API

Here's how the above service call may be written in Java

        import static griffon.util.CollectionUtils.map;
        import griffon.plugins.rmi.RmiConnector;
        import griffon.plugins.rmi.RMIClient;
        import griffon.util.CallableWithArgs;
        import java.util.Map;
        import exporter.Calculator;
 
        final double a = Double.parseDouble(model.getNum1());
        final double b = Double.parseDouble(model.getNum2());
        Map params = map().e("host", "localhost")
                          .e("port", 1199);
        Double result = RmiConnector.getInstance().withRmi(params, new CallableWithArgs<Double>() {
            RMIClient client = (RMIClient) args[0];
            return client.service("Calculator", new CallableWithArgs<Double>() {
                public Double call(Object[] args2) {
                    return ((Calculator) args2[0]).add(a, b);
                }
            });
        });

Testing
-------
Dynamic methods will not be automatically injected during unit testing, because addons are simply not initialized
for this kind of tests. However you can use `RmiEnhancer.enhance(metaClassInstance, rmiProviderInstance)` where 
`rmiProviderInstance` is of type `griffon.plugins.rmi.RmiProvider`. The contract for this interface looks like this

    public interface RmiProvider {
        Object withRmi(Map params, Closure closure);
        <T> T withRmi(Map params, CallableWithArgs<T> callable);
    }

It's up to you define how these methods need to be implemented for your tests. For example, here's an implementation that never
fails regardless of the arguments it receives

    class MyRmiProvider implements RmiProvider {
        Object withRmi(Map params, Closure closure) { null }
        public <T> T withRmi(Map params, CallableWithArgs<T> callable) { null }
    }
    
This implementation may be used in the following way

    class MyServiceTests extends GriffonUnitTestCase {
        void testSmokeAndMirrors() {
            MyService service = new MyService()
            RmiEnhancer.enhance(service.metaClass, new MyRmiProvider())
            // exercise service methods
        }
    }


[1]: http://en.wikipedia.org/wiki/Java_remote_method_invocation
[2]: http://grails.org/plugin/remoting
[3]: http://grails.org
[4]: http://grails.org/Download
'''
}
