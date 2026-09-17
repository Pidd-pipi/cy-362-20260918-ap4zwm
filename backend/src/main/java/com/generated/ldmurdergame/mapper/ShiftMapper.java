package com.generated.ldmurdergame.mapper;

import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.generated.ldmurdergame.model.Shift;

@Mapper
public interface ShiftMapper {
  String COLUMNS = "id, shift_date, slot, skill, required_count, created_by, created_at";

  @Insert("INSERT INTO shift (shift_date, slot, skill, required_count, created_by) "
      + "VALUES (#{shiftDate}, #{slot}, #{skill}, #{requiredCount}, #{createdBy})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  int insert(Shift shift);

  @Select("SELECT " + COLUMNS + " FROM shift WHERE id = #{id}")
  Shift findById(@Param("id") long id);

  @Select("SELECT " + COLUMNS + " FROM shift WHERE id = #{id} FOR UPDATE")
  Shift findByIdForUpdate(@Param("id") long id);

  @Select("SELECT " + COLUMNS + " FROM shift WHERE shift_date = #{date} ORDER BY slot, id")
  List<Shift> findByDate(@Param("date") LocalDate date);
}
