package com.a6raywa1cher.pasttyspring.rest;

import com.a6raywa1cher.pasttyspring.dao.interfaces.CommentService;
import com.a6raywa1cher.pasttyspring.dao.interfaces.ScriptService;
import com.a6raywa1cher.pasttyspring.dao.interfaces.UserService;
import com.a6raywa1cher.pasttyspring.models.Comment;
import com.a6raywa1cher.pasttyspring.models.Script;
import com.a6raywa1cher.pasttyspring.models.User;
import com.a6raywa1cher.pasttyspring.models.enums.Role;
import com.a6raywa1cher.pasttyspring.rest.dto.exceptions.EditingNotYourCommentException;
import com.a6raywa1cher.pasttyspring.rest.dto.mirror.CommentMirror;
import com.a6raywa1cher.pasttyspring.rest.dto.request.PutCommentDTO;
import com.a6raywa1cher.pasttyspring.rest.dto.request.UploadCommentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/comment")
public class CommentController {
	private ScriptService scriptService;
	private CommentService commentService;
	private UserService userService;

	@Autowired
	public CommentController(ScriptService scriptService, CommentService commentService, UserService userService) {
		this.scriptService = scriptService;
		this.commentService = commentService;
		this.userService = userService;
	}

	@Transactional
	@GetMapping("/{script_name}")
	public ResponseEntity<List<CommentMirror>> get(@PathVariable("script_name") String scriptName) {
		Optional<Script> optionalScript = scriptService.findByName(scriptName);
		if (optionalScript.isEmpty() || !optionalScript.get().isVisible()) {
			return ResponseEntity.notFound().build();
		}
		Script script = optionalScript.get();
		List<Comment> comments = commentService.getByScript(script);
		List<CommentMirror> commentsTopLevel = comments.stream()
				.filter(comment -> comment.getUpperLevel() == null)
				.map(comment -> CommentMirror.convert(comment, false, true, true))
				.collect(Collectors.toList());
		return ResponseEntity.ok(commentsTopLevel);
	}

	@Transactional
	@GetMapping("/{script_name}/{comment_id}")
	public ResponseEntity<CommentMirror> getComment(@PathVariable("script_name") String scriptName,
	                                                @PathVariable("comment_id") long commentId,
	                                                @RequestParam(value = "lower", required = false, defaultValue = "false")
			                                                boolean lower,
	                                                @RequestParam(value = "all_lower", required = false, defaultValue = "false")
			                                                boolean allLower) {
		Optional<Script> optionalScript = scriptService.findByName(scriptName);
		if (optionalScript.isEmpty() || !optionalScript.get().isVisible()) {
			return ResponseEntity.notFound().build();
		}
		Script script = optionalScript.get();
		Optional<Comment> optionalComment = commentService.getByScriptAndId(script, commentId);
		if (optionalComment.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		Comment comment = optionalComment.get();
		return ResponseEntity.ok(CommentMirror.convert(comment, false, lower, allLower));
	}

	@Transactional
	@PostMapping("/{script_name}")
	@PreAuthorize("hasAuthority(T(com.a6raywa1cher.pasttyspring.models.enums.Role).USER.name())")
	public ResponseEntity<CommentMirror> upload(@RequestBody @Valid UploadCommentDTO dto,
	                                            @PathVariable("script_name") String scriptName,
	                                            Authentication authentication) {
		Optional<Script> optionalScript = scriptService.findByName(scriptName);
		if (optionalScript.isEmpty() || !optionalScript.get().isVisible()) {
			return ResponseEntity.notFound().build();
		}
		Script script = optionalScript.get();
		User user = userService.findFromAuthentication(authentication).orElseThrow();
		Comment comment = new Comment();
		comment.setBody(dto.getBody());
		comment.setCreationTime(LocalDateTime.now());
		comment.setEdited(false);
		comment.setLowerLevel(Collections.emptyList());
		comment.setScript(script);
		if (dto.getParentId() != null) {
			Optional<Comment> optionalUpper = commentService.getByScriptAndId(script, dto.getParentId());
			if (optionalUpper.isEmpty()) {
				return ResponseEntity.notFound().build();
			}
			comment.setUpperLevel(optionalUpper.get());
		} else {
			comment.setUpperLevel(null);
		}
		comment.setUser(user);
		return ResponseEntity.ok(CommentMirror.convert(commentService.save(comment)));
	}

	@Transactional
	@PutMapping("/{script_name}/{comment_id}")
	@PreAuthorize("hasAuthority(T(com.a6raywa1cher.pasttyspring.models.enums.Role).USER.name())")
	public ResponseEntity<CommentMirror> put(@RequestBody @Valid PutCommentDTO dto,
	                                         @PathVariable("script_name") String scriptName,
	                                         @PathVariable("comment_id") long commentId,
	                                         Authentication authentication) {
		Optional<Script> optionalScript = scriptService.findByName(scriptName);
		if (optionalScript.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		Script script = optionalScript.get();
		Optional<Comment> optionalComment = commentService.getByScriptAndId(script, commentId);
		if (optionalComment.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		Comment comment = optionalComment.get();
		User user = userService.findFromAuthentication(authentication).orElseThrow();
		if (!Objects.equals(comment.getUser(), user)) {
			throw new EditingNotYourCommentException();
		}
		comment.setEdited(true);
		comment.setBody(dto.getBody());
		return ResponseEntity.ok(CommentMirror.convert(commentService.save(comment)));
	}

	@Transactional
	@DeleteMapping("/{script_name}/{comment_id}")
	@PreAuthorize("hasAuthority(T(com.a6raywa1cher.pasttyspring.models.enums.Role).USER.name())")
	public ResponseEntity<?> delete(@PathVariable("script_name") String scriptName,
	                                @PathVariable("comment_id") long commentId,
	                                Authentication authentication) {
		Optional<Script> optionalScript = scriptService.findByName(scriptName);
		if (optionalScript.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		Script script = optionalScript.get();
		Optional<Comment> optionalComment = commentService.getByScriptAndId(script, commentId);
		if (optionalComment.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		Comment comment = optionalComment.get();
		User user = userService.findFromAuthentication(authentication).orElseThrow();
		if (!Objects.equals(comment.getUser(), user) && !user.getRole().getTree().contains(Role.MODERATOR)) {
			throw new EditingNotYourCommentException();
		}
		commentService.delete(comment);
		return ResponseEntity.ok().build();
	}
}
