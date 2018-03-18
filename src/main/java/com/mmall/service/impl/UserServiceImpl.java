package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("iUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultConst = userMapper.checkUsername(username);
        if(resultConst == 0){
            return ServerResponse.createByErrorMsg("用户名不存在");
        }
        //MD5加密
        String md5Password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username,md5Password);
        if(user == null){
            return ServerResponse.createByErrorMsg("密码错误");
        }
        user.setPassword(org.apache.commons.lang.StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登陆成功",user);
    }
    public  ServerResponse<String> register(User user){
        ServerResponse volidResponse = this.checkVolid(user.getUsername(),Const.USERNAME);
        if(!volidResponse.isSuccess()){
            return volidResponse;
        }
        volidResponse = this.checkVolid(user.getEmail(),Const.EMAIL);
        if(!volidResponse.isSuccess()){
            return volidResponse;
        }
        user.setRole(Const.Role.ROLE_CUSTOMER);
        //MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int resultConst = userMapper.insert(user);
        if(resultConst == 0){
            return ServerResponse.createByErrorMsg("注册失败");
        }
        return ServerResponse.createBySuccessMsg("注册成功");
    }

    public ServerResponse<String> checkVolid(String str,String type){
        if(org.apache.commons.lang.StringUtils.isNotBlank(type)){
            if(Const.USERNAME.equals(type)){
                int resultConst = userMapper.checkUsername(str);
                if(resultConst>0){
                    return ServerResponse.createByErrorMsg("用户名已存在");
                }
            }
            if(Const.EMAIL.equals(type)){
                int resultConst = userMapper.checkEmail(str);
                if(resultConst>0){
                    return ServerResponse.createByErrorMsg("email已存在");
                }
            }
        }else {
            return ServerResponse.createByErrorMsg("参数错误");
        }
        return ServerResponse.createBySuccessMsg("校验成功");
    }

    public ServerResponse selectQuestion(String username){
        ServerResponse volidResponse = this.checkVolid(username,Const.USERNAME);
        if(volidResponse.isSuccess()){
            //用户名不存在
            return  ServerResponse.createBySuccessMsg("用户名不存在");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if(org.apache.commons.lang.StringUtils.isNotBlank(question)){
            return ServerResponse.createBySuccess(question);
        }
        return  ServerResponse.createByErrorMsg("找回密码的问题为空");
    }
    public ServerResponse<String> checkAnswer(String username,String question,String answer){
        int resultConst = userMapper.checkAnswer(username,question,answer);
        if(resultConst>0){
            //说明问题及答案是这个用户的，并且是正确的
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX+username,forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMsg("问题的答案错误");
    }
    public ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken){
        if(org.apache.commons.lang.StringUtils.isBlank(forgetToken)){
            return ServerResponse.createByErrorMsg("参数错误，token需要传递");
        }
        ServerResponse volidResponse = this.checkVolid(username,Const.USERNAME);
        if(volidResponse.isSuccess()){
            //用户名不存在
            return ServerResponse.createByErrorMsg("用户不存在");
        }
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX+username);
        if(org.apache.commons.lang.StringUtils.isBlank(token)){
            return ServerResponse.createByErrorMsg("token无效或者过期");
        }
        if(org.apache.commons.lang.StringUtils.equals(token,forgetToken)){
            String md5Password = MD5Util.MD5EncodeUtf8(passwordNew);
            int rowConst = userMapper.updatePasswordByUsername(username,md5Password);
            if(rowConst>0){
                return ServerResponse.createBySuccessMsg("修改密码成功");
            }
        }else {
            return ServerResponse.createByErrorMsg("token错误,请重新获取重置密码的token");
        }
        return ServerResponse.createByErrorMsg("修改密码失败");
    }

    public ServerResponse<String> resetPassword(String passwordOld,String passwordNew,User user){
        //防止横向越权,要校验一下这个用户的旧密码,一定要指定是这个用户.因为我们会查询一个Const(1),如果不指定id,那么结果就是true啦Const>0;
        int resultConst = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld),user.getId());
        if(resultConst == 0){
            return ServerResponse.createByErrorMsg("旧密码错误");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int updateConst = userMapper.updateByPrimaryKeySelective(user);
        if(updateConst>0){
            return ServerResponse.createBySuccessMsg("密码更新成功");
        }
        return ServerResponse.createByErrorMsg("密码更新失败");
    }
    public ServerResponse<User> updateInformation(User user){
        //username是不能被更新的
        //email也要进行一个校验,校验新的email是不是已经存在,并且存在的email如果相同的话,不能是我们当前的这个用户的.
        int resultConst = userMapper.checkEmailByUserId(user.getEmail(),user.getId());
        if(resultConst>0){
            return ServerResponse.createByErrorMsg("email已存在,请更换email再尝试更新");
        }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        int updateConst = userMapper.updateByPrimaryKeySelective(updateUser);
        if(updateConst>0){
            return ServerResponse.createBySuccess("更新个人信息成功",updateUser);
        }
        return ServerResponse.createByErrorMsg("更新个人信息失败");
    }

    public ServerResponse<User> getInformation(Integer userId){
        User user = userMapper.selectByPrimaryKey(userId);
        if(user == null){
            return ServerResponse.createByErrorMsg("找不到当前用户");
        }
        user.setPassword(org.apache.commons.lang.StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }

    /**
    * 检验是否为管理员
    * @Param user
    * */
    public ServerResponse checkAdminRole(User user){
        if(user != null && user.getRole().intValue() == Const.Role.ROLE_ADMIN){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }
}
