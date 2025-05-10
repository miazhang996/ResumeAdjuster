package org.example.resumeadjuster.Service.Impl;


import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Value;

import org.example.resumeadjuster.Service.UserService;
import org.example.resumeadjuster.Model.DTO.UserResponseDTO;
import org.example.resumeadjuster.Model.Entity.User;
import org.example.resumeadjuster.Repository.UserRepository;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;


/*
UserService 实现类， 提供用户相关的核心业务
本服务实现了User 的CRUD ，并集成了redis 缓存以提高性能 和减轻数据库负担

Redis 缓存策略说明：
1. 读操作优先从缓存中获取， 缓存命中时从数据库读取并更新缓存
2. 写操作先更新数据库，再同步更新缓存
3.删除操作先删除数据库记录，再清除相关缓存


缓存键设计：
- 使用两种缓存key （ID 和Email）支持不同查询方式
- 统一的缓存管理方法确保缓存数据一致性


缓存过期时可通过配置文件设置，默认30分钟


 */

@Service
public class UserServiceImpl implements UserService {

    // 注入user repository
    @Autowired
    private UserRepository userRepository;

    //redis 模板 用于缓存操作， Redis 在此服务中作为缓存层， 减少数据库访问次数，提高响应速度
    @Autowired
    private RedisTemplate<String, User> redisTemplate;

    @Autowired
    private EntityManagerFactory entityManagerFactory;
    /*
     // 缓存过期时间， 可通过配置文件中的 redis.user.cache.minutes的属性设置， 默认为30 分钟
    //设置过期时间的未来防止数据长期不一致和内存占用过大  timeTo live TTL
    30分钟后，如果该数据没有被再次访问或更新，Redis会自动删除这条缓存数据

     */

    @Value("${redis.user.cache.minutes:30}")
    private long cacheExpirationMinutes;
    // 生产基于id 的缓存键
    private String getUserCacheKey(long id){
        return "user"+id;
    }
    // 基于 email的缓存键 ， 为了支持通过邮箱查询用户的场景
    private String getEmailCacheKey(String email){
        return "email"+email;

    }

    /*
    更新用户相关的所有缓存
    这个方法确保同时更新ID 和email 两种方式缓存
    在任何修改用户信息的操作后调用，保持缓存与数据库同步
     */
    private void updatCache(User user){
        String idCacheKey=getUserCacheKey(user.getUserId());
        String emailCacheKey=getEmailCacheKey(user.getEmail());
        // 使用ID作为键，更新缓存
        // 同时设置过期时间，防止缓存长期占用内存

        redisTemplate.opsForValue().set(idCacheKey,user,cacheExpirationMinutes,TimeUnit.MINUTES);

        // 使用Email作为键，更新缓存
        // 同样设置相同的过期时间，保持一致性
        redisTemplate.opsForValue().set(emailCacheKey,user,cacheExpirationMinutes,TimeUnit.MINUTES);
    }

    /*
    清除用户相关的所有缓存
    在删除用户的同时调用此方法， 确保缓存数据不会残留
    同时清除基于ID和email的缓存，保持一致性
     */

    private void evictCache(User user){
        String idCacheKey=getUserCacheKey(user.getUserId());
        String emailCacheKey=getEmailCacheKey(user.getEmail());
        redisTemplate.delete(idCacheKey);

        redisTemplate.delete(emailCacheKey);
    }

/*
根据ID 查找用户
先检查缓存，缓存未命中再查询数据库并更新缓存
这种模式称为 "Cache-Aside"或"Lazy Loading"模式
 */


    @Override
    public User findById(long id) {
       // get cache key
        String cacheKey=getUserCacheKey(id);
        //step1: try to get user from redis cache
        // this step reduce greatly reduce database access, especially for hot data
        User cachedUser = redisTemplate.opsForValue().get(cacheKey);

        //if cache hit return cached user object directly
        if(cachedUser !=null){
            return cachedUser;
        }
        // step2 cache miss, fetch from database
        // only query database when cache has no data, reducing database load
        User user=userRepository.findById(id).orElseThrow(()->new NoSuchElementException("User not found with id: "+ id));
        //step3 store user information from database intocache
        //this allows direct cache retrieval for future queires of the same user
        updatCache(user);
        return user;
    }



