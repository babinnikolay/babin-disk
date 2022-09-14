package ru.babin.babindisk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.babin.babindisk.constant.Constant;
import ru.babin.babindisk.model.DiskItemHistory;

import java.time.LocalDateTime;
import java.util.List;

public interface DiskItemHistoryRepository extends JpaRepository<DiskItemHistory, Long> {

    @Modifying
    @Query(value = Constant.QUERY_UPSERT_DISK_HISTORY, nativeQuery = true)
    int upsertDiskItemsHistory(@Param("date") LocalDateTime updateDate);

    @Query(value = Constant.QUERY_GET_DISK_HISTORY, nativeQuery = true)
    List<DiskItemHistory> getHistoryByDiskItemId(@Param("disk_item_id") String id, @Param("date_start")  LocalDateTime dateStart, @Param("date_end") LocalDateTime dateEnd);

}
