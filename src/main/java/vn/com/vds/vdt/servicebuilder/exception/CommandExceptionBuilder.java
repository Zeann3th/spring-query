package vn.com.vds.vdt.servicebuilder.exception;

@SuppressWarnings("all")
public class CommandExceptionBuilder {
    public static CommandException exception(String code, String message) {
        throw new CommandException(code, message);
    }

    public static CommandException exception(String code) {
        throw new CommandException(code, "Unknown error mapping for code: " + code);
    }
}
