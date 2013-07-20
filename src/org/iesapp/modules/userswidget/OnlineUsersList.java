/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * OnlineUsersList.java
 *
 * Created on 17-feb-2012, 14:32:53
 */
package org.iesapp.modules.userswidget;

import org.iesapp.framework.pluggable.IniParameters;
import org.iesapp.framework.util.CoreCfg;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 *
 * @author Josep
 */
public class OnlineUsersList extends javax.swing.JDialog {
    
    private DefaultListModel listModel1;
    private final ArrayList<String> listDepart;
    private final ArrayList<String> listAbrevs;
    private String whoami;
    private int nOnline = 0;
    private Point lastPosition;
    private IniParameters iniParameters;
    private final CoreCfg coreCfg;

    /** Creates new form OnlineUsersList */
    public OnlineUsersList(java.awt.Frame parent, boolean modal, CoreCfg coreCfg) {
        super(parent, modal);
        this.coreCfg = coreCfg;
        initComponents();
        listModel1 = new DefaultListModel();
        listDepart = new ArrayList<String>();
        listAbrevs = new ArrayList<String>();
        jList1.setModel(listModel1);
        jList1.setCellRenderer( new CustomCellRenderer() );
      
    }

     public void setOpacity(float opac)
     {
         try {
           Class<?> awtUtilitiesClass = Class.forName("com.sun.awt.AWTUtilities");
           Method mSetWindowOpacity = awtUtilitiesClass.getMethod("setWindowOpacity", Window.class, float.class);
           mSetWindowOpacity.invoke(null, this, Float.valueOf(opac));
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(OnlineUsersSms.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(OnlineUsersSms.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(OnlineUsersSms.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(OnlineUsersSms.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(OnlineUsersSms.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(OnlineUsersSms.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public void update() throws SQLException
    {
        listModel1.removeAllElements();
        listDepart.clear();
        listAbrevs.clear();
        nOnline = 0;
        
        // Elimina de conectats els que executin anuncis o els usuaris que no siguin compatibles amb abrev
        
        String SQL1 = "SELECT DISTINCT IF(sl.usua='PREF','PREFECTURA', IF(sl.usua='ADMIN',"
                + "'ADMINISTRADOR',prof.nombre)) as nombre, prof.depart, sl.usua FROM sig_log "
                + " as sl INNER JOIN sig_professorat as prof "
                + " ON sl.usua=prof.abrev "
                + " WHERE DATE(inici)=CURRENT_DATE() AND sl.fi IS NULL AND sl.usua NOT LIKE 'GUARD' "
                + " AND sl.usua!='' AND tasca!='Anuncis' ORDER BY prof.nombre";
       // System.out.println("SQL1="+SQL1);
         try {
             Statement st = coreCfg.getMysql().createStatement();
             ResultSet rs1 = coreCfg.getMysql().getResultSet(SQL1,st);
            while(rs1!=null && rs1.next())
            {
                listModel1.addElement( new javax.swing.JLabel( rs1.getString("nombre"),
                        new javax.swing.ImageIcon(getClass().getResource("/org/iesapp/framework/icons/online.gif")), JLabel.LEADING));
                listDepart.add( rs1.getString("depart") );
                listAbrevs.add( rs1.getString("usua") );
                nOnline +=1;
            }
            if(rs1!=null){
                rs1.close();
                st.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(OnlineUsersList.class.getName()).log(Level.SEVERE, null, ex);
        }
       
        
        //Afegeix la resta d'usuaris que estan off-line
        
    
       SQL1 = "SELECT * FROM sig_professorat order by nombre";
       
       try {
            Statement st = coreCfg.getMysql().createStatement();
            ResultSet rs1 = coreCfg.getMysql().getResultSet(SQL1,st);
            while(rs1!=null && rs1.next())
            {
                if(!listAbrevs.contains(rs1.getString("abrev")))
                {
                    JLabel label = new javax.swing.JLabel( rs1.getString("nombre"),
                        new javax.swing.ImageIcon(getClass().getResource("/org/iesapp/framework/icons/offline.gif")), JLabel.LEADING);
                    label.setForeground(Color.gray);
                    listModel1.addElement(label);
                    listDepart.add( rs1.getString("depart") );
                    listAbrevs.add( rs1.getString("abrev") );
                }
            }
            if(rs1!=null){
                rs1.close();
                st.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(OnlineUsersList.class.getName()).log(Level.SEVERE, null, ex);
        }
  
       
             
        if(!listAbrevs.contains("PREF"))
        {
             JLabel label = new javax.swing.JLabel( "PREFECTURA",
                new javax.swing.ImageIcon(getClass().getResource("/org/iesapp/framework/icons/offline.gif")), JLabel.LEADING);
                    label.setForeground(Color.gray);
                    listModel1.addElement(label);
                    listDepart.add( "" );
                    listAbrevs.add( "PREF" ); 
        }
        if(!listAbrevs.contains("ADMIN"))
        {
             JLabel label = new javax.swing.JLabel( "ADMINISTRADOR",
                new javax.swing.ImageIcon(getClass().getResource("/org/iesapp/framework/icons/offline.gif")), JLabel.LEADING);
                    label.setForeground(Color.gray);
                    listModel1.addElement(label);
                    listDepart.add( "" );
                    listAbrevs.add( "ADMIN" ); 
        }
       
    }
    
    public int getNumUsua()
    {
        return nOnline;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();

        setUndecorated(true);
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
                formWindowLostFocus(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jList1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jList1.setFont(new java.awt.Font("Tahoma", 0, 10));
        jList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jList1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jList1.setName("jList1"); // NOI18N
        jList1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jList1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jList1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowLostFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowLostFocus
        this.setVisible(false);
    }//GEN-LAST:event_formWindowLostFocus

    private void jList1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jList1MouseClicked
        if(!this.iniParameters.getBoolean("userswidget.enableUsersNotes", true)){
            return;
        }
        int row = jList1.getSelectedIndex();
        if(row<0 || evt.getClickCount()<2 || listAbrevs.get(row).equals(whoami)) return;
        
        String nom = ((JLabel) listModel1.getElementAt(row)).getText();
        OnlineUsersDetails oud = new OnlineUsersDetails(null, false, nom,
                listDepart.get(row), listAbrevs.get(row), whoami, coreCfg);
        oud.setAlwaysOnTop(true);
        Point p = this.getLocationOnScreen();
        
        oud.setVisible(true);
        int heigth = oud.getHeight();
        oud.setLocation(p.x, p.y+this.getHeight()-heigth);
    }//GEN-LAST:event_jList1MouseClicked

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        setOpacity(0.9f);
    }//GEN-LAST:event_formWindowOpened


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList jList1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    void setWhoami(String whoami) {
        this.whoami = whoami;
        
    }

    void checkInstantSms(Point p) {
        
        lastPosition = p;
        
        String SQL1 = "SELECT mis.id, DATE_FORMAT(mis.DATA,'%d-%m-%Y %r') AS fecha, mis.missatge, "
                + " IF(mis.de='PREF', 'PREFECTURA', IF(mis.de='ADMIN', 'ADMINISTRADOR', prof.nombre)) AS remitente,"
                + " mis.de FROM sig_missatges AS mis LEFT JOIN "
                + " sig_professorat AS prof ON prof.abrev=mis.de WHERE mis.para='" + whoami + "' AND instantani=1";
        
        //System.out.println("SQL1="+SQL1);
        
         try {
             Statement st = coreCfg.getMysql().createStatement();
             ResultSet rs1 = coreCfg.getMysql().getResultSet(SQL1,st);
            while(rs1!=null && rs1.next())
            {
                int id = rs1.getInt("id");
                
                OnlineUsersSms sms = new OnlineUsersSms(null, false, rs1.getString("remitente"), rs1.getString("missatge"),
                        rs1.getString("fecha"), rs1.getString("de"));
                sms.setAlwaysOnTop(true);
                sms.setOpacity(0.0f);
                sms.setVisible(true);
                int heigth = sms.getHeight();
                int width = sms.getWidth();
                sms.setLocation(p.x, p.y-heigth);
                
                sms.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent evt) {
                        //System.out.println("triggered"+evt);  
                        replaySms( (String) evt.getNewValue());
                    }
                });
                
                SQL1 = "DELETE FROM sig_missatges WHERE id="+id;
                int nup = coreCfg.getMysql().executeUpdate(SQL1);
            }
            if(rs1!=null){
                rs1.close();
                st.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(OnlineUsersList.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //Realocate post-its on screen
        int npost =0;
        for(java.awt.Window win: OnlineUsersSms.getWindows())
        {
            if(win.getClass().equals(OnlineUsersSms.class) && win.isVisible())
            {
                OnlineUsersSms sms  = (OnlineUsersSms) win;
                int height = sms.getHeight();
                sms.setLocation(p.x-npost*15, p.y-height-npost*15);
                sms.setPostOrder(npost);
                sms.setEnableReply(iniParameters.getBoolean("userswidget.enableUsersNotes", true));
                npost += 1;
            }
        }
        
    }

    void clear() {
        String SQL1 = "DELETE FROM sig_missatges WHERE para='"+whoami+"' AND instantani=1";
        int nup = coreCfg.getMysql().executeUpdate(SQL1);
    }

    private void replaySms(String para) {
        //System.out.println("replaySms para"+para);
        int pos = listAbrevs.indexOf(para);
        if(pos<0) return;    
        String nom = ((JLabel) listModel1.getElementAt(pos)).getText();
        OnlineUsersDetails oud = new OnlineUsersDetails(null, false, nom,
                listDepart.get(pos), listAbrevs.get(pos), whoami, coreCfg);
        oud.setAlwaysOnTop(true);
        
        oud.setVisible(true);
        int heigth = oud.getHeight();
        oud.setLocation(lastPosition.x, lastPosition.y-heigth);
    }

    public void setParameters(IniParameters iniParameters) {
        this.iniParameters = iniParameters;
    }

    private static class CustomCellRenderer implements ListCellRenderer {

        public CustomCellRenderer() {
        }
       
         public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
             
             JLabel label = (JLabel) value;
           
             
             if(isSelected)
             {
                 label.setOpaque(true);
                 DefaultListCellRenderer adaptee = new DefaultListCellRenderer();
                 label.setBackground(adaptee.getForeground());
             }
             else
             {  
                 label.setOpaque(false);
             }
             
             return (Component) label;
        }
    }
}