 // find user by email , implements similar caching strategy as findbyid
    @Override
    public User findByEmail(String email) {
        // get cache key
        String cacheKey=getEmailCacheKey(email);
        //step1: try to get user from redis cache
        // this step reduce greatly reduce database access, especially for hot data
        User cachedUser = (User) redisTemplate.opsForValue().get(cacheKey);

        //if cache hit return cached user object directly
        if(cachedUser !=null){
            return cachedUser;
        }
        //cache miss, fetch from database

        User  user= userRepository.findByEmail(email).orElseThrow(()->new NoSuchElementException("User not found with email: "+email));
        //update cache
        updatCache(user);
        return user;
    }


    /*
    convert User entity to DTO
    this method doesn't involve cache operations, it's purely data conversion
     */
    @Override
    public UserResponseDTO mapToDTO(User user) {
        UserResponseDTO dto=new UserResponseDTO();

        dto.setUserId(user.getUserId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmailVerified(user.getEmailVerified());


        return dto;
    }




    /*
    更新用户最后登录时间
    使用@CacheEvit 表示： 更新用户数据后， 清除对应的缓存（key 为该用户邮箱）；
    下次调用 findByEmail 时会重新查数据库并缓存新数据，避免缓存和数据库不一致
     */

    @Override
    @Transactional
    public void updateLastLogin(User user) {
        // update user's last login time
        user.setLastLoginAt(OffsetDateTime.now());
        // save to database
        // @ Transactional annotation ensures atomicity of database operations
        userRepository.save(user);
        // update cache to ensure consistency with database
        // this step is crucial, otherwise user information in cache would be out of sync

        updatCache(user);

    }

    //create new user , saves to database first, then updates cache

    @Override
    public User createUser(User user) {
        //save user to database and get user object with generated id
        User savedUser=userRepository.save(user);
        //update cache after creating user
        // this ensures that if the new user is queried by id or email , it can be retreievd from cache
        updatCache(savedUser);

        return savedUser;
    }


    // update user  in database and cache
    @Override
    @Transactional
    public User updateUser(User user) {
        if (!userRepository.existsById(user.getUserId())) {
            throw new NoSuchElementException("User not found with id: "+ user.getUserId());
        }
        // update user record
        User updateUser=userRepository.save(user);

        //sync cache to ensure consistency with database
        updatCache(updateUser);


        return updateUser;
    }
    /*
    delete user
    gets complete user info first, then deletes database records and cache
     */

    @Override
    @Transactional
    public void deleteUser(Long id) {
        // Get user information first, ensuring user exists and obtaining complete info for cache clearing
        // If user doesn't exist, findById will throw an exception
        User user=findById(id); // findById() is implemented by JPA repository
        // Delete user record from database
        userRepository.deleteById(id);

        // Clear all cache entries for this user
        // This step is crucial, otherwise deleted user information would remain in cache as stale data
        evictCache(user);

    }

    @Override
    @Transactional
    public void resetDatabase() {
        try {
            // 首先删除子表数据（认证提供商）
            userRepository.deleteAllAuthProviders();

            // 然后删除用户数据
            userRepository.deleteAllUsers();

            // 重置序列
            String dbDialect = entityManagerFactory.getProperties()
                    .get("hibernate.dialect").toString().toLowerCase();

            if (dbDialect.contains("postgresql")) {
                userRepository.resetPostgresSequence();
            } else if (dbDialect.contains("mysql")) {
                userRepository.resetMySQLAutoIncrement();
            } else if (dbDialect.contains("h2")) {
                userRepository.resetH2Sequence();
            }

            System.out.println("数据库已成功重置：所有用户已删除，ID序列已重置");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("重置数据库失败: " + (e.getMessage() != null ? e.getMessage() : "未知错误"), e);
        }
    }
    }



