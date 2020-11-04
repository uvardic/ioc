package ioc.test;

import ioc.annotations.*;
import ioc.engine.Engine;

@Bean
class Main {

    @Autowired(verbose = true)
    private Class1 k;

    @Autowired(verbose = true)
    private Class2 k2;

    public void print() {
        k.print();
        k2.print();
    }

    public static void main(String[] args) {
        Main main = Engine.start(Main.class);
        main.print();
    }

}

@Service
class Class1 {

    @Autowired
    private Class2 k;

    @Autowired
    private Class3 k3;

    public void print() {
        System.err.println("CLsa 1");
    }

}

@Bean(scope = Bean.Type.SINGLETON)
class Class2 {

    @Autowired
    private Class3 k3;

    public void print() {
        k3.print();
    }

}

@Component
//@Bean(scope = Bean.Type.PROTOTYPE)
class Class3 {

    @Qualifier("class5")
    private Interface1 interface1;

    public void print() {
        interface1.printInf();
        System.err.println(this);
    }

}

interface Interface1 {

    void printInf();

}

@Qualifier("class4")
class Class4 implements Interface1 {

    @Override
    public void printInf() {
        System.err.println("E TAJ SAM klasa4");
    }
}

@Qualifier("class5")
class Class5 implements Interface1 {

    @Override
    public void printInf() {
        System.err.println("E Taj sam klasa5");
    }

}
