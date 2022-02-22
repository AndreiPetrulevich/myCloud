package geekbrains.myCloud.core;

public class GoToDir extends AuthorizedCloudMessage {

    private final String directory;


    public GoToDir(String token, String directory) {
        this.token = token;
        this.directory = directory;
    }

    public String getDirectory() {
        return directory;
    }

    @Override
    public CommandType getType() {
        return CommandType.GO_TO;
    }
}
