package org.project.cloudfilestorage.handler;

import jakarta.validation.ConstraintViolationException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import org.project.cloudfilestorage.dto.exception.ErrorResponseDto;
import org.project.cloudfilestorage.exception.FileSizeLimitExceededException;
import org.project.cloudfilestorage.exception.FolderAlreadyExistsException;
import org.project.cloudfilestorage.exception.FolderNotFoundException;
import org.project.cloudfilestorage.exception.InvalidPasswordException;
import org.project.cloudfilestorage.exception.InvalidPathException;
import org.project.cloudfilestorage.exception.ResourceAlreadyExistsException;
import org.project.cloudfilestorage.exception.ResourceNotFoundException;
import org.project.cloudfilestorage.exception.UserAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ResponseStatus(HttpStatus.CONFLICT)
  @ExceptionHandler({
      UserAlreadyExistsException.class,
      FolderAlreadyExistsException.class,
      ResourceAlreadyExistsException.class
  })
  public ErrorResponseDto handleUsernameNotFoundException(Exception e) {
    return new ErrorResponseDto(e.getMessage());
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(ConstraintViolationException.class)
  public ErrorResponseDto handleConstraintViolationException(ConstraintViolationException ex) {
    String combinedMessage = ex.getConstraintViolations()
        .stream()
        .map(cv -> cv.getMessage())
        .reduce((s1, s2) -> s1 + "; " + s2)
        .orElse("Validation error");
    return new ErrorResponseDto(combinedMessage);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ErrorResponseDto handleMethodArgumentNotValidException(
      MethodArgumentNotValidException ex) {
    String combinedMessage = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(err -> err.getField() + ": " + err.getDefaultMessage())
        .reduce((s1, s2) -> s1 + "; " + s2)
        .orElse("Validation error");
    return new ErrorResponseDto(combinedMessage);
  }


  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  @ExceptionHandler({UsernameNotFoundException.class, InvalidPasswordException.class})
  public ErrorResponseDto handleAuthenticationExceptionException(Exception e) {
    return new ErrorResponseDto(e.getMessage());
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler({IllegalArgumentException.class, InvalidPathException.class})
  public ErrorResponseDto handleIllegalArgumentException(Exception e) {
    return new ErrorResponseDto(e.getMessage());
  }

  @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ErrorResponseDto handleMaxSizeException(
      MaxUploadSizeExceededException ex) {
    return new ErrorResponseDto("Превышен максимальный размер всех загружаемых файлов 50MB.");
  }

  @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
  @ExceptionHandler(FileSizeLimitExceededException.class)
  public ErrorResponseDto handleMaxUploadSizeExceededException(FileSizeLimitExceededException e) {
    return new ErrorResponseDto("Файл слишком большой. Допустимый размер — не более 20MB.");
  }

  @ResponseBody
  @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
  public String handleHttpMediaTypeNotAcceptableException() {
    return "acceptable MIME type:" + MediaType.APPLICATION_JSON_VALUE;
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<String> handleUnauthorized(Exception e) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Пользователь не авторизован");
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler({
      NoSuchElementException.class,
      FolderNotFoundException.class,
      ResourceNotFoundException.class})
  public ErrorResponseDto handleNotFound(Exception e) {
    return new ErrorResponseDto(e.getMessage());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<String> handleOtherExceptions(Exception e) {
    if (e instanceof ResponseStatusException rse) {
      throw rse;
    }
    StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw));
    String stacktrace = sw.toString();
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(stacktrace);
  }
}
