package com.feiyang.bbs.service;

import java.util.List;

import com.feiyang.bbs.vo.BoardVO;

public interface BoardService {
    List<BoardVO> listVisibleBoards(String keyword);

    BoardVO getVisibleBoard(Long id);
}
