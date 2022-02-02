package geekbrains.myCloud.netty;

import geekbrains.myCloud.core.CloudMessage;
import geekbrains.myCloud.core.FileMessage;
import geekbrains.myCloud.core.FileRequest;
import geekbrains.myCloud.core.ListMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CloudServerHandler extends SimpleChannelInboundHandler<CloudMessage> {
    private Path currentDir;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        currentDir = Paths.get("data");
        sendList(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CloudMessage cloudMessage) throws Exception {
        switch (cloudMessage.getType()) {
            case FILE_REQUEST -> processFileRequest((FileRequest) cloudMessage, ctx);
            case FILE -> {
                processFileMessage((FileMessage) cloudMessage);
                sendList(ctx);
            }
        }
    }

    private void sendList(ChannelHandlerContext ctx) throws IOException {
        ctx.writeAndFlush(new ListMessage(currentDir));
    }

    private void processFileRequest(FileRequest cloudMessage, ChannelHandlerContext ctx) throws Exception {
        Path path = currentDir.resolve(cloudMessage.getFileName());
        ctx.writeAndFlush(new FileMessage(path));
    }

    private void processFileMessage(FileMessage cloudMessage) throws Exception {
        Files.write(currentDir.resolve(cloudMessage.getFileName()), cloudMessage.getBytes());
    }
}
