/*
 * DBSchools
 * Copyright (C) 2005 David C. Briccetti
 * www.davebsoft.com
 *
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 2 of the License, or 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.dbschools.gui;

import java.awt.Frame;
import java.lang.reflect.InvocationTargetException;
import java.rmi.AccessException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.UnknownHostException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.dbschools.DatabaseAccessException;
import com.dbschools.NoSuchUserPasswordException;

/**
 * Prompts for login information.
 */
abstract public class LoginDialog extends CustomDialog {

    private static final long serialVersionUID = -6442989336723990055L;

    public LoginDialog(Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        userName.grabFocus();
        setDefaultButton(login);

        if (parent == null) {
            setLocationRelativeTo(null);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        userName = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        login = new javax.swing.JButton();
        cancel = new javax.swing.JButton();
        statusMsg = new javax.swing.JLabel();
        password = new javax.swing.JPasswordField();
        jSeparator1 = new javax.swing.JSeparator();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        setTitle("DBSchools Login");
        jLabel2.setDisplayedMnemonic('U');
        jLabel2.setLabelFor(userName);
        jLabel2.setText("User Name:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 11, 11);
        getContentPane().add(jLabel2, gridBagConstraints);

        jLabel3.setDisplayedMnemonic('P');
        jLabel3.setLabelFor(password);
        jLabel3.setText("Password:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 11, 11);
        getContentPane().add(jLabel3, gridBagConstraints);

        userName.setColumns(15);
        userName.setMinimumSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 4);
        getContentPane().add(userName, gridBagConstraints);

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 5, 0));

        login.setMnemonic('L');
        login.setText("Log In");
        login.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loginActionPerformed(evt);
            }
        });

        jPanel1.add(login);

        cancel.setText("Cancel");
        cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelActionPerformed(evt);
            }
        });

        jPanel1.add(cancel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(17, 12, 11, 11);
        getContentPane().add(jPanel1, gridBagConstraints);

        statusMsg.setFont(new java.awt.Font("Dialog", 1, 14));
        statusMsg.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 12, 6, 11);
        getContentPane().add(statusMsg, gridBagConstraints);

        password.setColumns(15);
        password.setMinimumSize(new java.awt.Dimension(80, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 4);
        getContentPane().add(password, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        getContentPane().add(jSeparator1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelActionPerformed
        this.exitDialog(false);
    }//GEN-LAST:event_cancelActionPerformed

    protected void setStatusMsg(final String msg)
            throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
                statusMsg.setText(msg);
            }
        });
    }

    protected abstract void tryLogin() throws InterruptedException,
            InvocationTargetException, RemoteException, NotBoundException,
            AccessException, DatabaseAccessException, NoSuchUserPasswordException;

    private void loginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loginActionPerformed
        login.setEnabled(false);
        final SwingWorker worker = new SwingWorker() {

            private Exception exc;

            @Override
            public Object construct() {
                try {
                    tryLogin();
                } catch (Exception e) {
                    exc = e;
                }
                return null;
            }

            @Override
            public void finished() {
                if (exc != null) {
                    if (exc instanceof NoSuchUserPasswordException) {
                        statusMsg.setText("Incorrect user/password");
                    } else if (exc instanceof UnknownHostException) {
                        statusMsg.setText("Unknown host");
                    } else if (exc instanceof NotBoundException) {
                        statusMsg.setText("Unable to connect to the server (not bound)");
                    } else if (exc instanceof ConnectException) {
                        statusMsg.setText("Unable to connect to the server: " +
                                exc.getMessage());
                    } else if (exc instanceof RemoteException) {
                        exc.printStackTrace();
                        JOptionPane.showMessageDialog(null, exc.getClass().getName() + " " +
                                exc.getMessage());
                        statusMsg.setText("");
                    } else {
                        ErrorHandler.handleException(exc);
                        statusMsg.setText("");
                    }
                }

                if (loggedIn) {
                    hide();
                } else {
                    login.setEnabled(true);
                }
            }
        };
        worker.start();
    }//GEN-LAST:event_loginActionPerformed
    private boolean loggedIn = false;

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean b) {
        loggedIn = b;
    }

    public String getUserName() {
        return userName.getText();
    }

    public String getPassword() {
        return new String(password.getPassword());
    }

    public void setUserName(String userName) {
        this.userName.setText(userName);
    }

    public void setPassword(String password) {
        this.password.setText(password);
    }

    protected void rejectBadUserPass(String userNameText, final String passwordText) throws NoSuchUserPasswordException {
        if (userNameText.length() == 0 || passwordText.length() == 0) {
            throw new NoSuchUserPasswordException();
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancel;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton login;
    private javax.swing.JPasswordField password;
    private javax.swing.JLabel statusMsg;
    private javax.swing.JTextField userName;
    // End of variables declaration//GEN-END:variables
}
