package com.stackoverflow.controller.user;

import com.stackoverflow.entity.UserGroup;
import com.stackoverflow.entity.User;
import com.stackoverflow.service.common.GroupService;
import com.stackoverflow.service.common.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> createGroup(
            @RequestBody UserGroup group,
            @AuthenticationPrincipal User currentUser) {
        group.setCreator(currentUser);
        group.getMembers().add(currentUser);
        UserGroup savedGroup = groupService.createGroup(group);
        return ResponseEntity.ok(savedGroup);
    }

    @GetMapping
    public ResponseEntity<?> getMyGroups(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(groupService.getGroupsByMember(currentUser));
    }

    @PostMapping("/{groupId}/members/{userId}")
    public ResponseEntity<?> addMember(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser) {
        UserGroup group = groupService.findById(groupId)
            .orElseThrow(() -> new RuntimeException("Group not found"));
            
        if (!currentUser.equals(group.getCreator())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Only group creator can add members");
        }
        
        User newMember = userService.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        groupService.addMember(group, newMember);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<?> removeMember(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser) {
        UserGroup group = groupService.findById(groupId)
            .orElseThrow(() -> new RuntimeException("Group not found"));
            
        if (!currentUser.equals(group.getCreator())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Only group creator can remove members");
        }
        
        User member = userService.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        groupService.removeMember(group, member);
        return ResponseEntity.ok().build();
    }

    @MessageMapping("/group.activity")
    @SendTo("/topic/group.updates")
    public String sendGroupActivity(String activity) {
        return activity;
    }
}