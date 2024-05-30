package com.midpay.service.mallPay;

import com.midpay.utils.LocalUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

@Component
public class MallThreadLocal {

    @Resource
    private MallProperties mallProperties;

    public static ThreadLocal<MallProperties.EnvProperties> mallEnvThreadLocal = new ThreadLocal<>();
    public static ThreadLocal<MallProperties.EnvUser> mallUserThreadLocal = new ThreadLocal<>();

    public void setRandomMallEnv() {
        List<MallProperties.EnvProperties> envs = mallProperties.getEnvs();
        MallProperties.EnvProperties env = LocalUtil.randomExtract(envs);
        MallProperties.EnvUser envUser = LocalUtil.randomExtract(env.getUsers());
        this.setLocalMallEnv(env);
        this.setLocalMallUser(envUser);
    }

    public void setLocalMallEnv(String env, String mobile) {
        if (!StringUtils.hasText(env)) {
            return;
        }
        List<MallProperties.EnvProperties> envs = mallProperties.getEnvs();
        for (MallProperties.EnvProperties envProperties : envs) {
            if (envProperties.getEnv().equals(env)) {
                this.setLocalMallEnv(envProperties);

                List<MallProperties.EnvUser> users = envProperties.getUsers();
                for (MallProperties.EnvUser user : users) {
                    if (user.getMobile().equals(mobile)) {
                        this.setLocalMallUser(user);
                    }
                }
            }
        }
    }

    private void setLocalMallEnv(MallProperties.EnvProperties env) {
        mallEnvThreadLocal.set(env);
    }

    private void setLocalMallUser(MallProperties.EnvUser envUser) {
        mallUserThreadLocal.set(envUser);
    }

    public MallProperties.EnvProperties getLocalMallEnv() {
        return mallEnvThreadLocal.get();
    }

    public MallProperties.EnvUser getLocalMallUser() {
        return mallUserThreadLocal.get();
    }
}
