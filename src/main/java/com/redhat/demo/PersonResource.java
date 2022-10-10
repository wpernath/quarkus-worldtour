package com.redhat.demo;

import java.util.List;

import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/persons")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PersonResource {
    
    @GET
    public List<Person> listAllJson() {
        List<Person> persons = Person.findAll().list();
        return persons;
    }

    @POST
    @Transactional
    public Response create(Person p) {
        if( p == null || p.id != null ) throw new WebApplicationException("id != null");
        p.persist();
        return Response.ok(p).status(200).build();
    }

    @PUT
    @Transactional
    @Path("/{id}")
    public Person update(Long id, Person p) {
        Person entity = Person.findById(id);
        entity.salutation = p.salutation;
        entity.firstName = p.firstName;
        entity.lastName = p.lastName;
        return entity;
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public Response delete(Long id) {
        Person entity = Person.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Person with id of " + id + " does not exist.", 404);
        }
        entity.delete();
        return Response.status(204).build();
    }
}