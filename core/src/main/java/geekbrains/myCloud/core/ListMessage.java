package geekbrains.myCloud.core;

import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ListMessage implements CloudMessage {
    private final String path;
    private final List<String> files;

    public ListMessage(Path path) throws IOException {
        this.path = Paths.get("data").relativize(path).normalize().toString();
        files = Files.list(path)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
    }

    public String getPath() {
        return path;
    }

    @Override
    public CommandType getType() {
        return CommandType.LIST;
    }
}
