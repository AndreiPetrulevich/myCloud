package geekbrains.myCloud;

import geekbrains.myCloud.core.FileMessage;
import geekbrains.myCloud.core.ListMessage;

public interface ClientObserver {
    void handleListMessage(ListMessage message);
    void handleFileMessage(FileMessage message);
}
