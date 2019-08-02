package com.a6raywa1cher.pasttyspring.rest;

import com.a6raywa1cher.pasttyspring.configs.AppConfig;
import com.a6raywa1cher.pasttyspring.dao.interfaces.ScriptService;
import com.a6raywa1cher.pasttyspring.models.Script;
import com.a6raywa1cher.pasttyspring.models.User;
import com.a6raywa1cher.pasttyspring.models.enums.ScriptType;
import com.a6raywa1cher.pasttyspring.rest.dto.exceptions.NonPublicScriptUploadedByAnonymousException;
import com.a6raywa1cher.pasttyspring.rest.dto.exceptions.ScriptWithProvidedNameExistsException;
import com.a6raywa1cher.pasttyspring.rest.dto.mirror.ScriptMirror;
import com.a6raywa1cher.pasttyspring.rest.dto.request.UploadScriptDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Controller
@RequestMapping("/script")
public class ScriptController {
	private ScriptService scriptService;
	private AppConfig appConfig;

	@Autowired
	public ScriptController(ScriptService scriptService, AppConfig appConfig) {
		this.scriptService = scriptService;
		this.appConfig = appConfig;
	}

	public static String generateRandomName() {
		String characters = "abcdefghijklmnopqrstuvwxyz0123456789_-";
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
			script.setAuthor((User) authentication.getPrincipal());
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

	@GetMapping("/")
	public ResponseEntity<List<ScriptMirror>> getPage(
			@PageableDefault(sort = "creationTime", direction = Sort.Direction.DESC)
					Pageable pageable) {
		Page<ScriptMirror> page = scriptService.getList(pageable).map(ScriptMirror::convert);
		return ResponseEntity.ok(page.getContent());
	}
}
