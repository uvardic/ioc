package ioc.exception;


import org.apache.log4j.Logger;

public class IOCStateException extends IllegalStateException {

    private static final Logger logger = Logger.getLogger(IOCStateException.class);

    public IOCStateException() {
        super();
    }

    public IOCStateException(String s) {
        super(s);
        logger.error(s);
    }

}
