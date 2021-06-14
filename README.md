# Quarkus WorldTour demo project

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Initial steps
Here is a step by step doc how to reproduce the code. 

### bootstrapping the code
Either go to https://code.quarkus.io/ and create your skeleton or execute the following

```bash
$>  mvn io.quarkus:quarkus-maven-plugin:1.13.7.Final:create \
    -DprojectGroupId=org.wanja.demo \
    -DprojectArtifactId=demo \
    -DclassName="org.wanja.quarkus.demo.PersonResource" \
    -Dpath="/persons"
```

Now cd into the created project and open the complete folder with your preferred IDE.

### First steps
Open the PersonResource file and have a look at its structure. With 
```bash 
$> mvn compile quarkus:dev
```

You're going to start your local quarkus execution server. 

### Changing the hello method
```java
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("hello")
    public String hello() {
        return "hallo";
    }
```

Note, a curl to http://localhost:8080/persons/hello will immediately give you the return. Quarkus recompiles everything in the background.

### Adding another method
```java
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("hello/du/{name}")
    public String helloDu(@PathParam String name) {
        return "huhu" + ", " + name;
    }
```

```bash
$> curl http://localhost:8080/persons/hello/wanja
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
insert into person(id, first_name, last_name, salutation) values (nextval('hibernate_sequence'), 'Wanja', 'Pernath', 'Mr');
insert into person(id, first_name, last_name, salutation) values (nextval('hibernate_sequence'), 'Bobby', 'Brown', 'Mr');
insert into person(id, first_name, last_name, salutation) values (nextval('hibernate_sequence'), 'Elvis', 'Presley', 'Mr');
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

That's it. This is the entity. You don't have to specify getters and setters. 

### Create a PersonResource.java
Create a HelloResource, move everything in there and use the PersonResource for database access. We first just want to list all entities from the db

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
[{"id":1,"firstName":"Wanja","lastName":"Pernath","salutation":"Mr"},{"id":2,"firstName":"Bobby","lastName":"Brown","salutation":"Mr"},{"id":3,"firstName":"Elvis","lastName":"Presley","salutation":"Mr"}]
```

As you can see, as soon as you're adding the extensions to your project, quarkus:dev automatically starts a postgresql database as a docker container (if you have docker desktop installed). Which makes playing with your first db app very easy.


### Adding POST, PUT and DELETE 
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
            throw new WebApplicationException("Fruit with id of " + id + " does not exist.", 404);
        }
        entity.delete();
        return Response.status(204).build();
    }
```

### Adding a simple angular based index.html file 
Just copy from this repository and place it into `src/main/resources/META-INF/resources/index.html` 

```bash
$> cd src/main/resources/META-INF/resources
$> wget https://raw.githubusercontent.com/wpernath/quarkus-worldtour/main/src/main/resources/META-INF/resources/index.html
```
