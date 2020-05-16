package seu.hy.killmall.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import seu.hy.killmall.pojo.ItemKillSuccess;

import java.util.List;
@Mapper
@Repository
public interface ItemKillSuccessMapper {
    int deleteByPrimaryKey(String code);

    int insert(ItemKillSuccess record);

    int insertSelective(ItemKillSuccess record);

    ItemKillSuccess selectByPrimaryKey(String code);

    int updateByPrimaryKeySelective(ItemKillSuccess record);

    int updateByPrimaryKey(ItemKillSuccess record);

    @Select("SELECT COUNT(1) AS total FROM item_kill_success " +
            "WHERE user_id = #{userId} AND kill_id = #{killId} AND `status` IN (0)")
    int countByKillUserId(@Param("killId") Integer killId, @Param("userId") Integer userId);

//    KillSuccessUserInfo selectByCode(@Param("code") String code);

    int expireOrder(@Param("code") String code);

    List<ItemKillSuccess> selectExpireOrders();
}