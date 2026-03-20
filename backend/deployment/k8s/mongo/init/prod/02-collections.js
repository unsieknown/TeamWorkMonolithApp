db = db.getSiblingDB("team-work")
db.auth("team-work-admin", "password")

db.runCommand({
    create: "notes",
    validator: {
        $jsonSchema: {
            bsonType: "object",
            title: "Note Object Validation",
            required: ["title", "ownerId", "content", "archived"],
            properties: {
                _class: {
                    bsonType: "string",
                    enum: ["regular", "deadline"]
                },
                title: {
                    bsonType: "string",
                    description: "Title is required and must be specified",
                    minLength: 3,
                    maxLength: 40
                },
                ownerId: {
                    bsonType: "binData",
                    description: "Owner must be specified"
                },
                content: {
                    bsonType: "string",
                    description: "Description Is Required",
                    maxLength: 512
                },
                archived: {
                    bsonType: "bool",
                    description: "Archived flag Is Required"
                },
                createdAt: {
                    bsonType: "date",
                    description: "Created Date Is Required"
                },
                updatedAt: {
                    bsonType: "date",
                    description: "Modification Date Is Required"
                },
                category: {
                    bsonType: "string",
                    enum: ["SHOPPING", "MEETING", "DIARY", "RECEIPT"]
                },
                status: {
                    bsonType: "string",
                    enum: ["NEW", "COMPLETED", "IN_PROGRESS", "CANCELED"]
                },
                priority: {
                    bsonType: "string",
                    enum: ["HIGH", "MEDIUM", "LOW"]
                },
                deadline: {
                    bsonType: "date",
                    description: "Deadline Is Required"
                }
            },
            oneOf: [
                {
                    properties: {
                        _class: {
                            bsonType: "string",
                            enum: ["regular"]
                        }
                    },
                    required: ["category"]
                },
                {
                    properties: {
                        _class: {
                            bsonType: "string",
                            enum: ["deadline"]
                        }
                    },
                    required: ["priority", "status", "deadline"]
                }
            ]
        }
    }
})

