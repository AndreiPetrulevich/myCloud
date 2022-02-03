package geekbrains.myCloud.core;

import java.nio.file.Path;

public class GoToDir implements CloudMessage{

    private final Path directory;


    public GoToDir(Path directory) {
        this.directory = directory;
    }

    public Path getDirectory() {
        return directory;
    }

    @Override
    public CommandType getType() {
        return CommandType.GO_TO;
    }
}
