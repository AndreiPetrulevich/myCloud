package geekbrains.myCloud.core;

import lombok.Data;

import java.util.List;

@Data
public class ListMessage implements CloudMessage {
    private final String path;
    private final List<String> files;

    public ListMessage(String path, List<String> files) {
        this.path = path;
        this.files = files;
    }

    public String getPath() {
        return path;
    }

    @Override
    public CommandType getType() {
        return CommandType.LIST;
    }
}
