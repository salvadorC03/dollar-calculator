package app;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.Locale;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Obtener la tasa del dólar BCV.
 */
public class TasaDolarBCV extends Thread {
    
    //Objeto para comunicar con la interfaz de la aplicación
    Interfaz interfaz;
    
    /**
     * Constructor del objeto TasaDolarBCV
     * @param interfaz La interfaz de la aplicación
     */
    public TasaDolarBCV(Interfaz interfaz) {
        this.interfaz = interfaz;
        //Inicia el Thread
        start();
    }
    
    /**
     * Método para obtener JSONObject a partir de un URL.
     * @param url URL del JSON
     * @return Objeto JSONObject con el JSON
     * @throws IOException
     * @throws JSONException 
     */
    public static JSONObject getJson(URL url) throws IOException, JSONException {
        String json = IOUtils.toString(url, Charset.forName("UTF-8"));
        return new JSONObject(json);
    }
    
    /**
     * Acción ejecutada por el Thread
     */
    @Override
    public void run() {
        try {
            //Mostrar ventana cargando en la interfaz
            interfaz.ventanaCargando.setLocationRelativeTo(null);
            interfaz.ventanaCargando.setVisible(true);
            interfaz.ventanaCargando.toFront();
            
            //Obtiene el JSON con las tasas del Banco de Venezuela
            JSONObject tasas = getJson(new URL("https://www.bancodevenezuela.com/files/tasas/tasas2.json"));
            //Obtiene un String con la tasa del dólar BCV publicada en el Banco de Venezuela
            String tasaDolarString = (String) tasas.getJSONObject("mesacambio").getJSONObject("bcv").get("dolares");
            
            //Formatear String con coma (,) a double con punto (.)
            NumberFormat numberFormat = NumberFormat.getInstance(Locale.FRANCE);
            double tasa_dolar = numberFormat.parse(tasaDolarString).doubleValue();
            
            //Revisa si se deben limitar los decimales y si hay o no hay decimales
            if (interfaz.preferencias.redondear_precio && interfaz.preferencias.precio_decimales > 0) {
                interfaz.precio_dolar = interfaz.redondear(tasa_dolar, interfaz.preferencias.precio_decimales);
            } else {
                interfaz.precio_dolar = tasa_dolar;
            } 
            
            String fecha = new java.text.SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date());
            interfaz.ult_vez_act = fecha;
            
            interfaz.tasa_actual = "BCV";
            
            //Actualiza el precio en la interfaz
            interfaz.actualizarPrecio();
            
            //Actualiza las opciones en la interfaz
            interfaz.actualizarOpciones();
            
            //Ocultar ventana cargando
            interfaz.ventanaCargando.setVisible(false);
            //Mostrar mensaje success
            javax.swing.JOptionPane.showMessageDialog(null, "Obtenida la tasa del dólar BCV para el día actual: " + fecha, "Mensaje", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            //Si ocurre un error imprimirlo en consola y mostrar el siguiente mensaje
            System.out.println(e.getMessage() + "\n" + java.util.Arrays.toString(e.getStackTrace()));
            interfaz.ventanaCargando.setVisible(false);
            javax.swing.JOptionPane.showMessageDialog(null, "Ha ocurrido un error.", "¡Error!", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
}
