package com.a6raywa1cher.pasttyspring.rest;

import com.a6raywa1cher.pasttyspring.components.coderunner.CodeRunner;
import com.a6raywa1cher.pasttyspring.components.coderunner.CodeRunnerRequest;
import com.a6raywa1cher.pasttyspring.configs.AppConfig;
import com.a6raywa1cher.pasttyspring.configs.security.TokenUser;
import com.a6raywa1cher.pasttyspring.dao.interfaces.ScriptService;
import com.a6raywa1cher.pasttyspring.dao.interfaces.UserService;
import com.a6raywa1cher.pasttyspring.models.Script;
import com.a6raywa1cher.pasttyspring.models.User;
import com.a6raywa1cher.pasttyspring.models.enums.Role;
import com.a6raywa1cher.pasttyspring.models.enums.ScriptType;
import com.a6raywa1cher.pasttyspring.rest.dto.exceptions.*;
import com.a6raywa1cher.pasttyspring.rest.dto.mirror.ScriptMirror;
import com.a6raywa1cher.pasttyspring.rest.dto.request.*;
import com.a6raywa1cher.pasttyspring.rest.dto.response.ExecutionScriptResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Controller
@RequestMapping("/script")
public class ScriptController {
	private ScriptService scriptService;
	private AppConfig appConfig;
	private UserService userService;
	private CodeRunner codeRunner;

	@Autowired
	public ScriptController(ScriptService scriptService, AppConfig appConfig, UserService userService,
	                        CodeRunner codeRunner) {
		this.scriptService = scriptService;
		this.appConfig = appConfig;
		this.userService = userService;
		this.codeRunner = codeRunner;
	}

	public static String generateRandomName() {
		final String characters = "abcdefghijklmnopqrstuvwxyz0123456789_-";
		Random random = new Random();
		char[] text = new char[6 + random.nextInt(24 - 6)];
		for (int i = 0; i < text.length; i++) {
			text[i] = characters.charAt(random.nextInt(characters.length()));
		}
		return new String(text);
	}

