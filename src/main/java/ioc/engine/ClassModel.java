package ioc.engine;

import ioc.annotations.Qualifier;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.Set;

public class ClassModel {

    private final Constructor<?> defaultConstructor;

    private final Annotation annotation;

    private final Set<Field> dependencies;

    public ClassModel(Constructor<?> defaultConstructor, Annotation annotation, Set<Field> dependencies) {
        this.defaultConstructor = defaultConstructor;
        this.annotation = annotation;
        this.dependencies = dependencies;
    }

    public boolean isQualifier() {
        return annotation.annotationType().equals(Qualifier.class);
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public boolean isResolved() {
        return dependencies.stream().allMatch(this::isFieldAssignable);
    }

    private boolean isFieldAssignable(Field field) {
        return DependencySupplier.getResolvedClasses()
                .stream()
                .map(Object::getClass)
                .anyMatch(instanceClass -> field.getType().isAssignableFrom(instanceClass));
    }

    public void instantiate() {
        try {
            Object classInstance = defaultConstructor.newInstance();

            System.out.println(dependencies);
            dependencies.forEach(field -> setField(classInstance, field));
            dependencies.forEach(field -> printFieldInfo(classInstance, field));

            DependencySupplier.addResolvedClass(classInstance);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.getCause().printStackTrace();
//            e.printStackTrace();
        }
    }

    private void setField(Object classInstance, Field field) {
        try {
            field.set(classInstance, findAssignment(field));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    // todo sredi ovo gospode
    private Object findAssignment(Field field) {
        return DependencySupplier.getResolvedClasses()
                .stream()
                .filter(instance -> field.getType().isAssignableFrom(instance.getClass()))
                .findFirst()
                .orElseThrow();
    }

    // todo delete
    public Constructor<?> getDefaultConstructor() {
        return defaultConstructor;
    }

    private void printFieldInfo(Object classInstance, Field field) {
        try {
            System.out.printf(
                    "Initialized %s %s in %s on %s with %s%n\n",
                    field.getType(),
                    field.getName(),
                    field.getDeclaringClass().getName(),
                    new Date(),
                    field.get(classInstance).hashCode()
            );
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "ClassModel{" +
                "defaultConstructor=" + defaultConstructor +
                ", annotation=" + annotation +
                ", dependencies=" + dependencies +
                '}';
    }
}
