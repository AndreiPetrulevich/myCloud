package geekbrains.myCloud.core;

public class GoToDir implements CloudMessage {

    private final String directory;


    public GoToDir(String directory) {
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
