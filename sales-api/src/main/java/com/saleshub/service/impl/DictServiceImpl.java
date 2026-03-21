package com.saleshub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.saleshub.common.BusinessException;
import com.saleshub.entity.SysDict;
import com.saleshub.mapper.SysDictMapper;
import com.saleshub.service.DictService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DictServiceImpl implements DictService {

    private final SysDictMapper dictMapper;

    @Override
    public List<SysDict> listByType(String type) {
        return dictMapper.selectList(
            new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getType, type)
                .eq(SysDict::getStatus, "active")
                .orderByAsc(SysDict::getSort)
        );
    }

    @Override
    public List<SysDict> listAllByType(String type) {
        return dictMapper.selectList(
            new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getType, type)
                .orderByAsc(SysDict::getSort)
        );
    }

    @Override
    public List<SysDict> listAll() {
        return dictMapper.selectList(
            new LambdaQueryWrapper<SysDict>().orderByAsc(SysDict::getType).orderByAsc(SysDict::getSort)
        );
    }

    @Override
    public SysDict create(SysDict dict) {
        if (dict.getStatus() == null) dict.setStatus("active");
        if (dict.getSort() == null) dict.setSort(0);
        // 忽略软删除查找（UNIQUE KEY 不含 deleted，需处理软删除后重建的场景）
        SysDict existing = dictMapper.findByTypeAndCodeIgnoreDeleted(dict.getType(), dict.getCode());
        if (existing != null) {
            // 复活或更新（含软删除的记录，需绕过 @TableLogic 的 WHERE deleted=0）
            existing.setDeleted(0);
            if (dict.getLabel() != null) existing.setLabel(dict.getLabel());
            if (dict.getSort() != null) existing.setSort(dict.getSort());
            existing.setStatus(dict.getStatus() != null ? dict.getStatus() : "active");
            dictMapper.forceUpdateById(existing);
            return existing;
        }
        dictMapper.insert(dict);
        return dict;
    }

    @Override
    public SysDict update(Long id, SysDict dict) {
        SysDict existing = dictMapper.selectById(id);
        if (existing == null) throw new BusinessException("字典项不存在");
        if (dict.getType() != null) existing.setType(dict.getType());
        if (dict.getCode() != null) existing.setCode(dict.getCode());
        if (dict.getLabel() != null) existing.setLabel(dict.getLabel());
        if (dict.getSort() != null) existing.setSort(dict.getSort());
        if (dict.getStatus() != null) existing.setStatus(dict.getStatus());
        if (dict.getIconUrl() != null) existing.setIconUrl(dict.getIconUrl().isEmpty() ? null : dict.getIconUrl());
        dictMapper.updateById(existing);
        return existing;
    }

    @Override
    public void delete(Long id) {
        SysDict existing = dictMapper.selectById(id);
        if (existing == null) throw new BusinessException("字典项不存在");
        dictMapper.deleteById(id);
    }

    @Override
    public void clearIcon(Long id) {
        SysDict existing = dictMapper.selectById(id);
        if (existing == null) throw new BusinessException("字典项不存在");
        existing.setIconUrl(null);
        dictMapper.updateById(existing);
    }
}
