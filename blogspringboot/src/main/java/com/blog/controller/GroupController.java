package com.blog.controller;

import com.blog.entity.Group;
import com.blog.model.vo.ResultVO;
import com.blog.service.GroupService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api("群组接口")
@RestController("/group")
public class GroupController {
    @Autowired
    GroupService groupService;

    @PostMapping("/add")
    public ResultVO<?> add(@RequestBody Group group) {
        groupService.creat(group);
        return ResultVO.ok();
    }

    @PostMapping("/users")
    public ResultVO<?> creatGroup(@RequestBody Integer groupId, List<Integer> userIds) {
        groupService.insert(groupId, userIds);
        return ResultVO.ok();
    }

    @DeleteMapping("/{groupId}")
    public ResultVO<?> deleteGroup(@PathVariable Integer groupId) {
        groupService.deleteGroup(groupId);
        return ResultVO.ok();
    }

    @DeleteMapping("/users")
    public ResultVO<?> deleteGroupUser(@RequestBody List<Integer> userIds) {
        groupService.deleteGroupUser(userIds);
        return ResultVO.ok();
    }

    @GetMapping("/{userid}")
    public ResultVO<List<Group>> getGroup(@PathVariable Integer userid) {
        List<Group> groupList = groupService.getGroup(userid);
        return ResultVO.ok(groupList);
    }
}
