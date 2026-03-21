package com.saleshub.service;

import com.saleshub.entity.SysDict;
import java.util.List;

public interface DictService {
    List<SysDict> listByType(String type);
    List<SysDict> listAllByType(String type);
    List<SysDict> listAll();
    SysDict create(SysDict dict);
    SysDict update(Long id, SysDict dict);
    void delete(Long id);
    void clearIcon(Long id);
}
