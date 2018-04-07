package temaocho;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author robert
 */
class ComparadorTelef implements Comparator<String> {
    @Override
    public int compare(String telefonoUno, String telefonoDos) {
        /*
         * los numeros locales son mayores que los internacionales segun criterio de enunciado
         * orden de menor a mayor <- caso 1 ordenacion normal
         * Si el primer objeto  es menor que el segundo , debe retornar un número entero negativo.
         * Si el primer objeto  es mayor que el segundo , debe retornar un número entero positivo.
         * Si ambos son iguales, debe retornar 0.
         *
         * orden de mayor a menor  <- caso 2 internacionales vs locales
         * Si el primer objeto (o1) debe ir antes que el segundo objeto (o2), retornar entero negativo. 
         * Si el primer objeto (o1) debe ir después que el segundo objeto (o2), retornar entero positivo.
         * Si ambos son iguales, debe retornar 0.
         */
        //inicio caso 2
        if (telefonoUno.equals(telefonoDos)) { // Si ambos son iguales, debe retornar 0.
            return 0;
        }  
        if (telefonoUno.startsWith("+") && !telefonoDos.startsWith("+")) { // si el telefono1 es internacional y el telefono2 no es internacional entonces telefono2 es mayor
            return 1;
        }
        if (!telefonoUno.startsWith("+") && telefonoDos.startsWith("+")) { // si el telefono1 no es internacional y el segundo si entonces el telefono1 es mayor
            return -1;
        }
        //fin caso 1
        
        //inicio caso 2
        // se procede a ordenar los numeros internacionales
        long telefonoUnoInt=Long.parseLong(telefonoUno.replaceAll("\\+", "")); // convertir String a long y eliminacion signo + 
        long telefonoDosInt=Long.parseLong(telefonoDos.replaceAll("\\+", ""));
        
        if (telefonoDosInt<telefonoUnoInt) { //Si el primer objeto  es menor que el segundo , debe retornar un número entero negativo.
            return -1;
        }
        else if (telefonoDosInt>telefonoUnoInt) { //Si el primer objeto  es mayor que el segundo , debe retornar un número entero positivo.
            return 1;
        }else{ //Si ambos son iguales, debe retornar 0.
            return 0;
        }
        //fin caso 2
    }
}

