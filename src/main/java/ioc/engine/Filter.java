package ioc.engine;

import java.util.Arrays;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

class Filter {

    public static Set<Class<?>> relevantFilter(Set<Class<?>> loadedClasses) {
        return loadedClasses.stream()
                .filter(Filter::isRelevant)
                .filter(aClass -> !aClass.isAnnotation())
                .filter(aClass -> !aClass.isInterface())
                .collect(toSet());
    }

    private static boolean isRelevant(Class<?> aClass) {
        return Arrays.stream(aClass.getDeclaredAnnotations())
                .anyMatch(Engine::isAnnotationRelevant);
    }

}
