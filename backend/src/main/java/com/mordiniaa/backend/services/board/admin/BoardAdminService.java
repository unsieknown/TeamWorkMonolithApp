package com.mordiniaa.backend.services.board.admin;

import com.mordiniaa.backend.models.board.Board;
import com.mordiniaa.backend.models.board.BoardMember;
import com.mordiniaa.backend.models.board.permissions.BoardPermission;
import com.mordiniaa.backend.models.board.permissions.CategoryPermissions;
import com.mordiniaa.backend.models.board.permissions.CommentPermission;
import com.mordiniaa.backend.models.board.permissions.TaskPermission;
import com.mordiniaa.backend.models.user.mysql.AppRole;
import com.mordiniaa.backend.models.user.mysql.User;
import com.mordiniaa.backend.repositories.mongo.board.BoardRepository;
import com.mordiniaa.backend.repositories.mysql.TeamRepository;
import com.mordiniaa.backend.services.user.MongoUserService;
import com.mordiniaa.backend.services.user.UserService;
import com.mordiniaa.backend.utils.MongoIdUtils;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BoardAdminService {

    private final MongoTemplate mongoTemplate;
    private final MongoUserService mongoUserService;
    private final TeamRepository teamRepository;
    private final BoardRepository boardRepository;
    private final MongoIdUtils mongoIdUtils;
    private final UserService userService;

    /**
     * Method Called By Event During The Process Of Deactivating User
     * @param userId Deactivated User To Remove From Board
     */
    @Transactional
    public void handleUserDeletion(UUID userId) {

        User user = userService.getUser(userId);

        if (user.getRole().getAppRole().equals(AppRole.ROLE_MANAGER))
            removeBoardOwner(userId);
        else
            removeUserFromBoards(userId);
    }

    private void removeBoardOwner(UUID userId) {

        Query query = Query.query(Criteria.where("owner.userId").is(userId));
        Update update = new Update().unset("owner");

        mongoTemplate.updateMulti(query, update, Board.class);
    }

    private void removeUserFromBoards(UUID userId) {

        Query query = Query.query(Criteria.where("members.userId").is(userId));
        Update update = new Update().pull("members", Criteria.where("userId").is(userId));
        mongoTemplate.updateMulti(query, update, Board.class);
    }

    @Transactional
    public void setBoardOwner(String bId, UUID userId, UUID teamId) {

        ObjectId boardId = mongoIdUtils.getObjectId(bId);

        mongoUserService.checkUserAvailability(userId);

        if (!teamRepository.existsTeamByTeamIdAndManager_UserId(teamId, userId))
            throw new RuntimeException(); // TODO: Change In Exceptions Section

        Board board = boardRepository.findByIdAndTeamId(boardId, teamId)
                .orElseThrow(RuntimeException::new); // TODO: Change In Exceptions Section

        BoardMember ownerMember = createBoardOwner(userId);
        board.setOwner(ownerMember);
        boardRepository.save(board);
    }

    public BoardMember createBoardOwner(UUID userId) {
        BoardMember ownerMember = new BoardMember(userId);
        ownerMember.setBoardPermissions(Set.of(BoardPermission.values()));
        ownerMember.setCategoryPermissions(Set.of(CategoryPermissions.values()));
        ownerMember.setTaskPermissions(Set.of(TaskPermission.values()));
        ownerMember.setCommentPermissions(Set.of(CommentPermission.values()));
        return ownerMember;
    }
}
