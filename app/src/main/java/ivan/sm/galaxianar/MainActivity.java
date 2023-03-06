package ivan.sm.galaxianar;


import android.graphics.Point;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;

import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.collision.Ray;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.Texture;


public class MainActivity extends AppCompatActivity {

    private Scene scene;
    private Camera camera;
    private ModelRenderable proyectil;
    private boolean iniciarTiempo = true;
    private int contAliens = 20;
    private Point punto;
    private SoundPool soundPool;
    private int sound;
    private MediaPlayer musicaStart, musicaAliens;
    private ImageView imgGalaxian, imgNave;
    private TextView txtMenu, contTiempo, txtAliens ;
    private Button btnStart, btnDisparar;
    private View miraHorizontal, miraVertical;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obtener el objeto de pantalla del administrador de ventanas
        Display display = getWindowManager().getDefaultDisplay();
        // Crear un objeto Point para almacenar el tamaño de la pantalla en píxeles
        punto = new Point();
        display.getRealSize(punto);

        // Establecer el diseño de la actividad usando el archivo de diseño XML activity_main.xml
        setContentView(R.layout.activity_main);

        // Instancio los botones
        txtAliens = (TextView) findViewById(R.id.txtAliens);
        txtMenu = (TextView) findViewById(R.id.txtMenu);
        contTiempo = (TextView)findViewById(R.id.txtTiempo);
        imgGalaxian = (ImageView) findViewById(R.id.imgGalaxian);
        imgNave = (ImageView) findViewById(R.id.imgNave);
        btnStart = (Button) findViewById(R.id.btnStart);
        btnDisparar = (Button) findViewById(R.id.btnDisparar);
        miraHorizontal = (View) findViewById(R.id.vHorizontal);
        miraVertical = (View) findViewById(R.id.vVertical);

        // Asigno la musica a las variables
        musicaStart = MediaPlayer.create(this, R.raw.musica);
        musicaAliens = MediaPlayer.create(this, R.raw.naves);
        musicaAliens.setLooping(true); // Indico que la reproduzca en bucle

        // Obtenemos el fragmento personalizado del archivo de diseño y asignarlo a arFragment
        FragmentPersonalizado arFragment = (FragmentPersonalizado) getSupportFragmentManager().findFragmentById(R.id.arFragment);

        scene = arFragment.getArSceneView().getScene(); // Obtenemos el objeto de escena del fragmento AR

        camera = scene.getCamera(); // Obtenemos el objeto de cámara de la escena

        sonidoExplosion();// LLamamos al metodo para hacer el sonido de la explosion.
        agregarAliensAlaScene(); // Agregamos los Aliens a la escena
        formaProyectil(); //Agregamos el proyectil a la escena

        // Le doy una accion al boton guardar
        btnStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // Hago que inicialice el tiempo
                if (iniciarTiempo) {
                    tiempo();
                    iniciarTiempo = false;
                }

                //Reproduce la musiquita
                musicaStart.start();

