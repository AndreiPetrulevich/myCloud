package geekbrains.myCloud.core;

public class SignUp implements CloudMessage {
    private final String login;
    private final String password;

    public SignUp(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public String getLogin() {
        return login;
    }

    @Override
    public CommandType getType() {
        return CommandType.REG;
    }
}
