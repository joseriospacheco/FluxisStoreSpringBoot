package co.fluxis.store.mappers;

import co.fluxis.store.dtos.responses.ProductoRespose;
import co.fluxis.store.entities.Producto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductoMapper {

    ProductoRespose toRespose(Producto producto);
    List<ProductoRespose> toRespose(List<Producto> productos);

}
