package fr.miage.fsgbd;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * @author Galli Gregory, Mopolo Moke Gabriel
 */
public class GUI extends JFrame implements ActionListener {
    TestInteger testInt = new TestInteger();
    BTreePlus<Integer> bInt;
    private JButton buttonClean, buttonRemove, buttonLoad, buttonSave, buttonAddMany, buttonAddItem, buttonRefresh;
    private JTextField txtNbreItem, txtNbreSpecificItem, txtU, txtFile, removeSpecific;
    private final JTree tree = new JTree();
    private JButton btnLoadData, btnSearch;
    private JProgressBar pBar;
    private File file;
    private JLabel seqTime, indexTime ;
    
    

    public GUI() {
        super();
        build();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == buttonLoad || e.getSource() == buttonClean || e.getSource() == buttonSave || e.getSource() == buttonRefresh) {
            if (e.getSource() == buttonLoad) {
                BDeserializer<Integer> load = new BDeserializer<Integer>();
                bInt = load.getArbre(txtFile.getText());
                if (bInt == null)
                    System.out.println("Echec du chargement.");

            } else if (e.getSource() == buttonClean) {
                if (Integer.parseInt(txtU.getText()) < 2)
                    System.out.println("Impossible de cr?er un arbre dont le nombre de cl?s est inf?rieur ? 2.");
                else
                    bInt = new BTreePlus<Integer>(Integer.parseInt(txtU.getText()), testInt);
            } else if (e.getSource() == buttonSave) {
                BSerializer<Integer> save = new BSerializer<Integer>(bInt, txtFile.getText());
            }else if (e.getSource() == buttonRefresh) {
                tree.updateUI();
            }
        } else {
            if (bInt == null)
                bInt = new BTreePlus<Integer>(Integer.parseInt(txtU.getText()), testInt);

            if (e.getSource() == buttonAddMany) {
                for (int i = 0; i < Integer.parseInt(txtNbreItem.getText()); i++) {
                    int valeur = (int) (Math.random() * 10 * Integer.parseInt(txtNbreItem.getText()));
                    boolean done = bInt.addValeur(valeur);

					/*
					  On pourrait forcer l'ajout mais on risque alors de tomber dans une boucle infinie sans "r?gle" faisant sens pour en sortir

					while (!done)
					{
						valeur =(int) (Math.random() * 10 * Integer.parseInt(txtNbreItem.getText()));
						done = bInt.addValeur(valeur);
					}
					 */
                }

            } else if (e.getSource() == buttonAddItem) {
                if (!bInt.addValeur(Integer.parseInt(txtNbreSpecificItem.getText())))
                    System.out.println("Tentative d'ajout d'une valeur existante : " + txtNbreSpecificItem.getText());
                txtNbreSpecificItem.setText(
                        String.valueOf(
                                Integer.parseInt(txtNbreSpecificItem.getText()) + 2
                        )
                );

            } else if (e.getSource() == buttonRemove) {
                bInt.removeValeur(Integer.parseInt(removeSpecific.getText()));
            }
            else if (e.getSource() == btnLoadData)
            {
            	   JFileChooser fileChooser=new JFileChooser();
                   int choice=fileChooser.showOpenDialog(this);
                   if (choice==JFileChooser.APPROVE_OPTION)
                   {
                     
                       file=fileChooser.getSelectedFile();
                       SwingWorker swk= new SwingWorker(){
                           @Override
                           protected Object doInBackground() throws Exception {
                               // TODO Auto-generated method stub
                               try(BufferedReader br = new BufferedReader(new FileReader(file))) {
                                   int ligne = 0;
                                   String line="";
                                   while ((line = br.readLine()) != null) {
                                       ligne++;
                                       String id = line.substring( 0, line.indexOf(";"));
                                       int valeur = Integer.parseInt(id);
                                       bInt.addValeur(valeur,ligne); 
                                       if (ligne%1000==0) publish(ligne);
                                       
                                   }
                                   br.close();
                                   // line is not visible here.
                               } catch (Exception ioException) {
                                   ioException.printStackTrace();
                               }
                               return true;
                           }
                           protected void process(List chs){
                               int val=(int)chs.get(chs.size()-1);
                                  pBar.setValue(val);
                           }
                           protected void done(){
                               tree.setModel(new DefaultTreeModel(bInt.bArbreToJTree()));
                               for (int i = 0; i < tree.getRowCount(); i++)
                                   tree.expandRow(i);
                       
                               tree.updateUI();
                           }
                       };
                           
                           swk.execute();     
               }
            }
            
            else if (e.getSource()==btnSearch){
                try{
                if (file!=null)
                    {
                        Vector<Long> vtime=bInt.searchForLines(file);
                        System.out.println(vtime);
                        seqTime.setText("Temps de recherche Sequentiel Moyen en (ms) : "+vtime.get(0)/100+" / Min : "+vtime.get(1)+ " / Max: "+vtime.get(2));
                        indexTime.setText("Temps de recherche depuis l'index en (ms) Moyen : "+vtime.get(3)/100+" / Min : "+vtime.get(4)+ " / Max: "+vtime.get(5));
                    }
                else 
                JOptionPane.showMessageDialog(null, "Il faut Charger l arbre");
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        }

        tree.setModel(new DefaultTreeModel(bInt.bArbreToJTree()));
        for (int i = 0; i < tree.getRowCount(); i++)
            tree.expandRow(i);

        tree.updateUI();
    }

    private void build() {
        setTitle("Indexation - B Arbre");
        setSize(760, 760);
        setLocationRelativeTo(this);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(buildContentPane());
    }

    private JPanel buildContentPane() {
        GridBagLayout gLayGlob = new GridBagLayout();

        JPanel pane1 = new JPanel();
        pane1.setLayout(gLayGlob);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 5, 2, 0);

        JLabel labelU = new JLabel("Nombre max de cl?s par noeud (2m): ");
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1;
        pane1.add(labelU, c);

        txtU = new JTextField("4", 7);
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 2;
        pane1.add(txtU, c);

        JLabel labelBetween = new JLabel("Nombre de clefs ? ajouter:");
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 1;
        pane1.add(labelBetween, c);

        txtNbreItem = new JTextField("1000", 7);
        c.gridx = 1;
        c.gridy = 2;
        c.weightx = 1;
        pane1.add(txtNbreItem, c);


        buttonAddMany = new JButton("Ajouter n ?l?ments al?atoires ? l'arbre");
        c.gridx = 2;
        c.gridy = 2;
        c.weightx = 1;
        c.gridwidth = 2;
        pane1.add(buttonAddMany, c);

        JLabel labelSpecific = new JLabel("Ajouter une valeur sp?cifique:");
        c.gridx = 0;
        c.gridy = 3;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(labelSpecific, c);

        txtNbreSpecificItem = new JTextField("50", 7);
        c.gridx = 1;
        c.gridy = 3;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(txtNbreSpecificItem, c);

        buttonAddItem = new JButton("Ajouter l'?l?ment");
        c.gridx = 2;
        c.gridy = 3;
        c.weightx = 1;
        c.gridwidth = 2;
        pane1.add(buttonAddItem, c);

        JLabel labelRemoveSpecific = new JLabel("Retirer une valeur sp?cifique:");
        c.gridx = 0;
        c.gridy = 4;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(labelRemoveSpecific, c);

        removeSpecific = new JTextField("54", 7);
        c.gridx = 1;
        c.gridy = 4;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(removeSpecific, c);

        buttonRemove = new JButton("Supprimer l'?l?ment n de l'arbre");
        c.gridx = 2;
        c.gridy = 4;
        c.weightx = 1;
        c.gridwidth = 2;
        pane1.add(buttonRemove, c);

        JLabel labelFilename = new JLabel("Nom de fichier : ");
        c.gridx = 0;
        c.gridy = 5;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(labelFilename, c);
        seqTime=new JLabel("Times Indexed Search");
        
        seqTime.setForeground(Color.blue);
        c.gridx = 0;
        c.gridy = 8;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(seqTime,c);
        indexTime=new JLabel("Times Sequentiel Search");
        indexTime.setForeground(Color.blue);
        c.gridx = 0;
        c.gridy = 9;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(indexTime,c);
        pBar=new JProgressBar(0,1000);
        pBar.setValue(0);
        pBar.setBounds(40, 40, 750, 450);
        pBar.setStringPainted(true);
        c.gridx = 0;
        c.gridy = 7;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(pBar, c);

        txtFile = new JTextField("arbre.abr", 7);
        c.gridx = 1;
        c.gridy = 5;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(txtFile, c);

        buttonSave = new JButton("Sauver l'arbre");
        c.gridx = 2;
        c.gridy = 5;
        c.weightx = 0.5;
        c.gridwidth = 1;
        pane1.add(buttonSave, c);

        buttonLoad = new JButton("Charger l'arbre");
        c.gridx = 3;
        c.gridy = 5;
        c.weightx = 0.5;
        c.gridwidth = 1;
        pane1.add(buttonLoad, c);

        buttonClean = new JButton("Reset");
        c.gridx = 2;
        c.gridy = 6;
        c.weightx = 1;
        c.gridwidth = 2;
        pane1.add(buttonClean, c);

        buttonRefresh = new JButton("Refresh");
        c.gridx = 2;
        c.gridy = 7;
        c.weightx = 1;
        c.gridwidth = 2;
        pane1.add(buttonRefresh, c);
        btnLoadData = new JButton("Load data");
        btnLoadData.setForeground(Color.blue);
        c.gridx = 2;
        c.gridy = 8;
        c.weightx = 1;
        c.gridwidth = 2;
        pane1.add(btnLoadData, c);
        btnSearch = new JButton("Search for Lines");
        btnSearch.setForeground(Color.blue);
        c.gridx = 2;
        c.gridy = 9;
        c.weightx = 0.5;
        c.gridwidth = 2;
        pane1.add(btnSearch, c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.ipady = 400;       //reset to default
        c.weighty = 1.0;   //request any extra vertical space
        c.gridwidth = 4;   //2 columns wide
        c.gridx = 0;
        c.gridy = 10;
        
        JScrollPane scrollPane = new JScrollPane(tree);
        pane1.add(scrollPane, c);

        tree.setModel(new DefaultTreeModel(null));
        tree.updateUI();

        txtNbreItem.addActionListener(this);
        buttonAddItem.addActionListener(this);
        buttonAddMany.addActionListener(this);
        buttonLoad.addActionListener(this);
        buttonSave.addActionListener(this);
        buttonRemove.addActionListener(this);
        buttonClean.addActionListener(this);
        buttonRefresh.addActionListener(this);
        btnLoadData.addActionListener(this);
        btnSearch.addActionListener(this);
        return pane1;
    }
}

