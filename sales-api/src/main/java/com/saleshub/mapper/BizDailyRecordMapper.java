package com.saleshub.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.saleshub.entity.BizDailyRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface BizDailyRecordMapper extends BaseMapper<BizDailyRecord> {

    @Select("SELECT COALESCE(SUM(dgmv), 0) FROM biz_daily_record ${ew.customSqlSegment}")
    BigDecimal sumDgmv(@Param(Constants.WRAPPER) LambdaQueryWrapper<BizDailyRecord> wrapper);
}
