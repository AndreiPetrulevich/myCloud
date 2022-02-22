package geekbrains.myCloud.core;

import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Data
public class FileUploadMessage extends AuthorizedCloudMessage {
    private final String fileName;
    private final byte[] bytes;
    private final String serverDir;

    public FileUploadMessage(String token, Path path, String serverDir) throws IOException {
        this.token = token;
        fileName = path.getFileName().toString();
        bytes = Files.readAllBytes(path);
        this.serverDir = serverDir;
    }
    public String getFileName() {
        return fileName;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public String getServerDir() {
        return serverDir;
    }

    @Override
    public CommandType getType() {
        return CommandType.FILE_UPLOAD;
    }
}
