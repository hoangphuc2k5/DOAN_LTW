package com.stackoverflow.service.common;

import com.stackoverflow.entity.UserGroup;
import com.stackoverflow.entity.User;
import com.stackoverflow.repository.UserGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GroupService {

    @Autowired
    private UserGroupRepository groupRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public UserGroup createGroup(UserGroup group) {
        UserGroup savedGroup = groupRepository.save(group);
        // Notify group creator through WebSocket
        messagingTemplate.convertAndSendToUser(
            group.getCreator().getUsername(),
            "/queue/groups",
            "Group " + group.getName() + " created successfully"
        );
        return savedGroup;
    }

    public void addMember(UserGroup group, User user) {
        group.getMembers().add(user);
        groupRepository.save(group);
        
        // Notify new member through WebSocket
        messagingTemplate.convertAndSendToUser(
            user.getUsername(),
            "/queue/groups",
            "You have been added to group " + group.getName()
        );
    }

    public void removeMember(UserGroup group, User user) {
        group.getMembers().remove(user);
        groupRepository.save(group);
        
        // Notify removed member through WebSocket
        messagingTemplate.convertAndSendToUser(
            user.getUsername(),
            "/queue/groups",
            "You have been removed from group " + group.getName()
        );
    }

    public List<UserGroup> getGroupsByMember(User user) {
        return groupRepository.findByMembersContaining(user);
    }

    public Optional<UserGroup> findById(Long id) {
        return groupRepository.findById(id);
    }

    public void deleteGroup(UserGroup group) {
        // Notify all members through WebSocket
        for (User member : group.getMembers()) {
            messagingTemplate.convertAndSendToUser(
                member.getUsername(),
                "/queue/groups",
                "Group " + group.getName() + " has been deleted"
            );
        }
        groupRepository.delete(group);
    }
}