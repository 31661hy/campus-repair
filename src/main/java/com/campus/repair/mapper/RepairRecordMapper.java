package com.campus.repair.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.repair.entity.RepairRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 处理记录 Mapper：继承 BaseMapper 后自带单表增删改查。
 */
@Mapper
public interface RepairRecordMapper extends BaseMapper<RepairRecord> {
}
