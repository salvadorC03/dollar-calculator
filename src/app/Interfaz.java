package app;

import java.io.*;
import java.util.Locale;
import java.awt.HeadlessException;
import java.awt.Image;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;

/**
 * Dollar Calculator v1.1 Proyecto de Diplomado Java UNEWEB, alumno Salvador
 * Cammarata.
 *
 * @author Salvador
 */
public class Interfaz extends javax.swing.JFrame {

    //Variables)
    //Fondo (background)
    private static String background = "background_purple";

    //Valor del precio del dólar
    public static double precio_dolar = 1;

    //Última vez actualizado (Precio)
    public static String ult_vez_act = "ninguno";

    //Tasa actual
    public static String tasa_actual = "ninguno";

    //Preferencias
    static Preferencias preferencias;

    //Constructor de la interfaz
    public Interfaz() {
        initComponents();
        cargarDatos();
        iniciar();
    }

    /**
     * Este método inicia la aplicación y se encarga de ejecutar por primera vez
     * los métodos para actualizar precio y opciones en pantalla.
     */
    private void iniciar() {
        setLocationRelativeTo(null);
        setIconImage(new javax.swing.ImageIcon(getClass().getResource("/resources/icon.png")).getImage());
        actualizarPrecio();
        actualizarOpciones();

        if (preferencias.cargar_tasa) {
            switch (preferencias.tasa) {
                case "bcv":
                    new TasaDolarBCV(this);
                    break;
                case "today":
                    new TasaDolarToday(this);
                    break;
            }
        }
    }

    /**
     * Método para actualizar las preferencias del usuario. Este método se
     * ejecuta luego de hacer click en el botón Aceptar de la ventana
     * Preferencias.
     */
    public void actualizarPreferencias() {
        int redondearPrecioIndex = cmbPreferenciasRedondearPrecio.getSelectedIndex();
        int redondearResultadoIndex = cmbPreferenciasRedondearResultado.getSelectedIndex();

        switch (redondearPrecioIndex) {
            case 0:
                preferencias.redondear_precio = true;
                preferencias.precio_decimales = 0;
                precio_dolar = Math.round(precio_dolar);
                actualizarPrecio();
                break;
            case 11:
                preferencias.redondear_precio = false;
                break;
            default:
                preferencias.redondear_precio = true;
                preferencias.precio_decimales = redondearPrecioIndex;

                try {
                    precio_dolar = redondear(precio_dolar, preferencias.precio_decimales);
                    actualizarPrecio();
                } catch (ParseException e) {
                }

                break;
        }

        switch (redondearResultadoIndex) {
            case 0:
                preferencias.redondear_resultado = true;
                preferencias.resultado_decimales = 0;
                break;
            case 11:
                preferencias.redondear_resultado = false;
                break;
            default:
                preferencias.redondear_resultado = true;
                preferencias.resultado_decimales = redondearResultadoIndex;
                break;
        }

        if (checkBoxPreferenciasCargarPrecio.isSelected()) {
            preferencias.cargar_tasa = true;
            switch (cmbPreferenciasTasa.getSelectedIndex()) {
                case 0:
                    preferencias.tasa = "bcv";
                    break;
                case 1:
                    preferencias.tasa = "today";
                    break;
            }
        } else {
            preferencias.cargar_tasa = false;
        }
    }

    /**
     * Método para actualizar opciones en pantalla.
     */
    public void actualizarOpciones() {
        //Ocultar cálculo
        txtCalcular.setVisible(false);
        txtResultado.setVisible(false);
        labelIgualA.setVisible(false);
        botonAceptar.setVisible(false);

        //Mostrar opciones
        labelPrecioDolar.setVisible(true);
        labelIntroducirMonto.setVisible(true);
        labelUltimaVezAct.setVisible(true);
        labelTasaActual.setVisible(true);
        txtInformacion.setVisible(true);
        botonCalcular.setVisible(true);

        //Revisa la opción seleccionada en el Combo Box
        //== 0: Bolívares a Dólares. >= 1: Dólares a Bolívares
        switch (cmbOpciones.getSelectedIndex()) {
            case 0:
                labelIntroducirMonto.setText(
                        "Introducir monto en bolívares:");
                break;
            default:
                labelIntroducirMonto.setText(
                        "Introducir monto en dólares:");
        }

        //Revisa si la fecha de última vez act. no es igual a ninguno
        if (!ult_vez_act.equals("ninguno")) {
            labelUltimaVezAct.setText("Última vez actualizado: " + ult_vez_act);
        } else {
            labelUltimaVezAct.setText("");
        }

        //Revisa si la tasa actual no es igual a ninguno
        if (!tasa_actual.equals("ninguno")) {
            labelTasaActual.setText("Tasa actual: " + tasa_actual);
        } else {
            labelTasaActual.setText("");
        }
    }

    /**
     * Método para actualizar el precio en pantalla.
     */
    public void actualizarPrecio() {
        try {
            //Si hay decimales en el número se muestra un valor double
            if (preferencias.precio_decimales > 0) {
                labelPrecioDolar.setText("Precio del dólar: " + precio_dolar + " Bolívares.");
                //Si no hay decimales en el número se muestra un valor entero
            } else {
                labelPrecioDolar.setText("Precio del dólar: " + Math.round(precio_dolar) + " Bolívares.");
            }
        } catch (Exception e) {
        }
    }

    /**
     * Método para cambiar el fondo de pantalla.
     */
    private void cambiarFondo() throws NullPointerException {
        javax.swing.ImageIcon backgroundImage = new javax.swing.ImageIcon(getClass().getResource("/resources/" + background + ".jpg"));
        wallPaper.setIcon(backgroundImage);
        wallPaperAcercaDe.setIcon(backgroundImage);
        wallPaperCambiarPrecio.setIcon(backgroundImage);
        wallPaperPreferencias1.setIcon(backgroundImage);
        wallPaperPreferencias2.setIcon(backgroundImage);
    }

