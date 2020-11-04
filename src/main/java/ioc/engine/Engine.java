package ioc.engine;

import ioc.annotations.*;
import ioc.exception.IOCStateException;
import org.apache.log4j.Logger;

import java.lang.annotation.Annotation;
import java.text.SimpleDateFormat;
import java.util.*;

public class Engine {

    static {
        SimpleDateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
        System.setProperty("current.date.time", logDateFormat.format(new Date()));
    }

    private static final Logger logger = Logger.getLogger(Engine.class);

    public static final Set<Class<?>> relevantAnnotations = new HashSet<>();

    static {
        relevantAnnotations.add(Autowired.class);
        relevantAnnotations.add(Bean.class);
        relevantAnnotations.add(Component.class);
        relevantAnnotations.add(Qualifier.class);
        relevantAnnotations.add(Service.class);
    }

    static boolean isAnnotationRelevant(Annotation annotation) {
        return relevantAnnotations.contains(annotation.annotationType());
    }

    private static Set<ClassModel> mappedClasses;

    static ClassModel findClassModelFor(Class<?> aClass) {
        return mappedClasses.stream()
                .filter(classModel -> classModel.getName().equals(aClass.getName()))
                .findFirst()
                .orElseThrow(() -> new IOCStateException(
                        String.format("Class model for class: %s wasn't found!", aClass.getName())
                ));
    }

    public static <T> T start(Class<T> referenceClass) {
        logger.info("Starting...");
        Set<Class<?>> loadedClasses = Loader.loadAllClasses(referenceClass);
        Set<Class<?>> filteredClasses = Filter.relevantFilter(loadedClasses);
        mappedClasses = Mapper.mapToModel(filteredClasses);
        startWorker();
        return referenceClass.cast(findReferenceClassInstance(referenceClass));
    }

    private static Object findReferenceClassInstance(Class<?> referenceClass) {
        return DependencySupplier.getResolvedClasses()
                .stream()
                .filter(instance -> instance.getClass().equals(referenceClass))
                .findFirst()
                .orElseThrow(() -> new IOCStateException("Reference class instance not found!"));
    }

    private static final int depthLimit = 10000;

    private static void startWorker() {
        Queue<ClassModel> queuedClasses = new LinkedList<>(mappedClasses);

        int depth = 0;
        while (!queuedClasses.isEmpty()) {
            if (depth == depthLimit)
                throw new IOCStateException("Fatal engine failure! Check you shit!");

            ClassModel dequeuedModel = queuedClasses.remove();

            if (dequeuedModel.isResolved())
                dequeuedModel.resolveClass();
            else {
                queuedClasses.add(dequeuedModel);
                depth++;
            }
        }
    }

}
