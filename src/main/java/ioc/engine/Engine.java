package ioc.engine;

import ioc.annotations.Autowired;
import ioc.annotations.Bean;
import ioc.annotations.Component;
import ioc.annotations.Service;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;


// todo Provera za interfejse preko Dependency supliera
// todo namestiti verbose kako treba
// todo Ocistiti ClassModel

public class Engine {

    private static final Loader loader = new Loader();

    private static final Filter filter = new Filter();

    private static final Mapper mapper = new Mapper();

    public static <T> T start(Class<T> referenceClass) {
        Set<Class<?>> loadedClasses = loader.loadAllClasses(referenceClass);
        Set<Class<?>> filteredClasses = filter.relevantFilter(loadedClasses);
        Set<ClassModel> mappedClasses = mapper.mapToModel(filteredClasses);
        DependencySupplier.initializeQualifierContext(mappedClasses);
        inject(new LinkedList<>(mappedClasses));
        return referenceClass.cast(findReferenceClassInstance(referenceClass));
    }

    private static final int iterationLimit = 10000;

    public static void inject(Queue<ClassModel> queuedClasses) {
        int count = 0;

        while (!queuedClasses.isEmpty()) {
            if (count == iterationLimit)
                throw new IllegalStateException("Cyclic dependencies found!");

            ClassModel dequeuedModel = queuedClasses.remove();

            if (dequeuedModel.isResolved())
                dequeuedModel.instantiate();
            else
                queuedClasses.add(dequeuedModel);

            count++;
        }
    }

    private static Object findReferenceClassInstance(Class<?> referenceClass) {
        return DependencySupplier.getResolvedClasses()
                .stream()
                .filter(instance -> instance.getClass().equals(referenceClass))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Reference class instance not found!"));
    }

}

@Bean
class Engine2 {

    @Autowired
    private Klasa1 k;

    @Autowired
    private Klasa2 k2;

    public void ispis() {
        k.ispisi();
        k2.ispisi();
    }

    public static void main(String[] args) {
        Engine2 e = Engine.start(Engine2.class);
        e.ispis();
    }

}

@Service
class Klasa1 {

    @Autowired
    private Klasa2 k;

    public void ispisi() {
        System.err.println("");
        System.err.println("");
        System.err.println("");
        System.err.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
    }

}

@Component
class Klasa2 {

    @Autowired
    private Klasa3 k3;

    public void ispisi() {
        k3.ispisi();
    }

}

@Service
class Klasa3 {

    public void ispisi() {
        System.err.println("");
        System.err.println("");
        System.err.println("");
        System.err.println("VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV");
    }

}
