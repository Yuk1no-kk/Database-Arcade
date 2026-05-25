package com.example.arcadesystem.controller;

import com.example.arcadesystem.dto.ApiResponse;
import com.example.arcadesystem.model.Member;
import com.example.arcadesystem.service.MemberService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "member_id") String sort,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(memberService.list(keyword, sort, order, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<Member> getById(@PathVariable int id) {
        return ApiResponse.ok(memberService.getById(id));
    }

    @PostMapping
    public ApiResponse<Member> create(@RequestBody Member member) {
        return ApiResponse.ok("新增成功", memberService.create(member));
    }

    @PutMapping("/{id}")
    public ApiResponse<Member> update(@PathVariable int id, @RequestBody Member member) {
        return ApiResponse.ok("修改成功", memberService.update(id, member));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable int id) {
        memberService.delete(id);
        return ApiResponse.ok("删除成功", null);
    }
}
