# Quarkus WorldTour demo project

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Initial steps
Here is a step by step doc how to reproduce the code. 

### bootstrapping the code
Either go to https://code.quarkus.io/ and create your skeleton or execute the following

```bash
$>  mvn io.quarkus:quarkus-maven-plugin:2.2.3.Final:create \
    -DprojectGroupId=org.wanja.demo \
    -DprojectArtifactId=demo \
    -DclassName="org.wanja.quarkus.demo.HelloResource" \
    -Dpath="/hello"
```

Now cd into the created project and open the complete folder with your preferred IDE.

### First steps
Open the HelloResource file and have a look at its structure. With 
```bash 
$> mvn compile quarkus:dev
```

You're going to start your local quarkus execution server. 

### Changing the hello method
```java
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "hallo";
    }
```

Note, a curl to http://localhost:8080/hello will immediately give you the return. Quarkus recompiles everything in the background.

### Adding another method
```java
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("du/{name}")
    public String helloDu(@PathParam String name) {
        return "huhu" + ", " + name;
    }
```

```bash
$> curl http://localhost:8080/hello/wanja
wanja
```

### Making your string configurably
Add the following microprofile annotated String
```java
    @ConfigProperty(name = "message.hello")
    public String configHello;
```

In the application.properties you're able to configure that string
```java
message.hello=Huhu, Quarkus is so coool!
```

And make sure that `hello` and `helloDu` are consuming that property instead of the hard coded string

See: https://quarkus.io/guides/config-reference

## Working with a Database / PanacheEntity
Until now, it haven't been necessary to restart quarkus:dev. Every change to your code immediately reflected in a recompilation. Now we are extending the pom, as we are adding some more dependencies to the sauce. It might be that you now have to close quarkus:dev and restart it. 

Dependencies:

see: https://quarkus.io/guides/hibernate-orm#setting-up-and-configuring-hibernate-orm
and: https://quarkus.io/guides/hibernate-orm-panache

### Adding dependencies

```bash
$> mvn quarkus:add-extension -Dextensions="quarkus-hibernate-orm-panache,quarkus-jdbc-postgresql"
```

### Adding database properties
Add the following properties to your application.properties

```
# Datasource options
%prod.quarkus.datasource.db-kind=postgresql
%prod.quarkus.datasource.username=wanja
%prod.quarkus.datasource.password=wanja
%prod.quarkus.datasource.jdbc.url=jdbc:postgresql://localhost/wanjadb
%prod.quarkus.datasource.jdbc.max-size=8
%prod.quarkus.datasource.jdbc.min-size=2

quarkus.hibernate-orm.database.generation=drop-and-create
quarkus.hibernate-orm.log.sql=true
quarkus.hibernate-orm.sql-load-script=import.sql
```

https://quarkus.io/guides/datasource

### Add an import.sql file to src/main/resources
```sql
insert into person(id, first_name, last_name, salutation) values (nextval('hibernate_sequence'), 'Doro', 'Pesch', 'Mrs');
insert into person(id, first_name, last_name, salutation) values (nextval('hibernate_sequence'), 'Bobby', 'Brown', 'Mr');
insert into person(id, first_name, last_name, salutation) values (nextval('hibernate_sequence'), 'Elvis', 'Presley', 'Mr');
insert into person(id, first_name, last_name, salutation) values (nextval('hibernate_sequence'), 'Curt', 'Cobain', 'Mr');
insert into person(id, first_name, last_name, salutation) values (nextval('hibernate_sequence'), 'Nina', 'Hagen', 'Mrs');
insert into person(id, first_name, last_name, salutation) values (nextval('hibernate_sequence'), 'Jimmi', 'Henrix', 'Mr');
insert into person(id, first_name, last_name, salutation) values (nextval('hibernate_sequence'), 'Janis', 'Joplin', 'Mrs');
```

### Add the entity
Add a new file in src/main/java/org/wanja/quarkus/demo/Person.java
```java
package org.wanja.quarkus.demo;

import javax.persistence.Column;
import javax.persistence.Entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
public class Person extends PanacheEntity {
    
    @Column(name = "first_name")
    public String firstName;

    @Column(name = "last_name")
    public String lastName;

    @Column(name = "salutation")
    public String salutation;

}
```

That's it. This is the entity. You don't have to specify any getters and setters. 

### Create a PersonResource.java
Create a PersonResource, which listens on /persons and Consumes and Produces only JSON. 

```java
@Path("/persons")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PersonResource {
}
```

We first just want to list all entities from the db

```java
    @GET
    public List<Person> listAllJson() {
        List<Person> persons = Person.findAll().list();
        return persons;
    }
```

```bash
$> curl http://localhost:8080/persons
Could not find MessageBodyWriter for response object of type: java.util.ArrayList of media type: application/json
```

This is because, we are missing a dependency: quarkus-resteasy-jsonb (or quarkus-resteasy-jackson) which automates everything for us.

