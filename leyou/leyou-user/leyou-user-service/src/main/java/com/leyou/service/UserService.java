package com.leyou.service;

import com.leyou.mapper.UserMapper;
import com.leyou.pojo.User;
import com.leyou.utils.CodecUtils;
import com.leyou.utils.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "user:verify:";

    /**
     * 校验数据是否可用
     * @param data
     * @param type
     * @return
     */
    public Boolean checkUser(String data, Integer type) {

        User record = new User();
        if (type == 1) {
            record.setUsername(data);
        }else if(type == 2){
            record.setPhone(data);
        }else {
            return null;
        }
        return this.userMapper.selectCount(record) == 0;

    }

    /**
     * 发送验证码
     * @param phone
     */
    public void sendVerifyCode(String phone) {
        if(StringUtils.isBlank(phone)){
            return;
        }

        //生成6位数验证码
        String code = NumberUtils.generateCode(6);
        //发送消息到rabbitMQ
        Map<String,String> msg = new HashMap<>();
        msg.put("phone",phone);
        msg.put("code",code);
        this.amqpTemplate.convertAndSend("LEYOU.SMS.EXCHANGE","verifycode.sms",msg);

        //把验证码保存到redis中
        this.redisTemplate.opsForValue().set(KEY_PREFIX+phone,code,5, TimeUnit.MINUTES);

    }

    /**
     * 用户注册
     * @param user
     * @param code
     * @return
     */
    public Boolean register(User user, String code) {

        //1、查询redis中的验证码
        String redisCode = this.redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());

        //2、校验验证码
        if (!StringUtils.equals(code,redisCode)) {
            return false;
        }

        //3、生成盐
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);

        //4、加盐加密
        user.setPassword(CodecUtils.md5Hex(user.getPassword(),salt));

        //5、新增用户
        user.setId(null);
        user.setCreated(new Date());

        Boolean b = this.userMapper.insertSelective(user) == 1;
        //判断是否新增成功
        if (b) {
            this.redisTemplate.delete(KEY_PREFIX+user.getPhone());
        }
        return b;
    }

    /**
     * 用户登录
     * @param username
     * @param password
     * @return
     */
    public User queryUser(String username, String password) {

        User record = new User();
        record.setUsername(username);
        //通过用户查询相关信息
        User user = this.userMapper.selectOne(record);

        //判断user是否为空
        if (user == null) {
            return null;
        }

        //获取盐，对用户输入的密码加盐加密
        password = CodecUtils.md5Hex(password,user.getSalt());

        //和数据库中的密码比较(如果一样则返回user，不一样则返回null)
        if (StringUtils.equals(password,user.getPassword())) {
            return user;
        }

        return null;

    }
}