db.runCommand({
    create: "boards",
    validator: {
        $jsonSchema: {
            bsonType: "object",
            title: "Board Specification Schema",
            required: ["owner", "boardName", "teamId"],
            additionalProperties: false,
            properties: {
                _id: {
                    bsonType: "objectId"
                },
                teamId: {
                    bsonType: "binData",
                    title: "Team Id From MySQL",
                    description: "Team Is Required"
                },
                _class: {
                    bsonType: "string",
                    enum: ["board"]
                },
                owner: {
                    bsonType: "object",
                    title: "Owner Specification",
                    required: ["userId", "boardPermissions", "categoryPermissions", "taskPermissions", "commentPermissions"],
                    properties: {
                        userId: {
                            bsonType: "binData",
                            title: "Owner Id",
                            description: "Owner Id Is Required"
                        },
                        boardPermissions: {
                            bsonType: "array",
                            title: "User Board Permissions",
                            description: "Board Permissions Arr Is Required",
                            items: {
                                bsonType: "string",
                                enum: ["VIEW_BOARD", "EDIT_BOARD_SETTINGS", "DELETE_BOARD", "MANAGE_MEMBERS", "MANAGE_ROLES"]
                            }
                        },
                        categoryPermissions: {
                            bsonType: "array",
                            title: "User Category Permissions",
                            description: "Category Permissions Arr Is Required",
                            items: {
                                bsonType: "string",
                                enum: ["CREATE_CATEGORY", "RENAME_CATEGORY", "DELETE_CATEGORY", "MOVE_TASK_BETWEEN_CATEGORIES"]
                            }
                        },
                        taskPermissions: {
                            bsonType: "array",
                            title: "User Task Permissions",
                            description: "Task Permissions Arr Is Required",
                            items: {
                                bsonType: "string",
                                enum: ["CREATE_TASK", "DELETE_TASK", "EDIT_TASK", "CHANGE_TASK_STATUS", "ASSIGN_TASK", "UNASSIGN_TASK"]
                            }
                        },
                        commentPermissions: {
                            bsonType: "array",
                            title: "User Comment Permissions",
                            description: "Comment Permissions Arr Is Required",
                            items: {
                                bsonType: "string",
                                enum: ["COMMENT_TASK", "EDIT_OWN_COMMENT", "DELETE_OWN_COMMENT", "DELETE_ANY_COMMENT"]
                            }
                        }
                    }
                },
                boardName: {
                    bsonType: "string",
                    title: "Name of the Board",
                    description: "Board Name Is Required"
                },
                taskCategories: {
                    bsonType: "array",
                    title: "Categories Array For Tasks Inside a Board",
                    items: {
                        bsonType: "object",
                        title: "Task Category Definition",
                        required: ["position", "categoryName"],
                        properties: {
                            position: {
                                bsonType: "int",
                                minimum: 0,
                                description: "Position Cannot be less then 0"
                            },
                            categoryName: {
                                bsonType: "string",
                                minLength: 3,
                                maxLength: 40,
                                description: "Category Name Is Required"
                            },
                            tasks: {
                                bsonType: "array",
                                title: "Tasks Ids",
                                description: "Ids for referenced tasks",
                                items: {
                                    bsonType: "objectId",
                                    description: "Referenced Tasks"
                                }
                            }
                        }
                    }
                },
                members: {
                    bsonType: "array",
                    title: "Board Members",
                    description: "Members Referencing To Users Collection",
                    items: {
                        bsonType: "object",
                        title: "Member Specification",
                        required: ["userId", "boardPermissions", "categoryPermissions", "taskPermissions", "commentPermissions"],
                        properties: {
                            userId: {
                                bsonType: "binData",
                                title: "Member Id",
                                description: "Member Id Is Required"
                            },
                            boardPermissions: {
                                bsonType: "array",
                                title: "User Board Permissions",
                                description: "Board Permissions Arr Is Required",
                                items: {
                                    bsonType: "string",
                                    enum: ["VIEW_BOARD", "EDIT_BOARD_SETTINGS", "DELETE_BOARD", "MANAGE_MEMBERS", "MANAGE_ROLES"]
                                }
                            },
                            categoryPermissions: {
                                bsonType: "array",
                                title: "User Category Permissions",
                                description: "Category Permissions Arr Is Required",
                                items: {
                                    bsonType: "string",
                                    enum: ["CREATE_CATEGORY", "RENAME_CATEGORY", "DELETE_CATEGORY", "MOVE_TASK_BETWEEN_CATEGORIES"]
                                }
                            },
                            taskPermissions: {
                                bsonType: "array",
                                title: "User Task Permissions",
                                description: "Task Permissions Arr Is Required",
                                items: {
                                    bsonType: "string",
                                    enum: ["CREATE_TASK", "DELETE_TASK", "EDIT_TASK", "CHANGE_TASK_STATUS", "ASSIGN_TASK", "UNASSIGN_TASK"]
                                }
                            },
                            commentPermissions: {
                                bsonType: "array",
                                title: "User Comment Permissions",
                                description: "Comment Permissions Arr Is Required",
                                items: {
                                    bsonType: "string",
                                    enum: ["COMMENT_TASK", "EDIT_OWN_COMMENT", "DELETE_OWN_COMMENT", "DELETE_ANY_COMMENT"]
                                }
                            }
                        }
                    }
                },
                createdAt: {
                    bsonType: "date"
                },
                updatedAt: {
                    bsonType: "date"
                },
                archived: {
                    bsonType: "bool",
                    title: "Board Archive Status",
                    description: "Archived Status Is Required"
                },
                deleted: {
                    bsonType: "bool",
                    title: "Board Delete Status",
                    description: "Deleted Status Is Required"
                },
                highestTaskCategoryPosition: {
                    bsonType: "int",
                    minimum: 0,
                    description: "Position Cannot be less then 0"
                }
            }
        }
    }
})

