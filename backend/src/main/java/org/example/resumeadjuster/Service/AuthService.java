package org.example.resumeadjuster.Service;
import org.example.resumeadjuster.Model.DTO.AuthResponseDTO;
import org.example.resumeadjuster.Model.DTO.LoginRequestDTO;
import org.example.resumeadjuster.Model.DTO.SignupRequestDTO;


//处理用户身份验证（认证/登录/登出/Token 验证等），负责登录逻辑、安全性验证等。
public interface AuthService {
    AuthResponseDTO signup(SignupRequestDTO signupRequestDTO);
    AuthResponseDTO login(LoginRequestDTO loginRequestDTO);
    AuthResponseDTO authenticateWithGoogle(String idToken);
    void logout(String token);
    boolean isValidToken(String token);
}
