package com.luofeng.facetest.mapper;

import com.luofeng.facetest.pojo.Conversation;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ConversationMapper {
    
    /**
     * 插入新的对话记录
     * @param conversation 对话对象
     * @return 影响行数
     */
    @Insert("INSERT INTO conversation(user_id, title, start_time, last_update_time, status) " +
            "VALUES(#{userId}, #{title}, #{startTime}, #{lastUpdateTime}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertConversation(Conversation conversation);
    
    /**
     * 根据ID更新对话
     * @param conversation 对话对象
     * @return 影响行数
     */
    @Update("UPDATE conversation SET title = #{title}, last_update_time = #{lastUpdateTime}, status = #{status} " +
            "WHERE id = #{id}")
    int updateConversation(Conversation conversation);
    
    /**
     * 根据用户ID查询对话列表
     * @param userId 用户ID
     * @return 对话列表
     */
    @Select("SELECT * FROM conversation WHERE user_id = #{userId} ORDER BY last_update_time DESC")
    List<Conversation> selectConversationsByUserId(Integer userId);
    
    /**
     * 根据ID查询对话
     * @param id 对话ID
     * @return 对话对象
     */
    @Select("SELECT * FROM conversation WHERE id = #{id}")
    Conversation selectConversationById(Integer id);
}