package app;

/*
* Interfaz Calculadora
*/
public interface Calculadora {

    /**
    * Método abstracto resultado.
    * Este método toma como dos parámetros un double "cantidad" y otro double "precio"
     * @param cantidad La cantidad en Bolívares o en Dólares
     * @param precio El precio del dólar
     * @return El resultado de la operación entre los dos valores
    */
    double resultado(double cantidad, double precio);
}
