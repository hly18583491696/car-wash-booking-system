package com.carwash.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.carwash.entity.KnowledgeBase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * AI客服知识库Mapper
 * 
 * @author CarWash Team
 * @version 1.0.0
 */
@Mapper
public interface KnowledgeBaseMapper extends BaseMapper<KnowledgeBase> {

    /**
     * 根据分类查询知识条目
     */
    @Select("SELECT * FROM knowledge_base WHERE category = #{category} AND status = 1 AND deleted = 0 ORDER BY priority DESC")
    List<KnowledgeBase> findByCategory(@Param("category") String category);

    /**
     * 关键词模糊匹配
     */
    @Select("SELECT * FROM knowledge_base WHERE status = 1 AND deleted = 0 AND (keywords LIKE CONCAT('%', #{keyword}, '%') OR question LIKE CONCAT('%', #{keyword}, '%')) ORDER BY priority DESC LIMIT #{limit}")
    List<KnowledgeBase> searchByKeyword(@Param("keyword") String keyword, @Param("limit") int limit);

    /**
     * 获取所有启用的知识条目
     */
    @Select("SELECT * FROM knowledge_base WHERE status = 1 AND deleted = 0 ORDER BY priority DESC")
    List<KnowledgeBase> findAllActive();

    /**
     * 增加匹配次数
     */
    @Update("UPDATE knowledge_base SET hit_count = hit_count + 1 WHERE id = #{id}")
    void incrementHitCount(@Param("id") Long id);
}
