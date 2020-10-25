package ioc.engine;

import ioc.annotations.Autowired;
import ioc.annotations.Qualifier;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class Mapper {

    public Set<ClassModel> mapToModel(Set<Class<?>> filteredClasses) {
        return filteredClasses.stream()
                .map(this::createModel)
                .collect(toSet());
    }

    private ClassModel createModel(Class<?> aClass) {
        return new ClassModel(findDefaultConstructor(aClass), findAnnotation(aClass), findDependencies(aClass));
    }

    private Constructor<?> findDefaultConstructor(Class<?> aClass) {
        return Arrays.stream(aClass.getDeclaredConstructors())
                .filter(this::isDefault)
                .peek(constructor -> constructor.setAccessible(true))
                .findAny()
                .orElseThrow(() -> new IllegalStateException(
                        String.format("Default constructor for class: %s wasn't found!", aClass))
                );
    }

    private boolean isDefault(Constructor<?> constructor) {
        return constructor.getParameterCount() == 0;
    }

    private Annotation findAnnotation(Class<?> aClass) {
        return aClass.getDeclaredAnnotations()[0];
    }

    private Set<Field> findDependencies(Class<?> aClass) {
        return Arrays.stream(aClass.getDeclaredFields())
                .filter(this::isDependency)
                .peek(this::validate)
                .peek(field -> field.setAccessible(true))
                .collect(toSet());
    }

    private boolean isDependency(Field field) {
        return field.isAnnotationPresent(Autowired.class) || field.isAnnotationPresent(Qualifier.class);
    }

    private void validate(Field field) {
        if (field.isAnnotationPresent(Autowired.class) && field.isAnnotationPresent(Qualifier.class))
            throw new IllegalStateException(
                    String.format("Field: %s contains both Autowired and Qualifier annotations!", field)
            );

        if (field.getType().isInterface() && field.isAnnotationPresent(Autowired.class))
            throw new IllegalStateException(
                    String.format("Invalid annotation on field: %s!", field)
            );
    }

}
