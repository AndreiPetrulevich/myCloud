package geekbrains.myCloud;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Sender {


    static void getFile(DataInputStream is, Path serverDir, int size, byte[] buffer) throws IOException {
        String fileName = is.readUTF();
        System.out.println("received file: " + fileName);
        Long fileSize = is.readLong();

        try (FileOutputStream fos = new FileOutputStream(serverDir.resolve(fileName).toFile())) {
            for (int i = 0; i < (fileSize + size - 1) / size; i++) {
                int readBytes = is.read(buffer);
                fos.write(buffer, 0, readBytes);
            }
        }
    }

    static void sendFile(String fileName, DataOutputStream os, Path serverDir) throws IOException {
        os.writeUTF("#file#");
        os.writeUTF(fileName);
        Path filePath = serverDir.resolve(fileName);
        long fileSize = Files.size(filePath);
        byte[] bytes = Files.readAllBytes(filePath);
        os.writeLong(fileSize);
        os.write(bytes);
        os.flush();
    }
}

