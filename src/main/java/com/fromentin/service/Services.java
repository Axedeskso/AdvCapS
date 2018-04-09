package com.fromentin.service;

import generated.PallierType;
import generated.ProductType;
import generated.World;
import java.io.File;
import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;

public class Services {

    private World readWorldFromXml(String username) throws JAXBException {
        JAXBContext cont = JAXBContext.newInstance(World.class);
        Unmarshaller u = cont.createUnmarshaller();
        World world;
        try {
            world = (World) u.unmarshal(new File(username + "-world.xml"));
        } catch (UnmarshalException e) {
            InputStream input = getClass().getClassLoader().getResourceAsStream("world.xml");
            world = (World) u.unmarshal(input);
        }
        return world;
    }

    private void saveWorldToXml(World world, String username) throws JAXBException {
        JAXBContext cont = JAXBContext.newInstance(World.class);
        Marshaller m = cont.createMarshaller();
        m.marshal(world, new File(username + "-world.xml"));
    }

    public World getWorld(String username) throws JAXBException {
        World world = readWorldFromXml(username);
        majScore(world);
        world.setLastupdate(System.currentTimeMillis());
        saveWorldToXml(world, username);
        return world;
    }

    public Boolean updateProduct(ProductType newproduct, String username) throws JAXBException {
        World world = getWorld(username);
        ProductType product = findProductById(world, newproduct.getId());
        if (product == null) {
            return false;
        }
        int qtchange = newproduct.getQuantite() - product.getQuantite();
        if (qtchange > 0) {
            double baseRevenu;
            if (product.getQuantite() > 0) {
                baseRevenu = product.getRevenu() / product.getQuantite();
            } else {
                baseRevenu = product.getRevenu();
            }
            double q = product.getCroissance();
            double depense = product.getCout() * Math.pow(q, product.getQuantite()) * ((1 - Math.pow(q, qtchange)) / (1 - q));
            System.out.println("ACHAT : "+depense +" - "+ "MONEY : "+world.getMoney());
            world.setMoney(world.getMoney() - depense);
            product.setQuantite(product.getQuantite() + qtchange);
            if (product.getQuantite() > 1) {
                product.setRevenu(product.getRevenu() + (baseRevenu * qtchange));
            }
            for (PallierType unlock : product.getPalliers().getPallier()) {
                if (!unlock.isUnlocked()) {
                    if (product.getQuantite() >= unlock.getSeuil()) {
                        unlock.setUnlocked(true);
                        switch (unlock.getTyperatio()) {
                            case GAIN:
                                product.setRevenu(product.getRevenu() * unlock.getRatio());
                                break;
                            case VITESSE:
                                product.setVitesse((int) (product.getVitesse() / unlock.getRatio()));
                                product.setTimeleft((long) (product.getTimeleft() / unlock.getRatio()));
                                break;
                        }
                    } else {
                        break;
                    }
                }
            }
            for (PallierType unlock : world.getAllunlocks().getPallier()) {
                if (!unlock.isUnlocked()) {
                    if (product.getQuantite() >= unlock.getSeuil()) {
                        boolean ko = false;
                        for (ProductType p2 : world.getProducts().getProduct()) {
                            if (p2.getQuantite() < unlock.getSeuil()) {
                                ko = true;
                                break;
                            }
                        }
                        if (!ko) {
                            unlock.setUnlocked(true);
                            switch (unlock.getTyperatio()) {
                                case GAIN:
                                    for (ProductType p2 : world.getProducts().getProduct()) {
                                        p2.setRevenu(p2.getRevenu() * unlock.getRatio());
                                    }
                                    break;
                                case VITESSE:
                                    for (ProductType p2 : world.getProducts().getProduct()) {
                                        p2.setVitesse((int) (p2.getVitesse() / unlock.getRatio()));
                                        p2.setTimeleft((long) (p2.getTimeleft() / unlock.getRatio()));
                                    }
                                    break;
                            }
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
        } else {
            product.setTimeleft(product.getVitesse());
        }
        majScore(world);
        world.setLastupdate(System.currentTimeMillis());
        saveWorldToXml(world, username);
        return true;
    }

    public Boolean updateManager(PallierType newmanager, String username) throws JAXBException {
        World world = getWorld(username);
        PallierType manager = findManagerByName(world, newmanager.getName());
        if (manager == null) {
            return false;
        }
        manager.setUnlocked(true);
        ProductType product = findProductById(world, manager.getIdcible());
        if (product == null) {
            return false;
        }
        product.setManagerUnlocked(true);
        world.setMoney(world.getMoney() - manager.getSeuil());
        world.setLastupdate(System.currentTimeMillis());
        saveWorldToXml(world, username);
        return true;
    }

    public Boolean updateUpgrade(PallierType newupgrade, String username) throws JAXBException {
        World world = getWorld(username);
        PallierType upgrade = findUpgradeByName(world, newupgrade.getName());
        if (upgrade == null) {
            return false;
        }
        upgrade.setUnlocked(true);
        int id = upgrade.getIdcible();
        if (id == 0) {
            switch (upgrade.getTyperatio()) {
                case GAIN:
                    for (ProductType p2 : world.getProducts().getProduct()) {
                        p2.setRevenu(p2.getRevenu() * upgrade.getRatio());
                    }
                    break;
                case VITESSE:
                    for (ProductType p2 : world.getProducts().getProduct()) {
                        p2.setVitesse((int) (p2.getVitesse() / upgrade.getRatio()));
                        p2.setTimeleft((long) (p2.getTimeleft() / upgrade.getRatio()));
                    }
                    break;
            }
        } else if (id == -1) {
            world.setAngelbonus((int) (world.getAngelbonus() + upgrade.getRatio()));
        } else {
            ProductType product = findProductById(world, id);
            switch (upgrade.getTyperatio()) {
                case GAIN:
                    product.setRevenu(product.getRevenu() * upgrade.getRatio());
                    break;
                case VITESSE:
                    product.setVitesse((int) (product.getVitesse() / upgrade.getRatio()));
                    product.setTimeleft((long) (product.getTimeleft() / upgrade.getRatio()));
                    break;
            }
        }
        world.setMoney(world.getMoney() - upgrade.getSeuil());
        world.setLastupdate(System.currentTimeMillis());
        saveWorldToXml(world, username);
        return true;
    }

    public Boolean updateAngelUpgrade(PallierType newangelupgrade, String username) throws JAXBException {
        World world = getWorld(username);
        PallierType angelupgrade = findAngelUpgradeByName(world, newangelupgrade.getName());
        if (angelupgrade == null) {
            return false;
        }
        angelupgrade.setUnlocked(true);
        int id = angelupgrade.getIdcible();
        switch (angelupgrade.getTyperatio()) {
            case ANGE:
                world.setAngelbonus((int) (world.getAngelbonus() + angelupgrade.getRatio()));
                break;
            case GAIN:
                for (ProductType p2 : world.getProducts().getProduct()) {
                    p2.setRevenu(p2.getRevenu() * angelupgrade.getRatio());
                }
                break;
            case VITESSE:
                for (ProductType p2 : world.getProducts().getProduct()) {
                    p2.setVitesse((int) (p2.getVitesse() / angelupgrade.getRatio()));
                    p2.setTimeleft((long) (p2.getTimeleft() / angelupgrade.getRatio()));
                }
                break;
        }
        world.setActiveangels(world.getActiveangels() - angelupgrade.getSeuil());
        world.setLastupdate(System.currentTimeMillis());
        saveWorldToXml(world, username);
        return true;
    }

    private void majScore(World world) {
        long now = System.currentTimeMillis();
        for (ProductType p : world.getProducts().getProduct()) {
            if (p.isManagerUnlocked() && p.getQuantite() > 0) {
                long number = Math.floorDiv((now - world.getLastupdate() + p.getVitesse() - p.getTimeleft()), p.getVitesse());
                long tempsRestant = Math.floorMod((now - world.getLastupdate() + p.getVitesse() - p.getTimeleft()), p.getVitesse());
                world.setMoney(world.getMoney() + (p.getRevenu() * (1 + world.getActiveangels() * world.getAngelbonus() / 100)) * number);
                world.setScore(world.getScore() + (p.getRevenu() * (1 + world.getActiveangels() * world.getAngelbonus() / 100)) * number);
                p.setTimeleft(tempsRestant);
                if (p.getTimeleft() < 0) {
                    p.setTimeleft(0);
                }
            } else {
                if (p.getTimeleft() > 0 && p.getTimeleft() <= now - world.getLastupdate()) {
                    world.setMoney(world.getMoney() + (p.getRevenu() * (1 + world.getActiveangels() * world.getAngelbonus() / 100)));
                    world.setScore(world.getScore() + (p.getRevenu() * (1 + world.getActiveangels() * world.getAngelbonus() / 100)));
                    p.setTimeleft(0);
                } else if (p.getTimeleft() > 0) {
                    p.setTimeleft(p.getTimeleft() - (now - world.getLastupdate()));
                }
            }
        }
    }

    public void resetWorld(String username) throws JAXBException {
        World world = getWorld(username);
        double nbAngels = Math.round(150 * Math.sqrt(world.getScore() / Math.pow(10, 15)) - world.getTotalangels());
        world.setTotalangels(world.getTotalangels() + nbAngels);
        world.setActiveangels(world.getActiveangels() + nbAngels);
        JAXBContext cont = JAXBContext.newInstance(World.class);
        Unmarshaller u = cont.createUnmarshaller();
        InputStream input = getClass().getClassLoader().getResourceAsStream("world.xml");
        World world2 = (World) u.unmarshal(input);
        world2.setScore(world.getScore());
        world2.setTotalangels(world.getTotalangels());
        world2.setActiveangels(world.getActiveangels());
        saveWorldToXml(world2, username);
    }

    private ProductType findProductById(World world, int id) {
        return world.getProducts().getProduct().get(id - 1);
    }

    private PallierType findUpgradeByName(World world, String name) {
        for (PallierType upgrade : world.getUpgrades().getPallier()) {
            if (upgrade.getName().equals(name)) {
                return upgrade;
            }
        }
        return null;
    }

    private PallierType findAngelUpgradeByName(World world, String name) {
        for (PallierType angelupgrade : world.getAngelupgrades().getPallier()) {
            if (angelupgrade.getName().equals(name)) {
                return angelupgrade;
            }
        }
        return null;
    }

    private PallierType findManagerByName(World world, String name) {
        for (PallierType manager : world.getManagers().getPallier()) {
            if (manager.getName().equals(name)) {
                return manager;
            }
        }
        return null;
    }
}
