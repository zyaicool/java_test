package id.customer.core.response;



import id.customer.core.models.Result;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolationException;
import java.util.*;
import java.util.stream.Collectors;
@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {
    private Result result;

//    @ExceptionHandler(Exception.class)
//    public final ResponseEntity<Result> handleAllExceptions(Exception ex, WebRequest request) {
////        ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(),
////                request.getDescription(false));
//
//        result = new Result();
//        result.setCode(500);
//        result.setMessage(ex.getMessage());
//        result.setSuccess(false);
//        result.setData(request.getDescription(true));
//        return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
//    }

    @ExceptionHandler(javax.validation.ConstraintViolationException.class)
    public ResponseEntity<Result> inputValidationException(ConstraintViolationException ex, WebRequest request) {

        List<String> errors = new ArrayList<>();

        ex.getConstraintViolations().forEach(cv -> errors.add(cv.getMessage()));

        result = new Result();
        result.setCode(400);
        result.setMessage("");
        result.setSuccess(false);
        result.setData(errors);
        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);

    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        List<String> errors = new ArrayList<>();
        result = new Result();
        result.setCode(400);
        result.setMessage(ex.getMessage());
        result.setSuccess(false);
        result.setData(errors);
        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("timestamp", new Date());
        responseBody.put("status", status.value());

        List<String> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(x -> x.getDefaultMessage())
                .collect(Collectors.toList());

        responseBody.put("errors", errors);

        result = new Result();
        result.setCode(status.value());
        result.setMessage("");
        result.setSuccess(false);
        result.setData(errors);

        return new ResponseEntity<>(result, headers, status);
    }
}
