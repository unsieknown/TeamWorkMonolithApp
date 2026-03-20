db = db.getSiblingDB("team-work")
db.createUser({
    user: "team-work-admin",
    roles: [
        {role: "dbAdmin", db: "team-work"},
        {role: "dbOwner", db: "team-work"},
        {role: "userAdmin", db: "team-work"}
    ],
    pwd: "password"
})