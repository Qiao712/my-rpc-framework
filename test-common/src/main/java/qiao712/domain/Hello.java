package qiao712.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Hello implements Serializable {
    private Integer id;
    private String hello;
    private List<Double> list;
}
