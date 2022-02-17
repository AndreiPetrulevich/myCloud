package geekbrains.myCloud.netty;

import at.favre.lib.crypto.bcrypt.BCrypt;
import geekbrains.myCloud.core.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class CloudServerHandler extends SimpleChannelInboundHandler<CloudMessage> {
    private final Path SERVER_ROOT_DIR = Paths.get("data");
    private Repository repository;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.repository = new Repository();
        log.info("Channel activated");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CloudMessage cloudMessage) throws Exception {
        switch (cloudMessage.getType()) {
            case FILE_REQUEST -> processFileRequest((FileRequest) cloudMessage, ctx);
            case FILE -> {
                processFileMessage((FileMessage) cloudMessage);
                sendList(ctx, SERVER_ROOT_DIR);
            }
            case GO_TO -> processGoToDir((GoToDir) cloudMessage, ctx);
            case RENAME -> processFileRename((FileRename) cloudMessage, ctx);
            case DELETE -> deleteFile((FileDelete)cloudMessage, ctx);
            case REG -> register((SignUp) cloudMessage);
        }
    }

    private void sendList(ChannelHandlerContext ctx, Path path) throws IOException, InterruptedException {
        log.info("Write and flush list for" + path.toString());
        ctx.writeAndFlush(new ListMessage(path));
    }

    private void processFileRequest(FileRequest cloudMessage, ChannelHandlerContext ctx) throws Exception {
        Path path = SERVER_ROOT_DIR.resolve(cloudMessage.getFileName());
        log.info("Process file" + path.toString());
        ctx.writeAndFlush(new FileMessage(path)).sync();
    }

    private void processFileMessage(FileMessage cloudMessage) throws Exception {
        Files.write(SERVER_ROOT_DIR.resolve(cloudMessage.getFileName()), cloudMessage.getBytes());
    }

    private void processGoToDir(GoToDir cloudMessage, ChannelHandlerContext ctx){
        Path path = SERVER_ROOT_DIR.resolve(cloudMessage.getDirectory());
        try {
            sendList(ctx, path);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void processFileRename(FileRename command, ChannelHandlerContext ctx) {
        Path oldFileNamePath = SERVER_ROOT_DIR.resolve(command.getOldFileNamePath());
        Path newFileNamePath = SERVER_ROOT_DIR.resolve(command.getNewFileNamePath());
        if (Files.exists(oldFileNamePath)) {
            try {
                Files.move(oldFileNamePath, newFileNamePath);
                sendList(ctx, newFileNamePath.getParent());
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void deleteFile(FileDelete cloudMessage, ChannelHandlerContext ctx) {
        Path path = SERVER_ROOT_DIR.resolve(cloudMessage.getPathToDeleteFile());
        try {
            Files.delete(path);
            sendList(ctx, path.getParent());
        } catch (IOException | InterruptedException  e) {
            e.printStackTrace();
        }
    }

    private void register(SignUp cloudMessage) {
        String login = cloudMessage.getLogin();
        String passwordHash = BCrypt.withDefaults().hashToString(12, cloudMessage.getPassword().toCharArray());
        log.info("Register new user " + login);
        repository.addUser(login, passwordHash);
        //TODO: make new directory in SERVER_ROOT_DIR, named: "ClientID"
    }

}
