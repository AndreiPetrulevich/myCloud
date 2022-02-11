package geekbrains.myCloud.core;

import lombok.Data;

@Data
public class FileDelete implements CloudMessage{


    private final String pathToDeleteFile;

    public FileDelete(String pathToDeleteFile) {
        this.pathToDeleteFile = pathToDeleteFile;
    }

    public String getPathToDeleteFile() {
        return pathToDeleteFile;
    }

    @Override
    public CommandType getType() {
        return CommandType.DELETE;
    }
}
