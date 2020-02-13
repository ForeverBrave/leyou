package com.leyou.api;

import com.leyou.pojo.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface UserApi {

    /**
     * 用户登录
     * @param username
     * @param password
     * @return
     */
    @GetMapping("query")                //@RequestParam用于接收头参数
    public User queryUser(
            @RequestParam("username")String username,
            @RequestParam("password")String password);

}
