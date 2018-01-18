package com.fromentin.controller;

import com.fromentin.service.Services;
import com.google.gson.Gson;
import generated.World;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

@Path("")
public class GenericResource {

    @Context
    private UriInfo context;
    private Services services;

    public GenericResource() {
        this.services = new Services();
    }

    @GET
    @Path("world/xml")
    @Produces("application/xml")
    public Response getXml(@Context HttpServletRequest request) {
        String username =  request.getHeader("X-user");
        World world;
        try {
            //world = services.getWorld(username);
            world = services.getWorld();
            return Response.ok(world).build();
        } catch (JAXBException ex) {
            Logger.getLogger(GenericResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
    
    @GET
    @Path("world-json")
    @Produces("application/json")
    public Response getXmlJson(@Context HttpServletRequest request) {
        String username =  request.getHeader("X-user");
        //World world = null ;
        try {
            //World world = services.getWorld(username);
            World world = services.getWorld();
            return Response.ok(new Gson().toJson(world)).build();
        } catch (JAXBException ex) {
            Logger.getLogger(GenericResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}