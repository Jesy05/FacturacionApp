package ni.edu.uam.facturacion.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class Cargo {

    private Integer id;
    private String nombre;
    private String descripcion;


    @Override
    public String toString() {
        return nombre;
    }
}
