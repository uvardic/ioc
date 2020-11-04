package ioc.engine;

import ioc.annotations.Bean;
import ioc.annotations.Component;
import ioc.annotations.Qualifier;
import ioc.exception.IOCStateException;
import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toUnmodifiableSet;

class DependencySupplier {

    private static final Logger logger = Logger.getLogger(DependencySupplier.class);

    private DependencySupplier() {}

    private static final Map<String, Object> resolvedQualifiers = new HashMap<>();

    public static boolean isQualifierResolved(String key) {
        return resolvedQualifiers.containsKey(key);
    }

    public static void addResolvedQualifier(String key, Object instance) {
        if (isQualifierResolved(key))
            throw new IOCStateException(String.format("Key %s is already present!", key));

        logger.info(String.format("Adding %s to resolved qualifiers", instance.getClass().getName()));
        resolvedQualifiers.put(key, instance);
    }

    public static Object getQualifier(Qualifier qualifier) {
        return resolvedQualifiers.get(qualifier.value());
    }

    private static final Set<Object> resolvedClasses = new HashSet<>();

    public static void addResolvedClass(Object instance) {
        logger.info(String.format("Adding %s to resolved classes", instance.getClass().getName()));
        resolvedClasses.add(instance);
    }

    public static Object getResolverFor(Field dependency) {
        for (Object resolver : resolvedClasses) {
            if (!dependency.getType().isAssignableFrom(resolver.getClass()))
                continue;

            if (isPrototype(resolver)) {
                ClassModel resolverModel = Engine.findClassModelFor(resolver.getClass());
                return resolverModel.instantiate();
            }

            return resolver;
        }

        throw new IOCStateException(String.format("Injection for dependency: %s wasn't found!", dependency));
    }

    private static boolean isPrototype(Object resolver) {
        Bean bean = resolver.getClass().getDeclaredAnnotation(Bean.class);
        Component component = resolver.getClass().getDeclaredAnnotation(Component.class);
        return (bean != null && bean.scope().equals(Bean.Type.PROTOTYPE)) || component != null;
    }

    public static Set<Object> getResolvedClasses() {
        return resolvedClasses.stream().collect(toUnmodifiableSet());
    }
}
