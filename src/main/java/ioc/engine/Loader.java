package ioc.engine;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class Loader {

    private final Set<Class<?>> loadedClasses = new HashSet<>();

    public Set<Class<?>> loadAllClasses(Class<?> referenceClass) {
        loadedClasses.clear();

        String filePath = referenceClass.getProtectionDomain().getCodeSource().getLocation().getFile();
        loadWorker(new File(filePath), "");

        return loadedClasses;
    }

    private void loadWorker(File file, String packageName) {
        if (file.isDirectory()) {
            packageName += file.getName() + ".";

            for (File children : requireNonNull(file.listFiles()))
                loadWorker(children, packageName);

        } else if (isClass(file)) {
            String className = packageName + file.getName();
            loadedClasses.add(createClassObject(className));
        }
    }

    private boolean isClass(File file) {
        return file.getName().endsWith(".class");
    }

    private Class<?> createClassObject(String className) {
        try {
            return Class.forName(formatClassName(className));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private String formatClassName(String className) {
        return className
                .replaceAll("classes.", "")
                .replaceAll(".class", "");
    }

}
