import MyCustomClassLoader.CustomClassLoader;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.management.*;
import javax.management.openmbean.CompositeData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

public class JmxScannerMain {

    public static void main(String[] args) throws Exception {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        CustomClassLoader dynamic = CustomClassLoader.findAncestor(contextClassLoader);
        URL url = Paths.get("/home/tk/EAP-7.4.0/bin/client/jboss-cli-client.jar").toRealPath().toUri().toURL();
        dynamic.add(url);
        scanJBoss();
    }

    public static void scanJBoss() throws Exception {
        // Setup JMX connection
        String host = "localhost";
        int port = 9990; // default jboss management-http port
        String urlString = "service:jmx:remote+http://" + host + ":" + port;
        JMXServiceURL serviceURL = new JMXServiceURL(urlString);

        // App server credential
        String user = "user";
        String password = "Password123!";
        String[] credentials = new String[] { user, password };
        Map credentialsMap = new HashMap();
        credentialsMap.put("jmx.remote.credentials", credentials);

        //Establishing mbeanServerConnection
        JMXConnector jmxConnector = JMXConnectorFactory.connect(serviceURL, credentialsMap);
        MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();

//        Get all attributes of mBean Server
        queryAllMbeansAttribute(connection);

//        Query relevant attributes from JBoss App Server
        queryJBossServer(connection);

        jmxConnector.close();
    }

    public static void queryJBossServer(MBeanServerConnection connection) throws Exception{

        Map JbossInfo = new LinkedHashMap();
        JbossInfo.put("EP-JBossScan-Timestamp", new SimpleDateFormat("dd-MMM-yyyy'T'HH:mm:ssXXX")
                .format(new Date()));

        // Finding JVM runtime information
        ObjectName objectName = ObjectName.getInstance("java.lang:type=Runtime");
        String [] runtimeAttributes = {"ClassPath","VmName", "VmVendor", "VmVersion", "Uptime", "Pid", "StartTime"};
        List<Attribute> result =  connection.getAttributes(objectName, runtimeAttributes).asList();
        result.stream().forEach( t -> {
                    JbossInfo.put(t.getName(), t.getValue());
                }
        );

        Map heap = new LinkedHashMap();
        Object heapMemoryUsage = connection.getAttribute(new ObjectName("java.lang:type=Memory"), "HeapMemoryUsage");
        CompositeData cd = (CompositeData) heapMemoryUsage;
        heap.put("Max", cd.get("max"));
        heap.put("Committed", cd.get("committed"));
        heap.put("Used", cd.get("used"));
        JbossInfo.put("Heap-in-bytes", heap);

        // Get all JBoss Deployments
        String [] deploymentAttributes = {"runtimeName", "enabledTime", "enabled", "status"};
        String [] undertowAttributes = {"contextRoot", "server", "virtualHost"};
        List<Map> deployments = new ArrayList<>(); // List of all deployments in application server
        Set<ObjectInstance> mBeans =  connection.queryMBeans(ObjectName.getInstance("jboss.as:deployment=*"), null);
        mBeans.forEach(System.out::println);
        mBeans.forEach(bean -> {
                    try {
                        Map deployment = new LinkedHashMap();

                        // Get useful attributes of each JBoss Deployment
                        List<Attribute> deploymentResult = connection.getAttributes(bean.getObjectName(), deploymentAttributes).asList();
                        deploymentResult.forEach(x -> deployment.put(x.getName(), x.getValue()));

                        // Get additional useful attributes of each JBoss Deployment from the undertow subsystem
                        String underTowModel = String.format("jboss.as:deployment=%s,subsystem=undertow", deployment.get("runtimeName"));
                        List<Attribute> undertowResult = connection.getAttributes(ObjectName.getInstance(underTowModel), undertowAttributes).asList();
                        undertowResult.forEach(x -> deployment.put(x.getName(), x.getValue()));

                        deployments.add(deployment);
                    } catch (InstanceNotFoundException | MalformedObjectNameException | ReflectionException | IOException e) {
                        e.printStackTrace();
                    }
                }
        );

        // Consolidate all information and out to log file
        JbossInfo.put("Deployments", deployments);
        String jbossjson = new ObjectMapper().writeValueAsString(JbossInfo);
        System.out.println(jbossjson);

    }

    public static void queryAllMbeansAttribute (MBeanServerConnection connection) throws IOException, ReflectionException, InstanceNotFoundException, IntrospectionException {
        Set mbeans = connection.queryNames(null, null);
        for (Object mbean : mbeans) {
            MBeanInfo info = connection.getMBeanInfo((ObjectName)mbean);
            MBeanAttributeInfo[] attrInfo = info.getAttributes();
            System.out.println("Attributes for object: " + mbean +":\n");
            for (MBeanAttributeInfo attr : attrInfo)
            {
                System.out.println("  " + attr.getName() + "\n");
            }
        }
    }

}
