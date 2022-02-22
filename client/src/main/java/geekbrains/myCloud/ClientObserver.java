package geekbrains.myCloud;

import geekbrains.myCloud.core.ErrorMessage;
import geekbrains.myCloud.core.FileUploadMessage;
import geekbrains.myCloud.core.ListMessage;
import geekbrains.myCloud.core.AuthenticationSuccess;

public interface ClientObserver {
    void handleListMessage(ListMessage message);
    void handleFileMessage(FileUploadMessage message);
    void handleSuccessMessage(AuthenticationSuccess message);
    void handleErrorMessage(ErrorMessage message);
}
