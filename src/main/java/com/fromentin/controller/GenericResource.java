package com.fromentin.controller;

import com.fromentin.service.Services;
import com.google.gson.Gson;
import generated.PallierType;
import generated.ProductType;
import generated.World;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
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
    @Path("world")
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
    @Path("world")
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
    
    
//    PUT PRODUCTS
    @PUT
    @Path("product")
    @Consumes("application/json")
    public void putProduct(String data, @Context HttpServletRequest request) throws JAXBException {
        String username =  request.getHeader("X-user");
        ProductType product = new Gson().fromJson(data, ProductType.class);
        services.updateProduct(product, username);
    }
//    PUT UPGRADES
    @PUT
    @Path("upgrade")
    @Consumes("application/json")
    public void putUpgrade(String data, @Context HttpServletRequest request) throws JAXBException {
        String username =  request.getHeader("X-user");
        PallierType upgrade = new Gson().fromJson(data, PallierType.class);
        services.updateUpgrade(upgrade, username);
    }
//    PUT ANGELS
    @PUT
    @Path("angelupgrade")
    @Consumes("application/json")
    public void putAngelUpgrade(String data, @Context HttpServletRequest request) throws JAXBException {
        String username =  request.getHeader("X-user");
        PallierType angelupgrade = new Gson().fromJson(data, PallierType.class);
        services.updateAngelUpgrade(angelupgrade, username);
    }
//    DELETE WORLDng 
    @DELETE
    @Path("world")
    @Consumes("application/json")
    public void deleteWorld(@Context HttpServletRequest request) throws JAXBException {
        String username = request.getHeader("X-user");
        services.resetWorld(username);
    }
}