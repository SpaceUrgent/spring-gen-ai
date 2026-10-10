package io.spaceurgent.gen.ai.shared.api;

import io.spaceurgent.gen.ai.image.application.exception.IllustrationNotPossibleException;
import io.spaceurgent.gen.ai.image.application.exception.SpecGenerationException;

import io.spaceurgent.gen.ai.shared.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    ProblemDetail handleBadRequestException(BadRequestException ex) {
        log.warn(ex.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler
    ProblemDetail illustrationNotPossible(IllustrationNotPossibleException e) {
        log.info("Illustration not possible: {}", e.getReason());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
        problem.setProperty("reason", e.getReason());
        return problem;
    }

    @ExceptionHandler
    ProblemDetail specGenerationFailed(SpecGenerationException e) {
        log.error(e.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, e.getMessage());
    }
}
