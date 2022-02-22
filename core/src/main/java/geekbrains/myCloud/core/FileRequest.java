package geekbrains.myCloud.core;

import lombok.Data;

@Data
public class FileRequest extends AuthorizedCloudMessage {
    private final String fileName;

    public FileRequest(String token, String fileName) {
        this.token = token;
        this.fileName = fileName;
    }

    @Override
    public CommandType getType() {
        return CommandType.FILE_REQUEST;
    }
}