```bash
$> mvn quarkus:add-extension -Dextensions="quarkus-resteasy-jsonb"
```

And now we should be able to see
```bash
$> curl http://localhost:8080/persons
[{"id":1,"firstName":"Doro","lastName":"Pesch","salutation":"Mrs"},{"id":2,"firstName":"Bobby","lastName":"Brown","salutation":"Mr"},{"id":3,"firstName":"Elvis","lastName":"Presley","salutation":"Mr"},{"id":4,"firstName":"Curt","lastName":"Cobain","salutation":"Mr"},{"id":5,"firstName":"Nina","lastName":"Hagen","salutation":"Mrs"},{"id":6,"firstName":"Jimmi","lastName":"Henrix","salutation":"Mr"},{"id":7,"firstName":"Jannis","lastName":"Joplin","salutation":"Mrs"},{"id":8,"firstName":"Joe","lastName":"Cocker","salutation":"Mr"}]
```

As you can see, as soon as you're adding the extensions to your project, quarkus:dev automatically starts a postgresql database as a docker container (if you have docker desktop installed). Which makes playing with your first db app very easy.


### Adding POST, PUT and DELETE 

We want to have a simple CRUD application, so we are going to add POST, PUT and DELETE methods to our PersonResource.

POST
```java
    @POST
    @Transactional
    public Response create(Person p) {
        if( p == null || p.id != null ) throw new WebApplicationException("id != null");
        p.persist();
        return Response.ok(p).status(200).build();
    }
```

PUT
```java
    @PUT
    @Transactional
    @Path("{id}")
    public Person update(@PathParam Long id, Person p) {
        Person entity = Person.findById(id);
        entity.salutation = p.salutation;
        entity.firstName = p.firstName;
        entity.lastName = p.lastName;
        return entity;
    }
```

DELETE
```java
    @DELETE
    @Path("{id}")
    @Transactional
    public Response delete(@PathParam Long id) {
        Person entity = Person.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Person with id of " + id + " does not exist.", 404);
        }
        entity.delete();
        return Response.status(204).build();
    }
```

### Adding a simple angular based index.html file 
Just copy from this repository and place it into `src/main/resources/META-INF/resources/index.html` 

```bash
$> wget https://raw.githubusercontent.com/wpernath/quarkus-worldtour/main/src/main/resources/META-INF/resources/index.html -O src/main/resources/META-INF/resources/index.html
```

That's it. You now have a working simple CRUD application including an AngularJS based frontend. Isn't Quarkus cool?

## Moving quarkus to kubernetes
Quarkus has everything build in. Just add another extension

```bash
$> mvn quarkus:add-extension -Dextensions="quarkus-container-image-jib"
```

Change defaults with new application.properties props:

```
# Container image build 
# This is how your image is called (replace it with your hub/account/)
quarkus.container-image.image=quay.io/wpernath/demo-jar

# whenever you're calling mvn package, build the image
quarkus.container-image.build=true

# push the image to the repository, when done building 
quarkus.container-image.push=true

# build also an image on mvn package -Pnative
quarkus.native.container-build=true

# use jib to create the container image
quarkus.container-image.builder=jib
```

And execute:
```bash
$> mvn package -DskipTests
```

We now have our demo-jar image build and pushed to quay.io. Let's consume it now.

### Using OpenShift to deploy our app
Go and register an OpenShift instance for free for 30 days:
https://developers.redhat.com/developer-sandbox/get-started

Log into the developer sandbox, select one of the projects (<user-dev> or <user-stage>) and click on "+Add". Choose "Database" and select "PostgreSQL". If you choose "(Ephemeral)", your data won't be stored. So choose without "(Ephemeral)".

Make sure, "PostgreSQL Connection Username" and "Password" matches those in your `application.properties` file (otherwise, you have to provide more ENV vars to your Deployment later).

Click on "Create" and wait until your DB is started (you're getting a dark blue circle around your service name in "Topology"-View of OpenShift.)

### Add your app 
Click again on "+Add", choose "Container Image" and provide the URL of your image into "Image name from external registry". In my case, it's `quay.io/wpernath/demo-jar:v1.0.0`

- Choose an application name (this is for grouping only)
- Choose a Name (this is how your app is called)
- Choose Deployment
- Choose "Create a route to the application"
- On the bottom, click on "Deployment" link to add an Environment variable
- Name: QUARKUS_DATASOURCE_JDBC_URL
- Value: jdbc:postgresql://postgresql/wanjadb (in my case "postgresql")
- Click on "Create"

After a short while, your app is deployed and can be tested.


## make it native
Just compile your app with the following call to create a native container image, which will be pushed to quay.io.

```bash
$> mvn clean package -DskipTests -Pnative
```

If you give your image name another tag than "latest", like "v1.0.0-native", you're able to redeploy your image on OpenShift with that tag, instead of "v1.0.0".

That's it. 
