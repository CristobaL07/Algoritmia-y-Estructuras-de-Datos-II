package com.example.zenword;

import static java.lang.Boolean.FALSE;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import android.util.Log;
import android.text.Html;

public class MainActivity extends AppCompatActivity {

    Random r = new Random(); // Generador de números aleatorios
    int red = r.nextInt(256); // Color rojo aleatorio
    int green = r.nextInt(256); // Color verde aleatorio
    int blue = r.nextInt(256); // Color azul aleatorio
    TextView tv1[];
    TextView tv2[];
    TextView tv3[];
    TextView tv4[];
    TextView tv5[];
    int bonuses = 0;
    int ayudas = 0;
    int palabrasescritas = 0;
    TextView texto;
    HashSet<String> escritas = new HashSet<>(); // Conjunto de palabras ya escritas
    HashSet<Integer> primeras = new HashSet<>(); // Conjunto de posiciones iniciales
    int acertadas = 0;
    int posibles;
    HashSet<String> palabrasRepetidas = new HashSet<>(); // Conjunto de palabras repetidas
    HashMap<String, String> catalogoValidas = new HashMap<>();
    HashMap<Integer, Set<String>> catalogoLong = new HashMap<>();
    HashMap<Integer, Set<String>> catalogoLongValidas = new HashMap<>();
    TreeMap<String, Integer> palabrasOcultasMap; // Mapa para almacenar las palabras ocultas y su posición
    HashSet<String> palabrasOcultasSet = new HashSet<>(); // Obtiene las palabras ocultas
    HashMap<String, String> diccionario = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        texto = findViewById(R.id.Iterfaz2); // Inicializa el TextView para mostrar el estado del juego

        Button button = findViewById(R.id.button16);
        button.setBackgroundColor(Color.rgb(red, green, blue)); // Establece el color de fondo del botón

        cargarDiccionario();

        partida(); // Inicia una nueva partida

        TextView palabra = findViewById(R.id.Interfaz3);
        palabra.setText(""); // Limpia el TextView para la palabra

        mostraMissatge("Bona Sort!!", true); // Muestra un mensaje de bienvenida

        Button bonus = findViewById(R.id.Bonus);
        bonus.setTextSize(20);
        bonus.setTextColor(Color.BLACK);
        bonus.setGravity(Gravity.TOP);
        bonus.setPadding(67, 10, 0, 0);
        bonus.setText("0"); // Inicializa el botón de bonus

