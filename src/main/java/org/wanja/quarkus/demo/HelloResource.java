package org.wanja.quarkus.demo;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

@Path("/hello")
public class HelloResource {
    @ConfigProperty(name = "message.hello")
    public String configHello;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return configHello;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("du/{name}")
    public String helloDu(@PathParam String name) {
        return configHello + ", " + name;
    }

}
