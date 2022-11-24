package app;

import java.util.List;

/**
 * Preferencias del usuario.
 */
public class Preferencias {
    
    //Declaración de variables
    String background;
    double precio_dolar;
    boolean redondear_resultado;
    boolean redondear_precio;
    int resultado_decimales;
    int precio_decimales;
    boolean cargar_tasa;
    String tasa;

    /**
     * Constructor privado de la clase Preferencias
     * @param background Fondo de pantalla (String)
     * @param precio_dolar Precio del dólar (double)
     * @param redondear_resultado Redondear Resultado (boolean)
     * @param redondear_precio Redondear Precio (boolean)
     * @param resultado_decimales Decimales del Resultado (int)
     * @param precio_decimales Decimales dle Precio (int)
     * @param cargar_tasa Cargar Tasa (boolean)
     * @param tasa Tasa (String)
     */
    private Preferencias(String background, double precio_dolar, boolean redondear_resultado, boolean redondear_precio, int resultado_decimales, int precio_decimales, boolean cargar_tasa, String tasa) {
        this.background = background;
        this.precio_dolar = precio_dolar;
        this.redondear_resultado = redondear_resultado;
        this.redondear_precio = redondear_precio;
        this.resultado_decimales = resultado_decimales;
        this.precio_decimales = precio_decimales;
        this.cargar_tasa = cargar_tasa;
        this.tasa = tasa;
    }
    
    /**
     * Este método construye las preferencias por defecto.
     * @return Nuevo objeto preferencias con las preferencias por defecto.
     */
    public static Preferencias preferenciasPorDefecto() {
        return new Preferencias(
                "background_blue",
                1.0,
                true,
                true,
                3,
                3,
                true,
                "bcv"
        );
    }
    
    /**
     * Este método construye las preferencias del usuario a partir de una lista de String.
     * @param lista Lista de String con las preferencias.
     * @return Nuevo objeto preferencias con las preferencias de la lista.
     */
    public static Preferencias cargarPreferencias(List<String> lista) {
        return new Preferencias(
                lista.get(0),
                Double.parseDouble(lista.get(1)),
                Boolean.parseBoolean(lista.get(2)),
                Boolean.parseBoolean(lista.get(3)),
                Integer.parseInt(lista.get(4)),
                Integer.parseInt(lista.get(5)),
                Boolean.parseBoolean(lista.get(6)),
                lista.get(7)
        );
    }
    
    /**
     * Este método devuelve el valor en String de las preferencias.
     * @return String con las preferencias del usuario.
     */
    @Override
    public String toString() {
        return background + "\n"
                + precio_dolar + "\n"
                + redondear_resultado + "\n"
                + redondear_precio + "\n"
                + resultado_decimales + "\n"
                + precio_decimales + "\n"
                + cargar_tasa + "\n"
                + tasa;
    }
}
