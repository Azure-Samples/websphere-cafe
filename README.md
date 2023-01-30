# Websphere Cafe

This is the basic Java EE application used throughout the WebSphere on Azure demos.
It is a simple CRUD application. It uses Maven and Java EE 7 (JAX-RS, EJB, CDI, JPA, JSF, Bean Validation).

## Getting Started

### Prerequisites

You need to have the following software components installed for packaging the application.

* Install a Java SE implementation (for example, [Eclipse Open J9](https://www.eclipse.org/openj9/)).
* Install [Maven](https://maven.apache.org/download.cgi) 3.5.0 or higher.

### Packaging the application

Follow steps below to package the application.

```
git clone git@github.com:Azure-Samples/websphere-cafe.git
cd websphere-cafe
mvn clean package
```

The package should be successfully generated and located at `<your local clone of the repo>/websphere-cafe-application/target/websphere-cafe.ear`. 

If you don't see this, you must troubleshoot and resolve the reason why before continuing.

### Deploying the application

You need to set up a traditional WebSphere server or cluster for application deployment.
Once the server or cluster is running, create a data source with **JNDI name** as `jdbc/WebSphereCafeDB`, which is used by the application by default.

Now you're able to install the generated `websphere-cafe.ear` to your server or cluster. 

### Accessing the application

Once the application is successfully installed and running, visit it using the default context root `/websphere-cafe`, for example:

* Http endpoint: http://<host_name>:9080/websphere-cafe
* Https endpoint: https://<host_name>:9443/websphere-cafe
