package com.mordiniaa.backend.utils;

import com.mordiniaa.backend.exceptions.BadRequestException;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

@Component
public class MongoIdUtils {

    public ObjectId getObjectId(String id) {
        if (!ObjectId.isValid(id)) {
            throw new BadRequestException("Invalid Parameter");
        }
        return new ObjectId(id);
    }
}
