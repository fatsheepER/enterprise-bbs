package com.feiyang.bbs.config;

import com.feiyang.bbs.common.BusinessException;
import com.feiyang.bbs.common.ErrorCode;
import com.feiyang.bbs.common.Result;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException ex) {
        return Result.fail(ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            ConstraintViolationException.class,
            HttpMessageNotReadableException.class
    })
    public Result<Void> handleParameterException(Exception ex) {
        String message = resolveParameterMessage(ex);
        return Result.fail(ErrorCode.PARAM_ERROR, message);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Void> handleMaxUploadSizeException(MaxUploadSizeExceededException ex) {
        return Result.fail(ErrorCode.PARAM_ERROR, "头像文件不能超过 2MB");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception ex) {
        log.error("Unexpected server error", ex);
        return Result.fail(ErrorCode.INTERNAL_ERROR);
    }

    private String resolveParameterMessage(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException validException
                && validException.getBindingResult().hasFieldErrors()) {
            return validException.getBindingResult().getFieldError().getDefaultMessage();
        }
        if (ex instanceof BindException bindException
                && bindException.getBindingResult().hasFieldErrors()) {
            return bindException.getBindingResult().getFieldError().getDefaultMessage();
        }
        if (ex instanceof ConstraintViolationException violationException
                && !violationException.getConstraintViolations().isEmpty()) {
            return violationException.getConstraintViolations().iterator().next().getMessage();
        }
        return ErrorCode.PARAM_ERROR.getMessage();
    }
}
