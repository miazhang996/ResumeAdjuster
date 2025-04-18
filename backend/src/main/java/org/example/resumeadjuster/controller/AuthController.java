package org.example.resumeadjuster.controller;

import jakarta.validation.Valid;

import org.example.resumeadjuster.Model.DTO.AuthResponseDTO;
import org.example.resumeadjuster.Model.DTO.LoginRequestDTO;
import org.example.resumeadjuster.Model.DTO.SignupRequestDTO;
import org.example.resumeadjuster.Model.DTO.UserResponseDTO;

import org.example.resumeadjuster.Model.Entity.User;
import org.example.resumeadjuster.Service.UserService;
import org.example.resumeadjuster.Service.AuthService;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;



/*
Auth Controller
including:
1. User sign up
2. User login in
3.user log out
4. get user info

所有接口均为RESTful 风格，基于Json 数据交换
 */

@RestController
@RequestMapping("/api/auth")

public class AuthController {
    @Autowired
    private AuthService authService;
    @Autowired
    private UserService userService;

    /*
    用户注册API
    接收用户提交的注册信息，创建新用户， 并返回认证token

    HTTP status code:
    -201 created
    - 409 conflict 邮箱已经注册
    - 500 Internal Server Error

     */

    @PostMapping("/signup")
    public ResponseEntity<AuthResponseDTO> signup(@Valid @RequestBody SignupRequestDTO signupRequestDTO) {

        try{
            // 调用服务层处理注册逻辑
            AuthResponseDTO response = authService.signup(signupRequestDTO);
            // 注册成功，返回201状态码
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        }catch(IllegalArgumentException e){
            // 邮箱已存在等验证错误，返回409状态码
            System.out.println("参数错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        catch (Exception e) {
            // 其他服务器错误，返回500状态码
            // 添加日志
            System.out.println("服务器错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }


    /**
     * 用户登录API
     * 验证用户凭据，成功则返回认证令牌
     * HTTP状态码:
     * - 200 OK: 登录成功
     * - 401 Unauthorized: 凭据无效
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        try{
            AuthResponseDTO response= authService.login(loginRequestDTO);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            //登录失败 401
            System.out.println("登录失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

    }



    /**
     * Google OAuth登录API
     * 接收Firebase生成的ID令牌，验证并创建/登录用户
     *
     * @param idToken Firebase ID令牌，作为原始文本接收
     * @return 包含认证令牌和用户信息的响应DTO
     *
     * HTTP状态码:
     * - 200 OK: 认证成功
     * - 401 Unauthorized: 令牌无效
     */
    @PostMapping("/google")
    public ResponseEntity<AuthResponseDTO> googleLogin(@RequestBody String idToken) {
        try{
            // 调用服务层处理Google登录逻辑
            AuthResponseDTO reponse=authService.authenticateWithGoogle(idToken);
            return ResponseEntity.ok(reponse);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

    }

    /**
     * 用户登出API
     * 将当前令牌加入黑名单，使其无效
     *
     * @param token 从请求头中获取的授权令牌
     * @return 无内容响应
     *
     * HTTP状态码:
     * - 204 No Content: 登出成功
     * - 500 Internal Server Error: 服务器错误
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token){
        try{

            // 调用服务器底层处理逻辑
            authService.logout(token);
            return ResponseEntity.noContent().build();


        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }


    }

    /**
     * 验证令牌有效性API
     * 检查提供的令牌是否有效（未过期且未被撤销）
     *
     * @param token 要验证的JWT令牌
     * @return 布尔值，表示令牌是否有效
     *
     * HTTP状态码:
     * - 200 OK: 验证完成（结果可能是true或false）
     */
    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestParam String token) {
        // 调用服务层验证令牌
        boolean isValid = authService.isValidToken(token);
        // 返回验证结果
        return ResponseEntity.ok(isValid);
    }

    /**
     * 获取当前用户信息API
     * 根据当前认证上下文获取用户详细信息
     * 需要有效的认证令牌才能访问
     *
     * @return 包含用户信息的响应DTO
     *
     * HTTP状态码:
     * - 200 OK: 获取成功
     * - 401 Unauthorized: 未认证或认证已过期
     */
    @GetMapping("/user")
    public ResponseEntity<UserResponseDTO> getCurrentUser() {
        try {
            // get current authntication from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();

            // 获取用户实体
            User user = userService.findByEmail(userEmail);

            // 在Controller中转换为DTO
            UserResponseDTO userDTO = userService.mapToDTO(user);

            // 返回用户信息
            return ResponseEntity.ok(userDTO);
        } catch (Exception e) {
            // 未认证或获取用户信息失败，返回401状态码
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }






}
