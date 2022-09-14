package ru.babin.babindisk.constant;

public class Constant {
    public static final String QUERY_FIND_WRONG_PARENTS =
            "SELECT  " +
                    "   m.disk_item_id, " +
                    "   m.disk_item_parent_id, " +
                    "   m.disk_item_url, " +
                    "   m.disk_item_size, " +
                    "   m.disk_item_type, " +
                    "   m.disk_item_date " +
                    "FROM disk_item m " +
                    "LEFT JOIN disk_item l " +
                    "    ON m.disk_item_parent_id = l.disk_item_id " +
                    "    WHERE l.disk_item_type = 'FILE'";

    public static final String QUERY_FIND_BY_ID_WITH_CHILD =
            "WITH RECURSIVE hier AS (" +
                    "SELECT " +
                    "   disk_item_id, " +
                    "   disk_item_parent_id, " +
                    "   disk_item_url, " +
                    "   disk_item_size, " +
                    "   disk_item_type, " +
                    "   disk_item_date, " +
                    "   1 AS level " +
                    "FROM disk_item where disk_item_id = :id " +
                    "UNION ALL " +
                    "SELECT " +
                    "   u.disk_item_id, " +
                    "   u.disk_item_parent_id, " +
                    "   u.disk_item_url, " +
                    "   u.disk_item_size, " +
                    "   u.disk_item_type, " +
                    "   u.disk_item_date, " +
                    "   c.level + 1 " +
                    "FROM hier c " +
                    "   JOIN disk_item u ON u.disk_item_parent_id = c.disk_item_id) " +
                    "SELECT * FROM hier";
    public static final String QUERY_FIND_CHANGED_FILES_BETWEEN =
            "SELECT " +
                    "   disk_item_id,  " +
                    "   disk_item_parent_id,  " +
                    "   disk_item_url,  " +
                    "   disk_item_size,  " +
                    "   disk_item_type,  " +
                    "   disk_item_date " +
                    "FROM disk_item " +
                    "WHERE disk_item_type = 'FILE' AND disk_item_date BETWEEN :from AND :to";
    public static final String QUERY_UPDATE_FOLDERS_DATE_UP =
            "WITH RECURSIVE hierarchy AS (SELECT disk_item_id, " +
                    "                                    disk_item_parent_id, " +
                    "                                    disk_item_url, " +
                    "                                    disk_item_size disk_item_size, " +
                    "                                    disk_item_type, " +
                    "                                    disk_item_date " +
                    "                             FROM disk_item " +
                    "                             where disk_item_id IN (:ids) " +
                    "                             UNION ALL " +
                    "                             SELECT di.disk_item_id, " +
                    "                                    di.disk_item_parent_id, " +
                    "                                    di.disk_item_url, " +
                    "                                    COALESCE(di.disk_item_size, 0) as disk_item_size, " +
                    "                                    di.disk_item_type, " +
                    "                                    di.disk_item_date " +
                    "                             FROM hierarchy c " +
                    "                                      JOIN disk_item di ON di.disk_item_id = c.disk_item_parent_id) " +
                    "UPDATE disk_item " +
                    "SET disk_item_date = CAST(:date AS timestamptz) " +
                    "WHERE disk_item_id IN " +
                    "      (SELECT DISTINCT hierarchy.disk_item_id " +
                    "       FROM hierarchy " +
                    "       WHERE disk_item_type = 'FOLDER')";
    public static final String QUERY_UPDATE_FOLDERS_SIZE_DATE =
            "DROP TABLE IF EXISTS temp_hierarchy_table; " +
                    "DROP TABLE IF EXISTS temp_result_table; " +
                    "WITH RECURSIVE hierarchy AS (SELECT disk_item_id, " +
                    "                                    disk_item_parent_id, " +
                    "                                    disk_item_url, " +
                    "                                    disk_item_size disk_item_size, " +
                    "                                    disk_item_type, " +
                    "                                    CASE WHEN disk_item_parent_id IN (:ids) " +
                    "                                           THEN CAST(:date AS timestamptz) " +
                    "                                         ELSE disk_item_date " +
                    "                                    END AS disk_item_date, " +
                    "                                    1 AS           level " +
                    "                             FROM disk_item " +
                    "                             UNION ALL " +
                    "                             SELECT u.disk_item_id, " +
                    "                                    u.disk_item_parent_id, " +
                    "                                    u.disk_item_url, " +
                    "                                    COALESCE(u.disk_item_size, 0) as disk_item_size, " +
                    "                                    u.disk_item_type, " +
                    "                                    u.disk_item_date, " +
                    "                                    c.level + 1 " +
                    "                             FROM hierarchy c " +
                    "                                      JOIN disk_item u ON u.disk_item_parent_id = c.disk_item_id) " +
                    "SELECT * " +
                    "INTO TEMP temp_hierarchy_table " +
                    "FROM hierarchy " +
                    "ORDER BY level DESC; " +
                    "WITH RECURSIVE calculation AS (SELECT DISTINCT disk_item_id, " +
                    "                                               disk_item_parent_id, " +
                    "                                               COALESCE(disk_item_size, 0) disk_item_size, " +
                    "                                               disk_item_type, " +
                    "                                               disk_item_date " +
                    "                               FROM temp_hierarchy_table " +
                    "                               UNION ALL " +
                    "                               SELECT DISTINCT t.disk_item_id, " +
                    "                                               c.disk_item_parent_id, " +
                    "                                               COALESCE(t.disk_item_size, 0), " +
                    "                                               t.disk_item_type, " +
                    "                                               t.disk_item_date " +
                    "                               FROM calculation c " +
                    "                                        JOIN temp_hierarchy_table t ON t.disk_item_parent_id = c.disk_item_id) " +
                    "SELECT " +
                    "    disk_item_id,  " +
                    "    disk_item_parent_id,  " +
                    "    disk_item_size,  " +
                    "    disk_item_type,  " +
                    "    disk_item_date  " +
                    "INTO TEMP temp_result_table " +
                    "FROM calculation; " +
                    "UPDATE disk_item di_update " +
                    "SET disk_item_size = sq.disk_item_size, " +
                    "    disk_item_date = sq.disk_item_date " +
                    "FROM (SELECT calc.disk_item_id, " +
                    "             calc.disk_item_size, " +
                    "             CASE " +
                    "               WHEN di.disk_item_date > calc.disk_item_date " +
                    "               THEN di.disk_item_date ELSE calc.disk_item_date " +
                    "             END AS disk_item_date, " +
                    "             di.disk_item_parent_id, " +
                    "             di.disk_item_type, " +
                    "             di.disk_item_url " +
                    "      FROM disk_item di " +
                    "               INNER JOIN (SELECT DISTINCT t.disk_item_parent_id as disk_item_id, " +
                    "                                           max(t.disk_item_date) as disk_item_date, " +
                    "                                           sum(t.disk_item_size) as disk_item_size " +
                    "                           FROM temp_result_table t WHERE disk_item_type <> 'FOLDER' " +
                    "                           GROUP BY t.disk_item_parent_id) calc ON di.disk_item_id = calc.disk_item_id) sq " +
                    "WHERE di_update.disk_item_id = sq.disk_item_id";
    public static final String QUERY_UPSERT_DISK_HISTORY = "INSERT INTO disk_item_history (disk_item_date, disk_item_parent_id, disk_item_size, disk_item_type, disk_item_url, " +
            "                                           disk_item_id) (SELECT disk_item_date, " +
            "                                                                 disk_item_parent_id, " +
            "                                                                 disk_item_size, " +
            "                                                                 disk_item_type, " +
            "                                                                 disk_item_url, " +
            "                                                                 disk_item_id " +
            "                                                          FROM disk_item " +
            "                                                          WHERE disk_item.disk_item_date = :date)";

    public static final String QUERY_GET_DISK_HISTORY = "SELECT " +
                                                        " * " +
                                                        " FROM disk_item_history " +
                                                        " WHERE disk_item_id = :disk_item_id " +
                                                        "   AND disk_item_date >= :date_start " +
                                                        "   AND disk_item_date < :date_end " +
                                                        " ORDER BY disk_item_date";
    private Constant() {

    }
}
