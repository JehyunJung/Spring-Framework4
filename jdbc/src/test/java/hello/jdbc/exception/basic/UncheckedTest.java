package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class UncheckedTest {
    @Test
    void checked_catch(){
        Service service = new Service();
        service.callCatch();
    }

    @Test
    void checked_throw(){
        Service service = new Service();
        service.callThrow();

    }

    static class MyUncheckedException extends RuntimeException{
        public MyUncheckedException(String message) {
            super(message);
        }
    }

    static class Service{
        Repository repository = new Repository();

        public void callCatch() {
            try {
                repository.call();
            } catch (MyUncheckedException exception) {
                log.info("예외 처리, message={}", exception.getMessage(), exception);
            }
        }

        public void callThrow(){
            repository.call();
        }


    }

    static class Repository{
        public void call(){
            throw new MyUncheckedException("ex");
        }
    }
}
