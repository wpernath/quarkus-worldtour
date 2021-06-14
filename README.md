# Quarkus WorldTour demo project

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Initial steps
Here is a step by step doc how to reproduce the code. 

### bootstrapping the code
Either go to https://code.quarkus.io/ and create your skeleton or execute the following

```bash
$> mvn io.quarkus:quarkus-maven-plugin:1.13.7.Final:create \
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


## Adding a database
