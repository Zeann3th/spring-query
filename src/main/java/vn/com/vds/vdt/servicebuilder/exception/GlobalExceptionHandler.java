package vn.com.vds.vdt.servicebuilder.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.com.vds.vdt.servicebuilder.common.base.ApiResponse;
import vn.com.vds.vdt.servicebuilder.common.constants.ErrorCodes;


@RestControllerAdvice
@SuppressWarnings("all")
public class GlobalExceptionHandler {

    @ExceptionHandler(CommandException.class)
    public ApiResponse<Void> handleCommandException(CommandException ex) {
        return ApiResponse.error(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleGeneralException(Exception ex) {
        return ApiResponse.error(ErrorCodes.QS00003, ex.getMessage()); // System busy later, allow debug
    }
}

