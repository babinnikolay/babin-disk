package ru.babin.babindisk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.babin.babindisk.constant.Constant;
import ru.babin.babindisk.model.DiskItem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface DiskItemRepository extends JpaRepository<DiskItem, String> {

    @Query(value = Constant.QUERY_FIND_WRONG_PARENTS, nativeQuery = true)
    Set<DiskItem> findWrongParents();

    @Query(value = Constant.QUERY_FIND_BY_ID_WITH_CHILD, nativeQuery = true)
    List<DiskItem> findByIdWithChild(@Param("id") String id);

    @Query(value = Constant.QUERY_FIND_CHANGED_FILES_BETWEEN, nativeQuery = true)
    List<DiskItem> findAllChangedFilesBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Modifying
    @Query(value = Constant.QUERY_UPDATE_FOLDERS_DATE_UP, nativeQuery = true)
    int updateFoldersDateUp(@Param("date") LocalDateTime date, @Param("ids") List<String> ids);

    @Modifying
    @Query(value = Constant.QUERY_UPDATE_FOLDERS_SIZE_DATE, nativeQuery = true)
    int updateFoldersSizeAndDate(@Param("date") LocalDateTime date, @Param("ids") List<String> deletedIds);
}
