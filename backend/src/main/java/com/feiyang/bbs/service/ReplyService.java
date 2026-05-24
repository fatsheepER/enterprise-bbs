package com.feiyang.bbs.service;

import java.util.List;

import com.feiyang.bbs.dto.ReplyCreateDTO;
import com.feiyang.bbs.vo.ReplyVO;

public interface ReplyService {
    List<ReplyVO> listVisibleReplies(Long postId);

    ReplyVO createReply(Long currentUserId, Long postId, ReplyCreateDTO dto);

    Boolean deleteOwnReply(Long currentUserId, Long replyId);
}
