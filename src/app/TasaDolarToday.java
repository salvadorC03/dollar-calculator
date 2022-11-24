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
 * Obtener la tasa del dolar DolarToday
 * @author Salvador
 */
public class TasaDolarToday extends Thread {
    
    //Objeto para comunicar con la Interfaz de la aplicación 
    Interfaz interfaz;
    
    /**
     * Constructor del objeto TasaDolarToday
     * @param interfaz La interfaz de la aplicación
     */
    public TasaDolarToday(Interfaz interfaz) {
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
            
            //Obtiene el JSON con las tasas de DolarToday
            JSONObject tasas = getJson(new URL("https://s3.amazonaws.com/dolartoday/data.json"));
            //Obtiene un Double con la tasa del dólar DolarToday obtenida de la API
            double tasa_dolar = (Double) tasas.getJSONObject("USD").get("dolartoday");
            
            //Revisa si se deben limitar los decimales y si hay o no hay decimales
            if (interfaz.preferencias.redondear_precio && interfaz.preferencias.precio_decimales > 0) {
                interfaz.precio_dolar = interfaz.redondear(tasa_dolar, interfaz.preferencias.precio_decimales);
            } else {
                interfaz.precio_dolar = tasa_dolar;
            } 
            
            //Actualiza el precio en la interfaz
            interfaz.actualizarPrecio();
            
            //Ocultar ventana cargando
            interfaz.ventanaCargando.setVisible(false);
            //Mostrar mensaje success
            javax.swing.JOptionPane.showMessageDialog(null, "Obtenida la tasa del dólar DolarToday para el día actual: " + new java.text.SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date()), "Mensaje", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            //Si ocurre un error imprimirlo en consola y mostrar el siguiente mensaje
            System.out.println(e.getMessage() + "\n" + java.util.Arrays.toString(e.getStackTrace()));
            interfaz.ventanaCargando.setVisible(false);
            javax.swing.JOptionPane.showMessageDialog(null, "Ha ocurrido un error.", "¡Error!", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
}
