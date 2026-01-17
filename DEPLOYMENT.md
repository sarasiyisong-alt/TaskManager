# Deployment Guide for Task Manager

This guide details how to deploy your Task Manager application to the cloud so it is accessible via a public URL.

We recommend **Railway** for its simplicity and direct support for Docker and PostgreSQL.

## Prerequisites

1.  **GitHub Account**: Ensure your code is pushed to a GitHub repository.
2.  **Railway Account**: Sign up at [railway.app](https://railway.app).

## Deployment Steps (Railway)

### 1. Create a Project on Railway

1.  Log in to your Railway dashboard.
2.  Click the **+ New Project** button.
3.  Select **Deploy from GitHub repo**.
4.  Choose your **TaskManager** repository from the list.
5.  Click **Deploy Now**.
    *   *Note: Railway will attempt to build the application. It might fail initially because the database is not yet connected. This is normal.*

### 2. Add a PostgreSQL Database

1.  In your Railway project view, click the **+ New** button (or right-click the canvas).
2.  Select **Database** -> **PostgreSQL**.
3.  Railway will provision a new PostgreSQL database instance for you.
4.  Wait for the database to become active (green dot).

### 3. Connect the Database to Your Application

Spring Boot needs to know the database credentials. Railway makes this easy using Environment Variables.

1.  Click on your **TaskManager** application service (the box with your repo name).
2.  Go to the **Variables** tab.
3.  You need to reference the variables from your PostgreSQL service. Railway simplifies this with "Reference Variables".
4.  Add the following variables (Key = Value):

    | Key | Value (Reference) | Description |
    | :--- | :--- | :--- |
    | `SPRING_DATASOURCE_URL` | `${{PostgreSQL.DATABASE_URL}}` | The full connection string provided by the Postgres service. |
    | `SPRING_DATASOURCE_USERNAME` | `${{PostgreSQL.POSTGRES_USER}}` | The database username. |
    | `SPRING_DATASOURCE_PASSWORD` | `${{PostgreSQL.POSTGRES_PASSWORD}}` | The database password. |

    *Tip: When typing the value, start typing `${{PostgreSQL...` and Railway will autocomplete the available variables from your connected database service.*

5.  **Alternatively**, if you prefer manual entry:
    *   Click on the **PostgreSQL** service -> **Variables**.
    *   Copy the `DATABASE_URL`, `POSTGRES_USER`, and `POSTGRES_PASSWORD` values.
    *   Paste them into your App service's variables for `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` correspondingly.

### 4. Verify and Redeploy

1.  Once the variables are added, Railway usually triggers a redeploy automatically.
2.  If not, go to the **Deployments** tab of your application service and click **Redeploy**.
3.  Watch the **Build Logs** to ensure the `./mvnw clean package` (or similar build command) succeeds.
4.  Watch the **Deploy Logs** to see Spring Boot starting up.

### 5. Access Your Application

1.  Once the deployment is active (green checkmark), go to the **Settings** tab of your application service.
2.  Scroll down to **Networking**.
3.  Click **Generate Domain** (or use the one already generated).
4.  Click the simplified URL (e.g., `taskmanager-production.up.railway.app`).

**Success!** You should see your Task Manager login screen.

## Default Credentials

The application is configured to create a default Admin user if one does not exist:

*   **Username**: `Admin`
*   **Password**: `password`

You can use these credentials to log in immediately after deployment.

---

## Troubleshooting

*   **Build Fails**: Check the logs. If it says `Connection refused` to localhost:5432 during tests, it's because the build environment doesn't have the DB.
    *   *Fix*: You can disable tests during build by adding a variable `MAVEN_OPTS` = `-DskipTests`. (Or ensure your Dockerfile already skips them: `RUN mvn clean package -DskipTests`).
*   **Application Crashing**: Check Deploy Logs. If it says `Communications link failure`, verify your `SPRING_DATASOURCE_URL` variable is correct.
