import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.function.Function;

public class ReflectionCLI {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Error: Incorrect number of parameters.");
            return;
        }

        File classFile = new File(args[0]);
        String fileName = classFile.getName();
        String className = fileName.endsWith(".class")
                ? fileName.substring(0, fileName.length() - 6)
                : fileName;

        File parentDir = classFile.getParentFile();
        if (parentDir == null) {
            parentDir = new File(".");
        }

        try {
            URL[] urls = new URL[]{ parentDir.toURI().toURL() };
            URLClassLoader classLoader = new URLClassLoader(urls);
            Class<?> loadedClass = classLoader.loadClass(className);

            analyzeClass(loadedClass);
            classLoader.close();
        } catch (Exception e) {
            System.err.println("Error loading class: " + e.getMessage());
        }
    }

    private static String getParameterString(Class<?>[] paramTypes) {
        if (paramTypes.length == 0) {
            return "";
        }
        StringBuilder params = new StringBuilder();
        for (int i = 0; i < paramTypes.length; i++) {
            params.append(paramTypes[i].getSimpleName());
            if (i < paramTypes.length - 1) {
                params.append(", ");
            }
        }
        return params.toString();
    }

    public static Object[] createFieldRow(Field field) {
        return new Object[]{
                "Field",
                Modifier.toString(field.getModifiers()),
                field.getName(),
                "Type:" + field.getType().getSimpleName()
        };
    }

    public static Object[] createConstructorRow(Constructor<?> constructor) {
        return new Object[]{
                "Constructor",
                Modifier.toString(constructor.getModifiers()),
                constructor.getDeclaringClass().getSimpleName(),
                "(" + getParameterString(constructor.getParameterTypes()) + ")"
        };
    }

    public static Object[] createMethodRow(Method method) {
        return new Object[]{
                "Method",
                Modifier.toString(method.getModifiers()),
                method.getName(),
                method.getReturnType().getSimpleName() + "(" + getParameterString(method.getParameterTypes()) + ")"
        };
    }

    public static Object[] createInterfaceRow(Class<?> iface) {
        return new Object[]{
                "Interface",
                "",
                iface.getSimpleName(),
                iface.getName()
        };
    }

    public static <T> void showReflectionElements(T[] elements, String sectionTitle, Function<T, Object[]> rowCreator) {
        if (elements != null && elements.length > 0) {
            System.out.println(sectionTitle);
            for (T element : elements) {
                System.out.println(Arrays.toString(rowCreator.apply(element)));
            }
            System.out.println();
        }
    }

    public static void analyzeClass(Class<?> clazz) {
        showReflectionElements(clazz.getDeclaredFields(), "FIELDS", ReflectionCLI::createFieldRow);
        showReflectionElements(clazz.getDeclaredConstructors(), "CONSTRUCTORS", ReflectionCLI::createConstructorRow);
        showReflectionElements(clazz.getDeclaredMethods(), "METHODS", ReflectionCLI::createMethodRow);
        showReflectionElements(clazz.getInterfaces(), "INTERFACES", ReflectionCLI::createInterfaceRow);
    }
}