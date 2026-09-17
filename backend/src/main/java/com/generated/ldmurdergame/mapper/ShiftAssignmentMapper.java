package com.generated.ldmurdergame.mapper;

import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import com.generated.ldmurdergame.model.AssignmentView;
import com.generated.ldmurdergame.model.MyShiftView;
import com.generated.ldmurdergame.model.ShiftAssignment;

@Mapper
public interface ShiftAssignmentMapper {
  String COLUMNS = "id, shift_id, dm_id, shift_date, slot, created_at";

  @Insert("INSERT INTO shift_assignment (shift_id, dm_id, shift_date, slot) "
      + "VALUES (#{shiftId}, #{dmId}, #{shiftDate}, #{slot})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  int insert(ShiftAssignment assignment);

  @Select("SELECT COUNT(*) FROM shift_assignment WHERE shift_id = #{shiftId}")
  int countByShift(@Param("shiftId") long shiftId);

  @Select("SELECT a.dm_id, d.name AS dm_name FROM shift_assignment a "
      + "JOIN dm d ON d.id = a.dm_id WHERE a.shift_id = #{shiftId} ORDER BY a.id")
  List<AssignmentView> findViewsByShift(@Param("shiftId") long shiftId);

  @Select("SELECT COUNT(*) FROM shift_assignment "
      + "WHERE dm_id = #{dmId} AND shift_date = #{date} AND slot = #{slot}")
  int countByDmAndSlot(@Param("dmId") long dmId, @Param("date") LocalDate date, @Param("slot") String slot);

  @Select("SELECT " + COLUMNS + " FROM shift_assignment WHERE shift_id = #{shiftId} AND dm_id = #{dmId}")
  ShiftAssignment findByShiftAndDm(@Param("shiftId") long shiftId, @Param("dmId") long dmId);

  @Select("SELECT " + COLUMNS + " FROM shift_assignment WHERE shift_id = #{shiftId} AND dm_id = #{dmId} FOR UPDATE")
  ShiftAssignment findByShiftAndDmForUpdate(@Param("shiftId") long shiftId, @Param("dmId") long dmId);

  @Select("SELECT a.id AS assignment_id, a.shift_id, a.shift_date, a.slot, s.skill, s.required_count, "
      + "(SELECT COUNT(*) FROM shift_assignment x WHERE x.shift_id = a.shift_id) AS assigned_count "
      + "FROM shift_assignment a JOIN shift s ON s.id = a.shift_id "
      + "WHERE a.dm_id = #{dmId} ORDER BY a.shift_date, a.slot")
  List<MyShiftView> findMyShifts(@Param("dmId") long dmId);

  @Update("UPDATE shift_assignment SET dm_id = #{toDmId} WHERE id = #{id} AND dm_id = #{fromDmId}")
  int reassign(@Param("id") long id, @Param("fromDmId") long fromDmId, @Param("toDmId") long toDmId);
}
