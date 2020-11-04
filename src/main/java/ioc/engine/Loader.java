package ioc.engine;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

class Loader {

    private static final Set<Class<?>> loadedClasses = new HashSet<>();

    private static final String rootPackageName = "";

    public static Set<Class<?>> loadAllClasses(Class<?> referenceClass) {
        loadedClasses.clear();

        String filePath = referenceClass.getProtectionDomain().getCodeSource().getLocation().getFile();
        loadWorker(new File(filePath), rootPackageName);

        return loadedClasses;
    }

    private static void loadWorker(File file, String packageName) {
        if (file.isDirectory()) {
            packageName += file.getName() + ".";

            for (File children : requireNonNull(file.listFiles()))
                loadWorker(children, packageName);

        } else if (isClass(file)) {
            String className = packageName + file.getName();
            loadedClasses.add(createClassObject(className));
        }
    }

    private static boolean isClass(File file) {
        return file.getName().endsWith(".class");
    }

    private static Class<?> createClassObject(String className) {
        try {
            return Class.forName(formatClassName(className));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static String formatClassName(String className) {
        return className
                .replaceAll("classes.", "")
                .replaceAll(".class", "");
    }

}
