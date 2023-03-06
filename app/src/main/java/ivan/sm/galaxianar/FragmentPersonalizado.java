package ivan.sm.galaxianar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.google.ar.sceneform.ux.ArFragment;
public class FragmentPersonalizado extends ArFragment {

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

            // Crea un nuevo FrameLayout
            FrameLayout frameLayout = (FrameLayout) super.onCreateView(inflater, container, savedInstanceState);

            // Oculta el controlador de descubrimiento de planos
            //Es la herramienta que ayuda a encontrar un plano adecuado para los objetos Virtuales
            getPlaneDiscoveryController().hide();

            // Establece la vista de instrucciones en nulo. Elimina la vista lo que la hace invisible.
            // Es la vista que proporciona informacion al usuario de como usar el descubrimiento de plano
            getPlaneDiscoveryController().setInstructionView(null);

            return frameLayout;
        }
}
