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

    //LECTURE DU FICHIER XML
    
   //private World readWorldFromXml(String username) throws JAXBException {
   private World readWorldFromXml(String username) throws JAXBException {
        JAXBContext cont = JAXBContext.newInstance(World.class);
        Unmarshaller u = cont.createUnmarshaller();
        World world;
        try {
            // world = (World) u.unmarshal(new File(username+"-world.xml"));
            world = (World) u.unmarshal(new File(username+"world.xml"));
        } catch(UnmarshalException e) {
            InputStream input = getClass().getClassLoader().getResourceAsStream("world.xml");
            world = (World) u.unmarshal(input);
        }
        return world;
    }

   //SAUVEGARDE DU FICHIER XML
   
    private void saveWorldToXml(World world, String username) throws JAXBException {
        JAXBContext cont = JAXBContext.newInstance(World.class);
        Marshaller m = cont.createMarshaller();
        m.marshal(world, new File(username+"world.xml"));
    }
    
    //OBTENTION DU FICHIER XML
    
    //public World getWorld(String username) throws JAXBException {
    public World getWorld(String username) throws JAXBException {
        World world = readWorldFromXml(username);
        world.setLastupdate(System.currentTimeMillis());
        saveWorldToXml(world, username);
        return world;
    }
    /**
     * 
     * @param newproduct, prends en paramètre le produit
     * @param username, prend en paramètre le pseudo du joueur
     * @return false si l'action n'a pas pu être traitée
     * @throws JAXBException 
     */
    public Boolean updateProduct(ProductType newproduct, String username) throws JAXBException{
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        World world = getWorld(username);
        /**
         * Truver dans ce monde le produit équivalent à celui passé
         * en paramètre
         */
        ProductType product = findProductById(world, newproduct.getId());
        if(product == null)
            return false;
        /**
         * calcule de la quantité de produits achetés
         */
        int qtchange = newproduct.getQuantite() - product.getQuantite(); 
        /**
         * si des produits ont été achetés
         */
        if(qtchange > 0) { 
            double RevenuBase;
            /**
             * si on a déjà au moins un produit
             */
            if(product.getQuantite() > 0) 
                /**
                 * calcul du revenu de base
                 */
                RevenuBase = product.getRevenu() / product.getQuantite();
            /**
             * sinon on a déjà le revenu de base
             */
            else 
                RevenuBase = product.getRevenu();
            /**
             * transformation de la croissance au format 1.XX
             */
            double q = 1+product.getCroissance()/100; 
            /**
             * prix dépensé
             */
            double depense = product.getCout()*Math.pow(q, product.getQuantite())*((1-Math.pow(q, qtchange))/(1-q)); 
            /**
             * on retire la dépense de l'argent en cours
             */
            world.setMoney(world.getMoney() - depense); 
            /**
             * on augmente la quantité de produits
             */
            product.setQuantite(product.getQuantite() + qtchange);
            /**
             * Si on a au moins deux produits on augemente le revenu par le nouveau
             */
            if(product.getQuantite() > 1) 
                product.setRevenu(product.getRevenu() + (RevenuBase * qtchange));
            
            /**
             * on regarde si on a débloqué des unlocks du produit
             */
            for(PallierType unlock: product.getPalliers().getPallier()) {
                /**
                 * on regarde le premier non débloqué
                 */
                if( ! unlock.isUnlocked()) { 
                    /**
                     * si on a assez de produits pour débloquer cet unlock
                     */
                    if(product.getQuantite() >= unlock.getSeuil()) {
                        /**
                         * on indique qu'il est débloqué
                         */
                        unlock.setUnlocked(true); 
                        /**
                         * on ajoute le bonus correspondant
                         */
                        switch(unlock.getTyperatio()) { 
                            case GAIN:
                                product.setRevenu(product.getRevenu() * unlock.getRatio());
                                break;
                            case VITESSE:
                                product.setVitesse((int) (product.getVitesse() / unlock.getRatio()));
                                product.setTimeleft((long) (product.getTimeleft() / unlock.getRatio()));
                                break;
                        }
                    }
                    else
                        break;
                }
            }
            
            /**
             * on regarde si on a débloqué des unlocks appliqués à tous les produits
             */
            for(PallierType unlock: world.getAllunlocks().getPallier()) {
                if( ! unlock.isUnlocked()) {
                    /**
                     * on regarde si tous les autres ont la bonne quantité
                     */
                    if(product.getQuantite() >= unlock.getSeuil()) { 
                        boolean go = false;
                        for(ProductType p2: world.getProducts().getProduct()) {
                            if(p2.getQuantite() < unlock.getSeuil()) {
                                go = true;
                                break;
                            }
                        }
                        /**
                         * si tous les produits ont la bonne quantité
                         */
                        if(!go) { 
                            unlock.setUnlocked(true);
                            /**
                             * on applique le bon bonus
                             */
                            switch(unlock.getTyperatio()) { 
                                case GAIN:
                                    for(ProductType p2: world.getProducts().getProduct()) {
                                        p2.setRevenu(p2.getRevenu() * unlock.getRatio());
                                    }
                                    break;
                                case VITESSE:
                                    for(ProductType p2: world.getProducts().getProduct()) {
                                        p2.setVitesse((int) (p2.getVitesse() / unlock.getRatio()));
                                        p2.setTimeleft((long) (p2.getTimeleft() / unlock.getRatio()));
                                    }
                                    break;
                            }
                        }
                        else
                            break;
                    }
                    else
                        break;
                }
            }
        }
        /**
         * si pas de changement de quantité, cela veut dire qu'on a produit un produit. On passe donc le timeleft à la vitesse du produit
         */
        else 
            product.setTimeleft(product.getVitesse());
        
        
        world.setLastupdate(System.currentTimeMillis());
        saveWorldToXml(world, username);
        return true;
    }

    public Boolean updateUpgrade(PallierType newupgrade, String username) throws JAXBException{
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        World world = getWorld(username);
        PallierType upgrade = findUpgradeByName(world, newupgrade.getName());
        if(upgrade == null) {
            return false;
        }
        upgrade.setUnlocked(true);
        int id = upgrade.getIdcible();
        if(id == 0) {
            switch(upgrade.getTyperatio()) {
                case GAIN:
                    for(ProductType p2: world.getProducts().getProduct()) {
                        p2.setRevenu(p2.getRevenu() * upgrade.getRatio());
                    }
                    break;
                case VITESSE:
                    for(ProductType p2: world.getProducts().getProduct()) {
                        p2.setVitesse((int) (p2.getVitesse() / upgrade.getRatio()));
                        p2.setTimeleft((long) (p2.getTimeleft() / upgrade.getRatio()));
                    }
                    break;
            }
        }
        else if (id == -1) {
            world.setAngelbonus((int) (world.getAngelbonus() + upgrade.getRatio()));
        }
        else {
            ProductType product = findProductById(world, id);
            switch(upgrade.getTyperatio()) {
                case GAIN:
                    product.setRevenu(product.getRevenu() * upgrade.getRatio());
                    break;
                case VITESSE:
                    product.setVitesse((int) (product.getVitesse() / upgrade.getRatio()));
                    product.setTimeleft((long) (product.getTimeleft() / upgrade.getRatio()));
                    break;
            }
        }
        
        world.setMoney(world.getMoney()-upgrade.getSeuil());
        
        
        world.setLastupdate(System.currentTimeMillis());
        saveWorldToXml(world, username);
        return true;
    }

    public Boolean updateAngelUpgrade(PallierType newangelupgrade, String username) throws JAXBException{
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        World world = getWorld(username);
        PallierType angelupgrade = findAngelUpgradeByName(world, newangelupgrade.getName());
        if(angelupgrade == null) {
            return false;
        }
        angelupgrade.setUnlocked(true);
        int id = angelupgrade.getIdcible();
        switch(angelupgrade.getTyperatio()) {
            case ANGE:
                world.setAngelbonus((int) (world.getAngelbonus() + angelupgrade.getRatio()));
                break;
            case GAIN:
                for(ProductType p2: world.getProducts().getProduct()) {
                    p2.setRevenu(p2.getRevenu() * angelupgrade.getRatio());
                }
                break;
            case VITESSE:
                for(ProductType p2: world.getProducts().getProduct()) {
                    p2.setVitesse((int) (p2.getVitesse() / angelupgrade.getRatio()));
                    p2.setTimeleft((long) (p2.getTimeleft() / angelupgrade.getRatio()));
                }
                break;
        }
        
        world.setActiveangels(world.getActiveangels()-angelupgrade.getSeuil());
        
        world.setLastupdate(System.currentTimeMillis());
        saveWorldToXml(world, username);
        return true;
    }
    
    public void resetWorld(String username) throws JAXBException {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        return world.getProducts().getProduct().get(id-1);
    }

    private PallierType findUpgradeByName(World world, String name) {
        for(PallierType upgrade: world.getUpgrades().getPallier()) {
            if(upgrade.getName().equals(name)) {
                return upgrade;
            }
        }
        return null;
    }

    private PallierType findAngelUpgradeByName(World world, String name) {
        for(PallierType angelupgrade: world.getAngelupgrades().getPallier()) {
            if(angelupgrade.getName().equals(name)) {
                return angelupgrade;
            }
        }
        return null;
    }
}
