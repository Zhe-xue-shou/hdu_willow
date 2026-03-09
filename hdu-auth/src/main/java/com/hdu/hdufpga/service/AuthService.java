package com.hdu.hdufpga.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.ShearCaptcha;
import cn.hutool.captcha.generator.MathGenerator;
import cn.hutool.core.math.Calculator;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.hdu.hdufpga.entity.Result;
import com.hdu.hdufpga.entity.constant.SysConstant;
import com.hdu.hdufpga.entity.po.UserPO;
import com.hdu.hdufpga.entity.ro.LoginRO;
import com.hdu.hdufpga.entity.ro.VerificationCodeRO;
import com.hdu.hdufpga.entity.vo.UserVO;
import com.hdu.hdufpga.exception.AccountVerifyException;
import com.hdu.hdufpga.exception.VerificationCodeException;
import com.hdu.hdufpga.util.RedisUtil;
import com.hdu.hdufpga.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AuthService {

  @DubboReference(check = false)
  private UserService userService;

  @Resource
  private RedisUtil redisUtil;

  @Resource
  private SsoService ssoService;

  @Resource
  private HttpServletResponse response;

  /**
   * SSO登录
   *
   * @return 子系统的登录信息
   */
//  public Object login(LoginRO loginRO) throws Exception {
//    log.debug("LoginRO: {}", loginRO);
//    String username = loginRO.getUsername();
//    String password = loginRO.getPassword();
//    Integer departmentId = loginRO.getDepartmentId();
//    String applicationName = loginRO.getApplicationName();
//    String verificationCodeKey = loginRO.getVerificationCodeKey();
//    String verificationCodeValue = loginRO.getVerificationCodeValue();
//    // 如果已经登录则直接返回，登录名为用户名+学校id
//    String loginName = username + SysConstant.DASH + departmentId;
//    Object isLogin = isLogin(loginName, applicationName);
//    if (Objects.nonNull(isLogin)) {
//      return isLogin;
//    }
//    // 验证码是否正确
//    if (StrUtil.isBlank(verificationCodeKey) || StrUtil.isBlank(verificationCodeValue)) {
//      throw new VerificationCodeException("验证码为空");
//    }
//    Integer code = (Integer) redisUtil.get(verificationCodeKey);
//    redisUtil.del(verificationCodeKey);
//    if (!StrUtil.equals(String.valueOf(code), verificationCodeValue)) {
//      throw new VerificationCodeException("验证码错误");
//    }
//    log.debug("验证码检验通过");
//    // 查询用户信息
//    UserPO userPO = userService.getUserByUserName(username, departmentId);
//    if (Objects.isNull(userPO)) {
//      throw new AccountVerifyException("用户名为空");
//    }
//    // 比较密码
//    if (!StrUtil.equals(password, userPO.getPassword())) {
//      throw new AccountVerifyException("用户名或密码错误");
//    }
//    log.debug("账户验证成功");
//    // 通知子系统登录并获取他们的token信息
//    AbstractSsoService service = ssoService.getSsoService(applicationName);
//    if (Objects.isNull(service)) {
//      log.error("Service为空(null)");
//      return null;
//    }
//    Object result = service.login(loginName);
//    // 登录成功则登录SSO系统
//    if (Objects.nonNull(result)) {
//      StpUtil.login(loginName);
//      log.info("{} 成功登录 {} 子系统!", username, applicationName);
//      return result;
//    } else {
//      log.debug("None");
//    }
//    return null;
//  }

  /**
   * SSO登录（只负责认证 + 生成token）
   */
  public Object login(LoginRO loginRO) throws Exception {
    log.debug("LoginRO: {}", loginRO);

    String username = loginRO.getUsername();
    String password = loginRO.getPassword();
    Integer departmentId = loginRO.getDepartmentId();
    String verificationCodeKey = loginRO.getVerificationCodeKey();
    String verificationCodeValue = loginRO.getVerificationCodeValue();

    if (StrUtil.hasBlank(username, password, verificationCodeKey, verificationCodeValue)) {
      throw new RuntimeException("参数不完整");
    }

    String loginId = username + SysConstant.DASH + departmentId;

    Object code = redisUtil.get(verificationCodeKey);
    redisUtil.del(verificationCodeKey);

    if (code == null) {
      throw new VerificationCodeException("验证码已过期，请重新生成");
    }
    if (!StrUtil.equals(String.valueOf(code), verificationCodeValue)) {
      throw new VerificationCodeException("验证码错误");
    }

    log.debug("验证码校验通过");
    UserPO userPO = userService.getUserByUserName(username, departmentId);

    if (userPO == null) {
      throw new AccountVerifyException("用户不存在");
    }

    if (!StrUtil.equals(SecureUtil.md5(password), userPO.getPassword())) {
      throw new AccountVerifyException("用户名或密码错误");
    }

    log.debug("账户验证成功");

    StpUtil.login(loginId);

    UserVO userVO = userService.UserPO2UserVO(userPO);
    // 该方法会自动写入到Redis的satoken:session字段中
    // 后续的get方法也会自动从redis中读取 redis数据库由alone配置
    StpUtil.getSession().set("user", userVO);

    log.info("{} 登录成功", loginId);

    return StpUtil.getTokenInfo();
  }

  public Result logout() {
    if (!StpUtil.isLogin()) {
      return Result.error("未登录");
    }
    log.info("log out! info: {}", StpUtil.getTokenInfo());
    StpUtil.logout();
    return Result.ok();
  }

  /**
   * 检测是否登录
   *
   * @param username        用户名
   * @param applicationName 子系统名称
   * @return 若登录返回子系统Session信息
   */
//  public Object isLogin(String username, String applicationName) {
//    if (StpUtil.isLogin(username)) {
//      AbstractSsoService service = ssoService.getSsoService(applicationName);
//      if (Objects.isNull(service)) {
//        return null;
//      }
//      return service.login(username);
//    }
//    return null;
//  }

  /**
   * 生成四则运算验证码
   * <p>通过HttpServletResponse传递前端</p>
   * <p>uuid在Header中，结果存储在本地缓存</p>
   *
   * @param verificationCodeRO 验证码参数
   * @throws IOException
   */
  public void generateVerificationCode(VerificationCodeRO verificationCodeRO) throws IOException {
    // 验证码生成器
    MathGenerator mathGenerator = new MathGenerator(verificationCodeRO.getNumberLength());
    ShearCaptcha shearCaptcha = CaptchaUtil.createShearCaptcha(verificationCodeRO.getWidth(), verificationCodeRO.getHeight(), mathGenerator, verificationCodeRO.getThickness());
    // 生成唯一标识
    String uuid = RandomUtil.randomString(8);
    response.setHeader("uuid", uuid);
    // 计算结果
    String code = shearCaptcha.getCode();
    Integer result = (int) Calculator.conversion(code);
    // 存放到缓存中
    redisUtil.set("verificationCode:" + uuid, result, 1, TimeUnit.MINUTES);
    // 渲染到前端
    ServletOutputStream out = response.getOutputStream();
    shearCaptcha.write(out);
    out.close();
  }

  public Object thirdLogin(String token) {

    String secret = "secret_114514";

    // 1 验证 token
    if (!JWTUtil.verify(token, secret.getBytes())) {
      return Result.error("invalid token");
    }

    // 2 解析 JWT
    JWT jwt = JWTUtil.parseToken(token);

    JSONObject payload = jwt.getPayloads();

    String nonce = payload.getStr("nonce");

    if (redisUtil.hasKey("nonce:" + nonce)) {
      return Result.error("replay attack");
    }

    redisUtil.set("nonce:" + nonce, 1, 60, TimeUnit.SECONDS);

    String uid = payload.getStr("uid");
    String source = payload.getStr("source");
    Long exp = payload.getLong("exp");

    long now = System.currentTimeMillis() / 1000;

    // 3 检查过期
    if (exp == null || exp < now) {
      return Result.error("token expired");
    }

    // 4 创建用户
    UserVO userVO = userService.createThirdUser(uid, source);

    // 5 SaToken 登录
    StpUtil.login(uid);

    // 6 写 session
    StpUtil.getSession().set("user", userVO);

    return StpUtil.getTokenInfo();
  }
}