    /**
     * Este método sirve para calcular el valor de Bolívares a Dólares/Dólares a
     * Bolívares.
     *
     * @param operacion 0 = Calcular Bolívares a Dólares. 1 = Calcular Dólares a
     * Bolívares
     */
    private void calcular(int operacion) {
        //Capturar excepciones
        try {
            String informacionString = txtInformacion.getText();

            //Revisa si el campo de texto información está vacío
            if (informacionString.equals("")) {
                throw new Exception("Debes introducir un valor.");
            }

            //Variable que contiene el valor introducido por el usuario
            double informacion;

            //Revisa si el valor introducido tiene punto (.) o coma (,)
            if (informacionString.contains(",")) {
                NumberFormat numberFormat = NumberFormat.getInstance(Locale.FRANCE);
                informacion = numberFormat.parse(informacionString).doubleValue();
            } else {
                informacion = Double.parseDouble(informacionString);
            }

            //Revisa si el valor introducido es menor o igual a cero
            if (informacion <= 0.0) {
                throw new Exception("Por favor, introduce un valor mayor a cero.");
            }
            Calculadora calcular = null;

            //Ocultar opciones de pantalla
            labelPrecioDolar.setVisible(false);
            labelIntroducirMonto.setVisible(false);
            labelUltimaVezAct.setVisible(false);
            labelTasaActual.setVisible(false);
            txtInformacion.setVisible(false);
            botonCalcular.setVisible(false);

            //Mostrar cálculo
            txtCalcular.setVisible(true);
            txtResultado.setVisible(true);
            labelIgualA.setVisible(true);
            botonAceptar.setVisible(true);

            double resultado;
            //En esta variable se guarda el tipo de resultado (Dólares o Bolívares)
            String tipoResultado = "";
            //Seleccionar operación
            //0 = Bolívares a Dólares. 1 = Dólares a Bolívares
            switch (operacion) {
                case 0:
                    //Implementar método de interfaz
                    calcular = (cantidad, precio) -> cantidad / precio;
                    txtCalcular.setText(informacion + " Bolívares");
                    tipoResultado = "Dólares";
                    break;
                case 1:
                    //Implementar método de interfaz
                    calcular = (cantidad, precio) -> cantidad * precio;
                    txtCalcular.setText(informacion + " Dólares");
                    tipoResultado = "Bolívares";
                    break;
            }

            //Asignar resultado
            resultado = calcular.resultado(informacion, precio_dolar);

            //Este código se ejecuta si se deben limitar los decimales
            if (preferencias.redondear_resultado) {
                //Este código se ejecuta si el resultado lleva decimales
                if (preferencias.resultado_decimales > 0) {
                    txtResultado.setText(redondear(resultado, preferencias.resultado_decimales) + " " + tipoResultado);
                    //Este código se ejecuta si el resultado no lleva decimales
                } else {
                    txtResultado.setText(Math.round(resultado) + " " + tipoResultado);
                }
                //Este código se ejecuta si no se deben limitar los decimales
            } else {
                txtResultado.setText(resultado + " " + tipoResultado);
            }
        } catch (ParseException | NumberFormatException e) {
            //Si se capturó una excepción mostrar el siguiente error:
            txtInformacion.setBackground(java.awt.Color.RED);
            javax.swing.JOptionPane.showMessageDialog(null, "El valor introducido no es válido.", "¡Error!", javax.swing.JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            txtInformacion.setBackground(java.awt.Color.RED);
            javax.swing.JOptionPane.showMessageDialog(null, e.getMessage(), "¡Error!", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Este método sirve para cargar los parámetros guardados por el usuario
     * cuando se inicia la aplicación.
     */
    private void cargarDatos() {
        //Abrir lector de archivo
        File archivo = null;
        FileReader fr = null;
        BufferedReader br = null;

        try {
            archivo = new File("data.dat");
            fr = new FileReader(archivo);
            br = new BufferedReader(fr);

            //Array con las preferencias del archivo data.dat
            ArrayList<String> preferenciasList = new ArrayList<>();
            String parametro;

            //Leyendo el archivo
            while ((parametro = br.readLine()) != null) {
                preferenciasList.add(parametro);
            }

            //Establecer las preferencias del usuario. Si ocurre un error usar las preferencias por defecto.
            preferencias = Preferencias.cargarPreferencias(preferenciasList);
        } catch (Exception e) {
            //Si ocurre un error imprimir el mensaje en consola y cargar las opciones por defecto.
            System.err.println("Error al cargar opciones: " + e);
            preferencias = Preferencias.preferenciasPorDefecto();
        } finally {
            //Cerrar el lector de archivo
            try {
                if (fr != null) {
                    fr.close();
                }
            } catch (IOException e) {
                System.err.println("Error al cerrar lector de archivo: " + e);
            }

            try {
                //Asignar el valor del precio del dólar guardado en preferencias
                precio_dolar = preferencias.precio_dolar;

                //Asignar el fondo de pantalla guardado en preferencias          
                background = preferencias.background;
                cambiarFondo();

                //Asignar la tasa actual y la ult. vez act.
                ult_vez_act = preferencias.ult_vez_act;
                tasa_actual = preferencias.tasa_actual;
            } catch (Exception e) {
                //Si ocurre un error asignar preferencias por defecto
                System.err.println("Error al asignar opciones: " + e);
                preferencias = Preferencias.preferenciasPorDefecto();

                //Asignar el valor del precio del dólar guardado en preferencias
                precio_dolar = preferencias.precio_dolar;

                //Asignar el fondo de pantalla guardado en preferencias          
                background = preferencias.background;
                cambiarFondo();

                //Asignar la tasa actual y la ult. vez act.
                ult_vez_act = preferencias.ult_vez_act;
                tasa_actual = preferencias.tasa_actual;
            }
        }
    }

    /**
     * Este método se encarga de guardar las preferencias del usuario. Se
     * ejecuta este método al finalizar la aplicación.
     */
    public static void guardarDatos() {
        //Asignar tarea al cerrar el proceso
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                //Abrir escritor de archivo
                FileWriter fichero = null;
                PrintWriter pw = null;

                try {
                    //Crear nuevo fichero
                    fichero = new FileWriter("data.dat");
                    pw = new PrintWriter(fichero);

                    //Establecer el fondo y el precio del dólar en las preferencias
                    preferencias.background = background;
                    preferencias.precio_dolar = precio_dolar;

                    //Establecer la ult. vez act. y la tasa actual en las preferencias
                    preferencias.ult_vez_act = ult_vez_act;
                    preferencias.tasa_actual = tasa_actual;

                    //Guardar preferencias
                    pw.println(preferencias.toString());

                } catch (IOException e) {
                    System.err.println("Error al guardar opciones: " + e);
                } finally {
                    //Cerrar escritor de archivo
                    try {
                        if (fichero != null) {
                            fichero.close();
                        }
                    } catch (IOException e) {
                        System.err.println("Error al cerrar escritor de archivo: " + e);
                    }
                }
            }
        });
    }

    /**
     * Este método sirve para redondear un número a una determinada precisión de
     * decimales.
     *
     * @param numero El número a redondear
     * @param precision La precisión de decimales
     * @return <strong>resultado</strong> El número redondeado como resultado
     * final
     * @throws ParseException
     */
    public double redondear(double numero, int precision) throws ParseException {
        String formatString = "#.";

        for (int i = 0; i < precision; i++) {
            formatString = formatString.concat("#");
        }

        DecimalFormat df = new DecimalFormat(formatString);
        df.setRoundingMode(RoundingMode.CEILING);

        NumberFormat numberFormat = NumberFormat.getInstance(Locale.FRANCE);
        double resultado = numberFormat.parse(df.format(numero)).doubleValue();

        return resultado;
    }

    /**
     * Este método sirve para actualizar los componentes de la ventana
     * preferencias. Se revisan los valores guardados en las preferencias del
     * usuario para que estos se reflejen en los componentes de la ventana. Este
     * método se ejecuta cada vez que se abre la ventana preferencias.
     */
    public void actualizarVentanaPreferencias() {
        cmbPreferenciasRedondearPrecio.setSelectedIndex(preferencias.redondear_precio ? preferencias.precio_decimales : 11);
        cmbPreferenciasRedondearResultado.setSelectedIndex(preferencias.redondear_resultado ? preferencias.resultado_decimales : 11);

        checkBoxPreferenciasCargarPrecio.setSelected(preferencias.cargar_tasa);
        cmbPreferenciasTasa.setEnabled(checkBoxPreferenciasCargarPrecio.isSelected());
        cmbPreferenciasTasa.setSelectedIndex(preferencias.tasa.equals("bcv") ? 0 : 1);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        ventanaAcercaDe = new javax.swing.JDialog();
        labelAcercaDe = new javax.swing.JLabel();
        labelCreadoPor = new javax.swing.JLabel();
        labelCorreo = new javax.swing.JLabel();
        labelContacto = new javax.swing.JLabel();
        labelVersion = new javax.swing.JLabel();
        wallPaperAcercaDe = new javax.swing.JLabel();
        ventanaCambiarPrecio = new javax.swing.JDialog();
        labelCambiarPrecioIntroducirPrecio = new javax.swing.JLabel();
        txtCambiarPrecio = new javax.swing.JTextField();
        botonCambiarPrecio = new javax.swing.JButton();
        botonCambiarPrecioCancelar = new javax.swing.JButton();
        wallPaperCambiarPrecio = new javax.swing.JLabel();
        ventanaPreferencias = new javax.swing.JDialog();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        labelPreferenciasLimiteDecimales = new javax.swing.JLabel();
        labelPreferenciasPrecio = new javax.swing.JLabel();
        labelPreferenciasResultado = new javax.swing.JLabel();
        cmbPreferenciasRedondearResultado = new javax.swing.JComboBox<>();
        labelPreferenciasDecimales1 = new javax.swing.JLabel();
        labelPreferenciasDecimales2 = new javax.swing.JLabel();
        labelPreferenciasTituloPreferencias = new javax.swing.JLabel();
        cmbPreferenciasRedondearPrecio = new javax.swing.JComboBox<>();
        botonPreferenciasAceptar = new javax.swing.JButton();
        wallPaperPreferencias1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        labelPreferenciasLimiteDecimales1 = new javax.swing.JLabel();
        labelPreferenciasPrecio1 = new javax.swing.JLabel();
        labelPreferenciasTituloPreferencias1 = new javax.swing.JLabel();
        cmbPreferenciasTasa = new javax.swing.JComboBox<>();
        botonPreferenciasAceptar1 = new javax.swing.JButton();
        checkBoxPreferenciasCargarPrecio = new javax.swing.JCheckBox();
        wallPaperPreferencias2 = new javax.swing.JLabel();
        ventanaCargando = new javax.swing.JDialog();
        cmbOpciones = new javax.swing.JComboBox<>();
        txtInformacion = new javax.swing.JTextField();
        botonCalcular = new javax.swing.JButton();
        labelPrecioDolar = new javax.swing.JLabel();
        labelUltimaVezAct = new javax.swing.JLabel();
        labelTasaActual = new javax.swing.JLabel();
        labelIntroducirMonto = new javax.swing.JLabel();
        txtResultado = new javax.swing.JTextField();
        txtCalcular = new javax.swing.JTextField();
        botonAceptar = new javax.swing.JButton();
        labelIgualA = new javax.swing.JLabel();
        wallPaper = new javax.swing.JLabel();
        menu = new javax.swing.JMenuBar();
        menuOpciones = new javax.swing.JMenu();
        menuPrecio = new javax.swing.JMenu();
        opcionObtenerPrecioBCV = new javax.swing.JMenuItem();
        opcionObtenerPrecioToday = new javax.swing.JMenuItem();
        opcionIntroducirPrecio = new javax.swing.JMenuItem();
        menuCambiarFondo = new javax.swing.JMenu();
        opcionFondoAzul = new javax.swing.JMenuItem();
        opcionFondoMorado = new javax.swing.JMenuItem();
        opcionFondoGris = new javax.swing.JMenuItem();
        opcionElegirFondo = new javax.swing.JMenuItem();
        opcionPreferencias = new javax.swing.JMenuItem();
        opcionSalir = new javax.swing.JMenuItem();
        menuAyuda = new javax.swing.JMenu();
        opcion_AcercaDe = new javax.swing.JMenuItem();

        ventanaAcercaDe.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        ventanaAcercaDe.setResizable(false);
        ventanaAcercaDe.getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        ventanaAcercaDe.setSize(400,300);
        ventanaAcercaDe.setTitle("Aceca de");
        ventanaAcercaDe.setIconImage(new javax.swing.ImageIcon(getClass().getResource("/resources/icon.png")).getImage());

        labelAcercaDe.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        labelAcercaDe.setForeground(new java.awt.Color(255, 255, 255));
        labelAcercaDe.setText("Dollar Calculator");
        ventanaAcercaDe.getContentPane().add(labelAcercaDe, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 20, -1, 30));

        labelCreadoPor.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        labelCreadoPor.setForeground(new java.awt.Color(255, 255, 255));
        labelCreadoPor.setText("Programa creado por: Salvador Cammarata.");
        ventanaAcercaDe.getContentPane().add(labelCreadoPor, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 100, -1, -1));

