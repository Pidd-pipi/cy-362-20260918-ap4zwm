package com.generated.ldmurdergame.repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.generated.ldmurdergame.model.dm.Dm;

/**
 * 排班域全部数据访问。SQL 保持 H2(MODE=PostgreSQL) 与 PostgreSQL 双方言可执行，
 * 不使用数组类型、ON CONFLICT 等方言特性。
 */
@Repository
public class SchedulingRepository {
  private static final DateTimeFormatter CREATED_AT_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.CHINA);

  private final JdbcTemplate jdbc;

  public SchedulingRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public record ShiftRow(
      long id,
      LocalDate workDate,
      String startTime,
      String requiredSkill,
      int requiredCount) {
  }

  public record AssignmentRow(long id, long shiftId, long dmId, LocalDate workDate, String startTime) {
  }

  public record SwapRow(
      long id,
      long fromShiftId,
      LocalDate fromWorkDate,
      String fromStartTime,
      String requiredSkill,
      Long toShiftId,
      LocalDate toWorkDate,
      String toStartTime,
      long requesterId,
      long targetId,
      String status,
      String createdAt) {
  }

  public record AssigneeRow(long shiftId, Dm dm) {
  }

  // ---------- DM ----------

  public List<Dm> findAllDms() {
    return jdbc.query("SELECT id, name, skills FROM dm ORDER BY id",
        (rs, n) -> new Dm(rs.getLong("id"), rs.getString("name"), parseSkills(rs.getString("skills"))));
  }

  public Dm findDm(Long dmId) {
    List<Dm> list = jdbc.query("SELECT id, name, skills FROM dm WHERE id = ?",
        (rs, n) -> new Dm(rs.getLong("id"), rs.getString("name"), parseSkills(rs.getString("skills"))),
        dmId);
    return list.isEmpty() ? null : list.get(0);
  }

  // ---------- 班次 ----------

  public List<ShiftRow> findShiftsBetween(LocalDate from, LocalDate to) {
    return jdbc.query("""
        SELECT id, work_date, start_time, required_skill, required_count
        FROM dm_shift WHERE work_date BETWEEN ? AND ?
        ORDER BY work_date, start_time, required_skill
        """,
        (rs, n) -> new ShiftRow(
            rs.getLong("id"),
            rs.getDate("work_date").toLocalDate(),
            rs.getString("start_time"),
            rs.getString("required_skill"),
            rs.getInt("required_count")),
        Date.valueOf(from), Date.valueOf(to));
  }

  /** 事务内行锁读取，配合服务层单例锁，保证并发认领/换班的一致性。 */
  public ShiftRow findShiftForUpdate(Long shiftId) {
    List<ShiftRow> list = jdbc.query("""
        SELECT id, work_date, start_time, required_skill, required_count
        FROM dm_shift WHERE id = ? FOR UPDATE
        """,
        (rs, n) -> new ShiftRow(
            rs.getLong("id"),
            rs.getDate("work_date").toLocalDate(),
            rs.getString("start_time"),
            rs.getString("required_skill"),
            rs.getInt("required_count")),
        shiftId);
    return list.isEmpty() ? null : list.get(0);
  }

  public boolean existsShift(LocalDate workDate, String startTime, String requiredSkill) {
    Integer count = jdbc.queryForObject(
        "SELECT COUNT(*) FROM dm_shift WHERE work_date = ? AND start_time = ? AND required_skill = ?",
        Integer.class, Date.valueOf(workDate), startTime, requiredSkill);
    return count != null && count > 0;
  }

  public void insertShift(LocalDate workDate, String startTime, String requiredSkill, int requiredCount) {
    jdbc.update("""
        INSERT INTO dm_shift (work_date, start_time, required_skill, required_count)
        VALUES (?, ?, ?, ?)
        """, Date.valueOf(workDate), startTime, requiredSkill, requiredCount);
  }

  public List<AssigneeRow> findAssigneesBetween(LocalDate from, LocalDate to) {
    return jdbc.query("""
        SELECT a.shift_id AS shift_id, d.id AS dm_id, d.name AS name, d.skills AS skills
        FROM shift_assignment a
        JOIN dm d ON d.id = a.dm_id
        WHERE a.work_date BETWEEN ? AND ?
        ORDER BY a.id
        """,
        (rs, n) -> new AssigneeRow(
            rs.getLong("shift_id"),
            new Dm(rs.getLong("dm_id"), rs.getString("name"), parseSkills(rs.getString("skills")))),
        Date.valueOf(from), Date.valueOf(to));
  }

  public List<AssignmentRow> findAssignmentsForUpdate(long shiftId) {
    return jdbc.query("""
        SELECT id, shift_id, dm_id, work_date, start_time
        FROM shift_assignment WHERE shift_id = ? FOR UPDATE
        """,
        (rs, n) -> new AssignmentRow(
            rs.getLong("id"), rs.getLong("shift_id"), rs.getLong("dm_id"),
            rs.getDate("work_date").toLocalDate(), rs.getString("start_time")),
        shiftId);
  }

  public boolean hasAssignment(long dmId, LocalDate workDate, String startTime) {
    Integer count = jdbc.queryForObject(
        "SELECT COUNT(*) FROM shift_assignment WHERE dm_id = ? AND work_date = ? AND start_time = ?",
        Integer.class, dmId, Date.valueOf(workDate), startTime);
    return count != null && count > 0;
  }

  public void insertAssignment(long shiftId, long dmId, LocalDate workDate, String startTime) {
    jdbc.update("""
        INSERT INTO shift_assignment (shift_id, dm_id, work_date, start_time)
        VALUES (?, ?, ?, ?)
        """, shiftId, dmId, Date.valueOf(workDate), startTime);
  }

  public void deleteAssignment(long shiftId, long dmId) {
    jdbc.update("DELETE FROM shift_assignment WHERE shift_id = ? AND dm_id = ?", shiftId, dmId);
  }

  // ---------- 换班 ----------

  public boolean hasPendingSwapFromShift(long requesterId, long fromShiftId) {
    Integer count = jdbc.queryForObject("""
        SELECT COUNT(*) FROM swap_request
        WHERE requester_id = ? AND from_shift_id = ? AND status = 'PENDING'
        """, Integer.class, requesterId, fromShiftId);
    return count != null && count > 0;
  }

  public void insertSwap(
      long fromShiftId, LocalDate fromDate, String fromTime, String requiredSkill,
      Long toShiftId, LocalDate toDate, String toTime,
      long requesterId, long targetId) {
    jdbc.update("""
        INSERT INTO swap_request (
          from_shift_id, from_work_date, from_start_time, required_skill,
          to_shift_id, to_work_date, to_start_time, requester_id, target_id, status)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'PENDING')
        """,
        fromShiftId, Date.valueOf(fromDate), fromTime, requiredSkill,
        toShiftId, toDate == null ? null : Date.valueOf(toDate), toTime,
        requesterId, targetId);
  }

  public SwapRow findSwapForUpdate(Long swapId) {
    List<SwapRow> list = jdbc.query("""
        SELECT id, from_shift_id, from_work_date, from_start_time, required_skill,
               to_shift_id, to_work_date, to_start_time, requester_id, target_id,
               status, created_at
        FROM swap_request WHERE id = ? FOR UPDATE
        """,
        (rs, n) -> new SwapRow(
            rs.getLong("id"),
            rs.getLong("from_shift_id"),
            rs.getDate("from_work_date").toLocalDate(),
            rs.getString("from_start_time"),
            rs.getString("required_skill"),
            (Long) rs.getObject("to_shift_id"),
            rs.getDate("to_work_date") == null ? null : rs.getDate("to_work_date").toLocalDate(),
            rs.getString("to_start_time"),
            rs.getLong("requester_id"),
            rs.getLong("target_id"),
            rs.getString("status"),
            formatTimestamp(rs.getTimestamp("created_at"))),
        swapId);
    return list.isEmpty() ? null : list.get(0);
  }

  public void updateSwapStatus(long swapId, String status) {
    jdbc.update("UPDATE swap_request SET status = ? WHERE id = ?", status, swapId);
  }

  /** dmId 为空时不过问参与人；status 为空时不过滤状态。 */
  public List<SwapRow> findSwaps(LocalDate from, LocalDate to, Long dmId, String status) {
    StringBuilder sql = new StringBuilder("""
        SELECT id, from_shift_id, from_work_date, from_start_time, required_skill,
               to_shift_id, to_work_date, to_start_time, requester_id, target_id,
               status, created_at
        FROM swap_request
        WHERE from_work_date BETWEEN ? AND ?
        """);
    List<Object> params = new ArrayList<>(List.of(Date.valueOf(from), Date.valueOf(to)));
    if (dmId != null) {
      sql.append(" AND (requester_id = ? OR target_id = ?)");
      params.add(dmId);
      params.add(dmId);
    }
    if (status != null && !status.isBlank()) {
      sql.append(" AND status = ?");
      params.add(status);
    }
    sql.append(" ORDER BY created_at DESC, id DESC");
    return jdbc.query(sql.toString(),
        (rs, n) -> new SwapRow(
            rs.getLong("id"),
            rs.getLong("from_shift_id"),
            rs.getDate("from_work_date").toLocalDate(),
            rs.getString("from_start_time"),
            rs.getString("required_skill"),
            (Long) rs.getObject("to_shift_id"),
            rs.getDate("to_work_date") == null ? null : rs.getDate("to_work_date").toLocalDate(),
            rs.getString("to_start_time"),
            rs.getLong("requester_id"),
            rs.getLong("target_id"),
            rs.getString("status"),
            formatTimestamp(rs.getTimestamp("created_at"))),
        params.toArray());
  }

  private static List<String> parseSkills(String raw) {
    if (raw == null || raw.isBlank()) {
      return List.of();
    }
    return Arrays.stream(raw.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .toList();
  }

  private static String formatTimestamp(Timestamp timestamp) {
    if (timestamp == null) {
      return "";
    }
    LocalDateTime value = timestamp.toLocalDateTime();
    return value.format(CREATED_AT_FORMAT);
  }
}
