package com.generated.ldmurdergame.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.generated.ldmurdergame.model.Dm;

@Mapper
public interface DmMapper {
  @Select("SELECT id, name FROM dm ORDER BY id")
  List<Dm> findAll();

  @Select("SELECT id, name FROM dm WHERE id = #{id}")
  Dm findById(@Param("id") long id);

  @Select("SELECT COUNT(*) FROM dm")
  int countAll();

  @Select("SELECT skill FROM dm_skill WHERE dm_id = #{dmId} ORDER BY skill")
  List<String> findSkills(@Param("dmId") long dmId);

  @Insert("INSERT INTO dm (name) VALUES (#{name})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  int insert(Dm dm);

  @Insert("INSERT INTO dm_skill (dm_id, skill) VALUES (#{dmId}, #{skill})")
  int insertSkill(@Param("dmId") long dmId, @Param("skill") String skill);
}
