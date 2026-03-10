package com.luofeng.facetest.mapper;

import com.luofeng.facetest.pojo.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {
    /**
     * 查询所有用户
     */
    @Select("SELECT * FROM user")
    List<User> list();

    /**
     * 根据ID查询用户
     */
    @Select("SELECT * FROM user WHERE id = #{id}")
    User getById(Integer id);

    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM user WHERE username = #{username}")
    User getByUsername(String username);

    /**
     * 添加用户
     */
    @Insert("INSERT INTO user(username, password, create_time, avatar, name, email, phone, gender, birthday, address) VALUES(#{username}, #{password}, #{createTime}, #{avatar}, #{name}, #{email}, #{phone}, #{gender}, #{birthday}, #{address})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void add(User user);

    /**
     * 更新用户信息（不更新密码）
     */
    @Update("UPDATE user SET username = #{username}, avatar = #{avatar}, name = #{name}, email = #{email}, phone = #{phone}, gender = #{gender}, birthday = #{birthday}, address = #{address} WHERE id = #{id}")
    void update(User user);
    
    /**
     * 更新用户密码
     */
    @Update("UPDATE user SET password = #{password} WHERE id = #{id}")
    void updatePassword(@Param("id") Integer id, @Param("password") String password);

    /**
     * 根据ID删除用户
     */
    @Delete("DELETE FROM user WHERE id = #{id}")
    void deleteById(Integer id);
    
    /**
     * 条件查询用户
     */
    @Select("SELECT * FROM user WHERE username LIKE CONCAT('%', #{username}, '%') OR #{username} IS NULL")
    List<User> getUsersByCondition(@Param("username") String username);
}