                // Hago que cuando termine la musica empiece otra que esta asignada como bucle
                musicaStart.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        musicaAliens.start();
                    }
                });

                btnStart.setVisibility(View.GONE); // Oculto el boton empezar.
                imgGalaxian.setVisibility(View.GONE);// Oculto la imagen galaxian
                imgNave.setVisibility(View.GONE);// Oculto la imagen de la nave
                txtMenu.setVisibility(View.GONE); // Oculto las letras menu principal
                txtAliens.setVisibility(View.VISIBLE); //Muestro el contador de Aliens
                contTiempo.setVisibility(View.VISIBLE);// Muestro el tiempo;
                btnDisparar.setVisibility(View.VISIBLE); // Mostrar el botón "Disparar"
                miraHorizontal.setVisibility(View.VISIBLE); //Mostrar view horizontal
                miraVertical.setVisibility(View.VISIBLE); //Mostrar view vertical
            }

        });

        // Le damos la funcion disparar al btnDisparar
        btnDisparar.setOnClickListener(v -> {
            disparar();

        });

    }

    /**
     * Metodo que reproduce el sonido de la explosion
     */
    private void sonidoExplosion() {

        // Se crea un objeto AudioAttributes para definir las características del audio que se va a reproducir.
        AudioAttributes audioAttributes = new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).setUsage(AudioAttributes.USAGE_GAME).build();

        // Se crea un objeto SoundPool para reproducir sonidos cortos de forma eficiente.
        soundPool = new SoundPool.Builder().setMaxStreams(1).setAudioAttributes(audioAttributes).build();

        // Se carga el archivo de sonido de la explosión en el SoundPool.
        sound = soundPool.load(this, R.raw.explosion, 1);

    }

    /**
     * Metodo que reproduce la animacion disparar, calcula si choca, y resta al contador de aliens
     */
    private void disparar() {

        // Se crea un rayo a partir de las coordenadas de la cámara y del punto en el que se ha hecho clic.
        // Este rayo se utiliza para determinar la dirección del disparo y detectar colisiones con otros objetos en la escena.
        Ray ray = camera.screenPointToRay(punto.x / 2f, punto.y / 2f);

        //Creamos un nodo para insertar el proyectil
        Node node = new Node();
        node.setRenderable(proyectil);
        scene.addChild(node);

        // Se inicia un hilo de ejecución para animar el movimiento del proyectil.
        new Thread(() -> {

            //Reproducimos 200 veces para animar el movimiento del proyectil.
            for (int i = 0;i < 200;i++) {

                int finalI = i;
                runOnUiThread(() -> {

                    Vector3 vector3 = ray.getPoint(finalI * 0.1f); // Se obtiene la posición del proyectil en el momento actual multiplicando la distancia recorrida por 0.1.
                    node.setWorldPosition(vector3); // Se actualiza la posición del nodo del proyectil en la escena.

                    // Se comprueba si el proyectil colisiona con algún objeto en la escena.
                    Node nodeInContact = scene.overlapTest(node);
                    // Si hay una colisión con un objeto, se actualiza el marcador de aliens y se reproduce un sonido.
                    if (nodeInContact != null) {

                        contAliens--; //Resta el contador de alien
                        txtAliens.setText("Aliens: " + contAliens); //Actualiza el contador
                        scene.removeChild(nodeInContact);

                        soundPool.play(sound, 1f, 1f, 1, 0, 1f); //Reproduce el sonido

                    }

                });
                // Se espera un breve período de tiempo antes de actualizar la posición del proyectil de nuevo.
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            // Después de que el proyectil ha recorrido cierta distancia, se elimina el nodo del proyectil de la escena.
            runOnUiThread(() -> scene.removeChild(node));

        }).start();

    }

    /**
     * Metodo que crea un contador mientras queden Alien y lo muestra en pantalla
     */
    private void tiempo() {

        TextView timer = findViewById(R.id.txtTiempo);

        // Creo un hilo para contar el tiempo
        new Thread(() -> {

            //Inicializo los segundos a 0
            int segundos = 0;

            //Ira contando mientras queden aliens
            while (contAliens > 0) {

                try {
                    //Detenemos el hilo durante un segundo
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //Incremeta en uno el contador segundos
                segundos++;

                // Calcular minutos y segundos transcurridos
                int minutesPassed = segundos / 60;
                int secondsPassed = segundos % 60;

                // Voy actualizando el TextView
                runOnUiThread(() -> timer.setText(minutesPassed + ":" + secondsPassed));

            }

        }).start(); //Inicia el hilo creado

    }

    /**
     * Metodo que crea la forma del proyectil
     */
    private void formaProyectil() {

        // Cargamos la textura del proyectil
        Texture.builder().setSource(this, R.drawable.proyectil).build().thenAccept(texture -> {

            // Creamos un material opaco con la textura
            MaterialFactory.makeOpaqueWithTexture(this, texture).thenAccept(material -> {

                // Creamos la forma del proyectil como una esfera
                proyectil = ShapeFactory.makeSphere(0.01f,new Vector3(0f, 0f, 0f),material);
            });

        });

    }

    /**
     * Metodo que agrega 20 Aliens con el archivo alien.sfb en distintas posiciones randoms
     */
    private void agregarAliensAlaScene() {

        // Creamos un renderizable del archivo alien.sfb
        ModelRenderable.builder().setSource(this, Uri.parse("alien.sfb")).build().thenAccept(renderable -> {

            //Creamos 20 nodos
            for (int i = 0; i < 20; i++) {

                Node node = new Node(); // Nodo, es el objeto 3D
                node.setRenderable(renderable);// Establece el renderizable
                scene.addChild(node); // Agrega el nodo a la escena

                // Genera posiciones aleatorias
                Random random = new Random();
                int x = random.nextInt(10);
                int z = random.nextInt(10);
                int y = random.nextInt(20);

                // Invierte la coordenada z para que los alienígenas se coloquen en ambos lados del eje z.
                z = -z;

                // Indicamos la posicion del nodo con las posiciones randoms anteriores.
                node.setWorldPosition(new Vector3((float) x,y / 10f, (float) z ));

            }

        });

    }

}