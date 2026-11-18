package com.jsnws.server.common;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 匿名（含未登录）发言的本地随机头像。
 * 素材位于 jsnws-ui-v3/public/images/anonymous，匿名留言与游客树洞共用同一套约定。
 */
public final class AnonymousAvatars {

    public static final String[] LIST = {
            "/images/anonymous/anon-1.jpg",
            "/images/anonymous/anon-2.jpg",
            "/images/anonymous/anon-3.jpg",
            "/images/anonymous/anon-4.jpg",
            "/images/anonymous/anon-5.jpg",
            "/images/anonymous/anon-6.jpg",
            "/images/anonymous/anon-7.jpg",
            "/images/anonymous/anon-8.jpg"
    };

    private AnonymousAvatars() {
    }

    /** 随机取一张匿名头像 */
    public static String random() {
        return LIST[ThreadLocalRandom.current().nextInt(LIST.length)];
    }
}
