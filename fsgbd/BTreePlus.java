package fr.miage.fsgbd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Vector;
import java.util.stream.Stream;

import javax.swing.tree.DefaultMutableTreeNode;


/**
 * @author Galli Gregory, Mopolo Moke Gabriel
 * @param <Type>
 */
public class BTreePlus<Type> implements java.io.Serializable {
    private Noeud<Type> racine;
    private Noeud<Type> noeudPrecedent = null;
    private boolean refresh = false;

    public BTreePlus(int u, Executable e) {
        racine = new Noeud<Type>(u, e, null);
    }

    public void afficheArbre() {
        racine.afficheNoeud(true, 0);
    }

    /**
     * Méthode récursive permettant de récupérer tous les noeuds
     *
     * @return DefaultMutableTreeNode
     */
    public DefaultMutableTreeNode bArbreToJTree() {
    	if(noeudPrecedent != null) 
    	{
    		refresh = true;
    	}
        return bArbreToJTree(racine);
    }

    private DefaultMutableTreeNode bArbreToJTree(Noeud<Type> root) {
        StringBuilder txt = new StringBuilder();
        
        if (root.fils.size() == 0) 
        {
        	if (noeudPrecedent != null && !refresh)
        	{
        		noeudPrecedent.next = root;
        		noeudPrecedent = root;
        	}
        }
         
       
        for (Type key : root.keys)
            txt.append(key.toString()).append(" ");

        DefaultMutableTreeNode racine2 = new DefaultMutableTreeNode(txt.toString(), true);
        for (Noeud<Type> fil : root.fils)
            racine2.add(bArbreToJTree(fil));

        return racine2;
    }
    
    public boolean addValeur(Type valeur,int ligne) {
        refresh = false;
        this.noeudPrecedent =null;
       // System.out.println("Ajout de la valeur : " + valeur.toString());
        if (racine.contient(valeur) == null) {
            Noeud<Type> newRacine = racine.addValeur(valeur,ligne);
            if (racine != newRacine)
                racine = newRacine;
            return true;
        }
        return false;
    }


    public boolean addValeur(Type valeur) {
        System.out.println("Ajout de la valeur : " + valeur.toString());
        if (racine.contient(valeur) == null) {
            Noeud<Type> newRacine = racine.addValeur(valeur);
            if (racine != newRacine)
                racine = newRacine;
            return true;
        }
        return false;
    }


    public void removeValeur(Type valeur) {
        System.out.println("Retrait de la valeur : " + valeur.toString());
        if (racine.contient(valeur) != null) {
            Noeud<Type> newRacine = racine.removeValeur(valeur, false);
            if (racine != newRacine)
                racine = newRacine;
        }
    }
    
    
    public Vector<Long> searchForLines(File selectedFile) throws IOException {

        Long tempsSequentiel = (long)0, tempsIndex= (long)0, sequentielMin= (long)9999, sequentielMax= (long)0, indexMin= (long)9999, indexMax = (long)0;
        ArrayList<Integer> valeurs = new ArrayList<>();
        String id;
        try(BufferedReader br = new BufferedReader(new FileReader(selectedFile))) {
            int ligne = 0;
            for(String line; (line = br.readLine()) != null; ) {
                ligne++;
                if (ligne%100 ==0) {
                    id = line.substring(0, line.indexOf(";"));
                    int valeur = Integer.parseInt(id);
                   valeurs.add(valeur);
                }
            }
        } catch (IOException ioException) {
            System.out.println("Veuillez d'abbord charger les données du fichier");
        }
        System.out.println("Recherche des lignes de maniere sequentielle: ");
        for( Integer value : valeurs ) {
            Long debut = System.nanoTime();
            try(BufferedReader br = new BufferedReader(new FileReader(selectedFile))) {
                for(String line; (line = br.readLine()) != null; ) {
                     id = line.substring( 0, line.indexOf(";"));
                    if(Integer.parseInt(id) == (int)value)
                    {
                        Long fin = System.nanoTime();
                        Long total = (fin-debut)/1000;
                        System.out.println(line +" "+total+" microseconds");
                        tempsSequentiel = tempsSequentiel+total;
                        if (total > sequentielMax)
                            sequentielMax = total;
                        else if (total < sequentielMin)
                            sequentielMin = total;
                        break;
                    }
                }
                // line is not visible here.
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
       System.out.println("Temps total en sequentiel : "+tempsSequentiel+ " microseconds");

        String line;
        System.out.println("Recherche des lignes de maniere indexee: ");
        for( Integer value : valeurs ) {
            Long debut = System.nanoTime();
            try (Stream<String> lines = Files.lines(Paths.get(selectedFile.getAbsolutePath()))) {
                int test = Noeud.pointeurs.get(value);
                line = lines.skip(test).findFirst().get();
            }
                 Long fin = System.nanoTime();
                        Long total = (fin-debut)/1000;
            //System.out.println(line +" "+total+" microseconds");
            tempsIndex = tempsIndex+total;
            if (total > indexMax)
                indexMax = total;
            else if (total < indexMin)
                indexMin = total;

                    }

        
       Vector<Long> vtime =new Vector<>();
       vtime.add(tempsSequentiel);vtime.add(sequentielMin);vtime.add(sequentielMax);
       vtime.add(tempsIndex);vtime.add(indexMin);vtime.add(indexMax);
       return vtime;

    }
}
