package geekbrains.myCloud.netty;

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
    private final Path CURRENT_DIR = Paths.get("data");

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Channel activated");
        sendList(ctx, CURRENT_DIR);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CloudMessage cloudMessage) throws Exception {
        switch (cloudMessage.getType()) {
            case FILE_REQUEST -> processFileRequest((FileRequest) cloudMessage, ctx);
            case FILE -> {
                processFileMessage((FileMessage) cloudMessage);
                sendList(ctx, CURRENT_DIR);
            }
            case GO_TO -> processGoToDir((GoToDir) cloudMessage, ctx);
        }
    }

    private void sendList(ChannelHandlerContext ctx, Path path) throws IOException, InterruptedException {
        ctx.writeAndFlush(new ListMessage(path));
        log.debug("Write and flush dir list");
    }

    private void processFileRequest(FileRequest cloudMessage, ChannelHandlerContext ctx) throws Exception {
        Path path = CURRENT_DIR.resolve(cloudMessage.getFileName());
        ctx.writeAndFlush(new FileMessage(path)).sync();
    }

    private void processFileMessage(FileMessage cloudMessage) throws Exception {
        Files.write(CURRENT_DIR.resolve(cloudMessage.getFileName()), cloudMessage.getBytes());
    }

    private void processGoToDir(GoToDir cloudMessage, ChannelHandlerContext ctx){
        Path path = CURRENT_DIR.resolve(cloudMessage.getDirectory());
        try {
            sendList(ctx, path);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
