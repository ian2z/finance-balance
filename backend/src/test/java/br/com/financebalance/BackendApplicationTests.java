package br.com.financebalance;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "app.jwt.secret=segredo-de-teste-com-mais-de-32-caracteres-ok")
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
