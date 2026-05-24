package com.feiyang.bbs.controller;

import java.util.List;

import com.feiyang.bbs.common.Result;
import com.feiyang.bbs.service.BoardService;
import com.feiyang.bbs.vo.BoardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;

    @GetMapping
    public Result<List<BoardVO>> list(@RequestParam(required = false) String keyword) {
        return Result.success(boardService.listVisibleBoards(keyword));
    }

    @GetMapping("/{id}")
    public Result<BoardVO> detail(@PathVariable Long id) {
        return Result.success(boardService.getVisibleBoard(id));
    }
}