        labelCorreo.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        labelCorreo.setForeground(new java.awt.Color(255, 255, 255));
        labelCorreo.setText("Contacto:");
        ventanaAcercaDe.getContentPane().add(labelCorreo, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 150, -1, -1));

        labelContacto.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        labelContacto.setForeground(new java.awt.Color(255, 255, 255));
        labelContacto.setText("salvadorcammarata03@gmail.com");
        ventanaAcercaDe.getContentPane().add(labelContacto, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 170, -1, -1));

        labelVersion.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        labelVersion.setForeground(new java.awt.Color(255, 255, 255));
        labelVersion.setText("Versión 1.1");
        ventanaAcercaDe.getContentPane().add(labelVersion, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 250, -1, -1));

        wallPaperAcercaDe.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/background_blue.jpg"))); // NOI18N
        ventanaAcercaDe.getContentPane().add(wallPaperAcercaDe, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 400, 300));
        wallPaperAcercaDe.setSize(400, 300);

        ventanaCambiarPrecio.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        ventanaCambiarPrecio.setTitle("Cambiar precio");
        ventanaCambiarPrecio.getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        ventanaCambiarPrecio.setSize(365,163);
        ventanaCambiarPrecio.setResizable(false);
        ventanaCambiarPrecio.setIconImage(new javax.swing.ImageIcon(getClass().getResource("/resources/icon.png")).getImage());

        labelCambiarPrecioIntroducirPrecio.setFont(new java.awt.Font("Arial", 3, 14)); // NOI18N
        labelCambiarPrecioIntroducirPrecio.setForeground(new java.awt.Color(255, 255, 255));
        labelCambiarPrecioIntroducirPrecio.setText("Introducir precio del dólar:");
        ventanaCambiarPrecio.getContentPane().add(labelCambiarPrecioIntroducirPrecio, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 20, -1, -1));

        txtCambiarPrecio.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        ventanaCambiarPrecio.getContentPane().add(txtCambiarPrecio, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 50, 290, -1));

        botonCambiarPrecio.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        botonCambiarPrecio.setText("Aceptar");
        botonCambiarPrecio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonCambiarPrecioActionPerformed(evt);
            }
        });
        ventanaCambiarPrecio.getContentPane().add(botonCambiarPrecio, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 90, 80, -1));

        botonCambiarPrecioCancelar.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        botonCambiarPrecioCancelar.setText("Cancelar");
        botonCambiarPrecioCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonCambiarPrecioCancelarActionPerformed(evt);
            }
        });
        ventanaCambiarPrecio.getContentPane().add(botonCambiarPrecioCancelar, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 90, -1, -1));

        wallPaperCambiarPrecio.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/background_blue.jpg"))); // NOI18N
        ventanaCambiarPrecio.getContentPane().add(wallPaperCambiarPrecio, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 370, 130));
        wallPaperCambiarPrecio.setSize(370,130);

        ventanaPreferencias.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        ventanaPreferencias.setResizable(false);
        ventanaPreferencias.getContentPane().setLayout(null);
        ventanaPreferencias.setSize(400,300);
        ventanaPreferencias.setTitle("Preferencias");
        ventanaPreferencias.setIconImage(new javax.swing.ImageIcon(getClass().getResource("/resources/icon.png")).getImage());

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        labelPreferenciasLimiteDecimales.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        labelPreferenciasLimiteDecimales.setForeground(new java.awt.Color(255, 255, 255));
        labelPreferenciasLimiteDecimales.setText("Límite de decimales:");
        jPanel1.add(labelPreferenciasLimiteDecimales, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 60, -1, -1));

        labelPreferenciasPrecio.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        labelPreferenciasPrecio.setForeground(new java.awt.Color(255, 255, 255));
        labelPreferenciasPrecio.setText("Precio:");
        jPanel1.add(labelPreferenciasPrecio, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 130, -1, -1));

        labelPreferenciasResultado.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        labelPreferenciasResultado.setForeground(new java.awt.Color(255, 255, 255));
        labelPreferenciasResultado.setText("Resultado:");
        jPanel1.add(labelPreferenciasResultado, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 100, -1, -1));

        cmbPreferenciasRedondearResultado.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Sin decimales", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "Sin límite" }));
        jPanel1.add(cmbPreferenciasRedondearResultado, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 90, -1, -1));

        labelPreferenciasDecimales1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        labelPreferenciasDecimales1.setForeground(new java.awt.Color(255, 255, 255));
        labelPreferenciasDecimales1.setText("decimales.");
        jPanel1.add(labelPreferenciasDecimales1, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 100, -1, -1));

        labelPreferenciasDecimales2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        labelPreferenciasDecimales2.setForeground(new java.awt.Color(255, 255, 255));
        labelPreferenciasDecimales2.setText("decimales.");
        jPanel1.add(labelPreferenciasDecimales2, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 130, -1, -1));

        labelPreferenciasTituloPreferencias.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        labelPreferenciasTituloPreferencias.setForeground(new java.awt.Color(255, 255, 255));
        labelPreferenciasTituloPreferencias.setText("Preferencias");
        jPanel1.add(labelPreferenciasTituloPreferencias, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 10, -1, 30));

        cmbPreferenciasRedondearPrecio.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Sin decimales", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "Sin límite" }));
        jPanel1.add(cmbPreferenciasRedondearPrecio, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 130, -1, -1));

        botonPreferenciasAceptar.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        botonPreferenciasAceptar.setText("Aceptar");
        botonPreferenciasAceptar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonPreferenciasAceptarActionPerformed(evt);
            }
        });
        jPanel1.add(botonPreferenciasAceptar, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 190, -1, -1));

        wallPaperPreferencias1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/background_blue.jpg"))); // NOI18N
        jPanel1.add(wallPaperPreferencias1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 400, 280));
        wallPaperPreferencias1.setSize(400, 280);

        jTabbedPane1.addTab("Limitar decimales", jPanel1);

        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        labelPreferenciasLimiteDecimales1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        labelPreferenciasLimiteDecimales1.setForeground(new java.awt.Color(255, 255, 255));
        labelPreferenciasLimiteDecimales1.setText("Parámetros de inicio");
        jPanel2.add(labelPreferenciasLimiteDecimales1, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 60, -1, -1));

        labelPreferenciasPrecio1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        labelPreferenciasPrecio1.setForeground(new java.awt.Color(255, 255, 255));
        labelPreferenciasPrecio1.setText("Seleccionar tasa:");
        jPanel2.add(labelPreferenciasPrecio1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 130, -1, -1));

        labelPreferenciasTituloPreferencias1.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        labelPreferenciasTituloPreferencias1.setForeground(new java.awt.Color(255, 255, 255));
        labelPreferenciasTituloPreferencias1.setText("Preferencias");
        jPanel2.add(labelPreferenciasTituloPreferencias1, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 10, -1, 30));

        cmbPreferenciasTasa.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Tasa BCV", "Tasa DolarToday" }));
        jPanel2.add(cmbPreferenciasTasa, new org.netbeans.lib.awtextra.AbsoluteConstraints(115, 127, -1, -1));

        botonPreferenciasAceptar1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        botonPreferenciasAceptar1.setText("Aceptar");
        botonPreferenciasAceptar1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonPreferenciasAceptar1ActionPerformed(evt);
            }
        });
        jPanel2.add(botonPreferenciasAceptar1, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 190, -1, -1));

        checkBoxPreferenciasCargarPrecio.setForeground(new java.awt.Color(255, 255, 255));
        checkBoxPreferenciasCargarPrecio.setText("Cargar precio del dólar al iniciar la aplicación");
        checkBoxPreferenciasCargarPrecio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxPreferenciasCargarPrecioActionPerformed(evt);
            }
        });
        jPanel2.add(checkBoxPreferenciasCargarPrecio, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, -1, -1));

        wallPaperPreferencias2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/background_blue.jpg"))); // NOI18N
        jPanel2.add(wallPaperPreferencias2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 400, 280));
        wallPaperPreferencias2.setSize(400, 280);

        jTabbedPane1.addTab("Parámetros de inicio", jPanel2);

        ventanaPreferencias.getContentPane().add(jTabbedPane1);
        jTabbedPane1.setBounds(0, 0, 400, 300);

        ImageIcon cargando = new ImageIcon(getClass().getResource("/resources/loading.gif"));
        ventanaCargando.add(new JLabel("Obteniendo tasa... ", cargando, JLabel.CENTER));
        ventanaCargando.setSize(268, 143);
        ventanaCargando.setResizable(false);
        ventanaCargando.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        ventanaCargando.setTitle("Cargando");
        ventanaCargando.setIconImage(null);
        ventanaCargando.setIconImages(null);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Dollar Calculator");
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        cmbOpciones.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        cmbOpciones.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Bolívares a Dólares", "Dólares a Bolívares" }));
        cmbOpciones.setMaximumSize(new java.awt.Dimension(390, 20));
        cmbOpciones.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbOpcionesItemStateChanged(evt);
            }
        });
        getContentPane().add(cmbOpciones, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 20, -1, -1));

        txtInformacion.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        txtInformacion.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtInformacion.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtInformacionFocusGained(evt);
            }
        });
        getContentPane().add(txtInformacion, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 180, 350, 40));

        botonCalcular.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        botonCalcular.setText("Calcular");
        botonCalcular.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        botonCalcular.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonCalcularActionPerformed(evt);
            }
        });
        getContentPane().add(botonCalcular, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 280, 160, 40));

        labelPrecioDolar.setFont(new java.awt.Font("Arial", 1, 20)); // NOI18N
        labelPrecioDolar.setForeground(new java.awt.Color(255, 255, 255));
        labelPrecioDolar.setText("Precio del dólar:");
        labelPrecioDolar.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        getContentPane().add(labelPrecioDolar, new org.netbeans.lib.awtextra.AbsoluteConstraints(145, 90, -1, -1));

        labelUltimaVezAct.setFont(new java.awt.Font("Tahoma", 3, 12)); // NOI18N
        labelUltimaVezAct.setForeground(new java.awt.Color(255, 255, 255));
        labelUltimaVezAct.setText("Última vez actualizado:");
        getContentPane().add(labelUltimaVezAct, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 40, 240, 10));

        labelTasaActual.setFont(new java.awt.Font("Tahoma", 3, 12)); // NOI18N
        labelTasaActual.setForeground(new java.awt.Color(255, 255, 255));
        labelTasaActual.setText("Tasa actual:");
        getContentPane().add(labelTasaActual, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 20, 170, 10));

        labelIntroducirMonto.setFont(new java.awt.Font("Tahoma", 3, 12)); // NOI18N
        labelIntroducirMonto.setForeground(new java.awt.Color(255, 255, 255));
        labelIntroducirMonto.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelIntroducirMonto.setText("Introducir monto en:");
        getContentPane().add(labelIntroducirMonto, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 160, -1, -1));

        txtResultado.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        txtResultado.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtResultado.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        getContentPane().add(txtResultado, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 180, 250, 40));
        txtResultado.setEditable(false);

        txtCalcular.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        txtCalcular.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtCalcular.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        getContentPane().add(txtCalcular, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 80, 250, 40));
        txtCalcular.setEditable(false);

        botonAceptar.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        botonAceptar.setText("Aceptar");
        botonAceptar.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        botonAceptar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonAceptarActionPerformed(evt);
            }
        });
        getContentPane().add(botonAceptar, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 280, 160, 40));

        labelIgualA.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        labelIgualA.setForeground(new java.awt.Color(255, 255, 255));
        labelIgualA.setText("son iguales a:");
        getContentPane().add(labelIgualA, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 140, -1, -1));

        wallPaper.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/background_blue.jpg"))); // NOI18N
        getContentPane().add(wallPaper, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 570, 400));

        menu.setVerifyInputWhenFocusTarget(false);

        menuOpciones.setText("Opciones");
        menuOpciones.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        menuPrecio.setText("Precio");
        menuPrecio.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        opcionObtenerPrecioBCV.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        opcionObtenerPrecioBCV.setText("Obtener precio en internet (Tasa BCV)");
        opcionObtenerPrecioBCV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                opcionObtenerPrecioBCVActionPerformed(evt);
            }
        });
        menuPrecio.add(opcionObtenerPrecioBCV);

        opcionObtenerPrecioToday.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        opcionObtenerPrecioToday.setText("Obtener precio en internet (Tasa DolarToday)");
        opcionObtenerPrecioToday.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                opcionObtenerPrecioTodayActionPerformed(evt);
            }
        });
        menuPrecio.add(opcionObtenerPrecioToday);

        opcionIntroducirPrecio.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        opcionIntroducirPrecio.setText("Introducir precio...");
        opcionIntroducirPrecio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                opcionIntroducirPrecioActionPerformed(evt);
            }
        });
        menuPrecio.add(opcionIntroducirPrecio);

        menuOpciones.add(menuPrecio);

        menuCambiarFondo.setText("Color de fondo");
        menuCambiarFondo.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        opcionFondoAzul.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        opcionFondoAzul.setText("Azul");
        opcionFondoAzul.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                opcionFondoAzulActionPerformed(evt);
            }
        });
        menuCambiarFondo.add(opcionFondoAzul);

        opcionFondoMorado.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        opcionFondoMorado.setText("Morado");
        opcionFondoMorado.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                opcionFondoMoradoActionPerformed(evt);
            }
        });
        menuCambiarFondo.add(opcionFondoMorado);

        opcionFondoGris.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        opcionFondoGris.setText("Gris");
        opcionFondoGris.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                opcionFondoGrisActionPerformed(evt);
            }
        });
        menuCambiarFondo.add(opcionFondoGris);

        opcionElegirFondo.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        opcionElegirFondo.setText("Elegir fondo...");
        opcionElegirFondo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                opcionElegirFondoActionPerformed(evt);
            }
        });
        menuCambiarFondo.add(opcionElegirFondo);

        menuOpciones.add(menuCambiarFondo);

        opcionPreferencias.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        opcionPreferencias.setText("Preferencias...");
        opcionPreferencias.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                opcionPreferenciasActionPerformed(evt);
            }
        });
        menuOpciones.add(opcionPreferencias);

        opcionSalir.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        opcionSalir.setText("Salir");
        opcionSalir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                opcionSalirActionPerformed(evt);
            }
        });
        menuOpciones.add(opcionSalir);

        menu.add(menuOpciones);

        menuAyuda.setText("Ayuda");
        menuAyuda.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        opcion_AcercaDe.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        opcion_AcercaDe.setText("Acerca de...");
        opcion_AcercaDe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                opcion_AcercaDeActionPerformed(evt);
            }
        });
        menuAyuda.add(opcion_AcercaDe);

        menu.add(menuAyuda);

        setJMenuBar(menu);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    //Eventos
    //Cálculo Bolívares a Dólares
    private void botonCalcularActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonCalcularActionPerformed
        calcular(cmbOpciones.getSelectedIndex());
    }//GEN-LAST:event_botonCalcularActionPerformed

    //Cambiar el precio del dólar
    private void opcionIntroducirPrecioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_opcionIntroducirPrecioActionPerformed
        ventanaCambiarPrecio.setLocationRelativeTo(botonCambiarPrecio);
        ventanaCambiarPrecio.setVisible(true);
    }//GEN-LAST:event_opcionIntroducirPrecioActionPerformed

    //Botón Acerca De
    private void opcion_AcercaDeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_opcion_AcercaDeActionPerformed
        ventanaAcercaDe.setLocationRelativeTo(null);
        ventanaAcercaDe.setVisible(true);
    }//GEN-LAST:event_opcion_AcercaDeActionPerformed

    //Evento de ComboBox
    private void cmbOpcionesItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbOpcionesItemStateChanged
        actualizarOpciones();
    }//GEN-LAST:event_cmbOpcionesItemStateChanged

    //Botón salir
    private void opcionSalirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_opcionSalirActionPerformed
        System.exit(0);
    }//GEN-LAST:event_opcionSalirActionPerformed

    //Evento de textfield información
    private void txtInformacionFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtInformacionFocusGained
        txtInformacion.setBackground(java.awt.Color.WHITE);
    }//GEN-LAST:event_txtInformacionFocusGained

    //Evento de botón Aceptar (Calcular)
    private void botonAceptarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonAceptarActionPerformed
        actualizarOpciones();
    }//GEN-LAST:event_botonAceptarActionPerformed

    //Evento de botón Cancelar (Cambiar Precio)
    private void botonCambiarPrecioCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonCambiarPrecioCancelarActionPerformed
        txtCambiarPrecio.setText("");
        ventanaCambiarPrecio.dispose();
    }//GEN-LAST:event_botonCambiarPrecioCancelarActionPerformed

    //Evento de botón Aceptar (Cambiar Precio)
    private void botonCambiarPrecioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonCambiarPrecioActionPerformed
        //Capturar excepciones
        try {
            double nuevo_precio;

            //Revisa si el valor introducido tiene punto (.) o coma (,)
            if (txtCambiarPrecio.getText().contains(",")) {
                NumberFormat numberFormat = NumberFormat.getInstance(Locale.FRANCE);
                nuevo_precio = numberFormat.parse(txtCambiarPrecio.getText().trim()).doubleValue();
            } else {
                nuevo_precio = Double.parseDouble(txtCambiarPrecio.getText().trim());
            }

            //Revisa si el valor introducido es menor o igual a cero
            if (nuevo_precio > 0.0) {
                //Impide al usuario sobrepasar el valor máximo
                if (nuevo_precio > 9999999) {
                    nuevo_precio = 9999999;
                }

                //Asigna el valor del precio del dólar, dependiendo de si se deben limitar los decimales y de si hay o no hay decimales
                precio_dolar = preferencias.redondear_precio ? preferencias.precio_decimales > 0 ? redondear(nuevo_precio, preferencias.precio_decimales) : Math.round(nuevo_precio) : nuevo_precio;

                //Asigna la última vez act. y la tasa actual a ninguno
                ult_vez_act = "ninguno";
                tasa_actual = "ninguno";

                //Actualizar opciones y precio en pantalla
                actualizarOpciones();
                actualizarPrecio();
            } else {
                //Si el valor introducido es menor o igual a cero mostrar el siguiente error:
                txtCambiarPrecio.setBackground(java.awt.Color.RED);
                javax.swing.JOptionPane.showMessageDialog(null, "El valor introducido no es válido.", "¡Error!", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        } catch (ParseException | HeadlessException | NumberFormatException e) {
            //Si se capturó alguna excepción mostrar el siguiente error:
            txtCambiarPrecio.setBackground(java.awt.Color.RED);
            javax.swing.JOptionPane.showMessageDialog(null, "El valor introducido no es válido.", "¡Error!", javax.swing.JOptionPane.ERROR_MESSAGE);
        } finally {
            //Cerrar ventana cambiar precio
            txtCambiarPrecio.setBackground(java.awt.Color.WHITE);
            txtCambiarPrecio.setText("");
            ventanaCambiarPrecio.dispose();
        }

    }//GEN-LAST:event_botonCambiarPrecioActionPerformed

    //Evento de opción Cambiar Fondo (Morado)
    private void opcionFondoMoradoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_opcionFondoMoradoActionPerformed
        background = "background_purple";
        cambiarFondo();
    }//GEN-LAST:event_opcionFondoMoradoActionPerformed

    //Evento de opción Cambiar Fondo (Azul)
    private void opcionFondoAzulActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_opcionFondoAzulActionPerformed
        background = "background_blue";
        cambiarFondo();
    }//GEN-LAST:event_opcionFondoAzulActionPerformed

    //Evento de opción Cambiar Fondo (Gris)
    private void opcionFondoGrisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_opcionFondoGrisActionPerformed
        background = "background_gray";
        cambiarFondo();
    }//GEN-LAST:event_opcionFondoGrisActionPerformed

    //Evento de opción Obtener Precio (Tasa BCV)
    private void opcionObtenerPrecioBCVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_opcionObtenerPrecioBCVActionPerformed
        // TODO add your handling code here:
        new TasaDolarBCV(this);
    }//GEN-LAST:event_opcionObtenerPrecioBCVActionPerformed

    //Evento de opción Preferencias
    private void opcionPreferenciasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_opcionPreferenciasActionPerformed
        // TODO add your handling code here:
        actualizarVentanaPreferencias();
        ventanaPreferencias.setLocationRelativeTo(null);
        ventanaPreferencias.setVisible(true);
    }//GEN-LAST:event_opcionPreferenciasActionPerformed

    //Evento de opción Elegir Fondo...
    private void opcionElegirFondoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_opcionElegirFondoActionPerformed
        // TODO add your handling code here:
        JFileChooser fc = new JFileChooser();

        if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
                javax.swing.ImageIcon backgroundImage = new javax.swing.ImageIcon(fc.getSelectedFile().getAbsolutePath());

                //Ajustar imagen para encajar con el tamaño de los wallpaper
                Icon iconoWallPaper = new ImageIcon(backgroundImage.getImage().getScaledInstance(wallPaper.getWidth(),
                        wallPaper.getHeight(), Image.SCALE_DEFAULT));
                wallPaper.setIcon(iconoWallPaper);

                Icon iconoWallPaperAcercaDe = new ImageIcon(backgroundImage.getImage().getScaledInstance(wallPaperAcercaDe.getWidth(),
                        wallPaperAcercaDe.getHeight(), Image.SCALE_DEFAULT));
                wallPaperAcercaDe.setIcon(iconoWallPaperAcercaDe);

                Icon iconoWallPaperCambiarPrecio = new ImageIcon(backgroundImage.getImage().getScaledInstance(wallPaperCambiarPrecio.getWidth(),
                        wallPaperCambiarPrecio.getHeight(), Image.SCALE_DEFAULT));
                wallPaperCambiarPrecio.setIcon(iconoWallPaperCambiarPrecio);

                Icon iconoWallPaperPreferencias1 = new ImageIcon(backgroundImage.getImage().getScaledInstance(wallPaperPreferencias1.getWidth(),
                        wallPaperPreferencias1.getHeight(), Image.SCALE_DEFAULT));
                wallPaperPreferencias1.setIcon(iconoWallPaperPreferencias1);

                Icon iconoWallPaperPreferencias2 = new ImageIcon(backgroundImage.getImage().getScaledInstance(wallPaperPreferencias2.getWidth(),
                        wallPaperPreferencias2.getHeight(), Image.SCALE_DEFAULT));
                wallPaperPreferencias2.setIcon(iconoWallPaperPreferencias2);
            } catch (Exception e) {
                //Si se produce un error imprimir en consola y mostrar el siguiente mensaje
                System.err.println(e);
                javax.swing.JOptionPane.showMessageDialog(null, "Ha ocurrido un error.", "¡Error!", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_opcionElegirFondoActionPerformed

    //Evento de botón Aceptar ventana Preferencias
    private void botonPreferenciasAceptarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonPreferenciasAceptarActionPerformed
        // TODO add your handling code here:
        actualizarPreferencias();
        ventanaPreferencias.dispose();
    }//GEN-LAST:event_botonPreferenciasAceptarActionPerformed

    //Evento de opción Obtener Precio (Tasa DolarToday)
    private void opcionObtenerPrecioTodayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_opcionObtenerPrecioTodayActionPerformed
        // TODO add your handling code here:
        new TasaDolarToday(this);
    }//GEN-LAST:event_opcionObtenerPrecioTodayActionPerformed

    //Evento de botón Aceptar ventana Preferencias
    private void botonPreferenciasAceptar1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonPreferenciasAceptar1ActionPerformed
        // TODO add your handling code here:
        botonPreferenciasAceptarActionPerformed(evt);
    }//GEN-LAST:event_botonPreferenciasAceptar1ActionPerformed

    //Evento de Checkbox Cargar tasa al iniciar la aplicación
    private void checkBoxPreferenciasCargarPrecioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxPreferenciasCargarPrecioActionPerformed
        // TODO add your handling code here:
        cmbPreferenciasTasa.setEnabled(checkBoxPreferenciasCargarPrecio.isSelected());
    }//GEN-LAST:event_checkBoxPreferenciasCargarPrecioActionPerformed

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Interfaz.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Interfaz.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Interfaz.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Interfaz.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new Interfaz().setVisible(true);
        });
        //Guardar datos de inicio al finalizar el programa
        guardarDatos();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botonAceptar;
    public javax.swing.JButton botonCalcular;
    private javax.swing.JButton botonCambiarPrecio;
    private javax.swing.JButton botonCambiarPrecioCancelar;
    private javax.swing.JButton botonPreferenciasAceptar;
    private javax.swing.JButton botonPreferenciasAceptar1;
    private javax.swing.JCheckBox checkBoxPreferenciasCargarPrecio;
    private javax.swing.JComboBox<String> cmbOpciones;
    private javax.swing.JComboBox<String> cmbPreferenciasRedondearPrecio;
    private javax.swing.JComboBox<String> cmbPreferenciasRedondearResultado;
    private javax.swing.JComboBox<String> cmbPreferenciasTasa;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel labelAcercaDe;
    private javax.swing.JLabel labelCambiarPrecioIntroducirPrecio;
    private javax.swing.JLabel labelContacto;
    private javax.swing.JLabel labelCorreo;
    private javax.swing.JLabel labelCreadoPor;
    private javax.swing.JLabel labelIgualA;
    private javax.swing.JLabel labelIntroducirMonto;
    private javax.swing.JLabel labelPrecioDolar;
    private javax.swing.JLabel labelPreferenciasDecimales1;
    private javax.swing.JLabel labelPreferenciasDecimales2;
    private javax.swing.JLabel labelPreferenciasLimiteDecimales;
    private javax.swing.JLabel labelPreferenciasLimiteDecimales1;
    private javax.swing.JLabel labelPreferenciasPrecio;
    private javax.swing.JLabel labelPreferenciasPrecio1;
    private javax.swing.JLabel labelPreferenciasResultado;
    private javax.swing.JLabel labelPreferenciasTituloPreferencias;
    private javax.swing.JLabel labelPreferenciasTituloPreferencias1;
    private javax.swing.JLabel labelTasaActual;
    private javax.swing.JLabel labelUltimaVezAct;
    private javax.swing.JLabel labelVersion;
    private javax.swing.JMenuBar menu;
    private javax.swing.JMenu menuAyuda;
    private javax.swing.JMenu menuCambiarFondo;
    private javax.swing.JMenu menuOpciones;
    private javax.swing.JMenu menuPrecio;
    private javax.swing.JMenuItem opcionElegirFondo;
    private javax.swing.JMenuItem opcionFondoAzul;
    private javax.swing.JMenuItem opcionFondoGris;
    private javax.swing.JMenuItem opcionFondoMorado;
    private javax.swing.JMenuItem opcionIntroducirPrecio;
    private javax.swing.JMenuItem opcionObtenerPrecioBCV;
    private javax.swing.JMenuItem opcionObtenerPrecioToday;
    private javax.swing.JMenuItem opcionPreferencias;
    private javax.swing.JMenuItem opcionSalir;
    private javax.swing.JMenuItem opcion_AcercaDe;
    private javax.swing.JTextField txtCalcular;
    private javax.swing.JTextField txtCambiarPrecio;
    private javax.swing.JTextField txtInformacion;
    private javax.swing.JTextField txtResultado;
    private javax.swing.JDialog ventanaAcercaDe;
    private javax.swing.JDialog ventanaCambiarPrecio;
    public javax.swing.JDialog ventanaCargando;
    private javax.swing.JDialog ventanaPreferencias;
    private javax.swing.JLabel wallPaper;
    private javax.swing.JLabel wallPaperAcercaDe;
    private javax.swing.JLabel wallPaperCambiarPrecio;
    private javax.swing.JLabel wallPaperPreferencias1;
    private javax.swing.JLabel wallPaperPreferencias2;
    // End of variables declaration//GEN-END:variables

}
