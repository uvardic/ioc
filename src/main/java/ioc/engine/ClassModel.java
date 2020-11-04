package ioc.engine;

import ioc.annotations.Autowired;
import ioc.annotations.Qualifier;
import org.apache.log4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

class ClassModel {

    private static final Logger logger = Logger.getLogger(ClassModel.class);

    private final String name;

    private final Constructor<?> defaultConstructor;

    private final Annotation annotation;

    private final Set<Field> dependencies;

    public ClassModel(String name, Constructor<?> defaultConstructor, Annotation annotation, Set<Field> dependencies) {
        this.name = name;
        this.defaultConstructor = defaultConstructor;
        this.annotation = annotation;
        this.dependencies = dependencies;
    }

    public String getName() {
        return name;
    }

    public boolean isQualifier() {
        return annotation.annotationType().equals(Qualifier.class);
    }

    public boolean isResolved() {
        return dependencies.stream().allMatch(this::isDependencyResolved);
    }

    private boolean isDependencyResolved(Field dependency) {
        if (dependency.isAnnotationPresent(Qualifier.class)) {
            Qualifier qualifier = dependency.getAnnotation(Qualifier.class);
            return DependencySupplier.isQualifierResolved(qualifier.value());
        }

        return DependencySupplier.getResolvedClasses()
                .stream()
                .map(Object::getClass)
                .anyMatch(resolvedClass -> dependency.getType().isAssignableFrom(resolvedClass));
    }

    public void resolveClass() {
        logger.info("Resolving: " + getName());
        Object instance = instantiate();
        if (isQualifier()) {
            String key = ((Qualifier) annotation).value();
            DependencySupplier.addResolvedQualifier(key, instance);
        } else
            DependencySupplier.addResolvedClass(instance);
        logger.info("Resolved: " + getName());
    }

    public Object instantiate() {
        logger.info("Instantiating: " + getName());
        try {
            Object instance = defaultConstructor.newInstance();
            resolveDependencies(instance);
            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private void resolveDependencies(Object classInstance) {
        for (Field dependency : dependencies) {
            if (dependency.isAnnotationPresent(Qualifier.class)) {
                Qualifier qualifier = dependency.getAnnotation(Qualifier.class);
                injectQualifierDependency(classInstance, dependency, qualifier);
            } else {
                injectDependency(classInstance, dependency);
                if (isVerbose(dependency))
                    printFieldInfo(classInstance, dependency);
            }
        }
    }

    private void injectQualifierDependency(Object classInstance, Field dependency, Qualifier qualifier) {
        try {
            dependency.set(classInstance, DependencySupplier.getQualifier(qualifier));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void injectDependency(Object classInstance, Field dependency) {
        try {
            dependency.set(classInstance, DependencySupplier.getResolverFor(dependency));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private boolean isVerbose(Field dependency) {
        return dependency.getDeclaredAnnotationsByType(Autowired.class)[0].verbose();
    }

    private void printFieldInfo(Object classInstance, Field field) {
        try {
            logger.info(
                String.format(
                    "Initialized %s %s in %s on %s with %s",
                    field.getType(),
                    field.getName(),
                    field.getDeclaringClass().getName(),
                    new Date(),
                    field.get(classInstance).hashCode()
                )
            );
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "ClassModel{" +
                "name='" + name + '\'' +
                ", defaultConstructor=" + defaultConstructor +
                ", annotation=" + annotation +
                ", dependencies=" + dependencies +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ClassModel) {
            ClassModel other = (ClassModel) o;
            return Objects.equals(this.name, other.name);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name);
    }
}
