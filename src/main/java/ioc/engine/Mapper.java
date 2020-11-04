package ioc.engine;

import ioc.annotations.*;
import ioc.exception.IOCStateException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

class Mapper {

    public static Set<ClassModel> mapToModel(Set<Class<?>> filteredClasses) {
        return filteredClasses.stream()
                .map(Mapper::createModel)
                .collect(toSet());
    }

    private static ClassModel createModel(Class<?> aClass) {
        return new ClassModel(
                aClass.getName(),
                findDefaultConstructor(aClass),
                findAnnotation(aClass),
                findDependencies(aClass)
        );
    }

    private static Constructor<?> findDefaultConstructor(Class<?> aClass) {
        return Arrays.stream(aClass.getDeclaredConstructors())
                .filter(Mapper::isDefault)
                .peek(constructor -> constructor.setAccessible(true))
                .findAny()
                .orElseThrow(() -> new IOCStateException(
                        String.format("Default constructor for class: %s wasn't found!", aClass))
                );
    }

    private static boolean isDefault(Constructor<?> constructor) {
        return constructor.getParameterCount() == 0;
    }

    private static Annotation findAnnotation(Class<?> aClass) {
        return Arrays.stream(aClass.getDeclaredAnnotations())
                .filter(Engine::isAnnotationRelevant)
                .findFirst()
                .orElseThrow(() -> new IOCStateException(
                        String.format("Relevant annotation for class: %s wasn't found!", aClass)
                ));
    }

    private static Set<Field> findDependencies(Class<?> aClass) {
        return Arrays.stream(aClass.getDeclaredFields())
                .filter(Mapper::isDependency)
                .peek(Mapper::validate)
                .peek(field -> field.setAccessible(true))
                .collect(toSet());
    }

    private static boolean isDependency(Field field) {
        return field.isAnnotationPresent(Autowired.class) || field.isAnnotationPresent(Qualifier.class);
    }

    private static void validate(Field field) {
        if (field.isAnnotationPresent(Autowired.class) && field.isAnnotationPresent(Qualifier.class))
            throw new IOCStateException(
                    String.format("Field: %s contains both Autowired and Qualifier annotations!", field)
            );

        if (field.getType().isInterface() && field.isAnnotationPresent(Autowired.class))
            throw new IOCStateException(
                    String.format("Invalid annotation on field: %s!", field)
            );
    }

}
