package cn.chauncy.logic.login;

import cn.chauncy.event.CtxMsgEvent;
import cn.chauncy.message.ReqLogin;
import cn.chauncy.utils.eventbus.Subscribe;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginManager {

    private static final Logger logger = LoggerFactory.getLogger(LoginManager.class);

    @Subscribe
    public void onPlayerLogin(CtxMsgEvent<ReqLogin> msgEvent) {
        ChannelHandlerContext context = msgEvent.context();
        ReqLogin reqLogin = msgEvent.message();
        long uid = reqLogin.getUid();

    }
}
