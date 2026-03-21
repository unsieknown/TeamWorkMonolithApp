## Application Setup

The process of running the entire application is highly automated and requires only minimal setup.

### 1. Admin Password Configuration

In one of the environment directories (`dev`, `prod`, or `test`), create a file named:

```
admin_password.txt
```

Inside this file, provide the password for the application administrator.

This file will be mounted into the application as a secret volume. You can verify this in the `docker-compose` file under the `teamwork-app` service:

```
volumes:
  - ./admin_password.txt:/run/secrets/admin_password.txt:ro
```

---

### 2. Environment Variables

Next to the `docker-compose` file, create a `.env` file containing the required environment variables for the application and its components:

```
MONGO_DB_NAME=
MONGO_DB_USERNAME=
MONGODB_PASSWORD=
MONGODB_ROOT_NAME=
MONGODB_ROOT_PASSWORD=

MYSQL_ROOT_PASSWORD=
MYSQL_DATABASE=
MYSQL_USER=
MYSQL_PASSWORD=

STORAGE_ROOT=
PROFILE_IMAGES_ROOT=
BANNED_IPS_FILE=

JWT_SECRET=

REDIS_USER=
REDIS_PASSWORD=

MAIL_ADDR=
MAIL_PASSWD=
```

---

### 3. MongoDB Configuration

Ensure that the following variables:

* `MONGO_DB_NAME`
* `MONGO_DB_USERNAME`
* `MONGODB_PASSWORD`

match the values defined in all three `.js` initialization files located in:

```
backend/mongo/init
```

These files configure the MongoDB database, users, and collections.

You may either:

* Copy the values directly from these files, or
* Modify them — but **you must update all three files accordingly** to keep them consistent.

---

### 4. Storage Configuration

The following variables define storage locations used by the application:

* `STORAGE_ROOT`
* `PROFILE_IMAGES_ROOT`
* `BANNED_IPS_FILE`

These paths are used for storing files, profile images, and banned IP addresses.

Example values:

```
STORAGE_ROOT=/data/files_storage
PROFILE_IMAGES_ROOT=/data/profile_images_storage
BANNED_IPS_FILE=/data/banned_ips.txt
```

---

### 5. Redis Configuration

The values of:

* `REDIS_USER`
* `REDIS_PASSWORD`

must match the credentials defined in the `redis.conf` file.

Note:

* The default Redis user is disabled.
* Example (for `prod` environment):

    * Username: `redis-admin`
    * Password: `adminPassword`

These are example values and can be changed, but they **must remain consistent** with the configuration in `redis.conf`.

---

### 6. MySQL Configuration

MySQL-related variables can be freely configured, except for:

* `MYSQL_DATABASE`

This variable **must be set to**:

```
team-work
```

This is required because the migration scripts (located in `src/resources/db/migration`) use the statement:

```
use team-work;
```

---

### 7. Mail Sender Configuration

To enable email functionality:

1. Register an application in your Google account.
2. Set:

    * `MAIL_ADDR` — your email address used for the registration
    * `MAIL_PASSWD` — the generated application password

Important:

* Remove any whitespace characters from the generated password before using it.

---

This completes the full setup required to run the application.
