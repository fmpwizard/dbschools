package com.dbschools.music.ui;

import com.dbschools.music.dao.RemoteDao;
import com.dbschools.music.events.Event;
import com.dbschools.music.events.EventObserver;
import com.dbschools.music.events.TypeCode;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.*;

import com.dbschools.music.orm.Group;
import com.dbschools.music.orm.Instrument;

/**
 * A dialog allowing the selection of students by group and instrument.
 * 
 * @author David C. Briccetti
 */
public class StudentSelectorDialog extends javax.swing.JDialog {
    private static final long serialVersionUID = -5126944784986561307L;
    private final DefaultListModel groupsModel = new DefaultListModel();
    private final DefaultListModel instrumentsModel = new DefaultListModel();
    private final Collection<Group> selectedGroups = new ArrayList<Group>();
    private final Collection<Instrument> selectedInstruments = new ArrayList<Instrument>();
    private boolean canceled = false;
    
    public StudentSelectorDialog(java.awt.Frame parent, final RemoteDao remoteDao,
            final boolean includeTestingGroupsOnly) {
        super(parent, true);
        initComponents();
        setLocationRelativeTo(parent);
        groupsList.setModel(groupsModel);
        instrumentsList.setModel(instrumentsModel);
        setGroups(remoteDao.getGroups(), includeTestingGroupsOnly);
        setInstruments(remoteDao.getInstruments());
        allGroups.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectAllGroups();
            }});
        allInstruments.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectAllInstruments();
            }});
        remoteDao.addEventObserver(new EventObserver() {
            public void notify(Event event) {
                if (event.getTypeCode() == TypeCode.UPDATE_OBJECT && event.getDetails() instanceof Group) {
                    setGroups(remoteDao.getGroups(), includeTestingGroupsOnly);
                }
            }
        });
        getRootPane().setDefaultButton(selectButton);
    }
    
    public void setGroups(Collection<Group> groups, boolean includeTestingGroupsOnly) {
        groupsModel.clear();
        for (Group group : groups) {
            if (! includeTestingGroupsOnly || group.isDoesTesting()) {
                groupsModel.addElement(new NamedItemDisplayAdapter(group));
            }
        }
        selectAllGroups();
    }

    private void selectAllGroups() {
        groupsList.getSelectionModel().setSelectionInterval(0, groupsModel.size() - 1);
    }
    
    public void setInstruments(Collection<Instrument> instruments) {
        instrumentsModel.clear();
        for (Instrument inst : instruments) {
            instrumentsModel.addElement(new NamedItemDisplayAdapter(inst));
        }
        selectAllInstruments();
    }

    private void selectAllInstruments() {
        instrumentsList.getSelectionModel().setSelectionInterval(0, instrumentsModel.size() - 1);
    }
    
    public Collection<Group> getSelectedGroups() {
        return selectedGroups;
    }
    
    public Collection<Instrument> getSelectedInstruments() {
        return selectedInstruments;
    }

    public int getGroupsCount() {
        return groupsModel.getSize();
    }

    public int getInstrumentsCount() {
        return instrumentsModel.getSize();
    }

    public boolean isCanceled() {
        return canceled;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        GridBagConstraints gridBagConstraints;

        jScrollPane1 = new JScrollPane();
        instrumentsList = new JList();
        jLabel2 = new JLabel();
        jScrollPane2 = new JScrollPane();
        groupsList = new JList();
        jLabel1 = new JLabel();
        allGroups = new JButton();
        allInstruments = new JButton();
        jPanel1 = new JPanel();
        selectButton = new JButton();
        cancelButton = new JButton();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Student Selection Dialog");
        getContentPane().setLayout(new GridBagLayout());

        jScrollPane1.setPreferredSize(new Dimension(260, 300));

        instrumentsList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                instrumentsListMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(instrumentsList);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(0, 6, 0, 6);
        getContentPane().add(jScrollPane1, gridBagConstraints);

        jLabel2.setText("Instruments");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(4, 6, 0, 0);
        getContentPane().add(jLabel2, gridBagConstraints);

        jScrollPane2.setPreferredSize(new Dimension(260, 300));

        groupsList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                groupsListMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(groupsList);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(0, 6, 0, 0);
        getContentPane().add(jScrollPane2, gridBagConstraints);

        jLabel1.setText("Groups");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(4, 6, 0, 0);
        getContentPane().add(jLabel1, gridBagConstraints);

        allGroups.setMnemonic('G');
        allGroups.setText("All Groups");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 6, 0, 0);
        getContentPane().add(allGroups, gridBagConstraints);

        allInstruments.setMnemonic('I');
        allInstruments.setText("All Instruments");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 6, 0, 0);
        getContentPane().add(allInstruments, gridBagConstraints);

        selectButton.setText("OK");
        selectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                selectButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(selectButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(cancelButton)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(selectButton)
                    .add(cancelButton)))
        );

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(0, 0, 4, 0);
        getContentPane().add(jPanel1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void groupsListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_groupsListMouseClicked
        processSelectionOnDoubleClick(evt);
    }//GEN-LAST:event_groupsListMouseClicked

    private void instrumentsListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_instrumentsListMouseClicked
        processSelectionOnDoubleClick(evt);
    }//GEN-LAST:event_instrumentsListMouseClicked

    private void processSelectionOnDoubleClick(java.awt.event.MouseEvent evt) {
        if (evt.getClickCount() == 2) {
            processSelection();
        }
    }

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        canceled = true;
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void selectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectButtonActionPerformed
        processSelection();
    }//GEN-LAST:event_selectButtonActionPerformed

    private void processSelection() {
        canceled = false;
        buildListFromSelected(groupsList, selectedGroups);
        buildListFromSelected(instrumentsList, selectedInstruments);
        dispose();
    }
    
    private <T> void buildListFromSelected(JList jlist, Collection<T> col) {
        col.clear();
        for (Object o : jlist.getSelectedValues()) {
            NamedItemDisplayAdapter nida = (NamedItemDisplayAdapter) o;
            col.add((T) nida.getNamedItem());
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton allGroups;
    private JButton allInstruments;
    private JButton cancelButton;
    private JList groupsList;
    private JList instrumentsList;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JPanel jPanel1;
    private JScrollPane jScrollPane1;
    private JScrollPane jScrollPane2;
    private JButton selectButton;
    // End of variables declaration//GEN-END:variables

}
