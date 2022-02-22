package geekbrains.myCloud.core;

import lombok.Data;

@Data
public class FileDelete extends AuthorizedCloudMessage{
    private final String pathToDeleteFile;

    public FileDelete(String token, String pathToDeleteFile) {
        this.token = token;
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