public class ClientToXml {
    public static void main(String[] args) {
        String cliente;
        Document documentoDOM = DOMUtil.crearDOMVacio("datos_cliente");

        UIManager.put("OptionPane.minimumSize",new Dimension(800,200));  // tamaño de JOptionpanel
        cliente = JOptionPane.showInputDialog("Ingrese cliente: ", "12345678Z,\"nombre\",\"apellidos\", prueba@prueba.com,(952)333333,test@test.com ,952333333,test@TEST.com");
  
        //cliente = "X12345678F,\"robert\",\"anrei\",+(82)12345678, 612345678,test@TEST.com,(91)23456789 ,prueba@prueba.com";
        //cliente2 = "12345678Z,\"nombre\",\"apellidos\", prueba@prueba.com,(952)333333,test@test.com ,952333333,test@TEST.com";
        
        if ( cliente == null || cliente.length() == 0 )
        {
            documentoDOM.appendChild(documentoDOM.createComment("ERROR: No se ha introducido datos del cliente."));
        }else{
            //Creamos las expresiones regulares.
            Pattern DNI             = Pattern.compile("[XYxy]?[0-9]{1,9}[A-Za-z]");
            Pattern nombreOapelido  = Pattern.compile("\"[\\w\\s]+\"");
            Pattern telefono        = Pattern.compile("\\+?[0-9\\(\\)]*");
            Pattern mail            = Pattern.compile("[_a-zA-Z\\.]+[@]{1}[a-zA-Z\\.]+\\.{1}[a-zA-Z]+");
            
            Element raiz=documentoDOM.getDocumentElement(); //raiz del documento xml
            
            String datosCliente[] = cliente.split(","); //convertir string a array
            
            boolean exit=false; //Controlador de errores
            //==============================
            // LIMPIAR ESPACIOS EN BLANCO
            //==============================
            for (int x=0 ; x < datosCliente.length ; x++){
                datosCliente[x] = datosCliente[x].trim();
            }
            
            //========
            //  DNI
            //========
            Matcher mDNI = DNI.matcher(datosCliente[0]);
            if ( mDNI.matches() ) {
                Element id = documentoDOM.createElement("id"); // nombre del elemento en xml
                id.setTextContent(datosCliente[0]); //contenido del elemento en xml
                raiz.appendChild(id); // insertar en el documento xml el nodo
            }else{
                exit = true; documentoDOM.appendChild(documentoDOM.createComment("ERROR: No se encuentra DNI o NIE en posicion 0")); 
            }
            //============
            //  NOMBRE
            //============
            if(!exit){
                Matcher mNombre = nombreOapelido.matcher(datosCliente[1]);
                if ( mNombre.matches() ) {
                    Element nombre = documentoDOM.createElement("nombre"); //nombre del elemento en xml
                    nombre.setTextContent(datosCliente[1].replaceAll("\"", "").trim()); // remplaza comillas dobles y elimina espacio al inicio y final
                    raiz.appendChild(nombre); // inserta el nodo
                }else{ 
                    exit = true; documentoDOM.appendChild(documentoDOM.createComment("ERROR: No se encuentra nombre en posicion 2")); 
                }
            }
            //==============
            //  APELLIDOS
            //==============
            if(!exit){
                Matcher mApellido = nombreOapelido.matcher(datosCliente[2]);
                if ( mApellido.matches() ) {
                    Element nombre = documentoDOM.createElement("apellidos"); // nombre del elemneto en xml
                    nombre.setTextContent(datosCliente[2].replaceAll("\"", "").trim()); // remplaza comillas dobles y elimina espacio al inicio y final
                    raiz.appendChild(nombre); // inserta el nodo
                }else{ 
                    exit = true; documentoDOM.appendChild(documentoDOM.createComment("ERROR: No se encuentra apellidos en posicion 2")); 
                }
            }
            //==========================================
            //  CREACION ARRAY LIST -> TELEFONO Y MAIL
            //==========================================
            //crear array list para poder ordenar con criterios especificos        
            ArrayList<String> telefonos=new ArrayList<>();
            ArrayList<String> mails=new ArrayList<>();
            if(!exit){
                for (int i=3;i<datosCliente.length && !exit;i++){ // se inicia en posicion tres porque sino daria error al comparar dni nombre y apellidos en la expresiones regulares
                    Matcher mTelefono = telefono.matcher(datosCliente[i]);
                    Matcher mMail = mail.matcher(datosCliente[i]);
                    if ( mTelefono.matches() ) {
                        
                        String telLimpio = datosCliente[i].replaceAll("[\\(\\) ]", "").trim(); // remplaza parentesis y espacion en blanco al inicio y final
                        if (!telefonos.contains(telLimpio)) { // comprueba si en array list ya existe el telefono antes de añadirlo 
                            telefonos.add(telLimpio); // añade a la lista el telefono
                        } 
                        else {
                            documentoDOM.appendChild(documentoDOM.createComment("Advertencia: Telefono "+telLimpio+" duplicado"));
                        } 
                        
                    }else if( mMail.matches() ){
                        
                        String mailLimpio = datosCliente[i].toLowerCase(); // convierte todo el string a minisculas
                        if (!mails.contains(mailLimpio)) { // comprueba si en array list ya existe el telefono antes de añadirlo
                            mails.add(mailLimpio); //añade a la lista el mail
                        } 
                        else {
                            documentoDOM.appendChild(documentoDOM.createComment("Advertencia: Mail "+mailLimpio+" duplicado"));
                        }
                        
                    }else{
                        exit=true; 
                        documentoDOM.appendChild(documentoDOM.createComment("ERROR: Dato " + datosCliente[i] + " no esperado, se esperaba mail o telefono."));
                    }
                }
            }
            //================================================
            //  APPENCHILD DE TELEFONO Y MAIL -> ORDENADOS
            //================================================
            if(!exit){
                Collections.sort(telefonos,new ComparadorTelef()); // se ordena lso telefonos a traves de la clase Comparador telef
                
                Element elementoTelefonos=documentoDOM.createElement("telefonos"); // nombre del elemento en xml
                elementoTelefonos.setAttribute("total",""+telefonos.size()); // se crear el aitrubuto de los elementos tefono con nombre total y la cantidad de telefonos
                for (String stringTelefono: telefonos)
                {
                    Element elementoTelefono=documentoDOM.createElement("telefono"); // nombre del elemento en xml
                    elementoTelefono.setTextContent(stringTelefono); // contenido del elemento xml
                    elementoTelefonos.appendChild(elementoTelefono); // inserta el elemento y su contenido en el domcumento xml
                }
                raiz.appendChild(elementoTelefonos);
                
                Element elementoMails=documentoDOM.createElement("mails"); // nombre del elemento en xml
                elementoMails.setAttribute("total",""+mails.size()); // se crear el aitrubuto de los elementos tefono con nombre total y la cantidad de telefonos
                for (String stringMail: mails)
                {
                    Element elementoMail=documentoDOM.createElement("mail"); // nombre del elemento en xml
                    elementoMail.setTextContent(stringMail); // contenido del elemento xml
                    elementoMails.appendChild(elementoMail); // inserta el elemento y su contenido en el domcumento xml
                }
                raiz.appendChild(elementoMails);
            }
        }
        JOptionPane.showMessageDialog(null, DOMUtil.DOM2XML(documentoDOM));
    }
}
