package ni.edu.uam.facturacion.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Producto {

    private Integer id;
    private String nombre;
    private Double precio;
    private Integer existencia;
    private Categoria categoria;

    @Override
    public String toString() {
        return nombre;
    }
}
