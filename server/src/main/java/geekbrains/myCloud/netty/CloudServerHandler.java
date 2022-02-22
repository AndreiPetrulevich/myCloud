package geekbrains.myCloud.netty;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import geekbrains.myCloud.core.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class CloudServerHandler extends SimpleChannelInboundHandler<CloudMessage> {
    private final Path SERVER_ROOT_DIR = Paths.get("data");
    private UserRepository repository;
    private Algorithm tokenAlgo;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.repository = new UserRepository();
        log.info("Channel activated");

        String secret = System.getenv().get("JWT_SECRET");
        this.tokenAlgo = Algorithm.HMAC256(secret);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CloudMessage cloudMessage) throws Exception {
        try {
            switch (cloudMessage.getType()) {
                case FILE_REQUEST -> processFileRequest((FileRequest) cloudMessage, ctx);
                case FILE_UPLOAD -> {
                    FileUploadMessage message = (FileUploadMessage) cloudMessage;
                    processFileMessage(message);
                    Path clientDir = clientDirForMessage(message);
                    sendList(clientDir.resolve(message.getServerDir()), clientDir, ctx);
                }
                case GO_TO -> processGoToDir((GoToDir) cloudMessage, ctx);
                case RENAME -> processFileRename((FileRename) cloudMessage, ctx);
                case DELETE -> deleteFile((FileDelete) cloudMessage, ctx);
                case REG -> register((SignUp) cloudMessage, ctx);
                case LOGIN -> login((Login) cloudMessage, ctx);
            }
        } catch (AuthorizationException e) {
            ctx.writeAndFlush(new ErrorMessage(ErrorType.AUTHORIZATION_FAILED));
        }
    }

    private Path clientDirForMessage(AuthorizedCloudMessage msg) throws AuthorizationException {
        try {
            String clientId = getUserIdFromToken(msg.getToken());
            return SERVER_ROOT_DIR.resolve(clientId);
        } catch (JWTVerificationException e) {
            throw new AuthorizationException();
        }
    }

    private void sendList(Path path, Path clientDir, ChannelHandlerContext ctx) throws IOException, InterruptedException {
        log.info("Write and flush list for" + path.toString());
        List<String> files = Files.list(path)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
        ctx.writeAndFlush(new ListMessage(path.relativize(clientDir).normalize().toString(), files));
    }

    private void processFileRequest(FileRequest cloudMessage, ChannelHandlerContext ctx) throws IOException, InterruptedException, AuthorizationException {
        Path path = clientDirForMessage(cloudMessage).resolve(cloudMessage.getFileName());
        log.info("Process file" + path.toString());
        ctx.writeAndFlush(new FileDownloadMessage(path)).sync();
    }

    private void processFileMessage(FileUploadMessage cloudMessage) throws IOException, AuthorizationException {
        Path dir = clientDirForMessage(cloudMessage).resolve(cloudMessage.getServerDir());
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        Files.write(dir.resolve(cloudMessage.getFileName()), cloudMessage.getBytes());
    }

    private void processGoToDir(GoToDir cloudMessage, ChannelHandlerContext ctx) throws  AuthorizationException {
        Path clientDir = clientDirForMessage(cloudMessage);
        Path path = clientDir.resolve(cloudMessage.getDirectory());
        try {
            sendList(path, clientDir, ctx);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void processFileRename(FileRename command, ChannelHandlerContext ctx) throws AuthorizationException {
        Path clientDir = clientDirForMessage(command);
        Path oldFileNamePath = clientDir.resolve(command.getOldFileNamePath());
        Path newFileNamePath = clientDir.resolve(command.getNewFileNamePath());
        if (Files.exists(oldFileNamePath)) {
            try {
                Files.move(oldFileNamePath, newFileNamePath);
                sendList(newFileNamePath.getParent(), clientDir, ctx);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void deleteFile(FileDelete cloudMessage, ChannelHandlerContext ctx) throws AuthorizationException {
        Path clientDir = clientDirForMessage(cloudMessage);
        Path path = clientDirForMessage(cloudMessage).resolve(cloudMessage.getPathToDeleteFile());
        try {
            Files.delete(path);
            sendList(path.getParent(), clientDir, ctx);
        } catch (IOException | InterruptedException  e) {
            e.printStackTrace();
        }
    }

    private void register(SignUp cloudMessage, ChannelHandlerContext ctx) {
        String login = cloudMessage.getLogin();
        String passwordHash = BCrypt.withDefaults().hashToString(12, cloudMessage.getPassword().toCharArray());
        log.info("Register new user " + login);
        Optional<User> pUser = repository.getUserByLogin(login);
        if (pUser.isEmpty()) {
            repository.addUser(login, passwordHash);
            Optional<User> usr = repository.getUserByLogin(login);

            usr.ifPresentOrElse(u -> {
                String token = getTokenForUser(u);
                ctx.writeAndFlush(new AuthenticationSuccess(token));

                Path path = Paths.get(String.valueOf(SERVER_ROOT_DIR.resolve(u.getId())));
                try {
                    Files.createDirectories(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }, () -> {
                ctx.writeAndFlush(new ErrorMessage(ErrorType.UNKNOWN));
            });
        } else {
            ctx.writeAndFlush(new ErrorMessage(ErrorType.LOGIN_EXISTS));
        }
    }

    private String getUserIdFromToken(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(this.tokenAlgo).build();
        verifier.verify(token);

        DecodedJWT decoded = JWT.decode(token);
        return decoded.getId();
    }

    private String getTokenForUser(User user) {
        long now = System.currentTimeMillis();

        String token = JWT
                .create()
                .withExpiresAt(new Date(now + 60 * 60 * 1000))
                .withJWTId(user.getId())
                .withPayload(new HashMap<String, String>() {
                    {
                        put("user_id", user.getId());
                    }
                })
                .sign(this.tokenAlgo);

        return token;
    }

    private void login(Login cloudMessage, ChannelHandlerContext ctx) throws IOException, InterruptedException {
        String login = cloudMessage.getLogin();

        Optional<User> pUser = repository.getUserByLogin(login);

        if (pUser.isEmpty()) {
            ctx.writeAndFlush(new ErrorMessage(ErrorType.WRONG_CREDENTIALS));
            return;
        }

        User user = pUser.get();

        BCrypt.Result result = BCrypt.verifyer().verify(cloudMessage.getPassword().toCharArray(), user.getPasswordHash());
        if (!result.verified) {
            ctx.writeAndFlush(new ErrorMessage(ErrorType.WRONG_CREDENTIALS));
            return;
        }

        String token = getTokenForUser(user);
        ctx.writeAndFlush(new AuthenticationSuccess(token));

        Path clientDir = SERVER_ROOT_DIR.resolve(user.getId());
    }
}