db.runCommand({
    create: "tasks",
    validator: {
        $jsonSchema: {
            bsonType: "object",
            title: "Tasks Schema Validation",
            additionalProperties: false,
            required: [
                "positionInCategory",
                "title",
                "description",
                "taskStatus",
                "activityElements",
                "createdBy",
                "createdAt",
                "assignedTo"
            ],
            properties: {
                _id: {
                    bsonType: "objectId"
                },
                _class: {
                    bsonType: "string",
                    enum: ["task"]
                },
                positionInCategory: {
                    bsonType: "int",
                    title: "Task Position In Category",
                    minimum: 0,
                    description: "Task Position Is Required And >= 0"
                },
                title: {
                    bsonType: "string",
                    title: "Task Title",
                    minLength: 3,
                    maxLength: 40,
                    description: "Task Title Is Required"
                },
                description: {
                    bsonType: "string",
                    title: "Task Description",
                    description: "Task Description Is Required",
                    maxLength: 1024
                },
                taskStatus: {
                    bsonType: "string",
                    title: "Task Status",
                    description: "Task Status Is Required",
                    enum: ["COMPLETED", "UNCOMPLETED"]
                },
                activityElements: {
                    bsonType: "array",
                    title: "Task Activity Elements",
                    description: "Task Activity Elements Arr Is Required",
                    items: {
                        bsonType: "object",
                        title: "Task Activity Element",
                        additionalProperties: false,
                        required: ["user", "createdAt", "_class"],
                        properties: {
                            _class: {
                                bsonType: "string",
                                enum: ["category_change", "status_change", "comment"],
                            },
                            user: {
                                bsonType: "binData",
                                title: "Id Of Referenced User",
                                description: "User Id Is Required"
                            },
                            createdAt: {
                                bsonType: "date",
                                title: "Created At",
                                description: "Creation Date Is Required"
                            },
                            prevCategory: {
                                bsonType: "string",
                                title: "Previous Category",
                                description: "Previous Category Id Is Required"
                            },
                            nextCategory: {
                                bsonType: "string",
                                title: "Next Category",
                                description: "Next Category Id Is Required"
                            },
                            comment: {
                                bsonType: "string",
                                title: "Task Comment",
                                description: "Task Comment Is Required",
                                minLength: 3,
                                maxLength: 256
                            },
                            updated: {
                                bsonType: "bool",
                                title: "Comment Updated Status",
                                description: "Comment Updated Status Is Required"
                            },
                            commentId: {
                                bsonType: "binData",
                                title: "Id Of Specific Comment",
                                description: "CommentId is Required"
                            },
                            prevStatus: {
                                bsonType: "string",
                                enum: ["COMPLETED", "UNCOMPLETED"]
                            },
                            nextStatus: {
                                bsonType: "string",
                                enum: ["COMPLETED", "UNCOMPLETED"]
                            }
                        },
                        oneOf: [
                            {
                                properties: {
                                    _class: {
                                        bsonType: "string",
                                        enum: ["category_change"]
                                    }
                                },
                                required: ["prevCategory", "nextCategory"]
                            },
                            {
                                properties: {
                                    _class: {
                                        bsonType: "string",
                                        enum: ["comment"]
                                    }
                                },
                                required: ["comment", "updated", "commentId"]
                            },
                            {
                                properties: {
                                    _class: {
                                        bsonType: "string",
                                        enum: ["status_change"]
                                    }
                                },
                                required: ["prevStatus", "nextStatus"]
                            }
                        ]
                    }
                },
                createdBy: {
                    bsonType: "binData",
                    title: "Task Author Id",
                    description: "Author Id Is Required"
                },
                assignedTo: {
                    bsonType: "array",
                    title: "Array Of Assigned User Ids",
                    items: {
                        bsonType: "binData",
                        title: "Assigned User Id"
                    }
                },
                createdAt: {
                    bsonType: "date"
                },
                updatedAt: {
                    bsonType: "date"
                },
                deadline: {
                    bsonType: "date",
                    title: "Optional Deadline"
                }
            }
        }
    }
})

db.runCommand({
    create: "users",
    validator: {
        $jsonSchema: {
            bsonType: "object",
            title: "User Schema Validation",
            required: ["userId", "username", "imageKey", "deleted"],
            additionalProperties: false,
            properties: {
                _id: {
                    bsonType: "objectId"
                },
                _class: {
                    bsonType: "string",
                    enum: ["user"]
                },
                userId: {
                    bsonType: "binData",
                    title: "Id of Referenced User",
                    description: "User Id Is Required"
                },
                username: {
                    bsonType: "string",
                    title: "Name Of The User",
                    description: "Username Is Required",
                    minLength: 3,
                    maxLength: 40
                },
                imageKey: {
                    bsonType: "string",
                    title: "Profile Image For Specific User",
                    description: "Image Key Is Required"
                },
                deleted: {
                    bsonType: "bool",
                    title: "User Status",
                    description: "Deleted Status Field Is Required"
                }
            }
        }
    }
})

db.runCommand({
    create: "image_metadata",
    validator: {
        $jsonSchema: {
            bsonType: "object",
            title: "Image Metadata Schema Validation",
            required: ["originalName", "storedName", "ownerId", "extension", "size", "createdAt"],
            additionalProperties: false,
            properties: {
                _id:{
                    bsonType: "objectId"
                },
                _class: {
                    bsonType: "string",
                    enum: ["image_metadata"]
                },
                originalName: {
                    bsonType: "string",
                    title: "Original File Name",
                    description: "Original File Name Is Required"
                },
                storedName: {
                    bsonType: "string",
                    title: "Stored File Name",
                    description: "Stored File Name Is Required"
                },
                ownerId: {
                    bsonType: "binData",
                    title: "Id Of The Owner",
                    description: "Owner Id Is Required"
                },
                extension: {
                    bsonType: "string",
                    title: "File Extension",
                    description: "Extension Is Required",
                    enum: ["jpg", "png"]
                },
                size: {
                    bsonType: "long",
                    title: "File Size",
                    description: "File Size Is Required",
                    minimum: 0
                },
                createdAt: {
                    bsonType: "date"
                }
            }
        }
    }
})