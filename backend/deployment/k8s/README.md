## Kubernetes Setup

Running the application with Kubernetes requires a more structured and slightly more advanced setup compared to Docker Compose. Follow the steps below to properly configure all components.

---

### 1. MongoDB Configuration

Inside the `mongo` directory, create a subdirectory:

```bash
env/
```

Then create two files:

* `.env.public`
* `.env.secret`

**.env.public**

```env
MONGODB_ROOT_NAME=
```

**.env.secret**

```env
MONGODB_ROOT_PASSWORD=rootPass
```

Inside the `init` directory, you will find scripts responsible for initializing users, collections, and indexes. These scripts already contain default credentials.

You can:

* Keep the default values, or
* Modify them (as in the Docker setup)

However, if you change them, you must also update the corresponding values in the application configuration (described later).

---

### 2. MySQL Configuration

Inside the `mysql` directory, create:

```bash
env/
```

With two files:

* `.env.public`
* `.env.secret`

**.env.public**

```env
MYSQL_DATABASE=
MYSQL_USER=
```

**.env.secret**

```env
MYSQL_ROOT_PASSWORD=
MYSQL_PASSWORD=
```

Values can be chosen freely, except:

* `MYSQL_DATABASE` **must be set to** `team-work`

This is required because Flyway migration scripts use:

```sql
use `team-work`;
```

---

### 3. Redis Configuration

Inside the `redis` directory, create:

```bash
env/
```

With two files:

* `.env.public`
* `.env.secret`

**.env.public**

```env
REDIS_USER=
```

**.env.secret**

```env
REDIS_PASSWORD=
```

Default values (from `conf/redis.conf`) are:

* `REDIS_USER=redis-admin`
* `REDIS_PASSWORD=adminPassword`

These can be changed, but must remain consistent between:

* `.env` files
* `redis.conf`

Note: the default Redis user is disabled.

---

### 4. Teamwork Application Configuration

Inside the `teamwork` directory:

#### Admin Credentials

Create a directory:

```bash
admin_creds/
```

Inside it, create:

```bash
admin_password.txt
```

This file should contain the administrator password. It will be mounted into the application as a secret volume.

---

#### Environment Variables

Create another directory:

```bash
env/
```

With two files:

* `.env.public`
* `.env.secret`

**.env.public**

```env
MONGO_DB_NAME=
MONGO_DB_USERNAME=
STORAGE_ROOT=
PROFILE_IMAGES_ROOT=
BANNED_IPS_FILE=
MAIL_ADDR=
```

**.env.secret**

```env
MONGODB_PASSWORD=
JWT_SECRET=
MAIL_PASSWD=
```

---

### 5. MongoDB Credentials Consistency

As mentioned earlier, the following variables:

* `MONGO_DB_NAME`
* `MONGO_DB_USERNAME`
* `MONGODB_PASSWORD`

must match the values defined in:

```bash
mongo/init/prod
```

You may copy them directly or modify them, but any change must be reflected consistently across all initialization scripts and environment variables.

---

### 6. Storage Configuration

Example values:

```env
STORAGE_ROOT=/data/files_storage
PROFILE_IMAGES_ROOT=/data/profile_images_storage
BANNED_IPS_FILE=/data/banned_ips.txt
```

Important:

* The `/data` prefix is required by default.
* It is referenced in `teamwork-master.yml` (deployment section, e.g. volume/group configuration).

If you change `/data`, you must also update it inside `teamwork-master.yml`.

---

### 7. Mail Sender Configuration

To enable email functionality:

1. Register an application in your Google account.
2. Set:

    * `MAIL_ADDR` — your email address used during registration
    * `MAIL_PASSWD` — the generated application password

Important:

* Remove all whitespace characters from the generated password.

These variables are required for the mail sender to function correctly.

---

### 8. Troubleshooting

If you encounter issues, check the:

```bash
kustomization.yml
```

This file contains all required paths and references for each component.

---

### 9. Running the Application

To deploy the entire system:

1. Navigate one level above the `k8s` directory (where `kustomization.yml` is located).
2. Run:

```bash
kubectl apply -k k8s/
```

If everything is configured correctly:

* All components will start automatically
* The application should be available after approximately **1 minute**

Startup time may vary depending on your machine’s performance, as some components take longer to initialize.

---

This completes the Kubernetes setup for the application.