	@PostMapping("/upload")
	@Transactional(rollbackOn = IOException.class)
	public ResponseEntity<ScriptMirror> upload(@RequestBody @Valid UploadScriptDTO dto,
	                                           Authentication authentication) throws IOException {
		boolean isAnonymous = authentication == null;
		if (scriptService.findByName(dto.getName()).isPresent()) {
			throw new ScriptWithProvidedNameExistsException();
		}
		Script script = new Script();
		if (!isAnonymous) {
			script.setAuthor(userService.findFromAuthentication(authentication).orElseThrow());
		}
		script.setCreationTime(LocalDateTime.now());
		script.setName(dto.getName());
		script.setTitle(dto.getTitle());
		script.setDescription(dto.getDescription());
		script.setDialect(dto.getDialect());
		script.setMaxComputeTime(null);
		if (isAnonymous && !dto.isVisible()) {
			throw new NonPublicScriptUploadedByAnonymousException();
		}
		script.setVisible(dto.isVisible());
		script.setType(ScriptType.SCRIPT);

		String uuid = UUID.randomUUID().toString();
		Path segment = Paths.get(uuid.substring(0, 2), uuid.substring(2, 4), uuid.substring(4, 6),
				dto.getName());
		Path path = Paths.get(appConfig.getScriptsFolder(), segment.toString());

		script.setPathToFile(segment.toString());
		Script putted = scriptService.save(script);
		Files.createDirectories(path.getParent());
		try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(path.toFile()))) {
			writer.append(dto.getCode());
		}
		return ResponseEntity.ok(ScriptMirror.convert(putted));
	}

	private Pageable filterPageable(Pageable pageable) {
		if (pageable.getPageSize() > 150) {
			pageable = PageRequest.of(pageable.getPageNumber(), 150, pageable.getSort());
		}
		if (pageable.getSort().getOrderFor("maxComputeTime") != null) {
			pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
					Sort.by("creationTime").descending());
		}
		return pageable;
	}

	@GetMapping("")
	@Transactional
	public ResponseEntity<List<ScriptMirror>> getPage(
			@PageableDefault(sort = "creationTime", direction = Sort.Direction.DESC)
					Pageable pageable, Authentication authentication) {
		Pageable filtered = filterPageable(pageable);
		Page<ScriptMirror> page;
		Optional<User> user = userService.findFromAuthentication(authentication);
		if (user.isEmpty()) {
			page = scriptService.getList(filtered).map(ScriptMirror::convert);
		} else {
			page = scriptService.findAllByVisibleTrueOrAuthor(user.get(), filtered)
					.map(ScriptMirror::convert);
		}
		return ResponseEntity.ok(page.getContent());
	}

	@GetMapping("/u/{username}")
	@Transactional
	public ResponseEntity<List<ScriptMirror>> getPageByUsername(
			@PageableDefault(sort = "creationTime", direction = Sort.Direction.DESC)
					Pageable pageable, @PathVariable String username, Authentication authentication
	) {
		Pageable filtered = filterPageable(pageable);
		TokenUser tokenUser = authentication != null ? (TokenUser) authentication.getPrincipal() : null;
		if (tokenUser != null && tokenUser.getUsername().equals(username)) {
			return ResponseEntity.ok(
					scriptService.findAllByAuthor(userService.findFromTokenUser(tokenUser), filtered)
							.map(ScriptMirror::convert).getContent()
			);
		} else {
			Optional<User> optionalUser = userService.findByUsername(username);
			if (optionalUser.isEmpty()) {
				return ResponseEntity.notFound().build();
			}
			return ResponseEntity.ok(
					scriptService.findAllByVisibleTrueAndAuthor(optionalUser.get(), filtered)
							.map(ScriptMirror::convert).getContent()
			);
		}
	}

	@GetMapping("/s/{name}")
	public ResponseEntity<ScriptMirror> get(
			@PathVariable @Pattern(regexp = ControllerValidations.SCRIPT_NAME_REGEX)
					String name, Authentication authentication
	) throws IOException {
		Optional<Script> optionalScript = scriptService.findByName(name);
		if (optionalScript.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		Script script = optionalScript.get();
		if (!script.isVisible() && (authentication == null ||
				!(userService.findFromTokenUser((TokenUser) authentication.getPrincipal())).getId().equals(script.getAuthor().getId()))) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		ScriptMirror scriptMirror = ScriptMirror.convert(script);
		Path path = Paths.get(appConfig.getScriptsFolder(), script.getPathToFile());
		System.out.println(path.toAbsolutePath());
		try (FileInputStream stream = new FileInputStream(path.toFile())) {
			String code = new String(stream.readAllBytes());
			scriptMirror.setCode(code);
		}
		return ResponseEntity.ok().body(scriptMirror);
	}

	@PostMapping("/s/{name}/change_type")
	@PreAuthorize("hasAuthority(T(com.a6raywa1cher.pasttyspring.models.enums.Role).USER.name())")
	public ResponseEntity<ScriptMirror> changeType(@RequestBody @Valid ChangeTypeDTO dto,
	                                               @PathVariable String name,
	                                               Authentication authentication) {
		Optional<Script> optionalScript = scriptService.findByName(name);
		if (optionalScript.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		Script script = optionalScript.get();
		boolean isModerator = authentication.getAuthorities().contains(Role.MODERATOR);
		if (!isModerator && (!script.getAuthor().equals(userService.findFromTokenUser((TokenUser) authentication.getPrincipal())) || !script.isVisible())) {
			throw new NoEnoughRightsForChangeException();
		}
		if (!isModerator) {
			Set<Pair<ScriptType, ScriptType>> permitted = new HashSet<>();
			permitted.add(Pair.of(ScriptType.SCRIPT, ScriptType.REQUESTED_MODERATION));
			permitted.add(Pair.of(ScriptType.REQUESTED_MODERATION, ScriptType.SCRIPT));
			permitted.add(Pair.of(ScriptType.EXEC_SCRIPT, ScriptType.SCRIPT));
			ScriptType from = script.getType();
			ScriptType to = dto.getType();
			if (!permitted.contains(Pair.of(from, to))) {
				throw new NoEnoughRightsForChangeException();
			}
		}
		script.setType(dto.getType());
		return ResponseEntity.ok(ScriptMirror.convert(scriptService.save(script)));
	}

	@PostMapping("/s/{name}/exec")
	@PreAuthorize("hasAuthority(T(com.a6raywa1cher.pasttyspring.models.enums.Role).USER.name())")
	public CompletionStage<ResponseEntity<ExecutionScriptResponse>> exec(@RequestBody @Valid ExecuteScriptDTO dto,
	                                                                     @PathVariable String name,
	                                                                     Authentication authentication) {
		Optional<Script> optionalScript = scriptService.findByName(name);
		if (optionalScript.isEmpty()) {
			return CompletableFuture.completedFuture(ResponseEntity.notFound().build());
//			return CompletableFuture.supplyAsync(() -> ResponseEntity.notFound().build(), new SimpleAsyncTaskExecutor());
		}
		Script script = optionalScript.get();
		boolean isModerator = authentication.getAuthorities().contains(Role.MODERATOR);
		if (!script.getType().equals(ScriptType.EXEC_SCRIPT) && !isModerator) {
			throw new NotExecScriptException();
		}
		CodeRunnerRequest request = new CodeRunnerRequest(script, dto.getStdin());
		return codeRunner.execTask(request)
				.thenApply(response -> {
					ExecutionScriptResponse scriptResponse = new ExecutionScriptResponse();
					scriptResponse.setExitCode(response.getExitCode());
					scriptResponse.setStdout(response.getStdout());
					return ResponseEntity.ok(scriptResponse);
				})
				.exceptionally(throwable -> {
					ExecutionScriptResponse scriptResponse = new ExecutionScriptResponse();
					scriptResponse.setExitCode(-1);
					scriptResponse.setStdout(throwable.getMessage());
					return ResponseEntity.ok(scriptResponse);
				});
	}

	@SuppressWarnings("DuplicatedCode")
	@Transactional(rollbackOn = IOException.class)
	@PatchMapping("/s/{name}")
	@PreAuthorize("hasAuthority(T(com.a6raywa1cher.pasttyspring.models.enums.Role).USER.name())")
	public ResponseEntity<ScriptMirror> patch(@RequestBody @Valid PatchScriptDTO dto,
	                                          @PathVariable @Pattern(regexp = ControllerValidations.SCRIPT_NAME_REGEX)
	                                          @Valid String name, Authentication authentication)
			throws IOException {
		Optional<Script> optionalScript = scriptService.findByName(name);
		if (optionalScript.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		Script script = optionalScript.get();
		Optional<User> requester = userService.findFromAuthentication(authentication);
		if (requester.isEmpty() || script.getAuthor() == null || !requester.get().equals(script.getAuthor())) {
			throw new EditingNotYourScriptException();
		}
		if (dto.getName() != null && !script.getName().equals(dto.getName())) {
			Optional<Script> check = scriptService.findByName(dto.getName());
			if (check.isPresent()) {
				throw new ScriptWithProvidedNameExistsException();
			}
		}
		dto.patch(script);
		Script patched = scriptService.save(script);
		ScriptMirror response = ScriptMirror.convert(patched);
		Path path = Path.of(appConfig.getScriptsFolder(), patched.getPathToFile());
		if (dto.getCode() != null) {
			try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(path.toFile()))) {
				writer.append(dto.getCode());
			}
			response.setCode(dto.getCode());
		} else {
			try (FileInputStream stream = new FileInputStream(path.toFile())) {
				String code = new String(stream.readAllBytes());
				response.setCode(code);
			}
		}
		return ResponseEntity.ok(response);
	}

	@SuppressWarnings("DuplicatedCode")
	@Transactional(rollbackOn = IOException.class)
	@PutMapping("/s/{name}")
	@PreAuthorize("hasAuthority(T(com.a6raywa1cher.pasttyspring.models.enums.Role).USER.name())")
	public ResponseEntity<ScriptMirror> put(@RequestBody @Valid PutScriptDTO dto,
	                                        @PathVariable @Pattern(regexp = ControllerValidations.SCRIPT_NAME_REGEX)
	                                        @Valid String name, Authentication authentication)
			throws IOException {
		Optional<Script> optionalScript = scriptService.findByName(name);
		if (optionalScript.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		Script script = optionalScript.get();
		Optional<User> requester = userService.findFromAuthentication(authentication);
		if (requester.isEmpty() || script.getAuthor() == null || !requester.get().equals(script.getAuthor())) {
			throw new EditingNotYourScriptException();
		}
		if (!script.getName().equals(dto.getName())) {
			Optional<Script> check = scriptService.findByName(dto.getName());
			if (check.isPresent()) {
				throw new ScriptWithProvidedNameExistsException();
			}
		}
		dto.put(script);
		Script patched = scriptService.save(script);
		ScriptMirror response = ScriptMirror.convert(patched);
		Path path = Path.of(appConfig.getScriptsFolder(), patched.getPathToFile());
		try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(path.toFile()))) {
			writer.append(dto.getCode());
		}
		response.setCode(dto.getCode());
		return ResponseEntity.ok(response);
	}

	@Transactional(rollbackOn = IOException.class)
	@DeleteMapping("/s/{name}")
	@PreAuthorize("hasAuthority(T(com.a6raywa1cher.pasttyspring.models.enums.Role).USER.name())")
	public ResponseEntity<?> delete(@PathVariable String name, Authentication authentication)
			throws IOException {
		Optional<Script> optionalScript = scriptService.findByName(name);
		if (optionalScript.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		Script script = optionalScript.get();
		User user = userService.findFromAuthentication(authentication).orElseThrow();
		if (script.getAuthor() == null) {
			if (!user.getRole().getTree().contains(Role.MODERATOR)) {
				throw new NoEnoughRightsForChangeException();
			}
		} else if (!script.isVisible()) {
			if (!user.getRole().getTree().contains(Role.ADMIN)) {
				throw new NoEnoughRightsForChangeException();
			}
		} else {
			if (!script.getAuthor().equals(user) && !user.getRole().getTree().contains(Role.MODERATOR)) {
				throw new NoEnoughRightsForChangeException();
			}
		}
		scriptService.delete(script);
		Path path = Path.of(appConfig.getScriptsFolder(), script.getPathToFile());
		Files.delete(path);
		return ResponseEntity.ok().build();
	}
}
