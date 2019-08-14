package com.a6raywa1cher.pasttyspring;

import com.a6raywa1cher.pasttyspring.components.coderunner.CodeRunner;
import com.a6raywa1cher.pasttyspring.components.coderunner.CodeRunnerRequest;
import com.a6raywa1cher.pasttyspring.components.coderunner.CodeRunnerResponse;
import com.a6raywa1cher.pasttyspring.models.Script;
import com.a6raywa1cher.pasttyspring.models.enums.ScriptType;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
		"spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
		"app.scripts-folder=testsScriptsFolder"
//		"spring.main.allow-bean-definition-overriding=true"
})
//@TestPropertySource("classpath:appconfig.yml")
//@ContextConfiguration(classes = ExecScriptsTestConfig.class)
@AutoConfigureCache
@AutoConfigureDataJpa
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@ActiveProfiles("cr_test")
@Import(ExecScriptsTestConfig.class)
public class CodeRunnerTests {
	@Autowired
	private CodeRunner codeRunner;

	@Test
	public void execTest() throws Exception {
		Files.createDirectories(Path.of("testsScriptsFolder"));
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("testsScriptsFolder/javatest1")))) {
			writer.write(
					"import java.util.Scanner;\n\n" +
							"public class Main {\n\n" +
							"    public static void main(String[] args) {\n" +
							"        Scanner scanner = new Scanner(System.in);\n" +
							"        int a = scanner.nextInt();\n" +
							"        int b = scanner.nextInt();\n" +
							"        System.out.println(a + b);\n" +
							"    }\n" +
							"}\n");
		}
		Script script = new Script();
		script.setDialect("java");
		script.setType(ScriptType.EXEC_SCRIPT);
		script.setPathToFile("javatest1");
		script.setMaxComputeTime(1000L);

		CodeRunnerRequest request = new CodeRunnerRequest(script, "2\n3\n");
		CodeRunnerResponse response = codeRunner.execTask(request).get();
		Assert.assertEquals("5", response.getStdout());
		Assert.assertEquals(0, response.getExitCode());
		Assert.assertEquals(request, response.getRequest());
	}

	@Test
	public void infiniteLoopTest() throws Exception {
		Files.createDirectories(Path.of("testsScriptsFolder"));
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("testsScriptsFolder/javatest2")))) {
			writer.write(
					"import java.util.Scanner;\n\n" +
							"public class Main {\n\n" +
							"    public static void main(String[] args) {\n" +
							"        while(true) {}\n" +
							"    }\n" +
							"}\n");
		}
		Script script = new Script();
		script.setDialect("java");
		script.setType(ScriptType.EXEC_SCRIPT);
		script.setPathToFile("javatest2");
		script.setMaxComputeTime(1000L);

		CodeRunnerRequest request = new CodeRunnerRequest(script, "2\n3\n");
		try {
			codeRunner.execTask(request).get(5000, TimeUnit.MILLISECONDS);
		} catch (TimeoutException time) {
			Assert.fail();
		} catch (ExecutionException ce) {
			System.out.println("Test passed.");
		}
	}

	@After
	public void teardown() throws IOException {
		FileUtils.deleteDirectory(new File("testsScriptsFolder"));
	}
}
