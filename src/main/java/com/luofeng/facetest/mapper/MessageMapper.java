package com.luofeng.facetest.mapper;

import com.luofeng.facetest.pojo.Message;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MessageMapper {
    
    /**
     * 插入新的消息记录
     * @param message 消息对象
     * @return 影响行数
     */
    @Insert("INSERT INTO message(conversation_id, sender, content, send_time) " +
            "VALUES(#{conversationId}, #{sender}, #{content}, #{sendTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertMessage(Message message);
    
    /**
     * 根据对话ID查询消息列表
     * @param conversationId 对话ID
     * @return 消息列表
     */
    @Select("SELECT * FROM message WHERE conversation_id = #{conversationId} ORDER BY send_time ASC")
    List<Message> selectMessagesByConversationId(Integer conversationId);
}