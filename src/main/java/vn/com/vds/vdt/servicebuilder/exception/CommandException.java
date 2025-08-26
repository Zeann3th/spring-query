package vn.com.vds.vdt.servicebuilder.exception;

import lombok.Getter;

@Getter
@SuppressWarnings("all")
public class CommandException extends RuntimeException {
    private final String code;

    public CommandException(String code, String message) {
        super(message);
        this.code = code;
    }
}
