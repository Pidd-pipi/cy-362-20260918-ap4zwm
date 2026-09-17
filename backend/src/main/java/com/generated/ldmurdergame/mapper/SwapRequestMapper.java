package com.generated.ldmurdergame.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import com.generated.ldmurdergame.model.SwapRequest;
import com.generated.ldmurdergame.model.SwapView;

@Mapper
public interface SwapRequestMapper {
  String COLUMNS = "id, shift_id, from_dm_id, to_dm_id, status, note, created_at, updated_at";

  String VIEW_JOIN = "FROM swap_request r "
      + "JOIN shift s ON s.id = r.shift_id "
      + "JOIN dm f ON f.id = r.from_dm_id "
      + "LEFT JOIN dm t ON t.id = r.to_dm_id ";

  String VIEW_COLUMNS = "SELECT r.id, r.shift_id, r.from_dm_id, r.to_dm_id, r.status, r.note, r.created_at, "
      + "s.shift_date, s.slot, s.skill, f.name AS from_dm_name, t.name AS to_dm_name " + VIEW_JOIN;

  @Insert("INSERT INTO swap_request (shift_id, from_dm_id, note) VALUES (#{shiftId}, #{fromDmId}, #{note})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  int insert(SwapRequest request);

  @Select("SELECT " + COLUMNS + " FROM swap_request WHERE id = #{id}")
  SwapRequest findById(@Param("id") long id);

  @Select("SELECT " + COLUMNS + " FROM swap_request WHERE id = #{id} FOR UPDATE")
  SwapRequest findByIdForUpdate(@Param("id") long id);

  @Select("SELECT COUNT(*) FROM swap_request "
      + "WHERE shift_id = #{shiftId} AND from_dm_id = #{fromDmId} AND status IN ('OPEN', 'ACCEPTED')")
  int countActiveByShiftAndFrom(@Param("shiftId") long shiftId, @Param("fromDmId") long fromDmId);

  @Select(VIEW_COLUMNS + "WHERE r.status IN ('OPEN', 'ACCEPTED') OR r.from_dm_id = #{dmId} OR r.to_dm_id = #{dmId} "
      + "ORDER BY r.id DESC LIMIT 30")
  List<SwapView> findBoardSwapsForDm(@Param("dmId") long dmId);

  @Select(VIEW_COLUMNS + "ORDER BY r.id DESC LIMIT 30")
  List<SwapView> findBoardSwapsForManager();

  @Update("UPDATE swap_request SET status = 'ACCEPTED', to_dm_id = #{toDmId}, updated_at = CURRENT_TIMESTAMP "
      + "WHERE id = #{id} AND status = 'OPEN'")
  int updateAccepted(@Param("id") long id, @Param("toDmId") long toDmId);

  @Update("UPDATE swap_request SET status = 'COMPLETED', updated_at = CURRENT_TIMESTAMP "
      + "WHERE id = #{id} AND status = 'ACCEPTED'")
  int updateCompleted(@Param("id") long id);

  @Update("UPDATE swap_request SET status = 'CANCELLED', updated_at = CURRENT_TIMESTAMP "
      + "WHERE id = #{id} AND status IN ('OPEN', 'ACCEPTED')")
  int updateCancelled(@Param("id") long id);

  @Update("UPDATE swap_request SET status = 'OPEN', to_dm_id = NULL, updated_at = CURRENT_TIMESTAMP "
      + "WHERE id = #{id} AND status = 'ACCEPTED'")
  int updateReopened(@Param("id") long id);
}
