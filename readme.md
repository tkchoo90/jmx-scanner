# JMX Scanner

## Overview

* The Java virtual machine (Java VM) has built-in instrumentation that enables you to monitor and manage it using the Java Management Extensions (JMX) technology.
* These built-in management utilities are often referred to as out-of-the-box management tools for the Java VM. You can also monitor any appropriately instrumented applications using the JMX API.
* JMX is used widely in the industry for Java Monitoring and management. Examples: Grafana, Jconsole, VisualVM
* To do so, we need to establish a mbeanServerConnection to the Application Server and retrieve the relevant information we need from the relevant MBeans attribute.

In this sample project, we will develop a project that is able to establish a remote connection to a running JVM, which in this case, is a JBoss Application Server

## Concepts demonstrated

* mBeansQuery of a JVM to get useful information
* Using a custom class loader, dynamically inject jar files and classes. In this example, we do not want to bundle the vendor specific "jboss-cli-client.jar" during compilation, hence we perform a injection during runtime

## Set up

1. Set up your desired application (in this example, a JBoss App server)
2. Checkout the code, execute "mvn clean install". This will create a fat jar in the /target folder
3. open your terminal, execute the command 
<code>
   java -Djava.system.class.loader=MyCustomClassLoader.CustomClassLoader -jar jmx-scanner-1.0-SNAPSHOT-fatjar.jar
   </code>
   
## Credits
* https://docs.oracle.com/javase/7/docs/technotes/guides/management/agent.html
* https://stackoverflow.com/questions/60764/how-to-load-jar-files-dynamically-at-runtime
* https://github.com/update4j/update4j/blob/master/src/main/java/org/update4j/DynamicClassLoader.java
* https://dzone.com/articles/remote-jmx-access-wildfly-or

