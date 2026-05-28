package com.feiyang.bbs.mapper;

import java.util.List;

import com.feiyang.bbs.entity.Board;
import com.feiyang.bbs.vo.BoardVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BoardMapper {
    int insert(Board board);

    int update(Board board);

    Board findById(@Param("id") Long id);

    BoardVO selectBoardVOById(@Param("id") Long id);

    BoardVO selectVisibleBoardVOById(@Param("id") Long id);

    List<BoardVO> selectVisibleBoards(@Param("keyword") String keyword);

    List<BoardVO> selectAdminBoards(@Param("id") Long id,
                                    @Param("keyword") String keyword,
                                    @Param("status") Integer status);

    int countByName(@Param("name") String name);

    int countByNameExcludeId(@Param("name") String name, @Param("id") Long id);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int deleteById(@Param("id") Long id);
}
