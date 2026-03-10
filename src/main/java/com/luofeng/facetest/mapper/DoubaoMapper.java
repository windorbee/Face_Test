package com.luofeng.facetest.mapper;

import com.luofeng.facetest.pojo.testResult;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface DoubaoMapper {
    
    /**
     * 插入测试结果
     * @param testResult 测试结果对象
     * @return 影响行数
     */
    @Insert("INSERT INTO test_result(user_id, image, test_time, is_gland_face, level, probability, visualization_description) " +
            "VALUES(#{userId}, #{imagePath}, #{testTime}, #{isGlandFace}, #{level}, #{confidence}, #{visualizationDescription})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertTestResult(testResult testResult);
}