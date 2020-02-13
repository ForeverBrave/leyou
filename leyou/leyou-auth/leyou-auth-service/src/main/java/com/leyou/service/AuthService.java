package com.leyou.service;

import com.leyou.client.UserClient;
import com.leyou.config.JwtProperties;
import com.leyou.pojo.User;
import com.leyou.pojo.UserInfo;
import com.leyou.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserClient userClient;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录授权
     * @param username
     * @param password
     * @return
     */
    public String accredit(String username, String password) {

        try {
        //1、根据用户和密码查询
        User user = this.userClient.queryUser(username, password);

        //2、判断user
        if (user == null) {
            return null;
        }
            //3、jwtUtils生成jwt类型的token
            UserInfo userInfo = new UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            return JwtUtils.generateToken(userInfo,this.jwtProperties.getPrivateKey(),this.jwtProperties.getExpire());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
