package com.saleshub.service;

import com.saleshub.dto.GroupRequest;
import com.saleshub.entity.SysGroup;
import java.util.List;
import java.util.Map;

public interface GroupService {
    List<Map<String, Object>> listGroups();
    SysGroup createGroup(GroupRequest request);
    SysGroup updateGroup(Long id, GroupRequest request);
    void deleteGroup(Long id);
    void addMember(Long groupId, Long userId);
    void removeMember(Long userId);
    List<Map<String, Object>> getMembers(Long groupId);
    List<Map<String, Object>> getAvailableMembers(Long groupId);
    boolean isGroupLeader(Long groupId, Long userId);
}
