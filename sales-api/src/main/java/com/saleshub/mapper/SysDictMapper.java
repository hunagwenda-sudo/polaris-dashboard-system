package com.saleshub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.saleshub.entity.SysDict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SysDictMapper extends BaseMapper<SysDict> {
    /** 忽略软删除，按 type+code 查找（用于 upsert 场景） */
    @Select("SELECT id,type,code,label,sort,status,deleted FROM sys_dict WHERE type=#{type} AND code=#{code} LIMIT 1")
    SysDict findByTypeAndCodeIgnoreDeleted(String type, String code);

    /** 忽略软删除条件，强制更新（用于复活软删除记录） */
    @Update("UPDATE sys_dict SET type=#{d.type}, code=#{d.code}, label=#{d.label}, sort=#{d.sort}, status=#{d.status}, deleted=#{d.deleted} WHERE id=#{d.id}")
    int forceUpdateById(@Param("d") SysDict dict);
}
