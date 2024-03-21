package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class CheckedTest {

	@Test
	void checked_catch() {
		Service service = new Service();
		service.callCatch();
	}

	@Test
	void checked_throw() {
		Service service = new Service();
		Assertions.assertThatThrownBy(service::callThrow)
				.isInstanceOf(MyCheckedException.class);
	}

	/**
	 * Exception을 상속받은 예외는 체크 예외가 된다.
	 */
	static class MyCheckedException extends Exception {
		public MyCheckedException(String message) {
			super(message);
		}
	}

	/**
	 * Checked 예외는
	 * 예외를 잡아서 처리하거나, 던지거나 둘중 하나를 필수로 선택해야 한다.
	 */
	static class Service {
		Repository repository = new Repository();

		/**
		 * 에외를 잡아서 처리하는 코드
		 */
		public void callCatch() {
			try {
				repository.call();
			} catch (MyCheckedException e) {
				log.info("예외 처리, message = {}", e.getMessage(), e);
			}
		}

		/**
		 * 체크 예외를 밖으로 던지는 코드
		 * 체크 예외는 예외를 잡지 않고 밖으로 던지려면 throws 예외를 메서드에 필수로 선언해야한다.
		 * @throws MyCheckedException
		 */
		public void callThrow() throws MyCheckedException {
			repository.call();
		}
	}

	static class Repository {
		public void call() throws MyCheckedException {
			// 해당 예외를 여기에서 catch(잡을) 생각이 없다면 던져주어야한다.
			// check예외는 던져준다면 throws 로 선언을 해줘야한다.
			throw new MyCheckedException("ex");
		}
	}
}
