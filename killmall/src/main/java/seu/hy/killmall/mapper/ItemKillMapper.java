package seu.hy.killmall.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import seu.hy.killmall.pojo.ItemKill;
import java.util.List;

@Mapper
@Repository
public interface ItemKillMapper {
    @Select(" SELECT  " +
            "      a.*,  " +
            "      b.name AS itemName,  " +
            "      (  " +
            "        CASE WHEN (now() BETWEEN a.start_time AND a.end_time AND a.total > 0)  " +
            "          THEN 1  " +
            "        ELSE 0  " +
            "        END  " +
            "      )      AS canKill  " +
            "    FROM item_kill AS a LEFT JOIN item AS b ON b.id = a.item_id  " +
            "    WHERE a.is_active = 1")
    List<ItemKill> selectAll();

    ItemKill selectById(@Param("id") Integer id);

    int updateKillItem(@Param("killId") Integer killId);

    ItemKill selectByIdV2(@Param("id") Integer id);

    int updateKillItemV2(@Param("killId") Integer killId);
}