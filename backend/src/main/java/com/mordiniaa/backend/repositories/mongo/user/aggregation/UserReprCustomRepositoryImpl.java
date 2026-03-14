package com.mordiniaa.backend.repositories.mongo.user.aggregation;

import com.mordiniaa.backend.exceptions.ArgumentNotPresentException;
import com.mordiniaa.backend.models.user.mongodb.UserRepresentation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserReprCustomRepositoryImpl implements UserReprCustomRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public boolean allUsersAvailable(UUID... userIds) {

        if (userIds == null)
            throw new ArgumentNotPresentException("Users Of Interest Not Present In Collection");

        Set<UUID> ids = Set.of(userIds);

        return allUsersAvailable(ids);
    }

    @Override
    public boolean allUsersAvailable(Set<UUID> userIds) {
        if (userIds.isEmpty())
            throw new ArgumentNotPresentException("Users Of Interest Not Present In Collection");

        Query query = Query.query(
                Criteria.where("userId").in(userIds)
                        .and("deleted").is(false)
        );

        long count = mongoTemplate.count(query, UserRepresentation.class);
        return count == userIds.size();
    }
}
