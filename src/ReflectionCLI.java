import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

public class ReflectionCLI {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Error: Incorrect number of parameters. Please provide exactly one path to a .class file.");
            return;
        }

        try {
            File classFile = new File(args[0]);

            String fileName = classFile.getName();
            String className = fileName.endsWith(".class")
                    ? fileName.substring(0, fileName.length() - 6)
                    : fileName;

            File parentDir = classFile.getParentFile();
            if (parentDir == null) {
                parentDir = new File(".");
            }

            URL[] urls = new URL[]{ parentDir.toURI().toURL() };
            try (URLClassLoader classLoader = new URLClassLoader(urls)) {
                Class<?> loadedClass = classLoader.loadClass(className);

                System.out.println("Successfully loaded: " + loadedClass.getName());
            }

        } catch (Exception e) {
            System.err.println("An error occurred while loading the class: " + e.getMessage());
        }
    }
}