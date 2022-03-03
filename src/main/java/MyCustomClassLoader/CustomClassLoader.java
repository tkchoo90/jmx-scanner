/*
Credits to:
https://stackoverflow.com/questions/60764/how-to-load-jar-files-dynamically-at-runtime
https://github.com/update4j/update4j/blob/master/src/main/java/org/update4j/DynamicClassLoader.java
 */
package MyCustomClassLoader;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;

public class CustomClassLoader extends URLClassLoader {
    static {
        registerAsParallelCapable();
    }

    public CustomClassLoader() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public CustomClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
    }

    public static CustomClassLoader findAncestor(ClassLoader cl) {
        do {

            if (cl instanceof CustomClassLoader)
                return (CustomClassLoader) cl;

            cl = cl.getParent();
        } while (cl != null);

        return null;
    }
    public void add(URL url) {
        addURL(url);
    }


//    public void appendClassPath(String jarfile) throws IOException {
//        add(Paths.get(jarfile).toRealPath().toUri().toURL());
//    }

    /*
     *  Required for Java Agents when this classloader is used as the system classloader
     */
    @SuppressWarnings("unused")
    private void appendToClassPathForInstrumentation(String jarfile) throws IOException {
        add(Paths.get(jarfile).toRealPath().toUri().toURL());
    }


}
