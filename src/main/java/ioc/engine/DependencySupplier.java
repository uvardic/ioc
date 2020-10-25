package ioc.engine;

import ioc.annotations.Qualifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toUnmodifiableSet;

public class DependencySupplier {

    private static final Map<String, ClassModel> qualifierContext = new HashMap<>();

    private DependencySupplier() {}

    public static void initializeQualifierContext(Set<ClassModel> mappedClasses) {
        mappedClasses.stream()
                .filter(ClassModel::isQualifier)
                .forEach(DependencySupplier::fillContext);
    }

    private static void fillContext(ClassModel classModel) {
        Qualifier annotation = (Qualifier) classModel.getAnnotation();

        if (qualifierContext.containsKey(annotation.value()))
            throw new IllegalStateException(
                    String.format("Qualifier value: %s is already present!", annotation.value())
            );

        qualifierContext.put(annotation.value(), classModel);
    }

    private static final Set<Object> resolvedClasses = new HashSet<>();

    public static void addResolvedClass(Object instance) {
        resolvedClasses.add(instance);
    }

    public static Set<Object> getResolvedClasses() {
        return resolvedClasses.stream().collect(toUnmodifiableSet());
    }
}
