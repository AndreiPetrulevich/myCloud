package geekbrains.myCloud.core;

public class AuthenticationSuccess implements CloudMessage {

    private String token;

    public AuthenticationSuccess(String token) {
        this.token = token;
    }
    public String getToken() {
        return token;
    }


    @Override
    public CommandType getType() {
        return CommandType.AUTH_SUCCESS;
    }
}
