package geekbrains.myCloud.core;

import lombok.Data;

@Data
public class FileRename implements CloudMessage{
    private final String oldFileNamePath;
    private final String newFileNamePath;

    public FileRename(String oldFileName, String newFileName) {
        this.oldFileNamePath = oldFileName;
        this.newFileNamePath = newFileName;
    }

    public String getOldFileNamePath() {
        return oldFileNamePath;
    }

    public String getNewFileNamePath() {
        return newFileNamePath;
    }

    @Override
    public CommandType getType() {
        return CommandType.RENAME;
    }
}
