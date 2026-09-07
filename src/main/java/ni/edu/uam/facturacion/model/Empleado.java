package ni.edu.uam.facturacion.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Empleado {

    private Integer id;
    private String nombre;
    private String apellidos;
    private Cargo cargo;
    private LocalDate fechaDeContratacion;
    private String cedula;
    private boolean ativo;

    
}
