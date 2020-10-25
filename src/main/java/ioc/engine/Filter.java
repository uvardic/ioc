package ioc.engine;

import ioc.annotations.*;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class Filter {

    private static final Set<Class<?>> relevantAnnotations = new HashSet<>();

    static {
        relevantAnnotations.add(Autowired.class);
        relevantAnnotations.add(Bean.class);
        relevantAnnotations.add(Component.class);
        relevantAnnotations.add(Qualifier.class);
        relevantAnnotations.add(Service.class);
    }

    public Set<Class<?>> relevantFilter(Set<Class<?>> loadedClasses) {
        return loadedClasses.stream()
                .filter(this::isRelevant)
                .filter(aClass -> !aClass.isAnnotation())
                .filter(aClass -> !aClass.isInterface())
                .collect(toSet());
    }

    private boolean isRelevant(Class<?> aClass) {
        return Arrays.stream(aClass.getDeclaredAnnotations())
                .map(Annotation::annotationType)
                .anyMatch(relevantAnnotations::contains);
    }

}