        Button reiniciar = findViewById(R.id.Reiniciar);
        reiniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Oculta los TextViews de las palabras
                for (TextView textView : tv1) {
                    textView.setVisibility(View.INVISIBLE);
                }
                for (TextView textView : tv2) {
                    textView.setVisibility(View.INVISIBLE);
                }
                for (TextView textView : tv3) {
                    textView.setVisibility(View.INVISIBLE);
                }
                for (TextView textView : tv4) {
                    textView.setVisibility(View.INVISIBLE);
                }
                for (TextView textView : tv5) {
                    textView.setVisibility(View.INVISIBLE);
                }
                // Genera nuevos colores aleatorios y actualiza el botón
                red = r.nextInt(256);
                green = r.nextInt(256);
                blue = r.nextInt(256);
                button.setBackgroundColor(Color.rgb(red, green, blue));
                partida(); // Inicia una nueva partida
                bonuses = 0;
                bonus.setText("0");
                escritas.clear(); // Limpia las palabras escritas
                primeras.clear(); // Limpia las posiciones iniciales
                enableViews(R.id.myConstraintLayout); // Habilita todas las vistas en el layout
                acertadas = 0;
            }
        });
    }

    private void cargarDiccionario() {
        try {
            InputStream is = getResources().openRawResource(R.raw.paraules);
            BufferedReader r = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = r.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 2) {
                    String wordWithAccent = parts[0].trim();
                    String wordWithoutAccent = parts[1].trim();
                    diccionario.put(wordWithoutAccent, wordWithAccent);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String palabraInicial(Map<String, String> diccionario) {
        Random rand = new Random();
        int wordLength = rand.nextInt(5) + 3; // Genera un número entre 3 y 7
        String selectedWord = "";

        // Filtra las palabras del diccionario que tienen la longitud requerida
        Set<String> words = new HashSet<>();
        for (String word : diccionario.keySet()) {
            if (word.length() == wordLength) {
                words.add(word);
            }
        }

        // Selecciona una palabra aleatoria del conjunto filtrado
        if (!words.isEmpty()) {
            int randomIndex = rand.nextInt(words.size());
            Iterator<String> iterator = words.iterator();
            for (int i = 0; i <= randomIndex; i++) {
                selectedWord = iterator.next();
            }
        }

        // Ahora, la variable 'selectedWord' contiene la palabra seleccionada sin acento
        System.out.println("Palabra seleccionada: " + selectedWord);
        return selectedWord;
    }

    public void partida() {
        String selectedWord = palabraInicial(diccionario); // Obtiene una palabra inicial
        palabrasOcultasSet = new HashSet<>(); // Obtiene las palabras ocultas
        inicializar(selectedWord); // Obtiene el catálogo de palabras válidas

        Log.d("DEBUG", "Catálogo de palabras válidas: " + catalogoValidas);

        Comparator<String> comparator = new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                if (s1.length() != s2.length()) {
                    return Integer.compare(s1.length(), s2.length());
                } else {
                    return s1.compareTo(s2);
                }
            }
        };

        // Crear el TreeMap con el comparador personalizado
        TreeSet<String> palabrasOcultasTree = new TreeSet<String>(comparator);

        // Guarda la palabra y su posición
        palabrasOcultasTree.addAll(palabrasOcultasSet);

        palabrasOcultasMap = new TreeMap<>(comparator);

        int index = 1;
        for (String palabra : palabrasOcultasTree) {
            palabrasOcultasMap.put(palabra, index++); // Guarda la palabra y su posición
        }

        Log.d("DEBUG", "Catálogo de palabras por longitud: " + catalogoLong);
        Log.d("DEBUG", "Catálogo de palabras válidas por longitud: " + catalogoLongValidas);
        Log.d("DEBUG", "Catálogo de palabras ocultas: " + palabrasOcultasMap);

        // Crear filas de TextViews según el número de palabras ocultas
        switch (palabrasOcultasMap.size()) {
            case 1:
                Iterator<String> iterator = palabrasOcultasMap.keySet().iterator();
                tv1 = crearFilaTextViews(R.id.guideline3, iterator.next().length());
                break;
            case 2:
                Iterator<String> iterator2 = palabrasOcultasMap.keySet().iterator();
                tv1 = crearFilaTextViews(R.id.guideline3, iterator2.next().length());
                tv2 = crearFilaTextViews(R.id.guideline6, iterator2.next().length());
                break;
            case 3:
                Iterator<String> iterator3 = palabrasOcultasMap.keySet().iterator();
                tv1 = crearFilaTextViews(R.id.guideline3, iterator3.next().length());
                tv2 = crearFilaTextViews(R.id.guideline6, iterator3.next().length());
                tv3 = crearFilaTextViews(R.id.guideline7, iterator3.next().length());
                break;
            case 4:
                Iterator<String> iterator4 = palabrasOcultasMap.keySet().iterator();
                tv1 = crearFilaTextViews(R.id.guideline3, iterator4.next().length());
                tv2 = crearFilaTextViews(R.id.guideline6, iterator4.next().length());
                tv3 = crearFilaTextViews(R.id.guideline7, iterator4.next().length());
                tv4 = crearFilaTextViews(R.id.guideline8, iterator4.next().length());
                break;
            case 5:
                Iterator<String> iterator5 = palabrasOcultasMap.keySet().iterator();
                tv1 = crearFilaTextViews(R.id.guideline3, iterator5.next().length());
                tv2 = crearFilaTextViews(R.id.guideline6, iterator5.next().length());
                tv3 = crearFilaTextViews(R.id.guideline7, iterator5.next().length());
                tv4 = crearFilaTextViews(R.id.guideline8, iterator5.next().length());
                tv5 = crearFilaTextViews(R.id.guideline9, iterator5.next().length());
                break;
        }
        posibles = palabrasOcultasMap.size(); // Número de palabras ocultas posibles
        palabrasescritas = 0;
        String pantalla = "Has encertat " + palabrasescritas + " de " + catalogoValidas.size() + " paraules possibles: ";
        texto.setText(pantalla); // Actualiza el texto con el estado actual
        letrasBotones(selectedWord); // Configura los botones con las letras de la palabra seleccionada
    }


    public void letrasBotones(String selectedWord) {
        int wordLength = selectedWord.length();
        Button[] buttons = new Button[7];
        StringBuilder letrasPulsadas = new StringBuilder(); // StringBuilder para acumular letras pulsadas
        TextView interfaz = findViewById(R.id.Interfaz3);

        for (int i = 0; i < 7; i++) {
            String buttonID = "Letra" + (i + 1);
            int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
            buttons[i] = findViewById(resID);

            if (i < wordLength) {
                buttons[i].setTextSize(20);
                buttons[i].setText(String.valueOf(selectedWord.charAt(i)).toUpperCase());
                buttons[i].setVisibility(View.VISIBLE); // Los botones están visibles

                buttons[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Button button = (Button) v;
                        letrasPulsadas.append(button.getText().toString());
                        interfaz.setText(letrasPulsadas.toString());
                        interfaz.setTypeface(null, Typeface.BOLD); // Mostrar texto en negrita
                        button.setEnabled(FALSE); // Deshabilitar el botón después de ser pulsado
                    }
                });
            } else {
                buttons[i].setText(""); // Poner texto vacío
                buttons[i].setVisibility(View.INVISIBLE); //
            }
        }

        Button bonus = findViewById(R.id.Bonus);
        bonus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(catalogoValidas.size()); // Muestra un popup con el número de palabras válidas
            }
        });

        Button aleatorio = findViewById(R.id.Aleatorio);
        aleatorio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] j = new int[selectedWord.length()];
                HashSet<Integer> conjunto = new HashSet<>();
                for (int i = 0; i < j.length; i++) {
                    conjunto.add(i);
                }

                // Convertimos el HashSet a una List para poder barajarla
                List<Integer> conjuntoList = new ArrayList<>(conjunto);
                Collections.shuffle(conjuntoList); // Baraja la lista de índices

                // Asignamos los elementos barajados de la lista al arreglo j
                for (int i = 0; i < conjuntoList.size(); i++) {
                    j[i] = conjuntoList.get(i);
                }

                for (int i = 0; i < 7; i++) {
                    buttons[i].setText(""); // Limpia el texto de todos los botones
                }

                for (int i = 0; i < selectedWord.length(); i++) {
                    System.out.println(String.valueOf(selectedWord.charAt(j[i])) + ", " + j[i]);
                    buttons[i].setText(String.valueOf(selectedWord.charAt(j[i])).toUpperCase()); // Establece el nuevo texto barajado
                }

                System.out.println(selectedWord);
            }
        });


        // Añadir OnClickListener para el botón Send
        Button sendButton = findViewById(R.id.Send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean valido = verificarPalabras(selectedWord);
                letrasPulsadas.setLength(0); // Limpiar el StringBuilder
                interfaz.setText(""); // Limpiar el TextView
                if (valido) {
                    bonuses++;
                    bonus.setText(Integer.toString(bonuses));
                    if (bonuses % 5 == 0 && bonuses != 0) {
                        ayudas++;
                    }
                }
                for (int i = 0; i < 7; i++) {
                    buttons[i].setEnabled(true); // Habilita todos los botones
                }
                if (acertadas == posibles) {
                    mostraMissatge("Enhorabona! has guanyat", true); // Muestra mensaje de victoria
                    disableViews(R.id.myConstraintLayout); // Deshabilita todas las vistas
                }
            }
        });

        // Configurar el botón Clear para borrar el contenido del TextView Interfaz3
        Button clearButton = findViewById(R.id.Clear);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                letrasPulsadas.setLength(0); // Limpiar el StringBuilder
                interfaz.setText(""); // Limpiar el TextView
                for (int i = 0; i < 7; i++) {
                    buttons[i].setEnabled(true); // Habilita todos los botones
                }
            }
        });

        Button ayuda = findViewById(R.id.Ayuda);
        ayuda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ayudas > 0 && primeras.size() < palabrasOcultasMap.keySet().size()) {
                    String palabra = null;
                    boolean encontrado = false;
                    Iterator<String> iterator;
                    int ind;
                    while (!encontrado) {
                        encontrado = true;
                        ind = r.nextInt(palabrasOcultasMap.size());
                        iterator = palabrasOcultasMap.keySet().iterator();
                        for (int i = 0; i <= ind; i++) {
                            palabra = iterator.next(); // Selecciona una palabra aleatoria
                        }
                        for (String entry : escritas) {
                            if (palabra.equalsIgnoreCase(entry)) {
                                encontrado = false; // Verifica que la palabra no haya sido escrita antes
                            }
                        }
                        for (Integer entry : primeras) {
                            if (entry.equals(palabrasOcultasMap.get(palabra))) {
                                encontrado = false; // Verifica que la posición no haya sido usada antes
                            }
                        }
                    }

                    System.out.println(palabra + ", " + palabrasOcultasMap.get(palabra));
                    mostraPrimeraLletra(catalogoValidas.get(palabra), palabrasOcultasMap.get(palabra)); // Muestra la primera letra de la palabra
                    primeras.add(palabrasOcultasMap.get(palabra)); // Agrega la posición a las iniciales
                    ayudas--;
                    bonuses -= 5;
                    bonus.setText(Integer.toString(bonuses));
                }
            }
        });
    }


    public boolean verificarPalabras(String selectedWord) {
        TextView interfaz = findViewById(R.id.Interfaz3);
        String textoInterfaz = interfaz.getText().toString().toUpperCase(); // Obtiene el texto introducido en la interfaz y lo convierte a mayúsculas
        boolean encontrada = false;

        // Verifica si la palabra ya ha sido adivinada
        Iterator<String> iterator = escritas.iterator();
        while (iterator.hasNext()) {
            String palabra = iterator.next();
            if (palabra.equalsIgnoreCase(textoInterfaz)) {
                // Si la palabra ya ha sido adivinada, la resalta en rojo
                palabrasRepetidas.add(palabra.toLowerCase());  // Añade la palabra al conjunto de palabras repetidas
                actualizarTextoPantalla(catalogoValidas.size()); // Actualiza el texto en pantalla
                mostraMissatge("Aquesta ja la tens!", false); // Muestra mensaje de palabra repetida
                return false;
            }
        }

        // Verifica si la palabra es una de las palabras ocultas
        for (Map.Entry<String, Integer> entry : palabrasOcultasMap.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(textoInterfaz)) {
                mostraParaula(catalogoValidas.get(entry.getKey()), entry.getValue()); // Muestra la palabra en la interfaz
                mostraMissatge("Encertada!", true); // Muestra mensaje de palabra acertada
                acertadas++;
                interfaz.setText(""); // Limpia el TextView
                encontrada = true;
                palabrasescritas++;
                escritas.add(textoInterfaz.toLowerCase()); // Añade la palabra al conjunto de palabras adivinadas
                actualizarTextoPantalla(catalogoValidas.size()); // Actualiza el texto en pantalla
                break;
            }
        }

        // Verifica si la palabra es válida pero no es una de las ocultas
        for (String entry : catalogoValidas.keySet()) {
            if (entry.equalsIgnoreCase(textoInterfaz) && !encontrada) {
                interfaz.setText("");
                mostraMissatge("Paraula vàlida! tens un bonus", false); // Muestra mensaje de palabra válida
                palabrasescritas++;
                escritas.add(textoInterfaz.toLowerCase()); // Añade la palabra al conjunto de palabras adivinadas
                actualizarTextoPantalla(catalogoValidas.size()); // Actualiza el texto en pantalla
                return true;
            }
        }

        if (!encontrada) {
            mostraMissatge("Paraula no vàlida!", false); // Muestra mensaje de palabra no válida
        }

        return false;
    }

    private void actualizarTextoPantalla(int totalValidas) {
        // Actualiza el texto en pantalla con las palabras adivinadas y su estado
        String pantalla = "Has encertat " + palabrasescritas + " de " + totalValidas + " paraules possibles: ";

        // Utiliza un TreeSet para mantener las palabras ordenadas
        Set<String> palabrasOrdenadas = new TreeSet<>(escritas);

        // Construye el texto con las palabras adivinadas
        for (String palabra : palabrasOrdenadas) {
            if (palabrasRepetidas.contains(palabra)) {
                pantalla += "<font color='red'>" + catalogoValidas.get(palabra).toLowerCase() + "</font>"; // Palabra repetida en rojo
            } else {
                pantalla += catalogoValidas.get(palabra);
            }
            pantalla += ", ";
        }

        if (pantalla.endsWith(", ")) {
            pantalla = pantalla.substring(0, pantalla.length() - 2); // Elimina la última coma y espacio
        }

        texto.setText(Html.fromHtml(pantalla.trim())); // Actualiza el TextView con el texto formateado
    }

    public void inicializar(String word) {
        catalogoValidas = new HashMap<>();
        catalogoLong = new HashMap<>();
        catalogoLongValidas = new HashMap<>();

        for (Map.Entry<String, String> entry : diccionario.entrySet()) {
            // Verifica si la palabra puede formarse con las letras disponibles
            if (puedeFormarse(entry.getKey(), word)) {
                catalogoValidas.put(entry.getKey(), entry.getValue());
            }
        }

        // Inicializa conjuntos para cada longitud de palabra desde 3 hasta wordLength
        for (int i = 3; i <= word.length(); i++) {
            catalogoLong.put(i, new HashSet<>());
        }

        for (Map.Entry<String, String> entry : diccionario.entrySet()) {
            // Verifica la longitud de la palabra y la añade al conjunto correspondiente
            int length = entry.getKey().length();
            if (length >= 3 && length <= word.length()) {
                catalogoLong.get(length).add(entry.getKey());
            }
        }

        // Inicializar conjuntos para cada longitud de palabra desde 3 hasta wordLength
        for (int i = 3; i <= word.length(); i++) {
            catalogoLongValidas.put(i, new HashSet<>());
        }

        // Iterar sobre cada palabra en catalogoValidasSIN
        for (String palabra : catalogoValidas.keySet()) {
            // Verificar la longitud de la palabra y agregarla al conjunto correspondiente
            int length = palabra.length();
            if (length >= 3 && length <= word.length()) {
                catalogoLongValidas.get(length).add(palabra);
            }
        }

        // Crear un mapa de palabras agrupadas por longitud
        Map<Integer, Set<String>> palabrasPorLongitud = new HashMap<>();
        for (String palabra : catalogoValidas.keySet()) {
            int longitud = palabra.length();
            if (!palabrasPorLongitud.containsKey(longitud)) {
                palabrasPorLongitud.put(longitud, new HashSet<>());
            }
            palabrasPorLongitud.get(longitud).add(palabra);
        }

        // Set para asegurarse de que al menos una palabra de cada longitud superior esté seleccionada
        Set<String> reserva = new HashSet<>();

        // Asegurar que haya al menos una palabra para cada longitud desde 4 hasta wordLength
        for (int i = 4; i <= word.length(); i++) {
            if (palabrasPorLongitud.containsKey(i) && !palabrasPorLongitud.get(i).isEmpty()) {
                Iterator<String> iterator = palabrasPorLongitud.get(i).iterator();
                if (iterator.hasNext()) {
                    String palabra = iterator.next();
                    reserva.add(palabra);
                    iterator.remove();
                }
            }
        }

        // Seleccionar tantas palabras de longitud 3 como sea posible
        while (palabrasOcultasSet.size() + reserva.size() < 5
                && palabrasPorLongitud.containsKey(3)
                && !palabrasPorLongitud.get(3).isEmpty()) {
            Iterator<String> iterator = palabrasPorLongitud.get(3).iterator();
            if (iterator.hasNext()) {
                String palabra = iterator.next();
                palabrasOcultasSet.add(palabra);
                iterator.remove();
            }
        }

        // Agregar las palabras de reserva para completar hasta 5
        for (String palabra : reserva) {
            if (palabrasOcultasSet.size() < 5) {
                palabrasOcultasSet.add(palabra);
            } else {
                break;
            }
        }

        // Si aún no hay 5 palabras, seguir agregando palabras de las longitudes restantes
        for (int i = 4; i <= word.length() && palabrasOcultasSet.size() < 5; i++) {
            while (palabrasPorLongitud.containsKey(i) && !palabrasPorLongitud.get(i).isEmpty()
                    && palabrasOcultasSet.size() < 5) {
                Iterator<String> iterator = palabrasPorLongitud.get(i).iterator();
                if (iterator.hasNext()) {
                    String palabra = iterator.next();
                    palabrasOcultasSet.add(palabra);
                    iterator.remove();
                }
            }
        }
    }

    private boolean puedeFormarse(String word, String letrasDisponibles) {
        if (word.length() < 3) {
            return false; // Palabras de menos de 3 letras no son válidas
        }

        // Mapa para contar las letras disponibles
        Map<Character, Integer> letrasCount = new HashMap<>();

        // Cuenta las letras disponibles utilizando un Iterator
        Iterator<Character> letrasDisponiblesIterator = letrasDisponibles.chars().mapToObj(c -> (char) c).iterator();
        while (letrasDisponiblesIterator.hasNext()) {
            char c = letrasDisponiblesIterator.next();
            if (Character.isLetter(c)) {
                char lowerCaseChar = Character.toLowerCase(c);
                letrasCount.put(lowerCaseChar, letrasCount.getOrDefault(lowerCaseChar, 0) + 1);
            }
        }

        // Verifica si la palabra puede formarse con las letras disponibles utilizando un Iterator
        Iterator<Character> wordIterator = word.chars().mapToObj(c -> (char) c).iterator();
        while (wordIterator.hasNext()) {
            char c = wordIterator.next();
            if (Character.isLetter(c)) {
                char lowerCaseChar = Character.toLowerCase(c);
                if (letrasCount.getOrDefault(lowerCaseChar, 0) == 0) {
                    return false; // No se puede formar la palabra
                }
                letrasCount.put(lowerCaseChar, letrasCount.get(lowerCaseChar) - 1);
            }
        }

        return true; // Se puede formar la palabra
    }

    public TextView[] crearFilaTextViews(int guia, int lletres) {
        int margin = 5;
        int pre = 0;
        TextView[] textViews = new TextView[lletres];
        ConstraintLayout constraintLayout = findViewById(R.id.myConstraintLayout);
        for (int i = 0; i < lletres; i++) {
            int id = View.generateViewId();
            textViews[i] = new TextView(this);
            textViews[i].setId(id);
            textViews[i].setText("");
            textViews[i].setBackgroundColor(Color.rgb(red, green, blue));

            // Aquí se añaden las propiedades de estilo
            textViews[i].setTextColor(Color.WHITE); // Texto blanco
            textViews[i].setTextSize(28); // Tamaño de la fuente más grande (puedes ajustar según necesites)
            textViews[i].setGravity(Gravity.CENTER); // Texto centrado

            constraintLayout.addView(textViews[i]);

            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.connect(id, ConstraintSet.TOP, guia, ConstraintSet.TOP, margin);

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int widthDisplay = metrics.widthPixels;
            int heightDisplay = metrics.heightPixels;

            //Log.d("DEBUG", widthDisplay + ", " + heightDisplay + ", " + widthDisplay / 7);
            if (i == 0) {
                switch (lletres) {
                    case 7:
                        constraintSet.connect(id, ConstraintSet.LEFT, R.id.guideline13, ConstraintSet.RIGHT, 0);
                        break;
                    case 6:
                        constraintSet.connect(id, ConstraintSet.LEFT, R.id.guideline14, ConstraintSet.RIGHT, margin);
                        break;
                    case 5:
                        constraintSet.connect(id, ConstraintSet.LEFT, R.id.guideline, ConstraintSet.RIGHT, margin);
                        break;
                    case 4:
                        constraintSet.connect(id, ConstraintSet.LEFT, R.id.guideline2, ConstraintSet.RIGHT, margin);
                        break;
                    case 3:
                        constraintSet.connect(id, ConstraintSet.LEFT, R.id.guideline11, ConstraintSet.RIGHT, margin);
                        break;
                }
            } else {
                constraintSet.connect(id, ConstraintSet.LEFT, pre, ConstraintSet.RIGHT, margin);
            }
            constraintSet.constrainWidth(id, widthDisplay / 7);
            constraintSet.constrainHeight(id, widthDisplay / 7);
            constraintSet.applyTo(constraintLayout);
            pre = id;
        }
        return textViews;
    }

    private void mostraParaula(String s, int posicio) {
        char[] letras = s.toCharArray();
        String aux;
        switch (posicio) {
            case 1:
                for (int i = 0; i < letras.length; i++) {
                    aux = "" + letras[i];
                    aux = aux.toUpperCase();
                    tv1[i].setText(aux);
                }
                break;
            case 2:
                for (int i = 0; i < letras.length; i++) {
                    aux = "" + letras[i];
                    aux = aux.toUpperCase();
                    tv2[i].setText(aux);
                }
                break;
            case 3:
                for (int i = 0; i < letras.length; i++) {
                    aux = "" + letras[i];
                    aux = aux.toUpperCase();
                    tv3[i].setText(aux);
                }
                break;
            case 4:
                for (int i = 0; i < letras.length; i++) {
                    aux = "" + letras[i];
                    aux = aux.toUpperCase();
                    tv4[i].setText(aux);
                }
                break;
            case 5:
                for (int i = 0; i < letras.length; i++) {
                    aux = "" + letras[i];
                    aux = aux.toUpperCase();
                    tv5[i].setText(aux);
                }
                break;
        }
    }

    private void mostraPrimeraLletra(String s, int posicio) {
        char primeraLletra = s.toLowerCase().charAt(0); // Obtener la primera letra en minúscula
        String aux = "" + primeraLletra;

        switch (posicio) {
            case 1:
                tv1[0].setText(aux);
                break;
            case 2:
                tv2[0].setText(aux);
                break;
            case 3:
                tv3[0].setText(aux);
                break;
            case 4:
                tv4[0].setText(aux);
                break;
            case 5:
                tv5[0].setText(aux);
                break;
        }
    }

    private void mostraMissatge(String s, boolean llarg) {
        // Obtener el contexto de la aplicación
        Context context = getApplicationContext();

        // Configurar la duración del Toast
        int duration = llarg ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;

        // Crear el Toast
        Toast toast = Toast.makeText(context, s, duration);

        // Mostrar el Toast
        toast.show();
    }

    private void enableViews(int parent) {
        ConstraintLayout layout = findViewById(parent);
        if (layout != null) {
            int childCount = layout.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = layout.getChildAt(i);
                child.setEnabled(true);
            }
        }
    }

    private void disableViews(int parent) {
        ConstraintLayout layout = findViewById(parent);
        if (layout != null) {
            int childCount = layout.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = layout.getChildAt(i);
                int childId = child.getId();
                if (childId != R.id.Reiniciar && childId != R.id.Bonus) {
                    child.setEnabled(FALSE);
                }
            }
        }
    }

    private void showPopup(int totalValidas) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        String pantalla = "Has encertat " + palabrasescritas + " de " + totalValidas + " paraules possibles: ";
        builder.setTitle(pantalla);
        pantalla = "";
        for (String palabra : escritas) {
            if (palabrasRepetidas.contains(palabra)) {
                pantalla += "<font color='red'>" + catalogoValidas.get(palabra).toLowerCase() + "</font> ";
            } else {
                pantalla += catalogoValidas.get(palabra);
            }
            pantalla += ", ";
        }
        if (pantalla.endsWith(", ")) {
            pantalla = pantalla.substring(0, pantalla.length() - 2); // Elimina la última coma y espacio
        }
        builder.setMessage(Html.fromHtml(pantalla.trim()));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Acción al presionar OK
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}