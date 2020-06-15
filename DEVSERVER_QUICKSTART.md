If you want to create a development setup without much hassle, follow the setup steps below:

### 1. Install required dependencies
- Java 8 SDK (see also [Server Setup Guide](./SERVER_SETUP.md#Java%208>))
- Android SDK
    - Use the SDK Manager provided with the SDK to install the most recent Android Build Tools
- Maven
- Ant
- Docker and `docker-compose` (required if you want to run the provided PostgreSQL docker container)

### 2. Set missing environment variables
***Note:** You may want to add these variables to an initialization script so that you can source them whenever required without setting them permanently*

- `JAVA_HOME` should point to the installation directory of your Java SDK from step 1
- `ANDROID_HOME` should point to your Android SDK installation directory
- `M2_HOME` should point to your Maven installation directory

### 3. Build SORMAS
From inside the `sormas-base` directory:

1. Make sure your environment variables are set correctly as specified in step 2
2. Run `ant clean` to remove old build artifacts
3. Run `ant install-with-app` to build all components of SORMAS
4. Run `ant collect-all`, which will gather all files necessary for deployment in the `../deploy` directory

### 4. Install the development server
1. From inside the `postgresql-docker` directory:
    1. Run `docker-compose up -d`

2. From inside the `deploy` directory from step 3:
    1. If not already, make the scripts `server-{setup,update}.sh` executable (`chmod +x server-{setup,update}.sh`)
    2. Make sure your environment variables are set correctly as specified in step 2
    3. Run `server-setup.sh` (elevated privileges required); you will be asked for various configuration parameters throughout the installation process, so don't go AFK during setup
    4. If the setup ran without errors, proceed with the next step; otherwise, try to fix the problem and run it again (you may need to re-create the PostgreSQL database (`docker-compose down; docker-compose up -d`) and remove the sormas domain root created in your chosen installation directory)
    5. Run `server-update.sh`; if an error occurs, you may need to fix the error and run it again
    6. Verify everything is working as expected by opening `http://localhost:6080` in your local web browser; setup succeeded when the SORMAS login page is shown


## Working with the development server
- Open <http://localhost:6080/sormas-ui> to open the SORMAS UI
- REST interface available under <http://localhost:6080/sormas-rest>
- Open <http://localhost:6048> to open the Payara admin panel for the SORMAS domain (assumes default port)
- Use the `{start,stop}-payara-sormas.sh` scripts inside your SORMAS domain directory to start or stop the Payara server
- Use `docker-compose start` and `docker-compose `**`stop`** inside the `postgres-docker` directory for starting and stopping the PostgreSQL container
- When making changes to the source code, rebuild the affected subprojects and copy the updated artifacts to the `${SORMAS_DOMAIN_DIR}/autodeploy` directory so that they will be loaded by the Payara server
