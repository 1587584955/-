package seu.hy.killmall.mapper;


import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import seu.hy.killmall.pojo.ItemKillSuccess;
import seu.hy.killmall.pojo.KillSuccessUserInfo;

import java.util.List;
@Mapper
@Repository
public interface ItemKillSuccessMapper {
    int deleteByPrimaryKey(String code);

    int insert(ItemKillSuccess record);

    @Insert("insert into item_kill_success(code,item_id,kill_id,user_id,status,create_time) values(#{code},#{itemId},#{killId},#{userId},#{status},#{createTime})")
    int insertSelective(ItemKillSuccess record);

    ItemKillSuccess selectByPrimaryKey(String code);

    int updateByPrimaryKeySelective(ItemKillSuccess record);

    int updateByPrimaryKey(ItemKillSuccess record);

    @Select("SELECT COUNT(1) AS total FROM item_kill_success " +
            "WHERE user_id = #{userId} AND kill_id = #{killId} AND `status` IN (0)")
    int countByKillUserId(@Param("killId") Integer killId, @Param("userId") Integer userId);

    @Select(" SELECT\n" +
            "      a.*,\n" +
            "      b.user_name,\n" +
            "      b.phone,\n" +
            "      b.email,\n" +
            "      c.name AS itemName\n" +
            "    FROM item_kill_success AS a\n" +
            "      LEFT JOIN user b ON b.id = a.user_id\n" +
            "      LEFT JOIN item c ON c.id = a.item_id\n" +
            "    WHERE a.code = #{code}\n" +
            "          AND b.is_active = 1")
    KillSuccessUserInfo selectByCode(@Param("code") String code);

    int expireOrder(@Param("code") String code);

    List<ItemKillSuccess> selectExpireOrders();
}