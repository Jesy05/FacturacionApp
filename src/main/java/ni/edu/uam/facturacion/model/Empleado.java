package ni.edu.uam.facturacion.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Empleado {

    private Integer id;
    private String nombre;
    private String cedula;
    private Cargo cargo;

    @Override
    public String toString() {
        return nombre;
    }
}
