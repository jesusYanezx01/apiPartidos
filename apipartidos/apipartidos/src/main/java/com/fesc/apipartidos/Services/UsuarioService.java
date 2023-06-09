package com.fesc.apipartidos.Services;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.fesc.apipartidos.data.entidades.PartidoEntity;
import com.fesc.apipartidos.data.entidades.UsuarioEntity;
import com.fesc.apipartidos.data.repositorios.IPartidoRespositoy;
import com.fesc.apipartidos.data.repositorios.IUsuarioRepository;
import com.fesc.apipartidos.shared.PartidoDto;
import com.fesc.apipartidos.shared.UsuarioDto;

@Service //Anotacion para especificar que la clase es un servicio 
public class UsuarioService implements IUsuarioService {

    //Tambien tendremos que hacer un mapeo, entonces llamamos a ModelMapper
    @Autowired
    ModelMapper modelMapper; 

    @Autowired
    IUsuarioRepository iusuariorepository; 

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    IPartidoRespositoy iPartidoRespositoy; 

    @Override
    public UsuarioDto crearUsuario(UsuarioDto usuarioCrearDto) {

            //Validacion de correo si se encuentra en la tabla mas de una vez
          if(iusuariorepository.findByEmail(usuarioCrearDto.getEmail()) != null){
            throw new RuntimeException("Este correo ya se encuentra en uso"); //linea de codigo que representa un error estandar
        } 

             //Validacion de usuario si se encuentra en la tabla mas de una vez
        if(iusuariorepository.findByUsername(usuarioCrearDto.getUsername()) != null){
            throw new RuntimeException("Este usuario ya esta en uso");
        } 

        UsuarioEntity usuarioentityDto = modelMapper.map(usuarioCrearDto, UsuarioEntity.class);
        usuarioentityDto.setIdUsuario(UUID.randomUUID().toString());
        usuarioentityDto.setPasswordEncripatada(bCryptPasswordEncoder.encode(usuarioCrearDto.getPassword()));

        UsuarioEntity usuarioentity= iusuariorepository.save(usuarioentityDto);

        UsuarioDto usuarioDto = modelMapper.map(usuarioentity, UsuarioDto.class);

      
        return usuarioDto; 
    }

    @Override
    public UsuarioDto leerUsuario(String username) {

        UsuarioEntity usuarioEntity = iusuariorepository.findByUsername(username);

        if(usuarioEntity == null){
            throw new UsernameNotFoundException(username);
        }

        UsuarioDto usuarioDto = modelMapper.map(usuarioEntity, UsuarioDto.class);

        return usuarioDto;

    }

    @Override
    public List<PartidoDto> leerMispartidos(String username) {
        UsuarioEntity usuarioEntity = iusuariorepository.findByUsername(username);

        //lista de todos los partidos que trajo la consulta
        List<PartidoEntity> partidoEntityList= iPartidoRespositoy.getByUsuarioEntityIdOrderByCreadoDesc(usuarioEntity.getId()); 

        //tenemos que mapear porque tenemos que retornar en dto
        //creamos un objeto array 
        //List padre de ArrayList, llamamos la clase list, pero instanciamos el hijo (arrayList) 
        //Si ponemos ArrayList fijo se quedara estatico y no se podra convertir
        List<PartidoDto> partidoDtoList= new ArrayList<>();

        //Utilizamos el for each por lo que trabajaremos con una lista
        
        for(PartidoEntity partidoEntity : partidoEntityList){
           //cada valor que es recorrido y almacenado en partidoEntity  lo estamos mapeando a partidoDto
            PartidoDto partidoDto = modelMapper.map(partidoEntity, PartidoDto.class);
           //seguidamente se va añadiendo a partidoDtoList
            partidoDtoList.add(partidoDto);

        }
        return partidoDtoList; 
    }

    // Para usar este metodo tuvimos que extender en la interfaz de usuario servicio UserDetailsService
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        //buscamos el username del usuario
        UsuarioEntity usuarioEntity= iusuariorepository.findByUsername(username);


        //validamos si el username requerido, existe
        if(username == null){
            //si el username es nulo, aplicamos una excepcion 
            throw new UsernameNotFoundException(username);
        }


        //creamos el usuario con la informacion de la base de datos (username, contraseña encriptada y requiere una arraylist para almacenar los resultados)
        User usuario= new User(usuarioEntity.getUsername(), usuarioEntity.getPasswordEncripatada(), new ArrayList<>()); 
        
        //retorna el usuario
        return usuario; 
    }

    
    
}
