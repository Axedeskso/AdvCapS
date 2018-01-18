package com.fromentin.service;

import generated.World;
import java.io.File;
import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;

public class Services {

   //private World readWorldFromXml(String username) throws JAXBException {
   private World readWorldFromXml() throws JAXBException {
        JAXBContext cont = JAXBContext.newInstance(World.class);
        Unmarshaller u = cont.createUnmarshaller();
        World world = null;
        try {
            // world = (World) u.unmarshal(new File(username+"-world.xml"));
            world = (World) u.unmarshal(new File("world.xml"));
        } catch(UnmarshalException e) {
            InputStream input = getClass().getClassLoader().getResourceAsStream("world.xml");
            world = (World) u.unmarshal(input);
        }
        return world;
    }

    private void saveWorldToXml(World world) throws JAXBException {
        JAXBContext cont = JAXBContext.newInstance(World.class);
        Marshaller m = cont.createMarshaller();
        m.marshal(world, new File("world.xml"));
    }
    
    //public World getWorld(String username) throws JAXBException {
    public World getWorld() throws JAXBException {
        World world = readWorldFromXml();
        //majScore(world);
        world.setLastupdate(System.currentTimeMillis());
        //saveWorldToXml(world, username);
        return world;
    }

}
