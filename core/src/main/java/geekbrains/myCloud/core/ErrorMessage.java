package geekbrains.myCloud.core;

public class ErrorMessage implements CloudMessage{

    private ErrorType error;

    public ErrorMessage(ErrorType error) {
        this.error = error;
    }
    public ErrorType getError() {
        return error;
    }

    @Override
    public CommandType getType() {
        return CommandType.ERROR;
    }
}
