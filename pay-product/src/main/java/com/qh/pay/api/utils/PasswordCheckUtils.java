package com.qh.pay.api.utils;

import com.qh.common.utils.CacheUtils;
import com.qh.common.utils.R;
import com.qh.common.utils.ShiroUtils;
import com.qh.system.dao.UserDao;
import com.qh.system.domain.UserDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class PasswordCheckUtils {

    private static UserDao userDaoSt;

    @Autowired
    UserDao userDao;

    @PostConstruct
    public void beforeInit() {
        userDaoSt = userDao;
    }

    public static R checkFundPassword(String fundPassword){
        //校验资金密码
        UserDO user = ShiroUtils.getUser();
        String username = user.getUsername();
        String salt = CacheUtils.getLoginSalt(username);
        if(ParamUtil.isEmpty(salt)){
            return R.error("非法操作！");
        }

        user = userDaoSt.getByUserName(username);

        if (user == null || !fundPassword.equals(Md5Util.MD5(username + user.getFundPassword() + salt))) {
            return R.error("资金密码不正确!");
        }
        return new R();
    }
}
