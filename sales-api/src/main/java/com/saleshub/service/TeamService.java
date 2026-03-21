package com.saleshub.service;

import com.saleshub.dto.TeamRequest;
import com.saleshub.entity.SysTeam;
import java.util.List;
import java.util.Map;

public interface TeamService {
    List<Map<String, Object>> listTeamsWithStats();
    List<Map<String, Object>> listPartners();
    SysTeam createTeam(TeamRequest request);
    SysTeam updateTeam(Long id, TeamRequest request);
    void deleteTeam(Long id);
    void addMember(Long teamId, Long userId);
    void removeMember(Long teamId, Long userId);
    List<Map<String, Object>> getAvailableMembers(Long teamId);
    List<Map<String, Object>> getMembers(Long teamId);
    boolean isTeamLeader(Long teamId, Long userId);
    Map<String, Object> getTeamRecords(Long teamId, int page, int size);
}
