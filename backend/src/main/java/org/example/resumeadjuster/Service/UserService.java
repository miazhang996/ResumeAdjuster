package org.example.resumeadjuster.Service;

import org.example.resumeadjuster.Model.DTO.UserResponseDTO;
import org.example.resumeadjuster.Model.Entity.User;


//	管理用户本身的数据（CRUD），如查找、更新信息、映射成 DTO 等。

public interface UserService {
  User findById(long id);
  User findByEmail(String email);
  UserResponseDTO mapToDTO(User user);
  void updateLastLogin(User user);

  User createUser(User user);
  User updateUser(User user);
  void deleteUser(Long id);

  // 添加重置数据库的方法
  void resetDatabase();

}
