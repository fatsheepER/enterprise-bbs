package com.feiyang.bbs.service.impl;

import java.util.List;

import com.feiyang.bbs.common.BusinessException;
import com.feiyang.bbs.common.ErrorCode;
import com.feiyang.bbs.mapper.BoardMapper;
import com.feiyang.bbs.service.BoardService;
import com.feiyang.bbs.vo.BoardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {
    private final BoardMapper boardMapper;

    @Override
    public List<BoardVO> listVisibleBoards(String keyword) {
        return boardMapper.selectVisibleBoards(trimToNull(keyword));
    }

    @Override
    public BoardVO getVisibleBoard(Long id) {
        BoardVO board = boardMapper.selectVisibleBoardVOById(id);
        if (board == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return board;
    }

    private String trimToNull(String value) {
        String trimmed = value == null ? null : value.trim();
        return trimmed == null || trimmed.isEmpty() ? null : trimmed;
    }
}
